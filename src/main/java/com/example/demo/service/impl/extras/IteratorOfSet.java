package com.example.demo.service.impl.extras;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class IteratorOfSet implements Iterator{

    private int pos;

    private List list;

    public IteratorOfSet(Set set){
        list = new LinkedList(set);
        pos=-1;
    }

    @Override
    public boolean hasNext() {
        return pos < (list.size() - 1);
    }

    @Override
    public Object next() {
        if(pos < (list.size()-1)){
            Object o = list.get((pos+1));
            pos++;
            return o;
        }
        return null;
    }
}
