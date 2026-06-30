package com.mycompany.librarymanagementsystem;

import java.util.*;

public class ReportController {

    private void inOrderBookTraversal(BookNode node, ArrayList<Book> books) {
        if (node == null) return;
        inOrderBookTraversal(node.left, books);
        books.add(node.b);
        inOrderBookTraversal(node.right, books);
    }

    // BUG: This report depends on BorrowController.borrowed_id_by_ISBN, which is maintained by
    // borrow_book/return_book flows that are currently unsafe for the GUI. It can also throw if
    // an ISBN list contains only ids missing from borrow_log.
    public String getMostBorrowedBooksReport() {
        if (BorrowController.borrowed_id_by_ISBN.isEmpty()) {
            return "No books have been borrowed yet.";
        }

        int maxCount = 0;
        int mostBorrowedISBN = -1;

        for (var entry : BorrowController.borrowed_id_by_ISBN.entrySet()) {
            int count = entry.getValue().size();
            if (count > maxCount) {
                maxCount = count;
                mostBorrowedISBN = entry.getKey();
            }
        }

        ArrayList<Integer> ids = BorrowController.borrowed_id_by_ISBN.get(mostBorrowedISBN);
        Borrow sample = BorrowController.borrow_log.get(ids.get(0));

        return "=== MOST BORROWED BOOK ===\n" +
               "Title: " + sample.book.title + "\n" +
               "Author: " + sample.book.author + "\n" +
               "ISBN: " + mostBorrowedISBN + "\n" +
               "Total Borrows: " + maxCount;
    }

    // BUG: This report depends on the same borrow indexes as getMostBorrowedBooksReport, so it can
    // be inaccurate when borrow records and indexes are out of sync.
    public String getMostReadAuthorReport() {
        if (BorrowController.borrowed_id_by_ISBN.isEmpty()) {
            return "No books have been borrowed yet.";
        }

        HashMap<String, Integer> authorCounts = new HashMap<>();

        for (var entry : BorrowController.borrowed_id_by_ISBN.entrySet()) {
            int borrowCount = entry.getValue().size();
            ArrayList<Integer> ids = entry.getValue();
            Borrow sample = BorrowController.borrow_log.get(ids.get(0));
            if (sample == null) continue;

            String author = sample.book.author;

            if (authorCounts.containsKey(author)) {
                int currentCount = authorCounts.get(author);
                authorCounts.put(author, currentCount + borrowCount);
            } else {
                authorCounts.put(author, borrowCount);
            }
        }

        String mostReadAuthor = null;
        int maxCount = 0;
        for (var entry : authorCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostReadAuthor = entry.getKey();
            }
        }

        return "=== MOST READ AUTHOR ===\n" +
               "Author: " + mostReadAuthor + "\n" +
               "Total Borrows: " + maxCount;
    }

    public String getAvailableBooksReport(BookNode bookTreeRoot) {
        ArrayList<Book> allBooks = new ArrayList<>();
        inOrderBookTraversal(bookTreeRoot, allBooks);

        if (allBooks.isEmpty()) {
            return "No books in the library.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== AVAILABLE BOOKS ===\n");
        int totalAvailable = 0;

        for (Book book : allBooks) {
            int activeLoans = 0;
            ArrayList<Integer> ids = BorrowController.borrowed_id_by_ISBN.get(book.ISBN);
            if (ids != null) {
                for (int id : ids) {
                    Borrow b = BorrowController.borrow_log.get(id);
                    if (b != null && b.return_date == null) {
                        activeLoans++;
                    }
                }
            }
            int available = book.copy - activeLoans;
            totalAvailable += available;
            sb.append(book.title)
              .append(" | Total: ").append(book.copy)
              .append(" | Available: ").append(available)
              .append("\n");
        }
        sb.append("Total Available Copies: ").append(totalAvailable);

        return sb.toString();
    }
}
