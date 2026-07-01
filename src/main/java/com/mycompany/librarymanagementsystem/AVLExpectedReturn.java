package com.mycompany.librarymanagementsystem;

import java.time.LocalDate;
import java.util.*;

class Node {
    LocalDate date;
    ArrayList<Integer> recordIds;
    int height = 1;
    Node left;
    Node right;

    Node(LocalDate date, int recordId) {
        this.date = date;
        this.recordIds = new ArrayList<>();
        this.recordIds.add(recordId);
        this.left = null;
        this.right = null;
        this.height = 1;
    }
}

class AVLExpectedReturn {
    Node root;

    AVLExpectedReturn() {
        root = null;
    }

    static int height(Node n) {
        if (n == null) return 0;
        return n.height;
    }

    static int balance(Node n) {
        return height(n.left) - height(n.right);
    }

    static Node r_rotate(Node b) {
        Node a = b.left;
        Node T = a.right;

        a.right = b;
        b.left = T;

        b.height = 1 + Math.max(height(b.left), height(b.right));
        a.height = 1 + Math.max(height(a.left), height(a.right));

        return a;
    }

    static Node l_rotate(Node a) {
        Node b = a.right;
        Node T = b.left;

        b.left = a;
        a.right = T;

        a.height = 1 + Math.max(height(a.left), height(a.right));
        b.height = 1 + Math.max(height(b.left), height(b.right));

        return b;
    }

    static Node insert(Node root, LocalDate date, int recordId) {
        if (root == null) {
            return new Node(date, recordId);
        }

        int cmp = date.compareTo(root.date);

        if (cmp < 0) {
            root.left = insert(root.left, date, recordId);
        } else if (cmp > 0) {
            root.right = insert(root.right, date, recordId);
        } else {
            root.recordIds.add(recordId);
            return root;
        }

        root.height = 1 + Math.max(height(root.left), height(root.right));

        int bal = balance(root);

        if (bal > 1 && date.compareTo(root.left.date) <= 0) {
            return r_rotate(root);
        }

        if (bal < -1 && date.compareTo(root.right.date) > 0) {
            return l_rotate(root);
        }

        if (bal > 1 && date.compareTo(root.left.date) >= 0) {
            root.left = l_rotate(root.left);
            return r_rotate(root);
        }

        if (bal < -1 && date.compareTo(root.right.date) < 0) {
            root.right = r_rotate(root.right);
            return l_rotate(root);
        }

        return root;
    }
    
    static Node delete(Node root, LocalDate exp){
        if(root == null)
            return root;
        int cmp = exp.compareTo(root.date);
        if(cmp > 0)
            root.right = delete(root.right, exp);
        else if(cmp < 0)
            root.left = delete(root.left, exp);
        else{
            if(root.left == null || root.right == null)
                root = (root.left != null) ? root.left : root.right;
            else{
                Node curr = getLeftMost(root);
                Node temp = root;
                root = curr;
                root.right = temp.right;
                root.left = temp.left;
                root.right = delete(root.right, curr.date);
            }
        }
        if(root == null)
            return root;
        root.height = 1 + Math.max(height(root.left), height(root.right));
        int b = balance(root);
        if (b > 1 && balance(root.left) >= 0) {
            return r_rotate(root);
        }
        if (b > 1 && balance(root.left) < 0) {
            root.left = l_rotate(root.left);
            return r_rotate(root);
        }
        if (b < -1 && balance(root.right) <= 0) {
            return l_rotate(root);
        }
        if (b < -1 && balance(root.right) > 0) {
            root.right = r_rotate(root.right);
            return l_rotate(root);
        }
        return root;
    }
    
    static Node getLeftMost(Node root){
        Node curr = root.right;
        while(curr.left != null)
            curr = curr.left;
        return curr;
    }

    static Node search(Node root, LocalDate date) {
        if (root == null) return null;

        int cmp = date.compareTo(root.date);

        if (cmp == 0) return root;
        if (cmp < 0) return search(root.left, date);
        return search(root.right, date);
    }

    static ArrayList<Integer> find_less_than(Node root, LocalDate date) {
        ArrayList<Integer> result = new ArrayList<>();
        find_less_than_helper(root, date, result);
        return result;
    }

    static void find_less_than_helper(Node node, LocalDate date, ArrayList<Integer> result) {
        if (node == null) return;

        if (node.date.compareTo(date) < 0) {
            result.addAll(node.recordIds);
            find_less_than_helper(node.left, date, result);
            find_less_than_helper(node.right, date, result);
        } else {
            find_less_than_helper(node.left, date, result);
        }
    }


    static ArrayList<Integer> get_all_sorted(Node root) {
        ArrayList<Integer> result = new ArrayList<>();
        in_order(root, result);
        return result;
    }

    static void in_order(Node node, ArrayList<Integer> result) {
        if (node == null) return;

        in_order(node.left, result);
        result.addAll(node.recordIds);
        in_order(node.right, result);
    }

    void print_sideways(Node root, int space) {
        if (root == null) return;
        space += 10;
        print_sideways(root.right, space);
        System.out.println();
        for (int i = 10; i < space; i++) System.out.print(" ");
        System.out.println(root.date + " → " + root.recordIds);
        print_sideways(root.left, space);
    }
}