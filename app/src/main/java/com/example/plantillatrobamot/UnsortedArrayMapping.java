package com.example.plantillatrobamot;

import java.util.Iterator;

public class UnsortedArrayMapping<K, V>
{
    private K[] keys; //
    private V[] values; // Lista enlazada de conjuntos
    private int n;

    // Iterator;
    private int it;

    public Iterator IteratorUnsortedArrayMapping(){
        return new IteratorUnsortedArrayMapping();
    }
    
    // Reserva memòria pels dos arrays de longitud max i inicialitza n a 0
    public UnsortedArrayMapping(int max)
    {
        this.keys = (K[]) new Object[max];
        this.values = (V[]) new Object[max];
        n = 0;
    }
    // O(n): cerca lineal
    // Consultar el valor associat a la clau
    public V get(K key)
    {
        int i = -1;
        boolean trobat = false;

        while (!trobat && i < n - 1) {
            i++;
            trobat = key.equals(keys[i]);
        }
        if (trobat) {
            return values[i];
        } else {
            return null;
        }
    }
    // O(n): Retorna el valor anterior associat a la clau (cerca lineal), si n’hi havia
    // Afegir una parella clau-valor
    // Retorna el valor anterior associat a la clau, si n’hi havia, o null
    public V put(K key, V value)
    {
        int i = 0;
        V valor_anterior;
        for(i = 0; i < n; i++){
            if(keys[i].equals(key)){
                valor_anterior = values[i];
                values[i] = value;
                return valor_anterior;
            }
        }
        keys[n] = key;
        values[n] = value;
        n++;

        return null;
    }

    private class IteratorUnsortedArrayMapping implements Iterator {
        
        private int it;

        //Constructor Iterator
        private IteratorUnsortedArrayMapping() {
            it = 0;
        }

        //mira si hay mas nodos
        @Override
        public boolean hasNext() {
            return it < n;
        }

        //mira cual es el siguiente nodo
        @Override
        public Object next() {
            it++;
            return keys[it - 1];
        }

        public Object nextValue() {
            it++;
            return values[it - 1];
        }
    }
}

