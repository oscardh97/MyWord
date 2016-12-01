
import java.io.Serializable;
import java.util.ArrayList;

public class TDA_MAP  implements Serializable {
    private ArrayList<String> keys = new ArrayList<String>();
    private ArrayList<Object> values = new ArrayList<Object>();
    
    public TDA_MAP() {
    }
    public void put(String key, Object value) {
        int tempIndex = keys.indexOf(key);
        if (tempIndex == -1) {
            keys.add(key);
            values.add(value);
        } else {
            values.set(tempIndex, value);
        }
    }
    public boolean hasKey(String key) {
        return keys.indexOf(key) != -1;
    }
    public Object get(String key) {
        int tempIndex = keys.indexOf(key);
        if (tempIndex != -1) {
            return values.get(tempIndex);
        }
        return null;
    }
    public Object remove(String key) {
        int tempIndex = keys.indexOf(key);
        if (tempIndex != -1) {
            Object temp = values.get(tempIndex);
            keys.remove(tempIndex);
            values.remove(tempIndex);
            return temp;
        }
        return null;
    }
    public int getCountKies() {
        return keys.size();
    }
    public String toString() {
        String text = "{\n";
        for (int i = 0; i < this.keys.size(); i++) {
            text += "\t{" + this.keys.get(i) + ": " + this.values.get(i).toString() + "}\n";
        }
        return text + "}";
    }
    
    public ArrayList<Object> getValues() {
        return values;
    }
    
    public void setValues(ArrayList<Object> values) {
        this.values = values;
    }
    
    public ArrayList<String> getKeys() {
        return keys;
    }
    
}
