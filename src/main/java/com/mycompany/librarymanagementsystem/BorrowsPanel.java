package com.mycompany.librarymanagementsystem;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
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

public class BorrowsPanel extends JPanel {
    private final JTextField searchField = new JTextField(18);
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "ISBN", "Title", "Student", "Borrow Date", "Expected Return", "Return Date", "Graduated"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable borrowsTable = new JTable(tableModel);
    private final TableRowSorter<DefaultTableModel> rowSorter = new TableRowSorter<>(tableModel);

    public BorrowsPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Color.WHITE);

        borrowsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        borrowsTable.setRowSorter(rowSorter);

        add(createToolbar(), BorderLayout.NORTH);
        add(new JScrollPane(borrowsTable), BorderLayout.CENTER);

        refreshTable();
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setBackground(Color.WHITE);

        ModernButton borrowButton = ModernButton.toolbar("Borrow Book");
        ModernButton returnButton = ModernButton.toolbar("Return Book");
        ModernButton editButton = ModernButton.toolbar("Edit Selected");
        ModernButton deleteButton = ModernButton.toolbar("Delete Selected");
        ModernButton searchButton = ModernButton.toolbar("Search");
        ModernButton refreshButton = ModernButton.toolbar("Refresh");

        borrowButton.addActionListener(event -> borrowBook());
        returnButton.addActionListener(event -> returnSelectedBorrow());
        // Editing an existing borrow record's dates in place is left disabled: the expected-return
        // date is also a key in AVLExpectedReturn's date index, and that structure has no delete/
        // update operation, only insert. An in-place edit would leave a stale entry in that index
        // pointing at the same recordId under the old date. Fixing this properly means adding a
        // delete operation to AVLExpectedReturn first, which is a separate piece of work.
        editButton.addActionListener(event -> editSelectedBorrow());
        deleteButton.addActionListener(event -> deleteSelectedBorrow());
        searchButton.addActionListener(event -> applySearchFilter());
        refreshButton.addActionListener(event -> {
            searchField.setText("");
            refreshTable();
        });
        searchField.addActionListener(event -> applySearchFilter());

        toolbar.add(borrowButton);
        toolbar.add(returnButton);
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
        for (Borrow borrow : getAllBorrows()) {
            tableModel.addRow(new Object[]{
                borrow.id,
                borrow.book == null ? "" : borrow.book.ISBN,
                borrow.book == null ? "" : nullToEmpty(borrow.book.title),
                nullToEmpty(borrow.student_name),
                formatDate(borrow.borrow_date),
                formatDate(borrow.expected_return),
                formatDate(borrow.return_date),
                borrow.is_graduated ? "Yes" : "No"
            });
        }
        applySearchFilter();
    }

    private List<Borrow> getAllBorrows() {
        List<Borrow> borrows = new ArrayList<>(BorrowController.borrow_log.values());
        borrows.sort(Comparator.comparingInt(borrow -> borrow.id));
        return borrows;
    }

    private void applySearchFilter() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
        }
    }

    private Borrow getSelectedBorrow() {
        int selectedRow = borrowsTable.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }
        int modelRow = borrowsTable.convertRowIndexToModel(selectedRow);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        return BorrowController.borrow_log.get(id);
    }

    private void borrowBook() {
        BorrowFormData data = showBorrowDialog("Borrow Book", null, false);
        if (data == null) {
            return;
        }

        BookNode node = AVLBookController.search_for_book(data.isbn);
        if (node == null) {
            showMessage("No book with that ISBN exists. Add it in the Books tab first.");
            return;
        }

        BorrowController.BorrowResult result = BorrowController.borrow_book(
                node.b, data.student, LocalDate.now(), data.expectedReturn, data.graduated);
        refreshTable();

        switch (result) {
            case BORROWED -> {
            }
            case ADDED_TO_WAITLIST ->
                showMessage("Book was unavailable - " + data.student + " was added to the waiting list.");
            case ALREADY_ON_WAITLIST ->
                showMessage(data.student + " is already on the waiting list for this book.");
            case BORROW_LIMIT_EXCEEDED ->
                showMessage(data.student + " already has the maximum number of active borrows, so they can't borrow or be added to the waiting list. They must return a book first.");
        }
    }

    private void editSelectedBorrow() {
        Borrow borrow = getSelectedBorrow();
        if (borrow == null) {
            showMessage("Select a borrow record first.");
            return;
        }
        BorrowFormData data = showBorrowDialog("Edit Borrow", borrow, true);
        if (data == null) {
            return;
        }
        showMessage("Not yet available: editing borrow records isn't safe because the expected-return date index has no delete/update operation.");
    }

    private void returnSelectedBorrow() {
        Borrow borrow = getSelectedBorrow();
        if (borrow == null) {
            showMessage("Select a borrow record first.");
            return;
        }
        if (borrow.return_date != null) {
            showMessage("This record was already returned on " + borrow.return_date + ".");
            return;
        }
        BorrowController.return_book(borrow.id, this::promptForExpectedReturnDate);
        refreshTable();
    }

    // Supplies the expected-return date for the next waiting student, if returning this book
    // frees a copy for someone on the waiting list. Runs on the Swing event thread via a modal
    // dialog rather than blocking on System.in.
    private LocalDate promptForExpectedReturnDate(String studentName) {
        while (true) {
            String input = JOptionPane.showInputDialog(
                    this,
                    "A copy just freed up for " + studentName + " from the waiting list.\n"
                            + "Enter their expected return date (YYYY-MM-DD), or Cancel to leave them waiting:",
                    "Assign Waiting Book",
                    JOptionPane.QUESTION_MESSAGE
            );
            if (input == null) {
                return null;
            }
            try {
                return LocalDate.parse(input.trim());
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid date as YYYY-MM-DD.");
            }
        }
    }

    private void deleteSelectedBorrow() {
        Borrow borrow = getSelectedBorrow();
        if (borrow == null) {
            showMessage("Select a borrow record first.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(
                this,
                "Delete this borrow record? This cannot be undone.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        BorrowController.delete_borrow_record(borrow.id);
        refreshTable();
    }

    private BorrowFormData showBorrowDialog(String title, Borrow borrow, boolean editMode) {
        JDialog dialog = createDialog(title);
        JPanel form = createDialogPanel();

        JTextField isbnField = new JTextField(18);
        JTextField studentField = new JTextField(18);
        JTextField expectedReturnField = new JTextField(18);
        JCheckBox graduatedBox = new JCheckBox("Graduated");
        graduatedBox.setBackground(new Color(248, 249, 251));

        if (borrow != null) {
            isbnField.setText(borrow.book == null ? "" : String.valueOf(borrow.book.ISBN));
            studentField.setText(nullToEmpty(borrow.student_name));
            expectedReturnField.setText(formatDate(borrow.expected_return));
            graduatedBox.setSelected(borrow.is_graduated);
        }

        addDialogField(form, "ISBN", isbnField, 0);
        addDialogField(form, "Student", studentField, 1);
        addDialogField(form, "Expected Return", expectedReturnField, 2);
        GridBagConstraints checkConstraints = new GridBagConstraints();
        checkConstraints.gridx = 1;
        checkConstraints.gridy = 3;
        checkConstraints.anchor = GridBagConstraints.WEST;
        checkConstraints.insets = new Insets(0, 0, 8, 0);
        form.add(graduatedBox, checkConstraints);

        if (editMode) {
            JTextField borrowDateField = new JTextField(formatDate(borrow.borrow_date), 18);
            JTextField returnDateField = new JTextField(formatDate(borrow.return_date), 18);
            borrowDateField.setEditable(false);
            returnDateField.setEditable(false);
            addDialogField(form, "Borrow Date", borrowDateField, 4);
            addDialogField(form, "Return Date", returnDateField, 5);
        }

        final BorrowFormData[] result = new BorrowFormData[1];
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBackground(new Color(248, 249, 251));
        ModernButton saveButton = ModernButton.toolbar("Save");
        ModernButton cancelButton = ModernButton.toolbar("Cancel");
        saveButton.addActionListener(event -> {
            BorrowFormData data = readBorrowFormData(isbnField, studentField, expectedReturnField, graduatedBox);
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

    private BorrowFormData readBorrowFormData(JTextField isbnField, JTextField studentField,
            JTextField expectedReturnField, JCheckBox graduatedBox) {
        int isbn;
        try {
            isbn = Integer.parseInt(isbnField.getText().trim());
        } catch (NumberFormatException ex) {
            showMessage("ISBN must be a number.");
            return null;
        }
        String student = studentField.getText().trim();
        if (student.isEmpty()) {
            showMessage("Enter a student name.");
            return null;
        }
        LocalDate expected;
        try {
            expected = LocalDate.parse(expectedReturnField.getText().trim());
        } catch (DateTimeParseException ex) {
            showMessage("Enter the expected return date as YYYY-MM-DD.");
            return null;
        }
        return new BorrowFormData(isbn, student, expected, graduatedBox.isSelected());
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

    private String formatDate(LocalDate date) {
        return date == null ? "" : date.toString();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private static class BorrowFormData {
        final int isbn;
        final String student;
        final LocalDate expectedReturn;
        final boolean graduated;

        BorrowFormData(int isbn, String student, LocalDate expectedReturn, boolean graduated) {
            this.isbn = isbn;
            this.student = student;
            this.expectedReturn = expectedReturn;
            this.graduated = graduated;
        }
    }
}
