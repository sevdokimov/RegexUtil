package com.ess.regexutil.ideaplugin.utils;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseEventArea;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.editor.event.EditorMouseMotionListener;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.ex.RangeHighlighterEx;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class HoverEditorListener implements EditorMouseMotionListener, EditorMouseListener {

    public static final Key<BiConsumer<RangeHighlighterEx, Boolean>> ON_HOVER_KEY = Key.create("HoverEditorListener.ON_HOVER_KEY");

    private static RangeHighlighterEx curHlt;

    @Override
    public void mouseExited(@NotNull EditorMouseEvent event) {
        if (curHlt == null)
            return;

        if (!curHlt.isValid()) {
            curHlt = null;
            return;
        }

        BiConsumer<RangeHighlighterEx, Boolean> listener = curHlt.getUserData(ON_HOVER_KEY);
        assert listener != null;
        listener.accept(curHlt, false);
        curHlt = null;
    }

    @Override
    public void mouseMoved(@NotNull EditorMouseEvent e) {
        if (curHlt != null && !curHlt.isValid())
            curHlt = null;

        RangeHighlighterEx marker;

        if (!e.isOverText() || e.getArea() != EditorMouseEventArea.EDITING_AREA) {
            marker = null;
        } else {
            int offset = e.getOffset();

            if (curHlt != null && curHlt.getStartOffset() <= offset && offset <= curHlt.getEndOffset()) {
                marker = curHlt;
            } else {
                marker = getMarkerAt(e.getEditor(), e.getOffset());
            }
        }

        if (curHlt == marker)
            return;

        if (curHlt != null) {
            BiConsumer<RangeHighlighterEx, Boolean> listener = curHlt.getUserData(ON_HOVER_KEY);
            listener.accept(curHlt, false);
            curHlt = null;
        }

        if (marker != null) {
            BiConsumer<RangeHighlighterEx, Boolean> listener = marker.getUserData(ON_HOVER_KEY);
            listener.accept(marker, true);
            curHlt = marker;
        }
    }

    public static void install(@NotNull Editor editor) {
        HoverEditorListener listener = new HoverEditorListener();
        editor.addEditorMouseListener(listener);
        editor.addEditorMouseMotionListener(listener);
    }

    @Nullable
    private static RangeHighlighterEx getMarkerAt(@NotNull Editor editor, int offset) {
        Ref<RangeHighlighterEx> hltRef = new Ref<>();

        ((MarkupModelEx) editor.getMarkupModel()).processRangeHighlightersOverlappingWith(offset, offset, h -> {
            if (h.getUserData(ON_HOVER_KEY) == null)
                return true;

            hltRef.set(h);
            return false;
        });

        return hltRef.get();
    }

}
