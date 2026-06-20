package com.mycompany.librarymanagementsystem;


public class Book {
    int ISBN;
    int copy; // represents the number of book of a certain ISBN
    String title;
    String author; 
    String category;    
    public Book(){
        
    }
    public Book(int ISBN){
        this.ISBN = ISBN; 
        this.copy = 1;
    }

    public Book(int ISBN, int copy, String title, String author, String category) {
        this.ISBN = ISBN;
        this.copy = copy;
        this.title = title;
        this.author = author;
        this.category = category;
    }
}
