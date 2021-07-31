package com.example.demo.extras;

import com.example.demo.entity.ERole;
import com.example.demo.entity.EService;
import com.example.demo.entity.EUser;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ListManager {

    public static boolean hasDuplicates(List<LinkedHashMap> listRoles){
        boolean duplicate=false;
        List<Object> list;
        if(listRoles != null && !listRoles.isEmpty()) {
            list = new LinkedList<>();
            for(int i=0; i< listRoles.size();i++){
                list.add(listRoles.get(i));
            }
        }
        else{
            list = new LinkedList<>();
        }
        if(list.size()>1){
            for(int i=0; i < list.size() && !duplicate;i++) {
                for (int j = i + 1; j < list.size() && !duplicate; j++) {
                    if(list.get(0) instanceof LinkedHashMap){
                        LinkedHashMap elem = (LinkedHashMap) list.get(i);
                        if(elem.get("nameRole") != null){
                            String nameRole = (String) elem.get("nameRole");
                            if(((LinkedHashMap)list.get(j)).get("nameRole").equals(nameRole)&&(i != j)){
                                duplicate = true;
                            }
                        }
                    }
                    /*if (list.get(0) instanceof EUser) {
                        if (((EUser) list.get(i)).getName().toLowerCase().equals(((EUser) list.get(j)).getName().toLowerCase())) {
                            list.remove(j);
                            j--;
                        }
                    }
                    if (list.get(0) instanceof EService) {
                        if (((EService) list.get(i)).getName().toLowerCase().equals(((EService) list.get(j)).getName().toLowerCase())) {
                            list.remove(j);
                            j--;
                        }
                    }*/
                }
            }
        }
        return duplicate;
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
