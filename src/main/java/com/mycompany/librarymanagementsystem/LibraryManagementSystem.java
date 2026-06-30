package com.mycompany.librarymanagementsystem;

import javax.swing.SwingUtilities;

public class LibraryManagementSystem {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DashboardFrame().setVisible(true));
    }
}
