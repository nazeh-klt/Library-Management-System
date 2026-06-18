package com.mycompany.librarymanagementsystem;

public class BookController {
    static BookNode root=null;

    public static void add_book(int ISBN) {
        root = add_book_to_bst(root, ISBN);
    }
    public static BookNode search_for_book(int ISBN) {
        return search_for_book_bst(root, ISBN);
    }
    public static void delete_book(int ISBN) {
        root = delNode(root, ISBN);
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
    private static BookNode add_book_to_bst(BookNode root, int ISBN) {
        // root is the first book added to library, ISBN stands for International Standard Book Number, the ISBN of the book we want to add
        if (root == null) {
            // if there are currently no books we should add a book to the library
            return new BookNode(new Book(ISBN));
        }
        if (root.b.ISBN < ISBN) {
            root.right = add_book_to_bst(root.right, ISBN);
        } else if (root.b.ISBN > ISBN) {
            root.left = add_book_to_bst(root.left, ISBN);
        } else {
            root.b.counter++;
        }
        return root;
    }
    private static BookNode search_for_book_bst(BookNode root, int ISBN) {
        // root is null -> return false
        if (root == null) {
            return null;
        }
        // if root has key -> return true
        if (root.b.ISBN == ISBN) {
            return root;
        }

        if (ISBN > root.b.ISBN) {
            return search_for_book_bst(root.right, ISBN);
        }

        return search_for_book_bst(root.left, ISBN);
    }
    private static BookNode getSuccessor(BookNode curr) {
        curr = curr.right;
        while (curr != null && curr.left != null) {
            curr = curr.left;
        }
        return curr;
    }
    private static BookNode delNode(BookNode root, int ISBN) {
        if (root == null) {
            return root;
        }

        if (root.b.ISBN > ISBN) {
            root.left = delNode(root.left, ISBN);
        } else if (root.b.ISBN < ISBN) {
            root.right = delNode(root.right, ISBN);
        } else {
            // Node with 0 or 1 child
            if (root.left == null) {
                return root.right;
            }
            if (root.right == null) {
                return root.left;
            }

            // Node with 2 children
            BookNode succ = getSuccessor(root);
            root.b.ISBN = succ.b.ISBN;
            root.right = delNode(root.right, succ.b.ISBN);
        }
        return root;
    }
}
