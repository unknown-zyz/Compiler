package Backend;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class mMap {
    private HashMap<String, Object> map;

    public mMap() {
        this.map = new HashMap<>();
    }

    public void put(String key, Object value) {
        map.put(key, value);
    }

    public Object get(String key) {
        return map.get(key);
    }

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public void clear(){
        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String str = iterator.next();
            if (map.get(str) instanceof Memory memory && memory.getAddr().equals("$gp")) {
                continue;
            }
            iterator.remove();
        }
    }
}
