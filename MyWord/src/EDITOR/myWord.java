/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EDITOR;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Base64;
import javax.swing.JOptionPane;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author oscarito
 */
public class myWord  extends Thread{
    public static Socket globalSocket;
    private static JSONParser parser = new JSONParser();
    public static Object responseJSON;

    public myWord() {
        start();
    }
    public static void main(String[] args) {
        try {
            globalSocket = new Socket("localhost", 2000);
            
            new myWord();
            new LOGIN(null, true).show();
//            new PrincipalScreen().show();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al conectar con el servidor, contacte a su administrador\n"+
                    "Ing. Oscar Diaz\nCel: 9999-9999", "ERORR", JOptionPane.ERROR_MESSAGE);
            System.err.println("error socket");
        }
    } 
    @Override
    public void run(){
        try {
            while(globalSocket.isConnected()){
                try {
//                    if (globalSocket.getKeepAlive() == false) {
//                        continue;
//                    }
                    DataInputStream ENTRADA = new DataInputStream(globalSocket.getInputStream());
//                    System.out.println("read() -> " + globalSocket.getKeepAlive());
                    boolean success = ENTRADA.readBoolean();
                    System.out.println("readBoolean = " + success);
                    byte[] response = new byte[ENTRADA.readInt()];
                    ENTRADA.readFully(response);

                    responseJSON = (JSONObject)parser.parse(new String(Base64.getDecoder().decode(response)));
                    System.out.println("@response" + ((JSONObject)responseJSON).toJSONString());
                    try {
                        responseJSON = (JSONObject)parser.parse(((JSONObject)responseJSON).get("response").toString());
                    } catch (Exception e) {
                        responseJSON = (JSONArray)parser.parse(((JSONObject)responseJSON).get("response").toString());
                    }
                    
    //                String response = ENTRADA.readUTF();
    //                responseJSON = JSON.parse( ((JSONObject)JSON.parse( response)).get("respuesta").toString() );
    //                if( ((JSONObject)JSON.parse( response)).get("success").toString().equals("false") ){
                    if (!success) {
                        JOptionPane.showMessageDialog(null, ((JSONObject)responseJSON).get("mensaje") , "ERROR", JOptionPane.ERROR_MESSAGE);
                    }
                      System.out.println(response + "<------");
                }catch( Exception e){
                    e.printStackTrace();
                    System.out.println("El Server murió");
                    try {
                        globalSocket.close();
                    } catch (Exception X) {
                    }  
                    JOptionPane.showMessageDialog(null, "El server murio" , "ERROR", JOptionPane.ERROR_MESSAGE);
                    
                    break;
                }
            }
        } catch (Exception E) {
        }
        System.out.println("Off");
    }
    public static Object[] read( String endpoint, Object objeto){
        try {
            JSONObject data = new JSONObject();
            DataOutputStream SALIDA = new DataOutputStream(globalSocket.getOutputStream());
//            data.put("endpoint", endpoint);
//            System.out.println("DECODE 1 = " + new String(Base64.getDecoder().decode((byte[])objeto)));
            data.put("object",objeto);
//            System.out.println("DECODE 2 = " + Base64.getDecoder().decode((byte[])data.get("object")));
            byte[] testing = Base64.getEncoder().encode(data.toJSONString().getBytes());
            System.out.println("DECODE 2 = " + Base64.getDecoder().decode((byte[])testing));
            SALIDA.writeInt(testing.length);
            SALIDA.write(testing);
            SALIDA.writeUTF(endpoint);
            SALIDA.flush();
//            SALIDA.writeUTF(data.toJSONString());
//            DataInputStream ENTRADA = new DataInputStream(globalSocket.getInputStream());
//            String response = ENTRADA.readUTF();
            try {
                sleep( 1000 );
//                globalSocket.setTcpNoDelay(true);
//                globalSocket.setKeepAlive(true);
                
            } catch (Exception e) {
            }
//            DataInputStream ENTRADA = new DataInputStream(globalSocket.getInputStream());
//            ENTRADA.
//            boolean success = ENTRADA.readBoolean();
//            System.out.println("SUCCESS" + success);
            System.out.println("El Resultado es: " + responseJSON);
            return new Object[]{true, responseJSON};
        } catch (Exception e) {
            System.err.println("Error al leer");
            return new Object[]{"ERROR", false};
        }
    }
}
