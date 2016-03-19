import java.util.*;
/*
*   Custom comparator class that simply returns which Object, 
*   in this case which integer is larger by using the compareTo method
*   of the Integer class 
*/
class CustomComparator implements Comparator<Object> {

    Map<String, Integer> map;

    public CustomComparator(Map<String, Integer> map) {
        this.map = map;
    }

    public int compare(Object obj1, Object obj2) {

        if (map.get(obj1) == map.get(obj2)){
            return 1;
        }
        //use compareTo function of integer class 
        return ((Integer) map.get(obj2)).compareTo((Integer)map.get(obj1));

    }
}