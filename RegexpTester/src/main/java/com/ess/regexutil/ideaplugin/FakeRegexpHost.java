package com.ess.regexutil.ideaplugin;

import com.intellij.lang.Language;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;
import org.intellij.lang.regexp.RegExpLanguage;
import org.jetbrains.annotations.NotNull;

public class FakeRegexpHost extends LightElement {

    public FakeRegexpHost(@NotNull PsiManager manager) {
        super(manager, RegExpLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "FakeRegexpHost";
    }
}
