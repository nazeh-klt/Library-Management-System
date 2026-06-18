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

    HashMap<Integer, Borrow> borrow_log = new HashMap();
    HashMap<Integer, ArrayList<Integer>> borrowed_id_by_ISBN = new HashMap();
    HashMap<String, ArrayList<Integer>> borrowed_id_by_name = new HashMap();

    boolean check_available_book_by_ISBN(int ISBN) {
        int counter = 0;
        ArrayList<Integer> ids = borrowed_id_by_ISBN.get(ISBN);
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

    void borrow_book(Book book) {
        return;
    }

}
