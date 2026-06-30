package com.mycompany.librarymanagementsystem;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

    private final JTextField isbnField = new JTextField(10);
    private final JTextField titleField = new JTextField(18);
    private final JTextField studentField = new JTextField(18);
    private final JTextField requestDateField = new JTextField(12);
    private final JTextField priorityField = new JTextField(8);
    private final JCheckBox graduatedBox = new JCheckBox("Graduated");

    public WaitingListPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Color.WHITE);

        waitingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        waitingTable.setRowSorter(rowSorter);
        waitingTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                showSelectedRequestDetails();
            }
        });

        add(createToolbar(), BorderLayout.NORTH);
        add(new JScrollPane(waitingTable), BorderLayout.CENTER);
        add(createDetailsPanel(), BorderLayout.SOUTH);

        refreshTable();
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setBackground(Color.WHITE);

        ModernButton addButton = ModernButton.toolbar("Add Request");
        ModernButton editButton = ModernButton.toolbar("Edit Selected");
        ModernButton deleteButton = ModernButton.toolbar("Delete Selected");
        ModernButton searchButton = ModernButton.toolbar("Search");
        ModernButton refreshButton = ModernButton.toolbar("Refresh");

        // TODO: Adding requests safely needs a public backend method that uses BookController data
        // instead of BorrowController.borrow_book, which depends on buggy availability logic.
        addButton.addActionListener(event -> showTodo("Add Request needs a safe backend method before it can be enabled."));
        // TODO: Editing wait requests needs a backend method that reorders the priority queue safely.
        editButton.addActionListener(event -> showSelectedTodo("Edit Selected needs a safe backend update method before it can be enabled."));
        // TODO: Deleting wait requests needs a public backend method instead of direct heap mutation.
        deleteButton.addActionListener(event -> showSelectedTodo("Delete Selected needs a safe backend delete method before it can be enabled."));
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

        JLabel title = new JLabel("Waiting Request Details");
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
        addDetailField(detailsPanel, "Student", studentField, 4, 1);
        addDetailField(detailsPanel, "Requested", requestDateField, 6, 1);
        addDetailField(detailsPanel, "Priority", priorityField, 8, 1);

        GridBagConstraints checkConstraints = new GridBagConstraints();
        checkConstraints.gridx = 0;
        checkConstraints.gridy = 2;
        checkConstraints.gridwidth = 2;
        checkConstraints.anchor = GridBagConstraints.WEST;
        checkConstraints.insets = new Insets(8, 0, 0, 0);
        graduatedBox.setBackground(new Color(248, 249, 251));
        detailsPanel.add(graduatedBox, checkConstraints);

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

            List<BookQueue> requests = new ArrayList<>(queue.heap);
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
        BookNode node = BookController.search_for_book(isbn);
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

    private void showSelectedRequestDetails() {
        WaitingRow row = getSelectedWaitingRow();
        if (row == null) {
            return;
        }
        isbnField.setText(String.valueOf(row.isbn));
        titleField.setText(row.bookTitle);
        studentField.setText(nullToEmpty(row.request.studentName));
        requestDateField.setText(formatDate(row.request.requestDate));
        priorityField.setText(String.valueOf(row.priority));
        graduatedBox.setSelected(row.request.isGraduated);
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

    private String formatDate(LocalDate date) {
        return date == null ? "" : date.toString();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void showTodo(String message) {
        JOptionPane.showMessageDialog(this, "TODO: " + message);
    }

    private void showSelectedTodo(String message) {
        if (getSelectedWaitingRow() == null) {
            JOptionPane.showMessageDialog(this, "Select a waiting request first.");
            return;
        }
        showTodo(message);
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
}
