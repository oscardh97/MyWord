
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Base64;
import java.util.Set;
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
public class SQL {
    
    public static Connection connectionDB = null;
    public static Statement cmdDB = null;
    public static JSONParser jsonParser = new JSONParser();

    public SQL() {
        
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connectionDB = DriverManager.getConnection ("jdbc:mysql://127.0.0.1/myWord","root", "");
            cmdDB = connectionDB.createStatement();
//            System.out.println(SQL.READ("FILE"));
            System.out.println("Se ha logrado la conexion con la BD");
        } catch (Exception e) {
            System.err.println("Error Conexion " + e.toString());
        }
    }
    
    
    public Object[] getFieldsNames(String tablaNombre, boolean toString){
        String query = "SELECT * FROM `" + tablaNombre + "`";
        try {
            ResultSet result = cmdDB.executeQuery(query);
            System.out.println(query);
            ResultSetMetaData resultMD = result.getMetaData();
            if( resultMD.getColumnCount() > 0 ){
                String[] nombreColumnas = new String[resultMD.getColumnCount()];
                String stringColumnas = "";
                for(int i = 1; i <= resultMD.getColumnCount(); i++){
                    nombreColumnas[ i - 1] = resultMD.getColumnName(i);
                    stringColumnas += "`" + tablaNombre + "`.`" + resultMD.getColumnName(i) + "`";
                    stringColumnas += i == resultMD.getColumnCount() ? " " : ",";
                    
                }
                return toString ? new Object[]{stringColumnas, resultMD.getColumnCount()} : nombreColumnas;
            }
            return new String[]{};
        } catch (Exception e) {
            System.err.println("Error al obtener nombres de columnas");
            System.err.println(query);
            System.err.println(e.toString());
        }
        return new String[]{};
    }
    public JSONObject create(String tabla, JSONObject values){
        try {
//            String[] queryOptions = query.split("%VALUES");
//            Object[] nombreColumnas = getColumNombres( queryOptions[0], true );
//            String[] values = queryOptions[1].split("%&");
//            System.out.println(nombreColumnas[1].toString());
//            if( Integer.parseInt( nombreColumnas[1].toString() ) > 0 ){
//                if( Integer.parseInt( nombreColumnas[1].toString() ) == values.length){
                    String insertQuery = "INSERT INTO `" + tabla +"` (";
//                    for (String value : values) {
//                        insertQuery += value;
//                    }
                    String columNombres = "";
                    String valuesStr = " VALUES( ";
                    Set<String> keys = values.keySet();
                    int cont = 0;
                    for (String key : keys) {
                        System.out.println(key + " = " + values.get(key));
                        columNombres += "`" + key + "`";
                        if( values.get(key) instanceof String){
                            valuesStr += "'" + values.get(key) + "'";
                        }else{
                            valuesStr += values.get(key);
                        }
                        if( cont < keys.size() - 1){
                            columNombres += ",";
                            valuesStr += ",";
                        }else{
                            columNombres += ")";
                            valuesStr += ")";
                        }
                        cont++;
                    }
                    insertQuery += columNombres + valuesStr;
                    System.out.println(insertQuery);
                    boolean creado = cmdDB.execute( insertQuery );
                    System.out.println("@Creado ->" + creado);
//                    if( creado ){
                        String read = "SELECT * FROM `"+ tabla  + "` WHERE `ID` = ( SELECT MAX( `ID` ) FROM `" + tabla+ "`)";
                        System.out.println(read);
                        ResultSet response = cmdDB.executeQuery(read);
                        ResultSetMetaData resultMD = response.getMetaData();
                        JSONObject tupla = new JSONObject();
                        
                        while( response.next()){
                            for (int i = 1; i <= resultMD.getColumnCount(); i++) {
                                String columnName = resultMD.getColumnName(i);
                                Object value = response.getObject(i);
                                boolean addQuoations = columnName.contains("DATE") || value == null;
                                
                                tupla.put(columnName, (addQuoations ? "\"" : "") + value + (addQuoations ? "\"" : ""));
                            }
                        }
                        System.out.println("@TUPLA " + tupla.toJSONString());
                        return tupla;
//                    }
//                }
//            }
        } catch (Exception e) {
            System.err.println("Error al momento de crear");
        }
        
        return (JSONObject)
                new JSONObject().put("Error",true);
    }
    public String read(JSONObject query){
        String readQuery = "SELECT ";
        String tabla = query.get("tabla").toString();
        
        if( query.containsKey("columnas") ){
            readQuery += query.get("columnas").toString();
        }else{
            readQuery += "*";
        }
        
        readQuery += " FROM " + tabla;
        
        if( query.containsKey("condiciones") ){
            readQuery += " WHERE " + query.get("condiciones").toString();
        }
        System.out.println( readQuery );
        try {
            ResultSet response = cmdDB.executeQuery( readQuery );  
            ResultSetMetaData resultMD = response.getMetaData();
            JSONArray arregloResponse = new JSONArray();
            while( response.next()){
                JSONObject tupla = new JSONObject();
                for (int i = 1; i <= resultMD.getColumnCount(); i++) {
                    String columnName = resultMD.getColumnName(i);
                    Object value = response.getObject(i);
                    boolean addQuoations = columnName.contains("DATE") || value == null;

                    tupla.put(columnName, (addQuoations ? "\"" : "") + value + (addQuoations ? "\"" : ""));
                                
//                    tupla.put( resultMD.getColumnName(i), response.getObject(i) );
                }
                arregloResponse.add( tupla );
            }
            
            return arregloResponse.toJSONString();
        } catch (Exception e) {
            System.out.println("Error al leer ( Server )");
        }
        
        return "No pudo leer";
    }
    public Object READ(String tabla) {
        return READ(tabla, null, null, null);
    }
    public Object READ(String tabla, String[] columnas, Object[][] where, Object[][] join){
        JSONObject data = new JSONObject();
        String query = "SELECT ";
           data.put("tabla", tabla );
           String columnasStr = "";
           if( columnas != null && columnas.length > 0){
               columnasStr += "`" + tabla + "`.`ID`,";
               for (int i = 0; i < columnas.length; i++) {
                   columnasStr += "`" + tabla + "`.`" + columnas[i].toUpperCase() + ( i == columnas.length - 1 ? "` " : "`, ");
               }
           }else{
               columnasStr += getFieldsNames(tabla, true)[0];
           }
           String joinStr = "";
           if( join != null && join.length > 0){
                for (int i = 0; i < join.length; i++) {
                    String joinTabla = join[i][0].toString();
                    if( join[i][4] != null){
                        columnasStr += ",";
                        String[] columnsJoin = join[i][4].toString().split(",");
                        for (int j = 0; j < columnsJoin.length; j++) {
                            columnasStr += "`" + joinTabla + i + "`.`" + columnsJoin[j] + ( j == columnsJoin.length - 1 ? "` " : "`, ");
                        }
                    }
                    joinStr += " " + ( join[i][5] != null ? join[i][5] : "INNER JOIN") + " `" + joinTabla + "` AS `" + joinTabla + i + "` ON `" + tabla + "`.`" + join[i][1];
                    joinStr += "` " + join[i][2] + "`" + joinTabla + i + "`.`" + join[i][3] + "`";
                }
           }
           query += columnasStr + "FROM " + tabla + joinStr;
           
           if( where != null && where.length > 0 ){
               query += " WHERE ";
               for (int i = 0; i < where.length; i++) {
                   if( where[i].length == 3){
                       query += "`" + tabla + "`.`" + ((String)where[i][0]).toUpperCase() + "` " + where[i][1] + " '" + where[i][2] + "'";
                   }else{
                       System.err.println("CONDICION INCOMPLETA");
                       return "CONDICION INCOMPLETA";
                   }
                   query += i == where.length - 1 ? "" : " AND ";
               }
           }
            System.out.println(query);
            try {
                ResultSet response = cmdDB.executeQuery( query );  
                ResultSetMetaData resultMD = response.getMetaData();
                JSONArray arregloResponse = new JSONArray();
                while( response.next()){
                    JSONObject tupla = new JSONObject();
                    for (int i = 1; i <= resultMD.getColumnCount(); i++) {
                        System.out.println(resultMD.getColumnName(i));
                        
                        String columnName = resultMD.getColumnName(i);
                        Object value = response.getObject(i);
                        boolean esFecha = value instanceof Date;
                        boolean addQuoations = esFecha || value == null;

                        tupla.put(columnName, (addQuoations ? "\"" : "") + (esFecha ? value.toString() : value) + (addQuoations ? "\"" : ""));
//
//                        boolean esFecha = response.getObject(i) instanceof Date;
//                        tupla.put( resultMD.getColumnName(i), esFecha ? response.getObject(i).toString() : response.getObject(i) );
                    }
                    arregloResponse.add(tupla);
                }
                System.out.println("@RESPONSE" + arregloResponse.toJSONString());
                return arregloResponse;
            } catch (Exception e) {
                System.out.println("Error al leer ( Server )");
            }
        return new Object[]{};
    }
    public boolean DELETE(String tabla, Object[][] where){
        String query = "DELETE FROM `" + tabla + "`";
        if( where != null && where.length > 0 ){
            query += " WHERE ";
            for (int i = 0; i < where.length; i++) {
                if( where[i].length == 3){
                    Object value = "'" + where[i][2] + "'";
                    if (where[i][1] == "IN") {
                        JSONArray values = (JSONArray)where[i][2];
                        value = "(";
                        for (int j = 0; j < values.size(); j++) {
                            value += "'" + values.get(j) + "'" + (j == values.size() - 1 ? ")" : ",");
                        }
                    }
                    query += "`" + tabla + "`.`" + where[i][0] + "` " + where[i][1] + " " + value;
                }else{
                    System.err.println("CONDICION INCOMPLETA");
                    return false;
                }
                query += i == where.length - 1 ? "" : " AND ";
            }
            try {
                System.out.println("@DELETE QUERY -> " + query);
                return cmdDB.execute( query );
            } catch (Exception e) {
                System.err.println("ERROR DELETE");
            }
        }
        return false;
    }
}
