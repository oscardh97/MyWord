
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author oscarito
 */
public class FILE {
    private String name;
    private int id, user;
    private JSONObject content = new JSONObject();

    public FILE(String name, int user, JSONObject content) {
        this.name = name;
        this.user = user;
        this.content = content;
    }

    public FILE(int id) {
        this.id = id;
        JSONArray files = (JSONArray)SERVER.ORM.READ("FILE", null, new Object[][]{{"ID","=",id}}, null);
        if (files.size() > 0) {
            JSONObject file = (JSONObject)files.get(0);
            this.name = file.get("NAME").toString();
            this.user = Integer.parseInt(file.get("ID").toString());
            try {
                this.content = (JSONObject)(SQL.jsonParser.parse(file.get("CONTENT").toString()));
            } catch (Exception e) {
                this.content = null;
            }
        }
        
    }
    
    public FILE() {
    }
    
    
    public boolean update() {
        
        return false;
    }
}
