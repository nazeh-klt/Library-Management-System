package com.mycompany.librarymanagementsystem;

public class BookController {
    static BookNode root = null;
    //static BookNode AVLroot = null;
    public static void add_book(int ISBN, int copy, String title, String author, String category) {
        root = add_book_to_bst(root, ISBN, copy, title, author, category);
    }
    public static BookNode search_for_book(int ISBN) {
        return search_for_book_bst(root, ISBN);
    }
    public static void delete_book(int ISBN) {
        root = remove_book_from_library_bst(root, ISBN);
    }
    public static void print(){
        print(root);
    }
    
    private static void print(BookNode root){
        if(root == null)
            return;
        print(root.left);
        System.out.println(root.b.ISBN);
        print(root.right);
    }
    private static BookNode add_book_to_bst(BookNode root, int ISBN, int copy, String title, String author, String category) {
        if (root == null) {
            return new BookNode(new Book(ISBN, copy, title, author, category));
        }
        if (root.b.ISBN < ISBN) {
            root.right = add_book_to_bst(root.right, ISBN, copy, title, author, category);
        } else if (root.b.ISBN > ISBN) {
            root.left = add_book_to_bst(root.left, ISBN, copy, title, author, category);
        } else {
            System.out.println("هذا الكتاب موجود بالفعل في المكتبة");
        }
        return root;
    }
    private static BookNode search_for_book_bst(BookNode root, int ISBN) {
        if (root == null) {
            return null;
        }
        if (root.b.ISBN == ISBN) {
            return root;
        }

        if (ISBN > root.b.ISBN) {
            return search_for_book_bst(root.right, ISBN);
        }

        return search_for_book_bst(root.left, ISBN);
    }
    private static BookNode getLeftMost(BookNode root){
        BookNode current = root;
        while(current.left != null){
            current = current.left;
        }
        return current;
    }
    private static BookNode remove_book_from_library_bst(BookNode root, int ISBN) {
        if (root == null) {
            return root;
        }

        if (root.b.ISBN > ISBN) {
            root.left = remove_book_from_library_bst(root.left, ISBN);
        } else if (root.b.ISBN < ISBN) {
            root.right = remove_book_from_library_bst(root.right, ISBN);
        } else {
            // Node with 0 or 1 child
            if (root.left == null) {
                
                return root.right;
            }
            if (root.right == null) {
                return root.left;
            }

            // Node with 2 children
            BookNode current = getLeftMost(root.right);
            root.b.ISBN = current.b.ISBN;
            root.b.author = current.b.author;
            root.b.category = current.b.category;
            root.b.title = current.b.title;
            root.b.copy = current.b.copy;
            root.right = remove_book_from_library_bst(root.right, current.b.ISBN);
        }
        return root;
    }
}
