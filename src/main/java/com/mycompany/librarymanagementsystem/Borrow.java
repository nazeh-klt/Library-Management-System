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
    static int nextid = 0;
    int id;
    Book book;
    String student_name;
    LocalDate borrow_date;
    LocalDate return_date;
    LocalDate expected_return;
    boolean is_graduated;


    Borrow(Book book, String name ,LocalDate expected_return, boolean is_graduated){
        id = nextid;
        nextid++;
        this.book = book;
        this.student_name = name;
        this.borrow_date = LocalDate.now();
        this.expected_return = expected_return;
        this.is_graduated = is_graduated;
        
    }

}
