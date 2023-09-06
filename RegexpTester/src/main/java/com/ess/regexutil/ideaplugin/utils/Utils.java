package com.ess.regexutil.ideaplugin.utils;

import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.ui.JBColor;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.ui.JBUI;
import org.intellij.lang.regexp.intention.CheckRegExpForm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

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

    public static <T> T runCancelable(@NotNull String threadName, @Nullable ProgressIndicator progress, @NotNull Supplier<T> run)
            throws ProcessCanceledException {

        AtomicReference<Object> res = new AtomicReference<>();

        Thread thread = new Thread(() -> {
            try {
                res.set(run.get());
            } catch (ThreadDeath ignored) {
                // canceled
            } catch (RuntimeException e) {
                res.set(e);
            }
        }, threadName);

        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);

        thread.start();

        try {
            do {
                if (progress != null)
                    progress.checkCanceled();

                thread.join(100);
            } while (thread.isAlive());

        } catch (InterruptedException | ProcessCanceledException e) {
            thread.interrupt();

            AppExecutorUtil.getAppScheduledExecutorService().schedule(() -> {
                if (thread.isAlive())
                    thread.stop();
            }, 500, TimeUnit.MILLISECONDS);

            if (e instanceof ProcessCanceledException)
                throw (ProcessCanceledException)e;
            
            throw new ProcessCanceledException(e);
        }

        if (res.get() instanceof RuntimeException)
            throw (RuntimeException)res.get();

        return (T) res.get();
    }
}
