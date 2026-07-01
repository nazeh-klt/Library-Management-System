package com.mycompany.librarymanagementsystem;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
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

    private final JTextField idField = new JTextField(8);
    private final JTextField isbnField = new JTextField(10);
    private final JTextField studentField = new JTextField(16);
    private final JTextField expectedReturnField = new JTextField(12);
    private final JTextField borrowDateField = new JTextField(12);
    private final JTextField returnDateField = new JTextField(12);
    private final JCheckBox graduatedBox = new JCheckBox("Graduated");

    public BorrowsPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Color.WHITE);

        borrowsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        borrowsTable.setRowSorter(rowSorter);
        borrowsTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                showSelectedBorrowDetails();
            }
        });

        add(createToolbar(), BorderLayout.NORTH);
        add(new JScrollPane(borrowsTable), BorderLayout.CENTER);
        add(createDetailsPanel(), BorderLayout.SOUTH);

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
        editButton.addActionListener(event -> showSelectedTodo(
                "Editing dates isn't safe yet: the expected-return date index (AVLExpectedReturn) has no delete/update operation, only insert."));
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

    private JPanel createDetailsPanel() {
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBackground(new Color(248, 249, 251));
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel title = new JLabel("Borrow Details");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        GridBagConstraints titleConstraints = new GridBagConstraints();
        titleConstraints.gridx = 0;
        titleConstraints.gridy = 0;
        titleConstraints.gridwidth = 12;
        titleConstraints.anchor = GridBagConstraints.WEST;
        titleConstraints.insets = new Insets(0, 0, 8, 0);
        detailsPanel.add(title, titleConstraints);

        addDetailField(detailsPanel, "ID", idField, 0, 1);
        addDetailField(detailsPanel, "ISBN", isbnField, 2, 1);
        addDetailField(detailsPanel, "Student", studentField, 4, 1);
        addDetailField(detailsPanel, "Expected", expectedReturnField, 6, 1);
        addDetailField(detailsPanel, "Borrowed", borrowDateField, 8, 1);
        addDetailField(detailsPanel, "Returned", returnDateField, 10, 1);

        GridBagConstraints checkConstraints = new GridBagConstraints();
        checkConstraints.gridx = 0;
        checkConstraints.gridy = 2;
        checkConstraints.gridwidth = 2;
        checkConstraints.anchor = GridBagConstraints.WEST;
        checkConstraints.insets = new Insets(8, 0, 0, 0);
        graduatedBox.setBackground(new Color(248, 249, 251));
        detailsPanel.add(graduatedBox, checkConstraints);

        JLabel hint = new JLabel("To borrow: fill ISBN, Student, Expected (YYYY-MM-DD), Graduated, then click Borrow Book.");
        hint.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
        hint.setForeground(new Color(120, 120, 120));
        GridBagConstraints hintConstraints = new GridBagConstraints();
        hintConstraints.gridx = 2;
        hintConstraints.gridy = 2;
        hintConstraints.gridwidth = 10;
        hintConstraints.anchor = GridBagConstraints.WEST;
        hintConstraints.insets = new Insets(8, 0, 0, 0);
        detailsPanel.add(hint, hintConstraints);

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

    private void showSelectedBorrowDetails() {
        Borrow borrow = getSelectedBorrow();
        if (borrow == null) {
            return;
        }
        idField.setText(String.valueOf(borrow.id));
        isbnField.setText(borrow.book == null ? "" : String.valueOf(borrow.book.ISBN));
        studentField.setText(nullToEmpty(borrow.student_name));
        expectedReturnField.setText(formatDate(borrow.expected_return));
        borrowDateField.setText(formatDate(borrow.borrow_date));
        returnDateField.setText(formatDate(borrow.return_date));
        graduatedBox.setSelected(borrow.is_graduated);
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
        int isbn;
        try {
            isbn = Integer.parseInt(isbnField.getText().trim());
        } catch (NumberFormatException ex) {
            showMessage("ISBN must be a number.");
            return;
        }
        String student = studentField.getText().trim();
        if (student.isEmpty()) {
            showMessage("Enter a student name.");
            return;
        }
        LocalDate expected;
        try {
            expected = LocalDate.parse(expectedReturnField.getText().trim());
        } catch (DateTimeParseException ex) {
            showMessage("Enter the expected return date as YYYY-MM-DD.");
            return;
        }

        BookNode node = AVLBookController.search_for_book(isbn);
        if (node == null) {
            showMessage("No book with that ISBN exists. Add it in the Books tab first.");
            return;
        }

        BorrowController.BorrowResult result = BorrowController.borrow_book(
                node.b, student, LocalDate.now(), expected, graduatedBox.isSelected());
        refreshTable();

        switch (result) {
            case BORROWED -> {
                clearDetails();
            }
            case ADDED_TO_WAITLIST -> {
                clearDetails();
                showMessage("Book was unavailable - " + student + " was added to the waiting list.");
            }
            case ALREADY_ON_WAITLIST ->
                showMessage(student + " is already on the waiting list for this book.");
            case BORROW_LIMIT_EXCEEDED ->
                showMessage(student + " already has the maximum number of active borrows, so they can't borrow or be added to the waiting list. They must return a book first.");
        }
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
        clearDetails();
        refreshTable();
    }

    private void clearDetails() {
        idField.setText("");
        isbnField.setText("");
        studentField.setText("");
        expectedReturnField.setText("");
        borrowDateField.setText("");
        returnDateField.setText("");
        graduatedBox.setSelected(false);
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

    private void showSelectedTodo(String message) {
        if (getSelectedBorrow() == null) {
            JOptionPane.showMessageDialog(this, "Select a borrow record first.");
            return;
        }
        JOptionPane.showMessageDialog(this, "Not yet available: " + message);
    }
}
