package com.mycompany.librarymanagementsystem;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
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

    public BooksPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Color.WHITE);

        booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        booksTable.setRowSorter(rowSorter);

        add(createToolbar(), BorderLayout.NORTH);
        add(new JScrollPane(booksTable), BorderLayout.CENTER);

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

    private void addDialogField(JPanel panel, String label, JTextField field, int gridY) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = gridY;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(0, 0, 8, 10);
        panel.add(new JLabel(label), labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = gridY;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(0, 0, 8, 0);
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

    private void addBook() {
        BookFormData data = showBookDialog("Add Book", null);
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
        BookFormData data = showBookDialog("Edit Book", selected.b);
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
        refreshTable();
    }

    private BookFormData showBookDialog(String title, Book book) {
        JDialog dialog = createDialog(title);
        JPanel form = createDialogPanel();

        JTextField isbnField = new JTextField(18);
        JTextField titleField = new JTextField(18);
        JTextField authorField = new JTextField(18);
        JTextField categoryField = new JTextField(18);
        JTextField copyField = new JTextField(18);

        if (book != null) {
            isbnField.setText(String.valueOf(book.ISBN));
            titleField.setText(nullToEmpty(book.title));
            authorField.setText(nullToEmpty(book.author));
            categoryField.setText(nullToEmpty(book.category));
            copyField.setText(String.valueOf(book.copy));
        }

        addDialogField(form, "ISBN", isbnField, 0);
        addDialogField(form, "Title", titleField, 1);
        addDialogField(form, "Author", authorField, 2);
        addDialogField(form, "Category", categoryField, 3);
        addDialogField(form, "Copies", copyField, 4);

        final BookFormData[] result = new BookFormData[1];
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBackground(new Color(248, 249, 251));
        ModernButton saveButton = ModernButton.toolbar("Save");
        ModernButton cancelButton = ModernButton.toolbar("Cancel");
        saveButton.addActionListener(event -> {
            BookFormData data = readFormData(isbnField, titleField, authorField, categoryField, copyField);
            if (data != null) {
                result[0] = data;
                dialog.dispose();
            }
        });
        cancelButton.addActionListener(event -> dialog.dispose());
        buttons.add(saveButton);
        buttons.add(cancelButton);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        return result[0];
    }

    private JDialog createDialog(String title) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, title, java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(0, 12));
        dialog.setResizable(false);
        dialog.getContentPane().setBackground(new Color(248, 249, 251));
        dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        return dialog;
    }

    private JPanel createDialogPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(248, 249, 251));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(12, 12, 4, 12)
        ));
        return panel;
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

    private BookFormData readFormData(JTextField isbnField, JTextField titleField, JTextField authorField,
            JTextField categoryField, JTextField copyField) {
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
