/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.librarymanagementsystem;
import java.time.*;
/**
 *
 * @author Admin
 */
public class Borrow {
    Book book;
    String name;
    String book_title;
    LocalDate borrow_date;
    LocalDate return_date;
    LocalDate expected_return;

    Borrow(Book book, String name, String book_title, LocalDate borrow_date, LocalDate return_date ,LocalDate expected_return ){
        this.book = book;
        this.name = name;
        this.borrow_date = borrow_date;
        this.expected_return = expected_return;
        
        
    }

}
