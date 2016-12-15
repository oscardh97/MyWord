
import java.net.Socket;
import java.util.ArrayList;
import java.net.ServerSocket;
import static javafx.scene.input.DataFormat.FILES;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author oscarito
 */
public class SERVER {
    
    public static SQL ORM = null;
    public static TDA_MAP clients = new TDA_MAP();
    public static Socket newClient;
    public static ServerSocket serverSocket;
    public static TDA_MAP openedFiles = new TDA_MAP();
    public static void main(String[] args) {
        ORM = new SQL();
        try {
            
            //Doesn't work
//            try {
//                Runtime.getRuntime().exec("sudo /opt/lampp/share/xampp-control-panel/xampp-control-panel");
//            } catch (Exception e) {
//            }
            serverSocket = new ServerSocket(2000);
            System.out.println("Iniciando...");
            while(true){
                newClient = serverSocket.accept();
                System.out.println("New Client" + newClient);
                CLIENTE nCliente = new CLIENTE(newClient, ORM);
                nCliente.start();
//                clientes.put(nCliente);
                System.out.println(clients.toString());
            }
        } catch (Exception e) {
            System.err.println("Something went wrong " + e.toString());
        }
    }
    public static void loginClient(CLIENTE newClient) {
        clients.put(newClient.getUserId() + "", newClient);
        System.out.println("@CLIENTS => " + clients.toString());
    }
    
    public static void logoutClient(CLIENTE newClient) {
        if (clients.get(newClient.getUserId() + "") != null) {
            clients.remove(newClient.getUserId() + "");
        }
    }
    public static void openFile() {
        
    }
}
