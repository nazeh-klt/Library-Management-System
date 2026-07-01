package com.mycompany.librarymanagementsystem;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

// Uses AVLBookController exclusively (not BookController's plain BST). AVLBookController is the
// balanced structure the assignment asks for, and it's the one BorrowController.check_available_book_by_ISBN
// already trusts, so keeping a single source of truth for books avoids the "book exists in one
// tree but not the other" bug that used to block Borrow/Return.
public class BooksPanel extends JPanel {
    private final JTextField searchField = new JTextField(18);
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ISBN", "Title", "Author", "Category", "Copies"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable booksTable = new JTable(tableModel);
    private final TableRowSorter<DefaultTableModel> rowSorter = new TableRowSorter<>(tableModel);

    private final JTextField isbnField = new JTextField(12);
    private final JTextField titleField = new JTextField(20);
    private final JTextField authorField = new JTextField(20);
    private final JTextField categoryField = new JTextField(16);
    private final JTextField copyField = new JTextField(8);

    public BooksPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Color.WHITE);

        booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        booksTable.setRowSorter(rowSorter);
        booksTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                showSelectedBookDetails();
            }
        });

        add(createToolbar(), BorderLayout.NORTH);
        add(new JScrollPane(booksTable), BorderLayout.CENTER);
        add(createDetailsPanel(), BorderLayout.SOUTH);

        refreshTable();
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setBackground(Color.WHITE);

        ModernButton addButton = ModernButton.toolbar("Add Book");
        ModernButton editButton = ModernButton.toolbar("Edit Selected");
        ModernButton deleteButton = ModernButton.toolbar("Delete Selected");
        ModernButton searchButton = ModernButton.toolbar("Search");
        ModernButton refreshButton = ModernButton.toolbar("Refresh");

        addButton.addActionListener(event -> addBook());
        editButton.addActionListener(event -> editSelectedBook());
        deleteButton.addActionListener(event -> deleteSelectedBook());
        searchButton.addActionListener(event -> applySearchFilter());
        refreshButton.addActionListener(event -> {
            searchField.setText("");
            refreshTable();
        });
        searchField.addActionListener(event -> applySearchFilter());

        toolbar.add(addButton);
        toolbar.add(editButton);
        toolbar.add(deleteButton);
        toolbar.add(new JLabel("Search:"));
        toolbar.add(searchField);
        toolbar.add(searchButton);
        toolbar.add(refreshButton);
        return toolbar;
    }

    private JPanel createDetailsPanel() {
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBackground(new Color(248, 249, 251));
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel title = new JLabel("Book Details");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        GridBagConstraints titleConstraints = new GridBagConstraints();
        titleConstraints.gridx = 0;
        titleConstraints.gridy = 0;
        titleConstraints.gridwidth = 10;
        titleConstraints.anchor = GridBagConstraints.WEST;
        titleConstraints.insets = new Insets(0, 0, 8, 0);
        detailsPanel.add(title, titleConstraints);

        addDetailField(detailsPanel, "ISBN", isbnField, 0, 1);
        addDetailField(detailsPanel, "Title", titleField, 2, 1);
        addDetailField(detailsPanel, "Author", authorField, 4, 1);
        addDetailField(detailsPanel, "Category", categoryField, 6, 1);
        addDetailField(detailsPanel, "Copies", copyField, 8, 1);
        return detailsPanel;
    }

    private void addDetailField(JPanel panel, String label, JTextField field, int gridX, int gridY) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = gridX;
        labelConstraints.gridy = gridY;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(0, 0, 0, 4);
        panel.add(new JLabel(label), labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = gridX + 1;
        fieldConstraints.gridy = gridY;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(0, 0, 0, 10);
        panel.add(field, fieldConstraints);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Book book : getAllBooks()) {
            tableModel.addRow(new Object[]{
                book.ISBN,
                book.title,
                book.author,
                book.category,
                book.copy
            });
        }
        applySearchFilter();
    }

    private List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        collectBooks(AVLBookController.root, books);
        return books;
    }

    private void collectBooks(BookNode node, List<Book> books) {
        if (node == null) {
            return;
        }
        collectBooks(node.left, books);
        books.add(node.b);
        collectBooks(node.right, books);
    }

    private void applySearchFilter() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
        }
    }

    private void showSelectedBookDetails() {
        BookNode node = getSelectedBookNode();
        if (node == null) {
            return;
        }
        isbnField.setText(String.valueOf(node.b.ISBN));
        titleField.setText(nullToEmpty(node.b.title));
        authorField.setText(nullToEmpty(node.b.author));
        categoryField.setText(nullToEmpty(node.b.category));
        copyField.setText(String.valueOf(node.b.copy));
    }

    private void addBook() {
        BookFormData data = readFormData();
        if (data == null) {
            return;
        }
        if (AVLBookController.search_for_book(data.isbn) != null) {
            showMessage("A book with this ISBN already exists.");
            return;
        }
        AVLBookController.add_avl_book(data.isbn, data.copies, data.title, data.author, data.category);
        refreshTable();
        selectBookByIsbn(data.isbn);
    }

    private void editSelectedBook() {
        BookNode selected = getSelectedBookNode();
        if (selected == null) {
            showMessage("Select a book first.");
            return;
        }

        int oldIsbn = selected.b.ISBN;
        BookFormData data = readFormData();
        if (data == null) {
            return;
        }

        BookNode duplicate = AVLBookController.search_for_book(data.isbn);
        if (data.isbn != oldIsbn && duplicate != null) {
            showMessage("Another book already uses this ISBN.");
            return;
        }

        if (data.isbn == oldIsbn) {
            // ISBN unchanged: copy count is validated against active borrows before being applied,
            // since dropping it below the number of copies currently checked out would make
            // check_available_book_by_ISBN report a negative number of free copies as "available".
            if (!BorrowController.can_reduce_copies(data.isbn, data.copies)) {
                showMessage("Can't set copies below the number currently on loan for this ISBN.");
                return;
            }
            selected.b.title = data.title;
            selected.b.author = data.author;
            selected.b.category = data.category;
            AVLBookController.update_copy_count(data.isbn, data.copies);
        } else {
            // ISBN changed: this is really "delete old entry, add new one" rather than an
            // in-place edit, so it's blocked if the old ISBN has active loans out - see
            // deleteSelectedBook for the same reasoning.
            if (BorrowController.has_active_borrows(oldIsbn)) {
                showMessage("Can't change the ISBN of a book that has active loans. Wait until all copies are returned.");
                return;
            }
            AVLBookController.delete_avl_book(oldIsbn);
            AVLBookController.add_avl_book(data.isbn, data.copies, data.title, data.author, data.category);
        }

        refreshTable();
        selectBookByIsbn(data.isbn);
    }

    private void deleteSelectedBook() {
        BookNode selected = getSelectedBookNode();
        if (selected == null) {
            showMessage("Select a book first.");
            return;
        }

        if (BorrowController.has_active_borrows(selected.b.ISBN)) {
            showMessage("Can't delete a book that currently has active loans out.");
            return;
        }

        int answer = JOptionPane.showConfirmDialog(
                this,
                "Delete selected book?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        AVLBookController.delete_avl_book(selected.b.ISBN);
        clearDetails();
        refreshTable();
    }

    private BookNode getSelectedBookNode() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }
        int modelRow = booksTable.convertRowIndexToModel(selectedRow);
        int isbn = (int) tableModel.getValueAt(modelRow, 0);
        return AVLBookController.search_for_book(isbn);
    }

    private BookFormData readFormData() {
        try {
            int isbn = Integer.parseInt(isbnField.getText().trim());
            int copies = Integer.parseInt(copyField.getText().trim());
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String category = categoryField.getText().trim();

            if (isbn <= 0 || copies < 0 || title.isEmpty() || author.isEmpty() || category.isEmpty()) {
                showMessage("Enter a positive ISBN, non-negative copies, and all text fields.");
                return null;
            }
            return new BookFormData(isbn, copies, title, author, category);
        } catch (NumberFormatException ex) {
            showMessage("ISBN and Copies must be numbers.");
            return null;
        }
    }

    private void selectBookByIsbn(int isbn) {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            if ((int) tableModel.getValueAt(row, 0) == isbn) {
                int viewRow = booksTable.convertRowIndexToView(row);
                if (viewRow >= 0) {
                    booksTable.setRowSelectionInterval(viewRow, viewRow);
                }
                return;
            }
        }
    }

    private void clearDetails() {
        isbnField.setText("");
        titleField.setText("");
        authorField.setText("");
        categoryField.setText("");
        copyField.setText("");
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private static class BookFormData {
        final int isbn;
        final int copies;
        final String title;
        final String author;
        final String category;

        BookFormData(int isbn, int copies, String title, String author, String category) {
            this.isbn = isbn;
            this.copies = copies;
            this.title = title;
            this.author = author;
            this.category = category;
        }
    }
}
