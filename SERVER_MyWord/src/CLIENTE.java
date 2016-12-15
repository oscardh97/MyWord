
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
    
    public int getUserId() {
        return this.id;
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
                    } else if (endpointName.equals("readSharedFiles")) {
                        this.readSharedFiles(objectJSON);
                    } else if (endpointName.equals("updateFile")) {
                        this.updateFile(objectJSON);
                    } else if (endpointName.equals("deleteFiles")) {
                        this.deleteFiles(objectJSON);
                    } else if (endpointName.equals("openFile")) {
                        this.openFile(objectJSON);
                    } else if (endpointName.equals("listPermissions")) {
                        this.openFile(objectJSON);
                    } else if (endpointName.equals("readUsersXFile")) {
                        this.readUsersXFile(objectJSON);
                    } else if (endpointName.equals("shareFile")) {
                        this.shareFile(objectJSON);
                    } else if (endpointName.equals("getLock")) {
                        this.getLock(objectJSON);
                    } else if (endpointName.equals("getCompleteLog")) {
                        this.getCompleteLog(objectJSON);
                    } else if (endpointName.equals("readFileContent")) {
                        this.readFileContent(objectJSON);
                    } else if (endpointName.equals("updateBlock")) {
                        this.updateBlock(objectJSON);
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
                    registerLog("Logout", this.id);
                    SERVER.logoutClient(this);
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
                registerLog("Login", this.id);
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
            registerLog("Create File", this.id, respuesta.get("ID"));
            responder(respuesta.toJSONString(), true);
        } catch (Exception e) {
            System.err.println(e.toString());
            JSONObject respuesta = new JSONObject();
            respuesta.put("mensaje", "Error al crear el archivo");
            responder(respuesta, false);
        }
    }
    
    public void readSharedFiles(JSONObject object) {
        try {
            responder(ORM.READ("USER_X_FILE", 
                new String[]{"ID", "PRIVILEGE"}, 
                new Object[][]{{
//                    "ID", "=", Integer.parseInt(object.get("ID").toString()),
                    "ID_USER", "=", this.id
                }},
                new Object[][]{{
                    "FILE",
                    "ID_FILE",
                    "=",
                    "ID",
                    "ID,NAME,CREATION_USER,CREATION_DATE,MODIFICATION_DATE,CONTENT",
                    null
                }}),true
            );
            registerLog("Read Shared Files", this.id);
        } catch (Exception e) {
            e.printStackTrace();
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
            registerLog("Read My Files", this.id);
        } catch (Exception e) {
        }
    }
    public void readFileContent(JSONObject object) {
        try {
            JSONObject response = new JSONObject();
            responder(ORM.READ("FILE", 
                new String[]{"ID", "NAME", "CREATION_USER","CREATION_DATE", "MODIFICATION_DATE", "CONTENT"}, 
                new Object[][]{{
                    "ID", "=", object.get("ID").toString()
                }}, null),true
            );
//            response.put("CONTENT", ((FILE)SERVER.openedFiles.get(object.get("ID").toString())).getBlocks());
//            responder(response,true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void updateBlock(JSONObject object) {
        try {
            JSONObject response = new JSONObject();
            ((FILE)SERVER.openedFiles.get(object.get("idFile").toString())).modifyBlock(
                object.get("id").toString(), 
                (JSONArray)SQL.jsonParser.parse(object.get("content").toString()), 
                (boolean)object.get("unlock")
            );
            response.put("success", true);
            responder(response,true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void readUsersXFile(JSONObject object) {
        try {
            
            String strQuery = "SELECT U.`ID`,U.`NICKNAME`, UXF.`PRIVILEGE` FROM `USER` AS U " +
                        "LEFT JOIN `USER_X_FILE` AS UXF " +
                        "ON U.`ID` = UXF.`ID_USER` " +
                        "AND UXF.`ID_FILE` = " + object.get("ID") +  " " +
                        "WHERE U.`ID` != " + this.id;
            System.out.println(strQuery);
            try {
                ResultSet response = ORM.cmdDB.executeQuery( strQuery );  
                ResultSetMetaData resultMD = response.getMetaData();
                JSONArray arregloResponse = new JSONArray();
                while( response.next()){
                    JSONObject tupla = new JSONObject();
                    for (int i = 1; i <= resultMD.getColumnCount(); i++) {
                        System.out.println(resultMD.getColumnName(i));
                        tupla.put( resultMD.getColumnName(i), response.getObject(i) );
                    }
                    arregloResponse.add( tupla );
                }
                System.out.println(arregloResponse);
                responder(arregloResponse.toJSONString(), true);
//                        return arregloResponse;
            } catch (Exception e) {
                System.out.println(strQuery);
                e.printStackTrace();
                System.out.println("Error al leer ( Server )");
            }
//            responder(ORM.READ("USER", 
//                new String[]{"ID", "NAME","NICKNAME"}, 
//                new Object[][]{{
//                    "ID", "!=", this.id
//                }},
//                new Object[][]{{
//                    "USER_X_FILE",
//                    "ID",
//                    "=",
//                    "ID_USER",
//                    "ID,PRIVILEGE"
//                }}),true
//            );
        } catch (Exception e) {
            e.printStackTrace();
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
            registerLog("Delete Files", this.id, ((JSONArray)object.get("IDS")).toJSONString());
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
            registerLog("Open File", this.id, idFile);
            responder(response.toJSONString(),true);
        } catch (Exception e) {
            response.put("mensaje", "Error al abrir el archivo");
            responder(response.toJSONString(),false);
        }
    }
    public void getLock(JSONObject object) {
        JSONObject response = new JSONObject();
        try {
            String idFile = object.get("idFile").toString();
            String idBlock = object.get("id").toString();

//            if (SERVER.openedFiles.get(idFile) == null)
//                SERVER.openedFiles.put(idFile, new FILE(Integer.parseInt(object.get("ID").toString())));
            
            boolean isBlocked = ((FILE)SERVER.openedFiles.get(idFile)).getLock(idBlock, this);
            
            if (!isBlocked) {
                response.put("mensaje", "Está linea está siendo utilizado por alguién más");
            }
            response.put("success", isBlocked);
            responder(response.toJSONString(),isBlocked);
        } catch (Exception e) {
            response.put("mensaje", "Error al abrir el archivo");
            responder(response.toJSONString(),false);
        }
    }
    public void updateFile(JSONObject object) {
        try {
            ORM.UPDATE("FILE", object);
            this.openFile(object);
            registerLog("Update File", this.id, object.get("ID").toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void listPermissions(JSONObject object) {
    }
    
    public void getCompleteLog(JSONObject object) {
        try {
            
            responder(ORM.READ("LOG", 
                new String[]{"ID", "ACTION", "DATE"},
                null,
                new Object[][]{{
                    "FILE",
                    "ID_FILE",
                    "=",
                    "ID",
                    "NAME",
                    "LEFT JOIN"
                }, {
                    "USER",
                    "ID_USER",
                    "=",
                    "ID",
                    "NICKNAME",
                    "LEFT JOIN"
                }}),true
            );
        } catch (Exception e) {
        }
    }
    public void shareFile(JSONObject object) {
        
        try {
            int idFile = Integer.parseInt(object.get("idFile").toString());
            int idUser = Integer.parseInt(object.get("idUser").toString());
            int privilege = Integer.parseInt(object.get("privilege").toString());

            JSONArray privileges = (JSONArray)ORM.READ("USER_X_FILE", null, new Object[][]{
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
                    create.put("ID", Integer.parseInt(privilegeJSON.get("ID").toString()));
                    ORM.UPDATE("USER_X_FILE", create);
                }
            }
            registerLog("Share File", this.id, idFile);
            JSONObject response = new JSONObject();
            response.put("succes", true);
            responder(response.toJSONString(),true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void registerLog(String action, Object idUser) {
        registerLog(action, idUser, null);
    }
    
    public void registerLog(String action, Object idUser, Object idFile) {
        try {
            JSONObject createJSON = new JSONObject();
            
            createJSON.put("ACTION", action);
            createJSON.put("ID_USER", idUser != null ? idUser : this.id);
            
            if (idFile != null) {
                createJSON.put("ID_FILE", idFile);  
            }
            
            ORM.create("LOG", createJSON);
        } catch (Exception e) {
        }
    }
    
    public String toString() {
        return this.name;
    }
}
