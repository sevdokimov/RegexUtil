package com.ess.regexutil.ideaplugin;

import com.ess.regexutil.ideaplugin.utils.HeightLimiter;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorKind;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.psi.impl.source.resolve.FileContextUtil;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.LanguageTextField;
import com.intellij.util.concurrency.EdtScheduledExecutorService;
import com.intellij.util.ui.JBUI;
import org.intellij.lang.regexp.RegExpFile;
import org.intellij.lang.regexp.RegExpFileType;
import org.intellij.lang.regexp.RegExpLanguage;
import org.intellij.lang.regexp.psi.RegExpGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

public class RegexpTesterPanel extends SimpleToolWindowPanel implements Disposable {

    static final Key<Boolean> GROUP_HLT = Key.create("RegexpTesterPanel.GROUP_HLT");

    static final Key<Integer> MATCH_IDX = Key.create("RegexpTesterPanel.MATCH_IDX");

    private static final int REGEX_EDITOR_HEIGHT_LIMIT = 350;

    private static final TextAttributes MATCH_1_ATTR = new TextAttributes(null,
            new JBColor(new Color(0xE0E0FF), new Color(0x404074)),
            null, null, 0);

    private static final TextAttributes MATCH_2_ATTR = new TextAttributes(null,
            new JBColor(new Color(0xE0E0C0), new Color(0x505050)),
            null, null, 0);

    public static int REHIGHLIGHT_DELAY = 30;

    private final Project project;
    final Editor textEditor;

    private boolean disposed;

    private long rehighlightTime;

    private boolean resultReady;

    final LanguageTextField regexEditor;

    private FlagPanelAction flagsEditor;
    ComboBox<MatchType> matchTypeCombobox;

    final MatchingResultPanel resultsPanel;

    private String state;
    private Future<?> matchingFuture;

    public void saveState(@NotNull RegexPanelStateService.State res) {
        ApplicationManager.getApplication().assertIsDispatchThread();

        res.setRegexp(regexEditor.getText());
        res.setText(textEditor.getDocument().getText());
        res.setFlags(flagsEditor.getFlags());
        res.setMatchType(matchTypeCombobox.getItem());
    }

    public void loadState(@NotNull RegexPanelStateService.State res) {
        ApplicationManager.getApplication().assertIsDispatchThread();

        regexEditor.setText(StringUtil.notNullize(res.getRegexp()));

        WriteAction.run(() -> textEditor.getDocument().setText(StringUtil.notNullize(res.getText())));

        flagsEditor.setFlags(res.getFlags());

        MatchType matchType = res.getMatchType();
        if (matchType == null)
            matchType = MatchType.values()[0];

        matchTypeCombobox.setItem(matchType);
    }

    public enum MatchType {
        SUBSTRING("Substring"), ENTIRE_STRING("Entire string"), BEGINNING("From the beginning");

        MatchType(String title) {
            this.title = title;
        }

        private final String title;


        @Override
        public String toString() {
            return title;
        }
    }

    public RegexpTesterPanel(@NotNull Project project) {
        super(false, true);
        this.project = project;

        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(2, 3, 0, 3));

        regexEditor = new LanguageTextField(RegExpLanguage.INSTANCE, project, "", false) {
            @Override
            protected @NotNull EditorEx createEditor() {
                EditorEx editor = super.createEditor();

                editor.getSettings().setUseSoftWraps(false);
                editor.setHorizontalScrollbarVisible(true);
                editor.setVerticalScrollbarVisible(true);
                editor.setBorder(createEditorBorder(true));

                Color editorBackground = EditorColorsManager.getInstance().getGlobalScheme().getDefaultBackground();
                editor.setBackgroundColor(editorBackground);

                Font font = EditorUtil.getEditorFont();
                editor.getColorsScheme().setEditorFontName(font.getFontName());
                editor.getColorsScheme().setEditorFontSize(font.getSize());
                editor.getSettings().setLineCursorWidth(EditorUtil.getDefaultCaretWidth());

                FakeRegexpHost fakeRegexpHost = new FakeRegexpHost(PsiManager.getInstance(getProject()));

                PsiFile psiFile = PsiDocumentManager.getInstance(getProject()).getPsiFile(editor.getDocument());
                assert psiFile != null;
                psiFile.putUserData(FileContextUtil.INJECTED_IN_ELEMENT, SmartPointerManager.getInstance(getProject()).createSmartPsiElementPointer(fakeRegexpHost));

                RegexHighlighter.install(editor, groupIdx -> highlightGroup(resultsPanel.getResult(), -1, groupIdx, false));

                return editor;
            }
        };
        regexEditor.setPlaceholder("regular expression");

        textEditor = createTextEditor();

        JPanel regexpPanel = new JPanel(new BorderLayout());
        regexpPanel.setBorder(JBUI.Borders.emptyBottom(3));
        regexpPanel.add(new HeightLimiter(regexEditor, REGEX_EDITOR_HEIGHT_LIMIT), BorderLayout.CENTER);
        regexpPanel.add(createFlags(), BorderLayout.LINE_END);

        resultsPanel = new MatchingResultPanel(project, this::errorClicked);
        Disposer.register(this, resultsPanel);

        resultsPanel.setHoverListener((occurrenceIdx, groupIdx) -> highlightGroup(resultsPanel.getResult(), occurrenceIdx, groupIdx, true));

        resultsPanel.setTextSelection(range -> {
            if (!resultReady)
                return;

            textEditor.getSelectionModel().setSelection(range.getStartOffset(), range.getEndOffset());
            LogicalPosition position = textEditor.offsetToLogicalPosition(range.getStartOffset());
            textEditor.getCaretModel().moveToOffset(range.getStartOffset());
            textEditor.getScrollingModel().scrollTo(position, ScrollType.CENTER);

            textEditor.getContentComponent().requestFocus();
        });

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(createMatchTypeCombobox(), BorderLayout.PAGE_START);
        rightPanel.add(resultsPanel, BorderLayout.CENTER);

        JBSplitter splitter = new JBSplitter(false, "regex-tester-prop", 0.75f);
        splitter.setFirstComponent(textEditor.getComponent());
        splitter.setSecondComponent(rightPanel);

        add(regexpPanel, BorderLayout.NORTH);
        add(splitter, BorderLayout.CENTER);

        textEditor.setBorder(createEditorBorder(false));

        flagsEditor.addListener(x -> onStateChanged());

        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                onStateChanged();
            }
        };

        textEditor.getDocument().addDocumentListener(documentListener);
        regexEditor.addDocumentListener(documentListener);
    }

    public boolean isResultReady() {
        return resultReady;
    }

    private void highlightGroup(MatchResult result, int occurrenceIdx, int groupIdx, boolean highlightInRegexp) {
        if (!resultReady)
            return;

        for (RangeHighlighter hlt : textEditor.getMarkupModel().getAllHighlighters()) {
            if (hlt.getUserData(GROUP_HLT) != null)
                textEditor.getMarkupModel().removeHighlighter(hlt);
        }

        Editor regexEditor = this.regexEditor.getEditor();
        if (regexEditor != null) {
            for (RangeHighlighter hlt : regexEditor.getMarkupModel().getAllHighlighters()) {
                if (hlt.getUserData(GROUP_HLT) != null)
                    regexEditor.getMarkupModel().removeHighlighter(hlt);
            }
        }

        if (groupIdx == -1)
            return; // hide group highlighting

        if (regexEditor != null && groupIdx > 0 && highlightInRegexp) {
            TextRange groupRange = result.getGroupPositions().get(groupIdx - 1).first;
            RangeHighlighter hlt = regexEditor.getMarkupModel().addRangeHighlighter(EditorColors.IDENTIFIER_UNDER_CARET_ATTRIBUTES,
                    groupRange.getStartOffset(), groupRange.getEndOffset(),
                    HighlighterLayer.ELEMENT_UNDER_CARET, HighlighterTargetArea.EXACT_RANGE);
            hlt.putUserData(GROUP_HLT, true);
        }

        Stream<MatchResult.Occurrence> occurrences;
        if (occurrenceIdx < 0) {
            occurrences = result.getOccurrences().stream();
        } else {
            occurrences = Stream.of(result.getOccurrences().get(occurrenceIdx));
        }

        occurrences.forEach(occurrence -> {
            MatchResult.MatchGroup group = occurrence.getGroups().get(groupIdx);

            if (group.isMatch()) {
                RangeHighlighter hlt = textEditor.getMarkupModel().addRangeHighlighter(group.getStartOffset(), group.getEndOffset(),
                        HighlighterLayer.SELECTION, MatchingResultPanel.CURRENT_GROUP_ATTR, HighlighterTargetArea.EXACT_RANGE
                );

                hlt.putUserData(GROUP_HLT, true);
            }
        });
    }

    private void errorClicked(int errorIndex) {
        if (errorIndex >= 0 && errorIndex <= regexEditor.getDocument().getTextLength())
            regexEditor.setCaretPosition(errorIndex);
        regexEditor.grabFocus();
    }

    private JComponent createMatchTypeCombobox() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(JBUI.Borders.emptyBottom(10));

        panel.add(new JLabel("Match type: "));
        matchTypeCombobox = new ComboBox<>(MatchType.values());
        matchTypeCombobox.addActionListener(l -> onStateChanged());
        panel.add(matchTypeCombobox);

        JPanel res = new JPanel(new BorderLayout());
        res.add(panel, BorderLayout.LINE_START);
        return res;
    }

    private static Border createEditorBorder(boolean bottomBorder) {
        Border outBorder = JBUI.Borders.customLine(JBColor.border(), 1, 1, bottomBorder ? 1 : 0, 1);
        Border innerBorder = BorderFactory.createEmptyBorder(1, 3, 0, 0);
        return new CompoundBorder(outBorder, innerBorder);
    }

    private Editor createTextEditor() {
        Document document = EditorFactory.getInstance().createDocument("");

        EditorEx editor = (EditorEx) EditorFactory.getInstance().createEditor(document, this.project, EditorKind.CONSOLE);

        editor.setPlaceholder("Test string");

        EditorSettings settings = editor.getSettings();
        settings.setAdditionalLinesCount(0);
        settings.setAdditionalColumnsCount(1);
        settings.setRightMarginShown(false);
        settings.setRightMargin(-1);
        settings.setFoldingOutlineShown(false);
        settings.setLineNumbersShown(false);
        settings.setLineMarkerAreaShown(false);
        settings.setIndentGuidesShown(false);
        settings.setVirtualSpace(true);
        settings.setWheelFontChangeEnabled(false);
        settings.setAdditionalPageAtBottom(false);
        settings.setCaretRowShown(false);

        editor.getCaretModel().addCaretListener(new CaretListener() {
            @Override
            public void caretPositionChanged(@NotNull CaretEvent event) {
                if (event.getCaret() != null)
                    onCaretMove(event.getCaret().getOffset());
            }
        });

        return editor;
    }

    private JComponent createFlags() {
        JPanel flagPanel = new JPanel(new BorderLayout());

        flagsEditor = new FlagPanelAction();

        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(flagsEditor);

        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("RegexFlagToolbar", actionGroup, true);
        actionToolbar.setTargetComponent(flagPanel);
        actionToolbar.setMiniMode(true);

        flagPanel.add(actionToolbar.getComponent(), BorderLayout.PAGE_START);

        return flagPanel;
    }

    private String currentState() {
        return flagsEditor.getFlags()
                + "_" + textEditor.getDocument().getModificationStamp()
                + '_' + regexEditor.getDocument().getModificationStamp()
                + '_' + matchTypeCombobox.getItem().ordinal();
    }

    private void rehighlight() {
        if (disposed)
            return;

        long delay = rehighlightTime - System.currentTimeMillis();
        if (delay > 0) {
            EdtScheduledExecutorService.getInstance().schedule(this::rehighlight, ModalityState.NON_MODAL, delay, TimeUnit.MILLISECONDS);
            return;
        }

        rehighlightTime = 0;

        String stateId = currentState();

        if (stateId.equals(state))
            return;

        state = stateId;

        if (matchingFuture != null)
            matchingFuture.cancel(true);

        MatchResult outdatedResult = resultsPanel.getResult();

        ScheduledFuture<?> clearResultsPaneFuture = EdtScheduledExecutorService.getInstance().schedule(() -> {
            if (disposed)
                return null;

            if (outdatedResult == resultsPanel.getResult()) {
                resultsPanel.setProgressState();
                highlightText(null);
            }
            return null;
        }, 400, TimeUnit.MILLISECONDS);

        int flags = flagsEditor.getFlags();
        String regex = regexEditor.getText();
        String text = textEditor.getDocument().getText();
        MatchType matchType = matchTypeCombobox.getItem();
        List<Pair<TextRange, String>> groups = parseGroups(regexEditor);

        matchingFuture = ApplicationManager.getApplication().executeOnPooledThread(() -> {
            if (Thread.interrupted())
                return;

            MatchResult result = match(regex, groups, flags, text, matchType);
            if (Thread.interrupted())
                return;

            clearResultsPaneFuture.cancel(true);

            ApplicationManager.getApplication().invokeLater(() -> {
                if (!stateId.equals(state))
                    return;

                matchingFuture = null;

                resultsPanel.setResult(result);
                highlightText(result);
                resultReady = true;
            });
        });
    }

    private List<Pair<TextRange, String>> parseGroups(LanguageTextField regexEditor) {
        RegExpFile psiFile;

        Editor editor = regexEditor.getEditor();
        if (editor != null) {
            WriteAction.run(() -> PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument()));

            psiFile = (RegExpFile) PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        } else {
            psiFile = (RegExpFile) PsiFileFactory.getInstance(project).createFileFromText("regex", RegExpFileType.INSTANCE, regexEditor.getText());
        }

        List<Pair<TextRange, String>> res = new ArrayList<>();

        for (RegExpGroup group : psiFile.getGroups()) {
            if (!group.isCapturing())
                continue;

            res.add(Pair.pair(group.getTextRange(), group.getGroupName()));
        }

        return res;
    }

    private void onStateChanged() {
        ApplicationManager.getApplication().assertIsDispatchThread();

        resultReady = false;

        boolean rehighlightScheduled = rehighlightTime != 0;

        rehighlightTime = System.currentTimeMillis() + REHIGHLIGHT_DELAY;

        if (!rehighlightScheduled) {
            EdtScheduledExecutorService.getInstance().schedule(this::rehighlight, ModalityState.NON_MODAL, REHIGHLIGHT_DELAY, TimeUnit.MILLISECONDS);
        }
    }

    private void highlightText(@Nullable MatchResult result) {
        List<MatchResult.Occurrence> occurrences = result == null ? List.of() : result.getOccurrences();

        textEditor.getMarkupModel().removeAllHighlighters();

        for (int i = 0; i < occurrences.size(); i++) {
            MatchResult.Occurrence o = occurrences.get(i);

            TextAttributes attr = (i & 1) == 0 ? MATCH_1_ATTR : MATCH_2_ATTR;

            RangeHighlighter highlighter = textEditor.getMarkupModel()
                    .addRangeHighlighter(o.getStartOffset(), o.getEndOffset(), HighlighterLayer.WARNING, attr, HighlighterTargetArea.EXACT_RANGE);

            highlighter.putUserData(MATCH_IDX, i);
        }
    }

    private void onCaretMove(int offset) {
        if (!resultReady)
            return;

        MatchResult result = resultsPanel.getResult();
        if (result == null)
            return;

        if (!MatchingResultPanel.CARD_GROUPS.equals(resultsPanel.getCurrentCard())
                && !MatchingResultPanel.CARD_MATCHES.equals(resultsPanel.getCurrentCard()))
            return;

        List<MatchResult.Occurrence> occurrences = result.getOccurrences();

        for (int i = 0; i < occurrences.size(); i++) {
            MatchResult.Occurrence o = occurrences.get(i);

            if (o.containsOffset(offset) && !(i + 1 < occurrences.size() && occurrences.get(i + 1).containsOffset(offset))) {
                resultsPanel.selectOccurrence(i);
                return;
            }
        }
    }

    @Nullable
    private MatchResult match(String regex, List<Pair<TextRange, String>> groupPositions, int flags, String text, MatchType matchType) {
        if (regex.isEmpty())
            return null;

        Pattern pattern;
        
        try {
            pattern = Pattern.compile(regex, flags);
        } catch (PatternSyntaxException e) {
            return new MatchResult(e);
        }

        Matcher matcher = pattern.matcher(text);

        List<MatchResult.Occurrence> groups = new ArrayList<>();

        switch (matchType) {
            case ENTIRE_STRING: {
                if (matcher.matches()) {
                    groups.add(new MatchResult.Occurrence(matcher, groupPositions));
                }

                break;
            }

            case BEGINNING: {
                if (matcher.lookingAt()) {
                    groups.add(new MatchResult.Occurrence(matcher, groupPositions));
                }

                break;
            }
            case SUBSTRING: {
                while (matcher.find()) {
                    groups.add(new MatchResult.Occurrence(matcher, groupPositions));
                    if (Thread.currentThread().isInterrupted())
                        return null;
                }

                break;
            }

            default:
                throw new IllegalStateException();
        }

        return new MatchResult(matchType, text, groups, groupPositions);
    }

    @Override
    public void dispose() {
        disposed = true;

        if (textEditor != null)
            EditorFactory.getInstance().releaseEditor(textEditor);

        if (regexEditor != null) {
            Editor editor = regexEditor.getEditor();
            if (editor != null)
                EditorFactory.getInstance().releaseEditor(editor);
        }
    }
}
