package com.mycompany.librarymanagementsystem;

public class AVLBookController {

    static BookNode root = null;

    /*
    public static void turn_system_avl(){
        avlRoot = turn_bst_to_avl(root);
    }
     */
    public static void add_avl_book(int ISBN) {
        root = add_to_avl(root, ISBN);
    }
    
    public static void delete_avl_book(int ISBN){
        root = remove_book_from_library(root, ISBN);
    }

    /*
    private static void turn_bst_to_avl(BookNode avlRoot, BookNode root){
        if(root == null){
            return;
        }
        avlRoot = add_avl_book(avlRoot, avlRoot.b.ISBN);
        turn_bst_to_avl(root.left);
        turn_bst_to_avl(root.right);
    }
     */
    private static int height(BookNode n) {
        if (n == null) {
            return 0;
        }
        return n.height;
    }

    private static int getBalance(BookNode n) {
        if (n == null) {
            return 0;
        }
        return height(n.left) - height(n.right);
    }

    private static BookNode rightRotate(BookNode y) {
        BookNode x = y.left;
        BookNode T2 = x.right;
        x.right = y;
        y.left = T2;
        y.height = Math.max(height(y.left), height(y.right)) + 1;
        x.height = Math.max(height(x.left), height(x.right)) + 1;
        return x;
    }

    private static BookNode leftRotate(BookNode x) {
        BookNode y = x.right;
        BookNode T2 = y.left;
        y.left = x;
        x.right = T2;
        x.height = Math.max(height(x.left), height(x.right)) + 1;
        y.height = Math.max(height(y.left), height(y.right)) + 1;
        return y;
    }

    private static BookNode add_to_avl(BookNode n, int ISBN) {
        if (n == null) {
            return new BookNode(new Book(ISBN));
        }

        if (ISBN < n.b.ISBN) {
            n.left = add_to_avl(n.left, ISBN);
        } else if (ISBN > n.b.ISBN) {
            n.right = add_to_avl(n.right, ISBN);
        } else {
            return n;
        }
        n.height = 1 + Math.max(height(n.left), height(n.right));
        int balance = getBalance(n);

        if (balance > 1 && ISBN < n.left.b.ISBN) {
            return rightRotate(n);
        }

        if (balance < -1 && ISBN > n.right.b.ISBN) {
            return leftRotate(n);
        }

        if (balance > 1 && ISBN > n.left.b.ISBN) {
            n.left = leftRotate(n.left);
            return rightRotate(n);
        }

        if (balance < -1 && ISBN < n.right.b.ISBN) {
            n.right = rightRotate(n.right);
            return leftRotate(n);
        }

        return n;
    }
    
    private static BookNode getLeftMost(BookNode root){
        BookNode current = root;
        while(current.left != null){
            current = current.left;
        }
        return current;
    }
    
    private static BookNode remove_book_from_library(BookNode root, int ISBN){
        if(root == null){
            return root;
        }
        
        if(root.b.ISBN > ISBN){
            root.left = remove_book_from_library(root.left, ISBN);
        }
        else if(root.b.ISBN < ISBN){
            root.right = remove_book_from_library(root.right, ISBN);
        }
        else{
            if(root.left == null || root.right == null){
                root = (root.left != null) ? root.left : root.right;
            }
            else{
                int num = getLeftMost(root.right).b.ISBN;
                root.b.ISBN = num;
                root = remove_book_from_library(root.right,num);
            }
        }
        
        if(root == null){
            return root;
        }
        
        root.height = 1 + Math.max(root.left.height, root.right.height);
        int balance = getBalance(root);
        if(balance > 1 && ISBN < root.b.left.ISBN){
            return rightRotate(root);
        }
        if(balance > 1 && ISBN > root.b.left.ISBN){
            root.left = leftRotate(root.left);
            return rightRotate(root);
        }
        if(balance < -1 && ISBN > root.right.b.ISBN){
            return leftRotate(root);
        }
        if(balance < -1 && ISBN < root.right.b.ISBN){
            root.right = rightRotate(root.right);
            return leftRotate(root);
        } 
        return root;
    }
}
