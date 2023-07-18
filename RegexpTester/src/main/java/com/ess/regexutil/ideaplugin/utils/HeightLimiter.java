package com.ess.regexutil.ideaplugin.utils;

import javax.swing.*;
import java.awt.*;

public class HeightLimiter extends JPanel {

    private final int limit;

    public HeightLimiter(JComponent content, int limit) {
        super(new BorderLayout());

        add(content, BorderLayout.CENTER);
        this.limit = limit;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension preferredSize = super.getPreferredSize();
        if (preferredSize.height > limit)
            return new Dimension(preferredSize.width, limit);

        return preferredSize;
    }
}
