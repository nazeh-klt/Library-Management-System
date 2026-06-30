package com.mycompany.librarymanagementsystem;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

public class ModernButton extends JButton {
    private static final Color DEFAULT_BACKGROUND = new Color(48, 95, 155);
    private static final Color DEFAULT_HOVER = new Color(38, 80, 135);
    private static final Color DEFAULT_PRESSED = new Color(31, 67, 116);
    private static final Color DEFAULT_TEXT = Color.WHITE;

    private final Color normalBackground;
    private final Color hoverBackground;
    private final Color pressedBackground;
    private boolean hovered;

    public ModernButton(String text) {
        this(text, 130, 36);
    }

    public ModernButton(String text, int width, int height) {
        this(text, width, height, DEFAULT_BACKGROUND, DEFAULT_HOVER, DEFAULT_PRESSED);
    }

    public ModernButton(String text, int width, int height, Color normalBackground, Color hoverBackground, Color pressedBackground) {
        super(text);
        this.normalBackground = normalBackground;
        this.hoverBackground = hoverBackground;
        this.pressedBackground = pressedBackground;
        setPreferredSize(new Dimension(width, height));
        setMinimumSize(new Dimension(width, height));
        setMaximumSize(new Dimension(width, height));
        setBorder(new EmptyBorder(new Insets(8, 14, 8, 14)));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setForeground(DEFAULT_TEXT);
        setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent event) {
                hovered = false;
                repaint();
            }
        });
    }

    public static ModernButton sidebar(String text) {
        return new ModernButton(text, 146, 40, new Color(59, 91, 132), new Color(47, 78, 118), new Color(39, 66, 101));
    }

    public static ModernButton toolbar(String text) {
        return new ModernButton(text, 128, 34);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getButtonColor());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
        g2.dispose();
        super.paintComponent(graphics);
    }

    private Color getButtonColor() {
        if (getModel().isPressed()) {
            return pressedBackground;
        }
        if (hovered || getModel().isRollover()) {
            return hoverBackground;
        }
        return normalBackground;
    }
}
