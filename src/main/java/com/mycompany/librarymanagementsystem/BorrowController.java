package com.mycompany.librarymanagementsystem;

import java.util.*;
import java.time.*;

public class BorrowController {

    final static int max_borrowings = 5;
    private static final Scanner scanner = new Scanner(System.in);
    //static Comparator<WaitRequest> waitComparator = 
    //Comparator.comparing(WaitRequest::isGraduated).reversed().thenComparing(WaitRequest::requestDate);
    static HashMap<Integer, Borrow> borrow_log = new HashMap();
    static HashMap<Integer, ArrayList<Integer>> borrowed_id_by_ISBN = new HashMap();
    static HashMap<String, ArrayList<Integer>> borrowed_id_by_name = new HashMap();
    static HashMap<Integer, MaxPriorityQueue> wait_requests_queue_by_ISBN = new HashMap();
    static AVLExpectedReturn expected_return_index = new AVLExpectedReturn();

    // BUG: Availability is checked only against AVLBookController.root, while BookController stores
    // books in a separate BST root. Calls can report unavailable for books added through BookController.
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

    // BUG: The method name says "exceeded", but it returns true when the student is still allowed
    // to borrow. This inverted meaning makes direct GUI validation easy to misuse.
    public static boolean check_max_borrowings_exceeded(String name) {
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

    // BUG: This method depends on check_available_book_by_ISBN, so it inherits the separate-root
    // availability issue. The request_date parameter is also ignored for successful borrows.
    public static void borrow_book(Book book, String student_name, LocalDate request_date, LocalDate expected_return, boolean is_graduated) {
        if (!check_available_book_by_ISBN(book.ISBN)) {
            if (!check_max_borrowings_exceeded(student_name)) {
                System.out.println("Borrow limited exceeded, can't be added to waiting list, you must return a book if you want to borrow another");
                return;
            }
            MaxPriorityQueue heap = get_waitlist(book.ISBN);
            for (BookQueue r : heap.getElements()) {
                if (r.studentName.equals(student_name)) {
                    System.out.println("Already on waiting list for " + book.title);
                    return;
                }
            }
            System.out.println("Book currently unavailable, added to waiting list");
            heap.insert(new BookQueue(student_name, is_graduated, request_date));
            return;
        }

        if (!check_max_borrowings_exceeded(student_name)) {
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

    // BUG: This method delegates to return_book(int), which can block on console input through
    // processWaitingList. GUI code should not call this wrapper until that flow is made non-blocking.
    public static void return_book(String student_name, int ISBN) {
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

    // BUG: Returning a book can call processWaitingList, which reads from System.in for the next
    // expected return date. GUI code should not call this because it can block the Swing event thread.
    public static void return_book(int recordId) {
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
        removeFromWaitingList(borrow.book.ISBN, borrow.student_name);
        processWaitingList(borrow.book.ISBN);
    }

    ArrayList<Borrow> overdue() {
        ArrayList<Integer> ids = expected_return_index.find_less_than(expected_return_index.root, LocalDate.now());
        ArrayList<Borrow> borrows = new ArrayList<Borrow>();
        for (var id : ids) {
            if (borrow_log.get(id).return_date == null) {
                borrows.add(borrow_log.get(id));
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

    private static MaxPriorityQueue get_waitlist(int ISBN) {
        MaxPriorityQueue heap = wait_requests_queue_by_ISBN.get(ISBN);
        if (heap == null) {
            heap = new MaxPriorityQueue();
            wait_requests_queue_by_ISBN.put(ISBN, heap);
        }
        return heap;
    }

    // BUG: This method sorts the live heap list returned by getElements(), which can break the
    // priority queue structure. It also reads from System.in, so GUI code must not call it.
    private static void processWaitingList(int ISBN) {
        MaxPriorityQueue heap = wait_requests_queue_by_ISBN.get(ISBN);
        if (heap == null || heap.isEmpty() || !check_available_book_by_ISBN(ISBN)) {
            return;
        }

        // 1. Get all waiters and sort them by priority (highest first)
        ArrayList<BookQueue> waiters = heap.getElements();
        Collections.sort(waiters, new Comparator<BookQueue>() {
            @Override
            public int compare(BookQueue r1, BookQueue r2) {
                if (MaxPriorityQueue.hasHigherPriority(r1, r2)) {
                    return -1;
                } else if (MaxPriorityQueue.hasHigherPriority(r2, r1)) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        // 2. Find the first eligible waiter
        for (BookQueue next : waiters) {
            if (check_max_borrowings_exceeded(next.studentName)) {
                // This student can borrow now

                // Ask the user for the expected return date
                LocalDate expected = readExpectedReturnDate(scanner, next.studentName);

                // Remove this specific waiter from the heap
                heap.remove(next);

                // Retrieve the book
                BookNode node = AVLBookController.search_for_book(ISBN);
                if (node != null) {
                    // Use request_date = now (the waiter's original request time is already inside BookQueue)
                    borrow_book(node.b, next.studentName, LocalDate.now(), expected, next.isGraduated);
                    System.out.println("Book assigned to waiting student: " + next.studentName);
                }
                return;   // Only one copy was freed
            }
        }
        // If no eligible student, do nothing; waiting list unchanged
    }

    // BUG: This method reads from the console, which can block a Swing GUI. GUI flows should collect
    // dates from Swing inputs instead of calling this method.
    private static LocalDate readExpectedReturnDate(Scanner scanner, String studentName) {
        System.out.println("Enter expected return date for " + studentName + " (day month year):");
        int day = scanner.nextInt();
        int month = scanner.nextInt();
        int year = scanner.nextInt();
        scanner.nextLine(); // consume the newline
        return LocalDate.of(year, month, day);
    }

    private static void removeFromWaitingList(int ISBN, String studentName) {
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
}
    
    
    



//adding later filtering by expected return date
