package com.mycompany.librarymanagementsystem;

import java.time.*;

public class BookQueue {
    String studentName;
    boolean isGraduated;
    LocalDate requestDate;
    public BookQueue(String studentName, boolean isGraduated, LocalDate requestDate) {
        this.studentName = studentName;
        this.isGraduated = isGraduated;
        this.requestDate = requestDate;
    }
}
