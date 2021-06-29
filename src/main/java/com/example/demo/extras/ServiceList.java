package com.example.demo.extras;

import com.example.demo.model.Service;

import java.util.HashSet;
import java.util.Iterator;

public class ServiceList extends HashSet<Service> {


    public String toStringNames(){
        String str = new String("");
        Iterator iterator = new IteratorOfSet(this);
        while(iterator.hasNext()){
            str = str + "\""+((Service)iterator.next()).getName()+"\"";
            if(iterator.hasNext()){
                str = str + ",";
            }
        }
        return "{\"services\"=["+str+"]}";
    }


    @Override
    public String toString(){
        String str = new String("");
        Iterator iterator = new IteratorOfSet(this);
        while(iterator.hasNext()){
            str = str + ((Service)iterator.next()).toString();
            if(iterator.hasNext()){
                str = str + ",";
            }
        }
        return "["+str+"]";
    }


    public boolean equalsOnlyName(ServiceList list) {
        IteratorOfSet iterator = new IteratorOfSet(this);
        boolean exist = true;
        while(iterator.hasNext() && exist){
            if(!iterator.containsNameService((Service)iterator.next())){
                exist = false;
            }
        }
        return exist;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) return false;
        if(o == this) return true;
        if(getClass() != o.getClass()) return false;
        IteratorOfSet iterator = new IteratorOfSet(this);
        boolean exist = true;
        while(iterator.hasNext() && exist){
            if(!iterator.contains((Service)iterator.next())){
                exist = false;
            }
        }
        return exist;
    }
}
