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

    // Modify copy count directly without touching tree structure. Rejects negative values.
    // Does NOT check against active borrows - see BorrowController.can_reduce_copies for that.
    public static boolean update_copy_count(int ISBN, int newCount) {
        if (newCount < 0) {
            return false;
        }
        BookNode node = search_for_book_bst(root, ISBN);
        if (node == null) {
            return false;
        }
        node.b.copy = newCount;
        return true;
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

            // Node with 2 children.
            // Re-point this node at the successor's Book object rather than copying its fields
            // into the existing Book object. If any Borrow record already holds a reference to
            // the Book that used to live at this node, field-copying would silently corrupt that
            // record's data (its title/author would change to the successor's). Swapping the
            // reference instead leaves both Book objects, and everything pointing at them, intact.
            BookNode current = getLeftMost(root.right);
            root.b = current.b;
            root.right = remove_book_from_library_bst(root.right, current.b.ISBN);
        }
        return root;
    }
}
