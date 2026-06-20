/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.librarymanagementsystem;

import java.util.*;
import java.time.*;

/**
 *
 * @author Admin
 */
public class BorrowController {

    final static int max_borrowings = 5;
    static HashMap<Integer, Borrow> borrow_log = new HashMap();
    static HashMap<Integer, ArrayList<Integer>> borrowed_id_by_ISBN = new HashMap();
    static HashMap<String, ArrayList<Integer>> borrowed_id_by_name = new HashMap();
    static AVLExpectedReturn expected_return_index = new AVLExpectedReturn();

    boolean check_available_book_by_ISBN(int ISBN) {
        int counter = 0;
        ArrayList<Integer> ids = borrowed_id_by_ISBN.get(ISBN);
        if (ids == null) {
            return true;
        }
        if (ids != null) {
            for (var id : ids) {
                if (borrow_log.get(id).return_date == null) {
                    counter++;
                }
            }
            if (counter < borrow_log.get(ids.get(0)).book.counter) {
                return true;
            }
        }
        return false;
    }
    boolean check_max_borrowings_exceeded(String name){
        int counter = 0;
        ArrayList<Integer> ids = borrowed_id_by_name.get(name);
        if (ids == null) return true;
        for(var id: ids){
            if(borrow_log.get(id).return_date == null){
                counter++;
            }
        }
        if (counter >= max_borrowings)
            return false;
        return true;
    }

    void borrow_book(Book book, String student_name, LocalDate expected_return, boolean is_graduated) {
        if (!check_available_book_by_ISBN(book.ISBN) || !check_max_borrowings_exceeded(student_name)) {
            if(!check_available_book_by_ISBN(book.ISBN))
                System.out.println("Book Unavailable, no copies currently");
            if(!check_max_borrowings_exceeded(student_name))
                System.out.println("Borrow limited exceeded, you must return a book if you want to borrow another");
            return;
        }
        

        Borrow borrow = new Borrow(book, student_name, expected_return, is_graduated);

        borrow_log.put(borrow.id, borrow);

        ArrayList<Integer> ISBN_list = borrowed_id_by_ISBN.get(book.ISBN);
        if (ISBN_list == null) {
            ISBN_list = new ArrayList<>();
            borrowed_id_by_ISBN.put(book.ISBN, ISBN_list);
        }
        ISBN_list.add(borrow.id);

        ArrayList<Integer> name_list = borrowed_id_by_name.get(student_name);
        if (name_list == null) {
            name_list = new ArrayList<>();
            borrowed_id_by_name.put(student_name, name_list);
        }

        name_list.add(borrow.id);
        expected_return_index.root = AVLExpectedReturn.insert(expected_return_index.root, expected_return, borrow.id);
    }

    void return_book(String student_name, int ISBN) {
        ArrayList<Integer> ids = borrowed_id_by_name.get(student_name);

        if (ids == null || ids.isEmpty()) {
            System.out.println("ERROR: No borrow records found for student: " + student_name);
            return;
        }

        Integer foundId = null;
        LocalDate earliestDate = null;

        for (int id : ids) {
            Borrow b = borrow_log.get(id);

            if (b != null && b.book.ISBN == ISBN && b.return_date == null) {

                if (foundId == null || b.expected_return.isBefore(earliestDate)) {
                    foundId = id;
                    earliestDate = b.expected_return;
                }
            }
        }

        if (foundId == null) {
            System.out.println("ERROR: " + student_name + " has no active borrow for ISBN: " + ISBN);
            return;
        }

        return_book(foundId);
    }

    void return_book(int recordId) {
        Borrow borrow = borrow_log.get(recordId);

        if (borrow == null) {
            System.out.println("ERROR: No borrow record found with ID: " + recordId);
            return;
        }

        if (borrow.return_date != null) {
            System.out.println("ERROR: This book was already returned on: " + borrow.return_date);
            return;
        }

        borrow.return_date = LocalDate.now();
        System.out.println("SUCCESS: Book returned for Record ID: " + recordId);
    }
    ArrayList<Borrow> overdue(){
        ArrayList<Integer> ids= expected_return_index.find_less_than(expected_return_index.root, LocalDate.now());
        ArrayList<Borrow> borrows = new ArrayList<Borrow>();
        for(var id: ids){
            if(borrow_log.get(id).return_date == null)
                borrows.add(borrow_log.get(id));
        }
        return borrows;
       
}
    ArrayList<Borrow> filter_by_student_name(String name){
        ArrayList<Integer> ids= borrowed_id_by_name.get(name);
        if (ids == null) return new ArrayList<>();
        ArrayList<Borrow> borrows = new ArrayList<Borrow>();
        for(var id: ids){
            if(borrow_log.get(id).return_date == null)
                borrows.add(borrow_log.get(id));
        }
        return borrows;
       
}
        ArrayList<Borrow> filter_by_ISBN(int isbn){
        ArrayList<Integer> ids= borrowed_id_by_ISBN.get(isbn);
        if (ids == null) return new ArrayList<>();
        ArrayList<Borrow> borrows = new ArrayList<Borrow>();
        for(var id: ids){
            if(borrow_log.get(id).return_date == null)
                borrows.add(borrow_log.get(id));
        }
        return borrows;
       
}
}


//adding later filtering by expected return date
