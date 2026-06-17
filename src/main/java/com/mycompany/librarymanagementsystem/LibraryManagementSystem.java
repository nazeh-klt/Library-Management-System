package com.mycompany.librarymanagementsystem;

import java.util.*;
public class LibraryManagementSystem {

    public static void main(String[] args) {
       BookController bc = new BookController();
       Book b1 = new Book(100);
       Book b2 = new Book(1000);
       Book b3 = new Book(10000);
       Node n = bc.add_book_to_library(null,b1);
       n = bc.add_book_to_library(n, b3);
       System.out.println(bc.search_for_book(n, 100).b.ISBN);
       n = bc.add_book_to_library(n, b2);
       System.out.println(bc.search_for_book(n, 1000).b.ISBN);
       
    }
    
    
}
