package com.mycompany.librarymanagementsystem;

import javax.swing.SwingUtilities;

public class LibraryManagementSystem {

    public static void main(String[] args) {
        AVLBookController.add_avl_book(111, 1, "HELLO", "sami", "politics");
        AVLBookController.add_avl_book(222, 5, "How to make money", "nour", "economics");
        AVLBookController.add_avl_book(100, 2, "the art of the deal", "trump", "politics");
        
        
        SwingUtilities.invokeLater(() -> new DashboardFrame().setVisible(true));
    }
}
