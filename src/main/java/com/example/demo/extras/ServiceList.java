package com.example.demo.extras;

import com.example.demo.entity.EService;
import com.example.demo.model.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ServiceList extends HashSet<Service> {

    private Set<EService> services;


    public ServiceList(Set<EService> services){
        this.services=services;
    }

    public String toStringNames(){
        String str = new String("");
        Iterator iterator = new IteratorOfSet(services);
        while(iterator.hasNext()){
            EService eservice = (EService) iterator.next();
            Service service = new Service(eservice.getId(), eservice.getName(), eservice.getDescription(), eservice.getDate()) ;
            str = str + "\""+ service.getName()+"\"";
            if(iterator.hasNext()){
                str = str + ",";
            }
        }
        return "{\"services\"=["+str+"]}";
    }


    @Override
    public String toString(){
        String str = new String("");
        Iterator iterator = new IteratorOfSet(services);
        while(iterator.hasNext()){
            EService eservice = (EService) iterator.next();
            Service service = new Service(eservice.getId(), eservice.getName(), eservice.getDescription(), eservice.getDate()) ;
            str = str + service.toString();
            if(iterator.hasNext()){
                str = str + ",";
            }
        }
        return "["+str+"]";
    }


    public boolean equalsOnlyName(ServiceList list) {
        IteratorOfSet iterator = new IteratorOfSet(services);
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
        IteratorOfSet iterator = new IteratorOfSet(services);
        boolean exist = true;
        while(iterator.hasNext() && exist){
            if(!iterator.contains((Service)iterator.next())){
                exist = false;
            }
        }
        return exist;
    }
}
