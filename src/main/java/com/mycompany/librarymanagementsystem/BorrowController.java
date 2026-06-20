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

    static int next_id = 0;
    static HashMap<Integer, Borrow> borrow_log = new HashMap();
    static HashMap<Integer, ArrayList<Integer>> borrowed_id_by_ISBN = new HashMap();
    static HashMap<String, ArrayList<Integer>> borrowed_id_by_name = new HashMap();

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
            if (counter < borrow_log.get(ids.get(0)).book.copy) {
                return true;
            }
        }
        return false;
    }

    void borrow_book(Book book, String student_name, LocalDate expected_return, boolean is_graduated) {
        if (!check_available_book_by_ISBN(book.ISBN)) {
            System.out.println("no available copies");
            return;
        }
        int id = next_id;
        next_id++;

        Borrow borrow = new Borrow(book, student_name, expected_return, is_graduated);

        borrow_log.put(id, borrow);

        ArrayList<Integer> ISBN_list = borrowed_id_by_ISBN.get(book.ISBN);
        if (ISBN_list == null) {
            ISBN_list = new ArrayList<>();
            borrowed_id_by_ISBN.put(book.ISBN, ISBN_list);
        }
        ISBN_list.add(id);

        ArrayList<Integer> name_list = borrowed_id_by_name.get(student_name);
        if (name_list == null) {
            name_list = new ArrayList<>();
            borrowed_id_by_name.put(student_name, name_list);
        }
        name_list.add(id);

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
}
