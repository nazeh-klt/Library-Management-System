package com.mycompany.librarymanagementsystem;

class Node{
    Book b;
    Node left;
    Node right;
    int height;
    Node(Book b){
        this.b = b;
        left = right = null;
        height = 1;
    }
}

public class BookController {
    // Add a book to the library

    public Node add_book_to_library(Node root, Book b) {
        // root is the first book added to library, ISBN stands for International Standard Book Number, the ISBN of the book we want to add
        if (root == null) {
            // if there are currently no books we should add a book to the library
            return new Node(b);
        }
        if (root.b.ISBN < b.ISBN) {
            root.left = add_book_to_library(root.left, b);
        } else if (root.b.ISBN > b.ISBN) {
            root.right = add_book_to_library(root.right, b);
        } else {
            root.b.counter++;
        }
        return root;
    }

    public Node search_for_book(Node root, int ISBN) {
        // root is null -> return false
        if (root == null) {
            return null;
        }
        // if root has key -> return true
        if (root.b.ISBN == ISBN) {
            return root;
        }

        if (ISBN > root.b.ISBN) {
            return search_for_book(root.right, ISBN);
        }

        return search_for_book(root.left, ISBN);
    }

    // Get inorder successor (smallest in right subtree)
    public Node getSuccessor(Node curr) {
        curr = curr.right;
        while (curr != null && curr.left != null) {
            curr = curr.left;
        }
        return curr;
    }

    // Delete a node with value x from BST
    public Node delNode(Node root, int ISBN) {
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
            Node succ = getSuccessor(root);
            root.b.ISBN = succ.b.ISBN;
            root.right = delNode(root.right, succ.b.ISBN);
        }
        return root;
    }

}
