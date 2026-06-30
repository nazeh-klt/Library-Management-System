package com.mycompany.librarymanagementsystem;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class DashboardFrame extends JFrame {
    private static final String BOOKS_PANEL = "Books";
    private static final String BORROWS_PANEL = "Borrows";
    private static final String WAITING_LIST_PANEL = "Waiting List";
    private static final String REPORTS_PANEL = "Reports";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    public DashboardFrame() {
        setTitle("Library Management Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 500));
        setSize(900, 550);
        setLocationRelativeTo(null);

        add(createMenuPanel(), BorderLayout.WEST);
        add(createContentPanel(), BorderLayout.CENTER);

        showPanel(BOOKS_PANEL);
    }

    private JPanel createMenuPanel() {
        JPanel menuPanel = new JPanel(new BorderLayout(0, 16));
        menuPanel.setPreferredSize(new Dimension(170, 0));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));
        menuPanel.setBackground(new Color(238, 241, 245));

        JLabel titleLabel = new JLabel("<html><div style='text-align:center;'>Library<br>Management<br>System</div></html>");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        titleLabel.setForeground(new Color(45, 69, 99));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 10, 0));

        JPanel buttonsPanel = new JPanel(new GridLayout(4, 1, 0, 10));
        buttonsPanel.setBackground(new Color(238, 241, 245));
        buttonsPanel.add(createMenuButton(BOOKS_PANEL));
        buttonsPanel.add(createMenuButton(BORROWS_PANEL));
        buttonsPanel.add(createMenuButton(WAITING_LIST_PANEL));
        buttonsPanel.add(createMenuButton(REPORTS_PANEL));

        menuPanel.add(titleLabel, BorderLayout.NORTH);
        menuPanel.add(buttonsPanel, BorderLayout.CENTER);

        return menuPanel;
    }

    private ModernButton createMenuButton(String title) {
        ModernButton button = ModernButton.sidebar(title);
        button.addActionListener(event -> showPanel(title));
        return button;
    }

    private JPanel createContentPanel() {
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        contentPanel.setBackground(Color.WHITE);

        contentPanel.add(new BooksPanel(), BOOKS_PANEL);

        contentPanel.add(new BorrowsPanel(), BORROWS_PANEL);

        contentPanel.add(new WaitingListPanel(), WAITING_LIST_PANEL);

        contentPanel.add(new ReportsPanel(), REPORTS_PANEL);

        return contentPanel;
    }

    private JPanel createSimplePanel(String title, String message, String todo) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));

        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));

        JLabel todoLabel = new JLabel(todo);
        todoLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        todoLabel.setForeground(new Color(90, 90, 90));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 6));
        textPanel.setBackground(Color.WHITE);
        textPanel.add(messageLabel);
        textPanel.add(todoLabel);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(textPanel, BorderLayout.CENTER);
        alignLabelsLeft(panel);
        return panel;
    }

    private void showPanel(String title) {
        cardLayout.show(contentPanel, title);
    }

    private void alignLabelsLeft(JPanel panel) {
        for (Component component : panel.getComponents()) {
            if (component instanceof JLabel label) {
                label.setHorizontalAlignment(SwingConstants.LEFT);
            }
        }
    }
}
