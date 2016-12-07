
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author oscarito
 */
public class CLIENTE extends Thread{
    private Socket socket;
    private int id = -1;
    private String name;
    private SQL ORM;
    
    public CLIENTE(Socket socket, SQL ORM) {
        this.socket = socket;
        this.ORM = ORM;
        
        System.out.println("El Usuario se conecto");
    }

    public void setId(int id) {
        this.id = id;
    }
    
    
    @Override
    public void run(){
        try{
            while(!socket.getKeepAlive()){
                try {
//                    DataOutputStream SALIDA = new DataOutputStream(socket.getOutputStream());
//                    SALIDA.writeUTF("BIENVENIDO CLIENTE");
                    DataInputStream ENTRADA = new DataInputStream(socket.getInputStream());
                    byte[] r = new byte[ENTRADA.readInt()];
                    ENTRADA.readFully(r);
                    String endpointName = ENTRADA.readUTF();
                    System.out.println(new String(Base64.getDecoder().decode(r)));
                    JSONParser jsonParser = new JSONParser();
                    JSONObject objectJSON = (JSONObject)jsonParser.parse(new String(Base64.getDecoder().decode(r)));
                    System.out.println(endpointName);
                    objectJSON = (JSONObject)objectJSON.get("object");
                    if (endpointName.equals("login")) {
                        this.login(objectJSON);
                    } else if (endpointName.equals("createFile")) {
                        this.createFile(objectJSON);
                    } else if (endpointName.equals("readFiles")) {
                        this.readFiles(objectJSON);
                    } else if (endpointName.equals("updateFile")) {
                        
//                        byte[] fileX64 = new byte[ENTRADA.readInt()];
//                        ENTRADA.readFully(fileX64);
//                        objectJSON.put("CONTENT", fileX64);
                        this.updateFile(objectJSON);
                    } else if (endpointName.equals("deleteFiles")) {
                        this.deleteFiles(objectJSON);
                    } else if (endpointName.equals("openFile")) {
                        this.openFile(objectJSON);
                    } else if (endpointName.equals("listPermissions")) {
                        this.openFile(objectJSON);
                    }
                    
                    
//                    String query = ENTRADA.readUTF();
//                    System.out.println("Solicitud: " + query);
//                    JSONObject jsonQuery = (JSONObject)SQL.jsonParser.parse(query);
//                    System.out.println(jsonQuery);
//                    byte[] objeto = (byte[])jsonQuery.get("object");
                } catch (Exception e ){
//                    e.printStackTrace();
//                    System.out.println("@Error " + e.toString());
                    System.out.println("El cliente se jue");
                    socket.close();
                    break;
                }
            }
        } catch (Exception e ){

        }
    }
    
    public void responder(Object respuesta, boolean success) {
        System.out.println("SUCCESS -> " + success);
        try {
            if( !(respuesta instanceof String) ){
                if( respuesta instanceof JSONObject ){
                    respuesta = ((JSONObject)respuesta).toJSONString();
                }else{
                    respuesta = respuesta.toString();
                }
            }
            JSONObject jsonRespuesta = new JSONObject();
//            byte[] responseX64 = Base64.getEncoder().encode(respuesta.toString().getBytes());
            jsonRespuesta.put("response", respuesta);
            DataOutputStream SALIDA = new DataOutputStream(socket.getOutputStream());
            
            
            byte[] testing = Base64.getEncoder().encode(jsonRespuesta.toJSONString().getBytes());
            System.out.println("DECODE 2 = " + Base64.getDecoder().decode((byte[])testing));
            System.out.println(jsonRespuesta);
            
            SALIDA.writeBoolean(success);
            SALIDA.writeInt(testing.length);
            SALIDA.write(testing);
            SALIDA.flush();
//            SALIDA.writeUTF( jsonRespuesta.toJSONString() );

        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }
    public void login(JSONObject object) {
        try {
            System.out.println(object.toJSONString());
            String nickname = object.get("nickname").toString();
            String password = object.get("password").toString();
            JSONArray response = (JSONArray)ORM.READ("USER", null, new Object[][]{
                {"PASSWORD","=",password},
                {"NICKNAME","=",nickname}
            }, null);
            System.out.println("SIZE" + response.size());
            if( response.size() == 0 ){
                JSONObject respuesta = new JSONObject();
                respuesta.put("mensaje", "Usuario o contraseña invalido");
                responder( respuesta, false);
            } else {
                JSONObject usuario = (JSONObject)response.get(0);
                this.setId(Integer.parseInt(usuario.get("ID").toString()));
                this.name = usuario.get("NAME").toString();
    //            this.datos = (JSONObject)usuario;
                System.out.println(usuario);
                SERVER.loginClient(this);
                responder(usuario.toJSONString(), true);
            }
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }
    public void createFile(JSONObject object) {
        try {
            System.out.println(object.toJSONString());
            String name = object.get("NAME").toString();
            object.put("CREATION_USER", this.id);
            JSONObject respuesta = ORM.create("FILE", object);
            
//            respuesta.put("mensaje", "Usuario o contraseña invalido");
            responder(respuesta.toJSONString(), true);
        } catch (Exception e) {
            System.err.println(e.toString());
            JSONObject respuesta = new JSONObject();
            respuesta.put("mensaje", "Error al crear el archivo");
            responder(respuesta, false);
        }
    }
    
    public void readFiles(JSONObject object) {
        try {
            responder(ORM.READ("FILE", 
                new String[]{"ID", "NAME", "CREATION_USER","CREATION_DATE", "MODIFICATION_DATE", "CONTENT"}, 
                new Object[][]{{
                    "CREATION_USER", "=", this.id
                }}, null),true
            );
        } catch (Exception e) {
        }
    }
    public void deleteFiles(JSONObject object) {
        try {
            JSONObject response = new JSONObject();
            ORM.DELETE("FILE", 
                new Object[][]{{
                    "ID", "IN", object.get("IDS")
                }}
            );
            response.put("succes", ((JSONArray)object.get("IDS")).size());
            responder(response.toJSONString(),true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void openFile(JSONObject object) {
            JSONObject response = new JSONObject();
        try {
            String idFile = object.get("ID").toString();

            if (SERVER.openedFiles.get(idFile) == null)
                SERVER.openedFiles.put(idFile, new FILE(Integer.parseInt(object.get("ID").toString())));
            
            ((FILE)SERVER.openedFiles.get(idFile)).connectUser(this);
            
            response.put("success", true);
            responder(response.toJSONString(),true);
        } catch (Exception e) {
            response.put("mensaje", "Error al abrir el archivo");
            responder(response.toJSONString(),false);
        }
    }
    public void updateFile(JSONObject object) {
        try {
            ORM.UPDATE("FILE", object);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void listPermissions(JSONObject object) {
    }
    
    public void shareFile(JSONObject object) {
        int idFile = Integer.parseInt(object.get("idFile").toString());
        int idUser = Integer.parseInt(object.get("idUser").toString());
        int privilege = Integer.parseInt(object.get("privilege").toString());
        
        JSONArray privileges = (JSONArray)ORM.READ("USER", null, new Object[][]{
            {"ID_FILE","=",idFile},
            {"ID_USER","=",idUser}
        }, null);
        
        JSONObject create = new JSONObject();
        create.put("ID_USER", idUser);
        create.put("ID_FILE", idFile);
        create.put("PRIVILEGE", privilege);
        if (privileges.size() == 0) {
            if (privilege != 0) {
                ORM.create("USER_X_FILE", create);
            } 
        } else {
            JSONObject privilegeJSON = (JSONObject)privileges.get(0);
            
            if (privilege == 0) {
                ORM.DELETE("USER_X_FILE", new Object[][]{
                    {"ID","=",Integer.parseInt(privilegeJSON.get("ID").toString())}
                });
            } else {
                ORM.UPDATE("USER_X_FILE", create);
            }
        }
        JSONObject response = new JSONObject();
        response.put("succes", true);
        responder(response.toJSONString(),true);
    }
    
    public String toString() {
        return this.name;
    }
}
