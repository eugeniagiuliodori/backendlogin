package com.example.demo.extras;

import com.example.demo.model.Service;
import org.springframework.security.core.GrantedAuthority;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class IteratorOfSet implements Iterator {

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


    public Object next() {
        if(pos < (list.size()-1)){
            Object o = list.get((pos+1));
            pos++;
            return o;
        }
        return null;
    }


    public boolean containsNameService(Object o){
        if(!list.isEmpty()) {
            if (o == null) return true;
            if (list.get(0) instanceof Service) {
                try {
                    Service service = (Service) o;
                    boolean exist = false;
                    for (int i = 0; i < list.size() && !exist; i++) {
                        if (((Service) list.get(i)).equalsOnlyName(service)) {
                            exist = true;
                        }
                    }
                    return exist;
                } catch (Exception e) {
                    return false;
                }
            } else {
                return false;
            }
        }
        else{
            return false;
        }
    }

    public boolean contains(Object o){
        if(!list.isEmpty()) {
            if(list.get(0) instanceof GrantedAuthority) {
                String key = ((GrantedAuthority)o).getAuthority();
                boolean exist = false;
                if (!list.isEmpty()) {
                    if (list.get(0) instanceof GrantedAuthority) {
                        for (int i = 0; !exist && i < list.size(); i++) {
                            if (((GrantedAuthority) list.get(i)).getAuthority().equals(key)) {
                                exist = true;
                            }
                        }
                    }
                }
                return exist;
            }
            if(list.get(0) instanceof Service){
                if (o == null) return true;
                Service service = (Service) o;
                boolean exist = false;
                for (int i = 0; i < list.size() && !exist; i++) {
                    if (((Service) list.get(i)).equals(service)) {
                        exist = true;
                    }
                }
                return exist;
            }
            return false;
        }
        else{
            return false;
        }
    }
}
