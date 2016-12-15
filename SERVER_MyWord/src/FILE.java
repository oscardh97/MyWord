
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

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
    private boolean isOpen = true;
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
            cleanLock();
            start();
        }
        
    }
    
    @Override
    public void run(){
        while (isOpen) {
            try {
                this.update();
                sleep(5000);
            } catch (Exception e) {
            }
        }
        
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
                if (idUser == -1 || idUser == client.getUserId()) {
                    removeLock(client);
                    actualBlock.put("idUser", client.getUserId());
                    update();
                    return true;
                } 
//                else {
//                    //Responder
//                    return false;
//                }
            }
        }
        return false;
    }

    public JSONArray getBlocks() {
        return blocks;
    }
    
    public void cleanLock() {
        try {
            for (Object block : blocks) {
                JSONObject actualBlock = (JSONObject)block;
//                if (actualBlock.get("idUser").toString().equals(cliente.getUserId())) {
                    actualBlock.put("idUser", -1);
                    
//                }
            }
            update();
        } catch (Exception e) {
        }
    }
    
    public void removeLock (CLIENTE cliente) {
        
        for (Object block : blocks) {
            JSONObject actualBlock = (JSONObject)block;
            if (actualBlock.get("idUser").toString().equals(cliente.getUserId())) {
                actualBlock.put("idUser", -1);
                update();
            }
        }
    }
    
    public void modifyBlock (String idBlock, JSONArray content, boolean unlock) {
         for (Object block : blocks) {
            JSONObject actualBlock = (JSONObject)block;
            if (actualBlock.get("id").toString().equals(idBlock)) {
                try {
//                    String textLine = new String(Base64.getDecoder().decode(content));
                    actualBlock.put("content", content);
                } catch (Exception ex) {
                    Logger.getLogger(FILE.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                if (unlock)
                    actualBlock.put("idUser", -1);
                
                update();
                return;
            }
        }
        JSONObject newBlock = new JSONObject();
         
        newBlock.put("id", idBlock);
        newBlock.put("content", content);
        this.blocks.add(newBlock);
        update();
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
        System.out.println("@FILE.update\n" + this.toJSON());
        SERVER.ORM.UPDATE("FILE", this.toJSON());
//        JSONObject fileJSON = new JSONObject();
////        fileJSON.put(, name)
        return false;
    }
}
