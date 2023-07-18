package com.ess.regexutil.ideaplugin.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Utils {

    public static final boolean isDebug = Boolean.getBoolean("idea.debug");

    private Utils() {
    }

    public static boolean isLeafElementOfType(@Nullable PsiElement element, @NotNull IElementType type) {
        return element instanceof LeafElement && ((LeafElement)element).getElementType() == type;
    }

    public static void throwIfDebug(@Nullable String errorMessage) {
        throwIfDebug(errorMessage, null);
    }

    public static void throwIfDebug(@Nullable String errorMessage, @Nullable Throwable e) {
        if (isDebug) {
            throw new RuntimeException(errorMessage, e);
        }
    }

}
