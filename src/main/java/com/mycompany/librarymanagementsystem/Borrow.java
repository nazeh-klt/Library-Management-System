package com.mycompany.librarymanagementsystem;
import java.time.*;


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
