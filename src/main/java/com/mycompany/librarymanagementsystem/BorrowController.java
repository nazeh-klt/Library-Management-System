package com.mycompany.librarymanagementsystem;

import java.util.*;
import java.time.*;
import java.util.function.Function;

public class BorrowController {

    final static int max_borrowings = 5;
    private static final Scanner scanner = new Scanner(System.in);
    static HashMap<Integer, Borrow> borrow_log = new HashMap();
    static HashMap<Integer, ArrayList<Integer>> borrowed_id_by_ISBN = new HashMap();
    static HashMap<String, ArrayList<Integer>> borrowed_id_by_name = new HashMap();
    static HashMap<Integer, MaxPriorityQueue> wait_requests_queue_by_ISBN = new HashMap();
    static AVLExpectedReturn expected_return_index = new AVLExpectedReturn();

    
    public static boolean check_available_book_by_ISBN(int ISBN) {
        BookNode node = AVLBookController.search_for_book(ISBN);
        if (node == null) return false;

        int activeBorrows = 0;
        ArrayList<Integer> ids = borrowed_id_by_ISBN.get(ISBN);

        if (ids != null) {
            for (int id : ids) {
                Borrow b = borrow_log.get(id);
                if (b != null && b.return_date == null) {
                    activeBorrows++;
                }
            }
        }

        return activeBorrows < node.b.copy;
    }
    public static boolean can_borrow_more(String name) {
        int counter = 0;
        ArrayList<Integer> ids = borrowed_id_by_name.get(name);
        if (ids == null) {
            return true;
        }
        for (var id : ids) {
            if (borrow_log.get(id).return_date == null) {
                counter++;
            }
        }
        return counter < max_borrowings;
    }
    public static boolean can_reduce_copies(int ISBN, int newCount) {
        if (newCount < 0) {
            return false;
        }
        int activeBorrows = 0;
        ArrayList<Integer> ids = borrowed_id_by_ISBN.get(ISBN);
        if (ids != null) {
            for (int id : ids) {
                Borrow b = borrow_log.get(id);
                if (b != null && b.return_date == null) {
                    activeBorrows++;
                }
            }
        }
        return newCount >= activeBorrows;
    }
    public static boolean has_active_borrows(int ISBN) {
        ArrayList<Integer> ids = borrowed_id_by_ISBN.get(ISBN);
        if (ids == null) return false;
        for (int id : ids) {
            Borrow b = borrow_log.get(id);
            if (b != null && b.return_date == null) return true;
        }
        return false;
    }

    
    public enum BorrowResult {
        BORROWED,
        ADDED_TO_WAITLIST,
        ALREADY_ON_WAITLIST,
        BORROW_LIMIT_EXCEEDED
    }
    public static BorrowResult borrow_book(Book book, String student_name, LocalDate request_date, LocalDate expected_return, boolean is_graduated) {
        if (!check_available_book_by_ISBN(book.ISBN)) {
            if (!can_borrow_more(student_name)) {
                System.out.println("Borrow limited exceeded, can't be added to waiting list, you must return a book if you want to borrow another");
                return BorrowResult.BORROW_LIMIT_EXCEEDED;
            }
            MaxPriorityQueue heap = get_waitlist(book.ISBN);
            for (BookQueue r : heap.getElements()) {
                if (r.studentName.equals(student_name)) {
                    System.out.println("Already on waiting list for " + book.title);
                    return BorrowResult.ALREADY_ON_WAITLIST;
                }
            }
            System.out.println("Book currently unavailable, added to waiting list");
            heap.insert(new BookQueue(student_name, is_graduated, request_date));
            return BorrowResult.ADDED_TO_WAITLIST;
        }

        if (!can_borrow_more(student_name)) {
            System.out.println("Borrow limited exceeded, you must return a book if you want to borrow another");
            return BorrowResult.BORROW_LIMIT_EXCEEDED;
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
        return BorrowResult.BORROWED;
    }

    public static boolean return_book(String studentName, int ISBN) {
        ArrayList<Integer> ids = borrowed_id_by_name.get(studentName);

        if (ids == null || ids.isEmpty()) {
            System.out.println("ERROR: No borrow records found for student: " + studentName);
            return false;
        }

        Integer foundId = null;
        LocalDate earliestDate = null;

        // Isolate the oldest unresolved matching loan record
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
            System.out.println("ERROR: " + studentName + " has no active borrow for ISBN: " + ISBN);
            return false;
        }

        return return_book(foundId);
    }
    public static boolean return_book(int recordId) {
        Borrow borrow = borrow_log.get(recordId);

        if (borrow == null) {
            System.out.println("ERROR: No borrow record found with ID: " + recordId);
            return false;
        }

        if (borrow.return_date != null) {
            System.out.println("ERROR: This book was already returned on: " + borrow.return_date);
            return false;
        }

        // Process the return details dynamically using system time
        borrow.return_date = LocalDate.now();
        System.out.println("SUCCESS: Book returned for Record ID: " + recordId);

        // Maintain collection sync and trigger waitlist promotion rules
        remove_wait_request(borrow.book.ISBN, borrow.student_name);
        processWaitingList(borrow.book.ISBN);

        return true;
    }
    
    public static boolean delete_borrow_record(int recordId) {
        Borrow borrow = borrow_log.remove(recordId);
        if (borrow == null) {
            return false;
        }
        ArrayList<Integer> isbnList = borrowed_id_by_ISBN.get(borrow.book.ISBN);
        if (isbnList != null) {
            isbnList.remove(Integer.valueOf(recordId));
        }
        ArrayList<Integer> nameList = borrowed_id_by_name.get(borrow.student_name);
        if (nameList != null) {
            nameList.remove(Integer.valueOf(recordId));
        }
        return true;
    }
    
    ArrayList<Borrow> overdue() {
        ArrayList<Integer> ids = expected_return_index.find_less_than(expected_return_index.root, LocalDate.now());
        ArrayList<Borrow> borrows = new ArrayList<Borrow>();
        for (var id : ids) {
            Borrow b = borrow_log.get(id);
            if (b != null && b.return_date == null) {
                borrows.add(b);
            }
        }
        return borrows;
    }
    ArrayList<Borrow> filter_by_student_name(String name) {
        ArrayList<Integer> ids = borrowed_id_by_name.get(name);
        if (ids == null) {
            return new ArrayList<>();
        }
        ArrayList<Borrow> borrows = new ArrayList<Borrow>();
        for (var id : ids) {
            if (borrow_log.get(id).return_date == null) {
                borrows.add(borrow_log.get(id));
            }
        }
        return borrows;

    }
    ArrayList<Borrow> filter_by_ISBN(int isbn) {
        ArrayList<Integer> ids = borrowed_id_by_ISBN.get(isbn);
        if (ids == null) {
            return new ArrayList<>();
        }
        ArrayList<Borrow> borrows = new ArrayList<Borrow>();
        for (var id : ids) {
            if (borrow_log.get(id).return_date == null) {
                borrows.add(borrow_log.get(id));
            }
        }
        return borrows;

    }

    public static String add_to_waitlist(int ISBN, String studentName, boolean isGraduated) {
        if (AVLBookController.search_for_book(ISBN) == null) {
            return "No book with that ISBN exists.";
        }
        if (check_available_book_by_ISBN(ISBN)) {
            return "This book currently has copies available - borrow it directly instead of waiting.";
        }
        if (!can_borrow_more(studentName)) {
            return studentName + " already has the maximum number of active borrows and can't join the waiting list.";
        }
        MaxPriorityQueue heap = get_waitlist(ISBN);
        for (BookQueue r : heap.getElements()) {
            if (r.studentName.equals(studentName)) {
                return studentName + " is already on the waiting list for this book.";
            }
        }
        LocalDate requestDate = LocalDate.now();
        heap.insert(new BookQueue(studentName, isGraduated, requestDate));
        return null;
    }

    private static MaxPriorityQueue get_waitlist(int ISBN) {
        MaxPriorityQueue heap = wait_requests_queue_by_ISBN.get(ISBN);
        if (heap == null) {
            heap = new MaxPriorityQueue();
        }
        return heap;
    }

    private static void processWaitingList(int ISBN) {
        MaxPriorityQueue queue = wait_requests_queue_by_ISBN.get(ISBN);

        if (queue == null || queue.isEmpty()) {
            return;
        }

        BookNode node = AVLBookController.search_for_book(ISBN);
        if (node == null) {
            return;
        }

        if (check_available_book_by_ISBN(ISBN)) {
            // Extract the highest-priority waiting student record
            BookQueue nextStudent = queue.extractMax();

            // Compute the standard 14-day allocation window from today
            LocalDate automatedExpectedReturn = LocalDate.now().plusDays(14);

            // Execute borrow_book matching your exact five-parameter signature
            borrow_book(
                node.b, 
                nextStudent.studentName, 
                nextStudent.requestDate, 
                automatedExpectedReturn, 
                nextStudent.isGraduated
            );

            System.out.println("WAITLIST PROMOTION: " + nextStudent.studentName + 
                               " automatically assigned a copy of \"" + node.b.title + "\".");
        }
    }
    private static LocalDate readExpectedReturnDateConsole(String studentName) {
        System.out.println("Enter expected return date for " + studentName + " (day month year):");
        int day = scanner.nextInt();
        int month = scanner.nextInt();
        int year = scanner.nextInt();
        scanner.nextLine();
        return LocalDate.of(year, month, day);
    }

    public static void remove_wait_request(int ISBN, String studentName) {
        MaxPriorityQueue heap = wait_requests_queue_by_ISBN.get(ISBN);
        if (heap == null) {
            return;
        }
        for (BookQueue r : heap.getElements()) {
            if (r.studentName.equals(studentName)) {
                heap.remove(r);
                break;
            }
        }
    }

    public static boolean update_wait_request(int ISBN, String studentName, boolean newGraduated, LocalDate newRequestDate) {
        MaxPriorityQueue heap = wait_requests_queue_by_ISBN.get(ISBN);
        if (heap == null) {
            return false;
        }
        BookQueue found = null;
        for (BookQueue r : heap.getElements()) {
            if (r.studentName.equals(studentName)) {
                found = r;
                break;
            }
        }
        if (found == null) {
            return false;
        }
        heap.remove(found);
        heap.insert(new BookQueue(studentName, newGraduated, newRequestDate));
        return true;
    }
}
