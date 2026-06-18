/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.librarymanagementsystem;

/**
 *
 * @author USER
 */
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
