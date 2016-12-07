
import java.util.Base64;
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
public class FILE extends Thread {
    private String name;
    private int id, user;
    private int creationUser;
    private TDA_MAP usersConected = new TDA_MAP();
    private JSONArray blocks = new JSONArray();

    public FILE(String name, int user, JSONArray content) {
        this.name = name;
        this.user = user;
        this.blocks = content;
    }

    public FILE(int id) {
        this.id = id;
        JSONArray files = (JSONArray)SERVER.ORM.READ("FILE", 
                new String[]{"ID", "NAME", "CREATION_USER","CREATION_DATE", "MODIFICATION_DATE", "CONTENT"}, 
                new Object[][]{{
                    "ID", "=", this.id
                }}, null);
        if (files.size() > 0) {
            JSONObject file = (JSONObject)files.get(0);
            this.name = file.get("NAME").toString();
            this.user = Integer.parseInt(file.get("ID").toString());
            this.creationUser = Integer.parseInt(file.get("CREATION_USER").toString());
            try {
                this.blocks = (JSONArray)(SQL.jsonParser.parse(file.get("CONTENT").toString()));
            } catch (Exception e) {
                this.blocks = null;
            }
        }
        
    }
    
    @Override
    public void run(){
        
    }
    
    public FILE() {
    }
    
    public void connectUser(CLIENTE newClient) {
        this.usersConected.put(newClient.getId() + "", newClient);
    }
    
    public void disconnectUser(int idUser) {
        this.usersConected.remove(idUser + "");
    }
    
    public boolean getLock(String idBlock, CLIENTE client) {
        for (Object block : blocks) {
            JSONObject actualBlock = (JSONObject)block;
            if (actualBlock.get("id").toString().equals(idBlock)) {
                int idUser = Integer.parseInt(actualBlock.get("idUser").toString());
                if (idUser == -1 || idUser == client.getId()) {
                    actualBlock.put("idUser", client.getId());
                    return true;
                } else {
                    //Responder
                    return false;
                }
            }
        }
        return false;
    }
    
    public void removeLock (String idBlock) {
        
        for (Object block : blocks) {
            JSONObject actualBlock = (JSONObject)block;
            if (actualBlock.get("id").toString().equals(idBlock)) {
                actualBlock.put("idUser", -1);
            }
        }
    }
    
    public void modifyBlock (String idBlock, String content, boolean unlock) {
         for (Object block : blocks) {
            JSONObject actualBlock = (JSONObject)block;
            if (actualBlock.get("id").toString().equals(idBlock)) {
                actualBlock.put("content", content);
                
                if (unlock)
                    actualBlock.put("idUser", -1);
            }
        }
    }
    
    public JSONObject toJSON() {
        JSONObject fileJSON = new JSONObject();
        fileJSON.put("ID", this.id);
        
        String textBase64 = null;
        try {
            textBase64 = Base64.getEncoder().encodeToString(this.blocks.toJSONString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        fileJSON.put("CONTENT", textBase64);
        fileJSON.put("NAME", this.name);
        fileJSON.put("MODIFICATION_DATE", "now()");
        
            
        return fileJSON;
    }
    public boolean update() {
        JSONObject fileJSON = new JSONObject();
//        fileJSON.put(, name)
        return false;
    }
}
