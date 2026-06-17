/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.librarymanagementsystem;

// a bNode is a node that represents a book
public class bNode {
    Book b;
    bNode left;
    bNode right;
    public bNode(){
        this.b = null;
    }
    public bNode(Book b){
        this.b = b;
        left = right = null;
    }
}
