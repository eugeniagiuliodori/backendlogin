package com.example.demo.extras;

import org.springframework.security.core.GrantedAuthority;

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

    public boolean contains(String key){
        boolean exist = false;
        if(!list.isEmpty()){
            if(list.get(0) instanceof GrantedAuthority){
                for(int i=0; !exist && i<list.size();i++){
                    if(((GrantedAuthority)list.get(i)).getAuthority().equals(key)){
                        exist=true;
                    }
                }
            }
        }
        return exist;
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
