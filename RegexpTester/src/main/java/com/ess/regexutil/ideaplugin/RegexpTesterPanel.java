package com.ess.regexutil.ideaplugin;

import com.ess.regexutil.ideaplugin.utils.DocumentChangeListener;
import com.ess.regexutil.ideaplugin.utils.HeightLimiter;
import com.ess.regexutil.ideaplugin.utils.Utils;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
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
import com.intellij.psi.PsiFileFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.LanguageTextField;
import com.intellij.util.concurrency.EdtScheduledExecutorService;
import com.intellij.util.ui.JBUI;
import org.intellij.lang.regexp.RegExpFile;
import org.intellij.lang.regexp.RegExpFileType;
import org.intellij.lang.regexp.psi.RegExpGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class RegexpTesterPanel extends SimpleToolWindowPanel implements Disposable, Supplier<State> {

    static final Key<Boolean> GROUP_HLT = Key.create("RegexpTesterPanel.GROUP_HLT");

    static final Key<Boolean> MATCHED_PARTS = Key.create("RegexpTesterPanel.MATCHED_PARTS");

    static final Key<Integer> MATCH_IDX = Key.create("RegexpTesterPanel.MATCH_IDX");

    private static final int REGEX_EDITOR_HEIGHT_LIMIT = 350;

    private static final int REPLACEMENT_EDITOR_HEIGHT_LIMIT = 200;

    private static final TextAttributes MATCH_1_ATTR = new TextAttributes(null,
            new JBColor(new Color(0xE0E0FF), new Color(0x404074)),
            null, null, 0);

    private static final TextAttributes MATCH_2_ATTR = new TextAttributes(null,
            new JBColor(new Color(0xE0E0C0), new Color(0x505050)),
            null, null, 0);

    public static final TextAttributes MATCHED_REGEXP = new TextAttributes(null,
            new JBColor(new Color(0xD0FFD0), new Color(0x224822)),
            null, null, 0);

    private static final TextAttributes MATCHED_REGEXP_2 = MATCHED_REGEXP;

    private static final TextAttributes MATCHED_TEXT = MATCHED_REGEXP;

    private final Project project;
    final Editor textEditor;

    /**
     * Content under the regexp editor
     */
    private final JPanel centralPanel = new JPanel(new BorderLayout(0, 3));
    private final JComponent matchTypeComboboxPanel;

    private final JPanel rightPanel = new JPanel(new BorderLayout());

    private JPanel replacementPanel;
    final ReplacementTextField replacementInput;

    private boolean disposed;

    final LanguageTextField regexEditor;

    private FlagPanelAction flagsEditor;
    ComboBox<MatchType> matchTypeCombobox;

    MatchingResultPanel resultsPanel;

    protected final MatchingProcessor matchingProcessor;

    private ScheduledFuture<?> clearResultsPaneFuture;

    public void saveState(@NotNull RegexPanelStateService.State res) {
        res.setRegexp(regexEditor.getText());
        res.setText(textEditor.getDocument().getText());
        res.setReplacement(replacementInput.getText());
        res.setFlags(flagsEditor.getFlags());
        res.setMatchType(matchTypeCombobox.getItem());
    }

    public void loadState(@NotNull RegexPanelStateService.State res) {
        ApplicationManager.getApplication().assertIsDispatchThread();

        regexEditor.setText(StringUtil.notNullize(res.getRegexp()));

        WriteAction.run(() -> textEditor.getDocument().setText(StringUtil.notNullize(res.getText())));

        replacementInput.setText(res.getReplacement());
        
        flagsEditor.setFlags(res.getFlags());

        MatchType matchType = res.getMatchType();
        if (matchType == null)
            matchType = MatchType.values()[0];

        matchTypeCombobox.setItem(matchType);
    }

    public RegexpTesterPanel(@NotNull Project project) {
        super(false, false);
        this.project = project;

        setLayout(new BorderLayout(0, 3));
        setBorder(JBUI.Borders.empty(2, 3, 0, 3));

        regexEditor = new RegexpTextField(project, groupIdx -> highlightGroup(resultsPanel.getResult(), -1, groupIdx, false));

        textEditor = createTextEditor();

        replacementInput = new ReplacementTextField(project);

        JPanel regexpPanel = new JPanel(new BorderLayout());
        regexpPanel.add(new HeightLimiter(regexEditor, REGEX_EDITOR_HEIGHT_LIMIT), BorderLayout.CENTER);
        regexpPanel.add(createFlags(), BorderLayout.LINE_END);

        matchingProcessor = new MatchingProcessor(this, project);
        matchingProcessor.addListener(this::onMatchFinished);
        matchingProcessor.addAnalyzingListener(this::onAnalyzeFinished);

        resultsPanel = new MatchingResultPanel(project, this::errorClicked, matchingProcessor);
        Disposer.register(this, resultsPanel);

        resultsPanel.setHoverListener((occurrenceIdx, groupIdx) -> highlightGroup(resultsPanel.getResult(), occurrenceIdx, groupIdx, true));

        resultsPanel.setTextSelection(range -> {
            if (!matchingProcessor.isResultReady())
                return;

            textEditor.getSelectionModel().setSelection(range.getStartOffset(), range.getEndOffset());
            LogicalPosition position = textEditor.offsetToLogicalPosition(range.getStartOffset());
            textEditor.getCaretModel().moveToOffset(range.getStartOffset());
            textEditor.getScrollingModel().scrollTo(position, ScrollType.CENTER);

            textEditor.getContentComponent().requestFocus();
        });

        matchTypeComboboxPanel = createMatchTypeCombobox();
        rightPanel.add(matchTypeComboboxPanel, BorderLayout.NORTH);
        rightPanel.add(resultsPanel, BorderLayout.CENTER);

        JBSplitter splitter = new JBSplitter(false, "regex-tester-prop", 0.75f);
        splitter.setFirstComponent(textEditor.getComponent());
        splitter.setSecondComponent(rightPanel);

        add(regexpPanel, BorderLayout.NORTH);

        centralPanel.add(splitter, BorderLayout.CENTER);

        add(centralPanel, BorderLayout.CENTER);

        textEditor.setBorder(Utils.createEditorBorder(false));

        flagsEditor.addListener(x -> matchingProcessor.onStateChanged());

        DocumentListener documentListener = new DocumentChangeListener(e -> matchingProcessor.onStateChanged());

        textEditor.getDocument().addDocumentListener(documentListener);
        regexEditor.addDocumentListener(documentListener);
        replacementInput.addDocumentListener(documentListener);
    }

    private void highlight(@NotNull Editor editor, List<TextRange> ranges, TextAttributes matchedRegexp) {
        for (TextRange range : ranges) {
            RangeHighlighter highlighter = editor.getMarkupModel().addRangeHighlighter(
                    range.getStartOffset(), range.getEndOffset(),
                    HighlighterLayer.ELEMENT_UNDER_CARET,
                    matchedRegexp, HighlighterTargetArea.EXACT_RANGE);

            highlighter.putUserData(MATCHED_PARTS, true);
        }
    }

    private void onAnalyzeFinished() {
        Editor regexEditor = this.regexEditor.getEditor();
        if (regexEditor != null) {
            for (RangeHighlighter hlt : regexEditor.getMarkupModel().getAllHighlighters()) {
                if (hlt.getUserData(MATCHED_PARTS) != null)
                    regexEditor.getMarkupModel().removeHighlighter(hlt);
            }
        }

        for (RangeHighlighter hlt : textEditor.getMarkupModel().getAllHighlighters()) {
            if (hlt.getUserData(MATCHED_PARTS) != null)
                textEditor.getMarkupModel().removeHighlighter(hlt);
        }

        RegexpAnalyzer anResult = matchingProcessor.getAnalyzingResult();

        if (anResult == null || !anResult.getState().equals(get()))
            return;

        if (regexEditor != null) {
            highlight(regexEditor, anResult.getMatchedRegexp(), MATCHED_REGEXP);

            highlight(regexEditor, anResult.getAdditionalMatchedRegexp(), MATCHED_REGEXP_2);
        }

        if (anResult.getMatchedText() != null) {
            RangeHighlighter highlighter = textEditor.getMarkupModel().addRangeHighlighter(
                    anResult.getMatchedText().getStartOffset(),
                    anResult.getMatchedText().getEndOffset(),
                    HighlighterLayer.ELEMENT_UNDER_CARET,
                    MATCHED_TEXT, HighlighterTargetArea.EXACT_RANGE);

            highlighter.putUserData(MATCHED_PARTS, true);
        }
    }

    private void onMatchFinished(MatchResult result) {
        if (matchingProcessor.isResultReady()) {
            if (clearResultsPaneFuture != null) {
                clearResultsPaneFuture.cancel(true);
                clearResultsPaneFuture = null;
            }

            highlightText(matchingProcessor.getResult());
            resultsPanel.setResult(result);
        } else {
            if (clearResultsPaneFuture == null && !MatchingResultPanel.CARD_PROGRESS.equals(resultsPanel.getCurrentCard())) {
                clearResultsPaneFuture = EdtScheduledExecutorService.getInstance().schedule(() -> {
                    if (disposed || clearResultsPaneFuture == null)
                        return;

                    clearResultsPaneFuture = null;

                    resultsPanel.setProgressState();
                    highlightText(null);
                }, 400, TimeUnit.MILLISECONDS);
            }
        }
    }

    private void highlightGroup(MatchResult result, int occurrenceIdx, int groupIdx, boolean highlightInRegexp) {
        if (!matchingProcessor.isResultReady())
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
        if (!matchingProcessor.isResultReady())
            return;

        if (errorIndex == MatchingResultPanel.INVALID_REPLACEMENT && matchTypeCombobox.getItem() == MatchType.REPLACE) {
            replacementInput.grabFocus();
            return;
        }

        if (errorIndex >= 0 && errorIndex <= regexEditor.getDocument().getTextLength())
            regexEditor.setCaretPosition(errorIndex);
        regexEditor.grabFocus();
    }

    private JComponent createMatchTypeCombobox() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        panel.add(new JLabel("Match type: "));
        matchTypeCombobox = new ComboBox<>(MatchType.values());

        matchTypeCombobox.addActionListener(l -> {
            if (matchTypeCombobox.getItem() == MatchType.REPLACE) {
                replaceMode();
            } else {
                matchMode();
            }

            revalidate();

            matchingProcessor.onStateChanged();
        });

        panel.add(matchTypeCombobox);

        JPanel res = new JPanel(new BorderLayout());
        res.add(panel, BorderLayout.LINE_START);
        return res;
    }

    private void replaceMode() {
        if (replacementPanel == null) {
            replacementPanel = new JPanel(new BorderLayout(5, 0));

            HeightLimiter limiter = new HeightLimiter(replacementInput, REPLACEMENT_EDITOR_HEIGHT_LIMIT);
            replacementPanel.add(addFreeBottomSpace(limiter), BorderLayout.CENTER);
        }

        if (replacementPanel.getParent() != null)
            return;

        centralPanel.add(replacementPanel, BorderLayout.NORTH);
        replacementPanel.add(addFreeBottomSpace(matchTypeComboboxPanel), BorderLayout.EAST);
    }

    private void matchMode() {
        if (replacementPanel == null || replacementPanel.getParent() == null)
            return;

        centralPanel.remove(replacementPanel);

        assert replacementPanel.getComponentCount() == 2;
        replacementPanel.remove(1);
        
        rightPanel.add(matchTypeComboboxPanel, BorderLayout.NORTH);
    }

    private Editor createTextEditor() {
        Document document = EditorFactory.getInstance().createDocument("");

        EditorEx editor = (EditorEx) EditorFactory.getInstance().createEditor(document, this.project, EditorKind.CONSOLE);

        editor.setPlaceholder("Test string");
        editor.setShowPlaceholderWhenFocused(true);

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
        if (!matchingProcessor.isResultReady())
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

    @Override
    public void dispose() {
        if (disposed)
            return;

        disposed = true;

        matchingProcessor.dispose();

        if (textEditor != null)
            EditorFactory.getInstance().releaseEditor(textEditor);

        if (regexEditor != null) {
            Editor editor = regexEditor.getEditor();
            if (editor != null)
                EditorFactory.getInstance().releaseEditor(editor);
        }

        if (replacementInput != null) {
            Editor editor = replacementInput.getEditor();
            if (editor != null)
                EditorFactory.getInstance().releaseEditor(editor);
        }
    }

    private static JPanel addFreeBottomSpace(JComponent component) {
        JPanel res = new JPanel(new BorderLayout());
        res.add(component, BorderLayout.NORTH);
        return res;
    }

    @Override
    public State get() {
        ApplicationManager.getApplication().assertIsDispatchThread();

        return new State(
                regexEditor.getText(),
                textEditor.getDocument().getText(),
                replacementInput.getText(),
                flagsEditor.getFlags(),
                matchTypeCombobox.getItem(),
                parseGroups(regexEditor)
        );
    }
}
