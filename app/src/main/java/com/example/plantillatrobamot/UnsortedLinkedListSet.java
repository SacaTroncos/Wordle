package com.example.plantillatrobamot;

import java.util.Iterator;

public class UnsortedLinkedListSet <E> {

    public Iterator IteratorUnsortedLinkedList(){
            return new IteratorUnsortedLinkedList();
    }
    private class Node {
        private E elem;
        private Node next;

        public Node(E elem, Node next) {
            this.elem = elem;
            this.next = next;
        }
    }
    private Node first;

    public UnsortedLinkedListSet() {
        first = null;
    }
    public boolean isEmpty() {
        return first == null;
    }

    public boolean contains(E elem) {
        Node p = first;
        boolean trobat = false;
        while (p != null && !trobat) {
            trobat = p.elem.equals(elem);
            p = p.next;
        }
        return trobat;
    }

    public boolean add(E elem) {
        boolean trobat = contains(elem);
        if (!trobat) {
            Node n = new Node(elem, first);
            first = n;
        }
        return !trobat;
    }

    public boolean remove(E elem) {
        Node p = first; Node pp = null; boolean trobat = false;
        while (p != null && !trobat) {
            trobat = p.elem.equals(elem);
            if (!trobat) {
                pp = p;
                p = p.next;
            }
        }
        if (trobat) {
            if (pp == null) {
                first = p.next;
            } else {
                pp.next = p.next;
            }
        }
        return trobat;
    }

    private class IteratorUnsortedLinkedList implements Iterator {
        private Node idxIterator;
        private IteratorUnsortedLinkedList() {
            idxIterator = first;
        }
        public boolean hasNext() {
            return idxIterator != null;
        }
        public Object next() {
            E elem = idxIterator.elem;
            idxIterator = idxIterator.next;
            return elem;
        }
    }
}
