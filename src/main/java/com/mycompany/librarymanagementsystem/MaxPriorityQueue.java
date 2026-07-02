package com.mycompany.librarymanagementsystem;

import java.util.*;
public class MaxPriorityQueue {
    public ArrayList<BookQueue> heap;

    public MaxPriorityQueue() {
        heap = new ArrayList<>();
    }

    public int size() {
        return heap.size();
    }
    public boolean isEmpty() {
        return heap.isEmpty();
    }

    public void insert(BookQueue request) {
        heap.add(request);
        bubbleUp(heap.size() - 1);
    }

    public BookQueue peekMax() {
        if (isEmpty()) throw new NoSuchElementException("Heap is empty");
        return heap.get(0);
    }

    public BookQueue extractMax() {
        if (isEmpty()) throw new NoSuchElementException("Heap is empty");
        BookQueue max = heap.get(0);
        BookQueue last = heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) {
            heap.set(0, last);
            bubbleDown(0);           
        }
        return max;
    }

    // Remove a specific element (optional, for waiting list removal)
    public boolean remove(BookQueue request) {
        int index = heap.indexOf(request);
        if (index == -1) return false;
        removeAt(index);
        return true;
    }

    private void removeAt(int index) {
        BookQueue moved = heap.remove(heap.size() - 1);
        if (index < heap.size()) {
            heap.set(index, moved);
            // since we don't know wether the removed element need to be pushed up or down we use both
            bubbleUp(index);
            bubbleDown(index);
        }
    }

    // Bubble up the element at idx until heap property holds
    private void bubbleUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (hasHigherPriority(heap.get(index), heap.get(parent))) {
                swap(index, parent);
                index = parent;
            } else {
                break;
            }
        }
    }

    // Push down the element at idx until heap property holds
    private void bubbleDown(int index) {
        int size = heap.size();
        while (true) {
            int left = 2 * index + 1;
            int right = 2 * index + 2;
            int largest = index;

            if (left < size && hasHigherPriority(heap.get(left), heap.get(largest)))
                largest = left;
            if (right < size && hasHigherPriority(heap.get(right), heap.get(largest)))
                largest = right;

            if (largest != index) {
                swap(index, largest);
                index = largest;
            } else {
                break;
            }
        }
    }

    private void swap(int i, int j) {
        BookQueue temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }

    
    public ArrayList<BookQueue> getElements() {
        return new ArrayList<>(heap);
    }
    
    public static boolean hasHigherPriority(BookQueue r1, BookQueue r2){
        if(r1.isGraduated != r2.isGraduated){
            return r1.isGraduated;
        }
        return r1.requestDate.isBefore(r2.requestDate);
    }
}
