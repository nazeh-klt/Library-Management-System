package com.mycompany.librarymanagementsystem;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
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
        ModernButton overdueButton = ModernButton.toolbar("Overdue Loans");

        mostBorrowedButton.addActionListener(event -> showMostBorrowedReport());
        mostReadAuthorsButton.addActionListener(event -> showMostReadAuthorReport());
        overdueButton.addActionListener(event -> showOverdueReport());
        availableBooksButton.addActionListener(event -> showAvailableBooksReport());
        refreshButton.addActionListener(event -> showAvailableBooksReport());
        clearButton.addActionListener(event -> reportArea.setText(""));

        toolbar.add(mostBorrowedButton);
        toolbar.add(mostReadAuthorsButton);
        toolbar.add(overdueButton);
        toolbar.add(availableBooksButton);
        toolbar.add(new JLabel(" "));
        toolbar.add(refreshButton);
        toolbar.add(clearButton);
        return toolbar;
    }

    private void showMostBorrowedReport() {
        reportArea.setText(reportController.getMostBorrowedBooksReport());
        reportArea.setCaretPosition(0);
    }

    private void showMostReadAuthorReport() {
        reportArea.setText(reportController.getMostReadAuthorReport());
        reportArea.setCaretPosition(0);
    }

    private void showAvailableBooksReport() {
        // AVLBookController.root, not BookController.root - the AVL tree is the single book
        // store the rest of the app (BooksPanel, BorrowController) now reads and writes.
        reportArea.setText(reportController.getAvailableBooksReport(AVLBookController.root));
        reportArea.setCaretPosition(0);
    }

    private void showOverdueReport() {
        reportArea.setText(reportController.getOverdueLoansReport());
        reportArea.setCaretPosition(0);
    }
    
    private void showWelcomeText() {
        reportArea.setText("Select a report from the toolbar.");
    }
}
