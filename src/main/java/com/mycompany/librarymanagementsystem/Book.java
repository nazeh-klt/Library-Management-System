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
    int counter; // represents the number of book of a certain ISBN
    Book left;
    Book right;
    public Book(){
        
    }
    public Book(int ISBN){
        this.ISBN = ISBN; 
        this.counter = 1;
        left = right = null;
    }
}
