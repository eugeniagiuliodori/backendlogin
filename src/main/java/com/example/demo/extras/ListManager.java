package com.example.demo.extras;

import com.example.demo.entity.ERole;
import com.example.demo.entity.EService;
import com.example.demo.entity.EUser;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ListManager {

    public static boolean hasDuplicates(Set<?> set){
        boolean duplicate=false;
        List<Object> list = new LinkedList<>(set);
        for(int i=0; i < list.size() && !duplicate;i++){
            for(int j=i+1; j < list.size() && !duplicate; j++){
                if(list != null && !list.isEmpty()&& list.get(0) instanceof ERole) {
                    if (((ERole)list.get(i)).getNameRole().toLowerCase().equals(((ERole)list.get(j)).getNameRole().toLowerCase())) {
                        return true;
                    }
                    if (((EUser)list.get(i)).getName().toLowerCase().equals(((EUser)list.get(j)).getName().toLowerCase())) {
                        return true;
                    }
                    if (((EService)list.get(i)).getName().toLowerCase().equals(((EService)list.get(j)).getName().toLowerCase())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /*    Util information:

    private boolean isSetTypeOf( Set<?> set, Class<?> clazz )
{
    for ( Object object : set )
    {
        if ( object.getClass().equals( clazz ) )
        {
            return true;
        }
    }
    return false;
}


    */

}
