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
import java.util.Map;
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

public class WaitingListPanel extends JPanel {
    private final JTextField searchField = new JTextField(18);
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ISBN", "Title", "Student", "Graduated", "Request Date", "Priority"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable waitingTable = new JTable(tableModel);
    private final TableRowSorter<DefaultTableModel> rowSorter = new TableRowSorter<>(tableModel);
    private final List<WaitingRow> tableRows = new ArrayList<>();

    public WaitingListPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Color.WHITE);

        waitingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        waitingTable.setRowSorter(rowSorter);

        add(createToolbar(), BorderLayout.NORTH);
        add(new JScrollPane(waitingTable), BorderLayout.CENTER);

        refreshTable();
        
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                refreshTable();
            }
        });
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setBackground(Color.WHITE);

        ModernButton addButton = ModernButton.toolbar("Add Request");
        ModernButton editButton = ModernButton.toolbar("Edit Selected");
        ModernButton deleteButton = ModernButton.toolbar("Delete Selected");
        ModernButton searchButton = ModernButton.toolbar("Search");
        ModernButton refreshButton = ModernButton.toolbar("Refresh");

        addButton.addActionListener(event -> addRequest());
        editButton.addActionListener(event -> editSelectedRequest());
        deleteButton.addActionListener(event -> deleteSelectedRequest());
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
        tableRows.clear();
        tableRows.addAll(getWaitingRows());
        tableModel.setRowCount(0);

        for (WaitingRow row : tableRows) {
            tableModel.addRow(new Object[]{
                row.isbn,
                row.bookTitle,
                row.request.studentName,
                row.request.isGraduated ? "Yes" : "No",
                formatDate(row.request.requestDate),
                row.priority
            });
        }
        applySearchFilter();
    }

    private List<WaitingRow> getWaitingRows() {
        List<WaitingRow> rows = new ArrayList<>();
        for (Map.Entry<Integer, MaxPriorityQueue> entry : BorrowController.wait_requests_queue_by_ISBN.entrySet()) {
            Integer isbn = entry.getKey();
            MaxPriorityQueue queue = entry.getValue();
            if (queue == null || queue.heap == null) {
                continue;
            }

            List<BookQueue> requests = new ArrayList<>(queue.getElements());
            requests.sort((first, second) -> {
                if (MaxPriorityQueue.hasHigherPriority(first, second)) {
                    return -1;
                }
                if (MaxPriorityQueue.hasHigherPriority(second, first)) {
                    return 1;
                }
                return 0;
            });

            for (int index = 0; index < requests.size(); index++) {
                rows.add(new WaitingRow(isbn, getBookTitle(isbn), requests.get(index), index + 1));
            }
        }
        rows.sort(Comparator.comparingInt((WaitingRow row) -> row.isbn).thenComparingInt(row -> row.priority));
        return rows;
    }

    private String getBookTitle(int isbn) {
        BookNode node = AVLBookController.search_for_book(isbn);
        if (node == null || node.b == null) {
            return "";
        }
        return nullToEmpty(node.b.title);
    }

    private void applySearchFilter() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
        }
    }

    private WaitingRow getSelectedWaitingRow() {
        int selectedRow = waitingTable.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }
        int modelRow = waitingTable.convertRowIndexToModel(selectedRow);
        if (modelRow < 0 || modelRow >= tableRows.size()) {
            return null;
        }
        return tableRows.get(modelRow);
    }

    private void addRequest() {
        WaitingFormData data = showWaitingDialog("Add Request", null);
        if (data == null) {
            return;
        }

        String error = BorrowController.add_to_waitlist(data.isbn, data.student, data.graduated);
        if (error != null) {
            showMessage(error);
            return;
        }
        refreshTable();
    }

    private void editSelectedRequest() {
        WaitingRow row = getSelectedWaitingRow();
        if (row == null) {
            showMessage("Select a waiting request first.");
            return;
        }
       
        WaitingFormData data = showWaitingDialog("Edit Request", row);
        if (data == null) {
            return;
        }

        boolean updated = BorrowController.update_wait_request(
                row.isbn, row.request.studentName, data.graduated, data.requestDate);
        if (!updated) {
            showMessage("Could not find that request anymore - it may have already been processed.");
        }
        refreshTable();
    }

    private void deleteSelectedRequest() {
        WaitingRow row = getSelectedWaitingRow();
        if (row == null) {
            showMessage("Select a waiting request first.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(
                this,
                "Remove " + row.request.studentName + " from the waiting list for this book?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        BorrowController.remove_wait_request(row.isbn, row.request.studentName);
        refreshTable();
    }

    private WaitingFormData showWaitingDialog(String title, WaitingRow row) {
        JDialog dialog = createDialog(title);
        JPanel form = createDialogPanel();

        JTextField isbnField = new JTextField(18);
        JTextField titleField = new JTextField(18);
        JTextField studentField = new JTextField(18);
        JTextField requestDateField = new JTextField(18);
        JTextField priorityField = new JTextField(18);
        JCheckBox graduatedBox = new JCheckBox("Graduated");
        graduatedBox.setBackground(new Color(248, 249, 251));

        if (row != null) {
            isbnField.setText(String.valueOf(row.isbn));
            titleField.setText(row.bookTitle);
            studentField.setText(nullToEmpty(row.request.studentName));
            requestDateField.setText(formatDate(row.request.requestDate));
            priorityField.setText(String.valueOf(row.priority));
            graduatedBox.setSelected(row.request.isGraduated);
            isbnField.setEditable(false);
            titleField.setEditable(false);
            studentField.setEditable(false);
            priorityField.setEditable(false);
        }

        addDialogField(form, "ISBN", isbnField, 0);
        if (row != null) {
            addDialogField(form, "Title", titleField, 1);
        }
        addDialogField(form, "Student", studentField, row == null ? 1 : 2);
        if (row != null) {
            addDialogField(form, "Requested", requestDateField, 3);
        }
        if (row != null) {
            addDialogField(form, "Priority", priorityField, 4);
        }

        GridBagConstraints checkConstraints = new GridBagConstraints();
        checkConstraints.gridx = 1;
        checkConstraints.gridy = row == null ? 3 : 5;
        checkConstraints.anchor = GridBagConstraints.WEST;
        checkConstraints.insets = new Insets(0, 0, 8, 0);
        form.add(graduatedBox, checkConstraints);

        final WaitingFormData[] result = new WaitingFormData[1];
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBackground(new Color(248, 249, 251));
        ModernButton saveButton = ModernButton.toolbar("Save");
        ModernButton cancelButton = ModernButton.toolbar("Cancel");
        saveButton.addActionListener(event -> {
            WaitingFormData data = readWaitingFormData(isbnField, studentField, requestDateField, graduatedBox, row == null);
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

    private WaitingFormData readWaitingFormData(JTextField isbnField, JTextField studentField,
            JTextField requestDateField, JCheckBox graduatedBox, boolean useCurrentDate) {
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
        if (useCurrentDate) {
            return new WaitingFormData(isbn, student, LocalDate.now(), graduatedBox.isSelected());
        }
        LocalDate requestDate;
        try {
            requestDate = LocalDate.parse(requestDateField.getText().trim());
        } catch (DateTimeParseException ex) {
            showMessage("Enter the requested date as YYYY-MM-DD.");
            return null;
        }
        return new WaitingFormData(isbn, student, requestDate, graduatedBox.isSelected());
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

    private static class WaitingRow {
        final int isbn;
        final String bookTitle;
        final BookQueue request;
        final int priority;

        WaitingRow(int isbn, String bookTitle, BookQueue request, int priority) {
            this.isbn = isbn;
            this.bookTitle = bookTitle;
            this.request = request;
            this.priority = priority;
        }
    }

    private static class WaitingFormData {
        final int isbn;
        final String student;
        final LocalDate requestDate;
        final boolean graduated;

        WaitingFormData(int isbn, String student, LocalDate requestDate, boolean graduated) {
            this.isbn = isbn;
            this.student = student;
            this.requestDate = requestDate;
            this.graduated = graduated;
        }
    }
}
