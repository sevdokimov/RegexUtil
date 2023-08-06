package com.ess.regexutil.ideaplugin.utils;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import org.intellij.lang.regexp.intention.CheckRegExpForm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.lang.reflect.Field;

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

    public static Border createEditorBorder(boolean bottomBorder) {
        Border outBorder = JBUI.Borders.customLine(JBColor.border(), 1, 1, bottomBorder ? 1 : 0, 1);
        Border innerBorder = BorderFactory.createEmptyBorder(1, 3, 0, 0);
        return new CompoundBorder(outBorder, innerBorder);
    }

    public static Key<Boolean> getCheckRegexpEditorKey() {
        Field field;

        try {
            field = CheckRegExpForm.class.getField("CHECK_REG_EXP_EDITOR");
        } catch (NoSuchFieldException e) {
            try {
                Class<?> keyClass = CheckRegExpForm.class.getClassLoader().loadClass(CheckRegExpForm.class.getName() + ".Keys");
                field = keyClass.getField("CHECK_REG_EXP_EDITOR");
            } catch (ClassNotFoundException | NoSuchFieldException ex) {
                throw new RuntimeException(ex);
            }
        }

        try {
            return (Key<Boolean>) field.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
