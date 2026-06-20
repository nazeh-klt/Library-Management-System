package com.mycompany.librarymanagementsystem;

class BookNode{
    Book b;
    BookNode left;
    BookNode right;
    int height;
    BookNode(Book b){
        this.b = b;
        left = right = null;
        height = 1;
    }
}
