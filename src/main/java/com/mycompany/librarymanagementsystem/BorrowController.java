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

    // Availability now checks AVLBookController exclusively, which is the single book store the
    // GUI writes to (BooksPanel was switched from BookController to AVLBookController).
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

    // Used by BooksPanel before allowing a full delete - a book with active loans out
    // shouldn't be removable from the catalog.
    public static boolean has_active_borrows(int ISBN) {
        ArrayList<Integer> ids = borrowed_id_by_ISBN.get(ISBN);
        if (ids == null) return false;
        for (int id : ids) {
            Borrow b = borrow_log.get(id);
            if (b != null && b.return_date == null) return true;
        }
        return false;
    }

    // What actually happened when borrow_book was called - lets callers (like the GUI) report
    // the real outcome instead of guessing from an availability check made before the call,
    // which can be wrong if the student was blocked for an unrelated reason (borrow limit,
    // already on the waitlist).
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

    // Console-based overloads kept for a non-GUI/testing entry point. Both now delegate to the
    // date-provider overloads below rather than duplicating the return logic.
    public static void return_book(String student_name, int ISBN) {
        return_book(student_name, ISBN, BorrowController::readExpectedReturnDateConsole);
    }

    public static void return_book(int recordId) {
        return_book(recordId, BorrowController::readExpectedReturnDateConsole);
    }

    // GUI-safe overload: instead of blocking on System.in when the waiting list needs a new
    // expected-return date, this takes a callback the caller supplies (e.g. a Swing dialog).
    public static void return_book(String student_name, int ISBN, Function<String, LocalDate> dateProvider) {
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
        return_book(foundId, dateProvider);
    }

    public static void return_book(int recordId, Function<String, LocalDate> dateProvider) {
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
        remove_wait_request(borrow.book.ISBN, borrow.student_name);
        processWaitingList(borrow.book.ISBN, dateProvider);
    }

    // Removes a borrow record and its index entries entirely (distinct from returning a book -
    // this erases the record itself, e.g. for correcting a data-entry mistake in the GUI).
    // Known limitation: the AVLExpectedReturn date index has no delete operation, so a stale
    // recordId can be left behind there. overdue() below null-checks borrow_log lookups so a
    // stale id can't cause a crash, it will just be silently skipped since it's no longer present.
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

    // Explicit "join the waiting list" action for the GUI's Add Request button. Deliberately
    // separate from borrow_book: that method auto-lends the book if it happens to be available,
    // which isn't what someone clicking "Add Request" in the Waiting List tab is asking for.
    // Returns null on success, or a user-facing reason string on failure.
    public static String add_to_waitlist(int ISBN, String studentName, boolean isGraduated, LocalDate requestDate) {
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
        heap.insert(new BookQueue(studentName, isGraduated, requestDate));
        return null;
    }

    private static MaxPriorityQueue get_waitlist(int ISBN) {
        MaxPriorityQueue heap = wait_requests_queue_by_ISBN.get(ISBN);
        if (heap == null) {
            heap = new MaxPriorityQueue();
            wait_requests_queue_by_ISBN.put(ISBN, heap);
        }
        return heap;
    }

    private static void processWaitingList(int ISBN, Function<String, LocalDate> dateProvider) {
        MaxPriorityQueue heap = wait_requests_queue_by_ISBN.get(ISBN);
        if (heap == null || heap.isEmpty() || !check_available_book_by_ISBN(ISBN)) {
            return;
        }

        // getElements() returns a copy, so sorting it here does not disturb the heap's own
        // internal ordering.
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

        for (BookQueue next : waiters) {
            if (can_borrow_more(next.studentName)) {
                LocalDate expected = dateProvider.apply(next.studentName);
                if (expected == null) {
                    // Caller (e.g. GUI dialog) was cancelled - leave the waiting list untouched
                    // rather than assigning the book with no expected return date.
                    return;
                }

                heap.remove(next);

                BookNode node = AVLBookController.search_for_book(ISBN);
                if (node != null) {
                    borrow_book(node.b, next.studentName, LocalDate.now(), expected, next.isGraduated);
                    System.out.println("Book assigned to waiting student: " + next.studentName);
                }
                return;
            }
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

    // Reorders a waiting request by removing and reinserting it with new priority-relevant
    // fields, rather than mutating a BookQueue in place (its fields aren't otherwise settable,
    // and the heap has no "resift after external mutation" operation).
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
