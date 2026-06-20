/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.librarymanagementsystem;

/**
 *
 * @author samid
 */
public class Book {
    int ISBN;
    int copy; // represents the number of book of a certain ISBN
    String title;
    String author; 
    String category;    
    Book left;
    Book right;
    public Book(){
        
    }
    public Book(int ISBN){
        this.ISBN = ISBN; 
        this.copy = 1;
        left = right = null;
    }

    public Book(int ISBN, int copy, String title, String author, String category, Book left, Book right) {
        this.ISBN = ISBN;
        this.copy = copy;
        this.title = title;
        this.author = author;
        this.category = category;
        this.left = left;
        this.right = right;
    }
}
