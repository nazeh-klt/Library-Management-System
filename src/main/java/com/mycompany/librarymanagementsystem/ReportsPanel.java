package com.mycompany.librarymanagementsystem;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ReportsPanel extends JPanel {
    private final ReportController reportController = new ReportController();
    private final JTextArea reportArea = new JTextArea();

    public ReportsPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Color.WHITE);

        reportArea.setEditable(false);
        reportArea.setLineWrap(true);
        reportArea.setWrapStyleWord(true);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        reportArea.setBackground(new Color(248, 249, 251));
        reportArea.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(createToolbar(), BorderLayout.NORTH);
        add(new JScrollPane(reportArea), BorderLayout.CENTER);

        showWelcomeText();
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setBackground(Color.WHITE);

        ModernButton mostBorrowedButton = ModernButton.toolbar("Most Borrowed Books");
        ModernButton mostReadAuthorsButton = ModernButton.toolbar("Most Read Authors");
        ModernButton availableBooksButton = ModernButton.toolbar("Available Books");
        ModernButton refreshButton = ModernButton.toolbar("Refresh");
        ModernButton clearButton = ModernButton.toolbar("Clear");

        // TODO: Do not call ReportController.getMostBorrowedBooksReport until borrow indexes are
        // updated by safe GUI-compatible borrow/return methods.
        mostBorrowedButton.addActionListener(event -> showTodo("Most Borrowed Books depends on unsafe borrow indexes."));
        // TODO: Do not call ReportController.getMostReadAuthorReport until borrow indexes are safe.
        mostReadAuthorsButton.addActionListener(event -> showTodo("Most Read Authors depends on unsafe borrow indexes."));
        availableBooksButton.addActionListener(event -> showAvailableBooksReport());
        refreshButton.addActionListener(event -> showAvailableBooksReport());
        clearButton.addActionListener(event -> reportArea.setText(""));

        toolbar.add(mostBorrowedButton);
        toolbar.add(mostReadAuthorsButton);
        toolbar.add(availableBooksButton);
        toolbar.add(new JLabel(" "));
        toolbar.add(refreshButton);
        toolbar.add(clearButton);
        return toolbar;
    }

    private void showAvailableBooksReport() {
        reportArea.setText(reportController.getAvailableBooksReport(BookController.root));
        reportArea.setCaretPosition(0);
    }

    private void showWelcomeText() {
        reportArea.setText("Select a report from the toolbar.");
    }

    private void showTodo(String message) {
        JOptionPane.showMessageDialog(this, "TODO: " + message);
    }
}
