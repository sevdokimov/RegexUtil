package com.ess.regexutil.ideaplugin;

import com.ess.regexutil.ideaplugin.utils.Utils;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.impl.source.resolve.FileContextUtil;
import com.intellij.ui.LanguageTextField;
import org.intellij.lang.regexp.RegExpLanguage;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.function.IntConsumer;

public class RegexpTextField extends LanguageTextField {
    private final IntConsumer groupSelectListener;

    public RegexpTextField(@NotNull Project project, @NotNull IntConsumer groupSelectListener) {
        super(RegExpLanguage.INSTANCE, project, "", false);

        this.groupSelectListener = groupSelectListener;
        setPlaceholder("regular expression");
        setShowPlaceholderWhenFocused(true);
    }

    @Override
    protected @NotNull EditorEx createEditor() {
        EditorEx editor = super.createEditor();

        editor.getSettings().setUseSoftWraps(false);
        editor.setHorizontalScrollbarVisible(true);
        editor.setVerticalScrollbarVisible(true);
        editor.setBorder(Utils.createEditorBorder(true));

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

        RegexHighlighter.install(editor, groupSelectListener);

        return editor;
    }
}
