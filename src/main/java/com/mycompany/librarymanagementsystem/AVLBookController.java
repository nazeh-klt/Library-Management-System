package com.mycompany.librarymanagementsystem;

public class AVLBookController {

    static BookNode root = null;

    public static void add_avl_book(int ISBN, int copy, String title, String author, String category) {
        root = add_to_avl(root, ISBN, copy, title, author, category);
    }

    public static void delete_avl_book(int ISBN) {
        root = remove_book_from_library(root, ISBN);
    }
    
    public static BookNode search_for_book(int ISBN) {
        return search_for_book_avl(root, ISBN);
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

    private static BookNode add_to_avl(BookNode n, int ISBN, int copy, String title, String author, String category) {
        if (n == null) {
            return new BookNode(new Book(ISBN, copy, title, author, category));
        }

        if (ISBN < n.b.ISBN) {
            n.left = add_to_avl(n.left, ISBN, copy, title, author, category);
        } else if (ISBN > n.b.ISBN) {
            n.right = add_to_avl(n.right, ISBN, copy, title, author, category);
        } else {
           System.out.println("هذا الكتاب موجود بالفعل في المكتبة");
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
    
    private static BookNode search_for_book_avl(BookNode root, int ISBN) {
        if (root == null) {
            return null;
        }
        if (root.b.ISBN == ISBN) {
            return root;
        }

        if (ISBN > root.b.ISBN) {
            return search_for_book_avl(root.right, ISBN);
        }

        return search_for_book_avl(root.left, ISBN);
    }

    private static BookNode getLeftMost(BookNode root) {
        BookNode current = root;
        while (current.left != null) {
            current = current.left;
        }
        return current;
    }

    private static BookNode remove_book_from_library(BookNode root, int ISBN) {
        if (root == null) {
            return root;
        }

        if (root.b.ISBN > ISBN) {
            root.left = remove_book_from_library(root.left, ISBN);
        } else if (root.b.ISBN < ISBN) {
            root.right = remove_book_from_library(root.right, ISBN);
        } else {
            if (root.left == null || root.right == null) {
                root.b.copy--;
                root = (root.left != null) ? root.left : root.right;
            } else {
                root.b.copy--;
                BookNode current = getLeftMost(root.right);
                root.b.ISBN = current.b.ISBN;
                root.b.author = current.b.author;
                root.b.category = current.b.category;
                root.b.title = current.b.title;
                root.b.copy = current.b.copy;
                root = remove_book_from_library(root.right, root.b.ISBN);
            }
        }

        if (root == null) {
            return root;
        }

        root.height = 1 + Math.max(height(root.left), height(root.right));
        int balance = getBalance(root);
        if (balance > 1 && ISBN < root.left.b.ISBN) {
            return rightRotate(root);
        }
        if (balance > 1 && ISBN > root.left.b.ISBN) {
            root.left = leftRotate(root.left);
            return rightRotate(root);
        }
        if (balance < -1 && ISBN > root.right.b.ISBN) {
            return leftRotate(root);
        }
        if (balance < -1 && ISBN < root.right.b.ISBN) {
            root.right = rightRotate(root.right);
            return leftRotate(root);
        }
        return root;
    }
}
