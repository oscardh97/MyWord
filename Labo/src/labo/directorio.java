/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package labo;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author oscarito
 */
@XmlRootElement(name="directorio")
public class directorio {
    
    
    private int cantFiles = 0;
    ArrayList<file> files = new ArrayList<file>();
    
    public directorio() {
    }
    
    
    public void addFile(file newFile) {
        this.files.add(newFile);
        this.cantFiles = this.files.size();
    }

    @XmlElement
    public int getCantFiles() {
        return cantFiles;
    }

    @XmlElement
    public ArrayList<file> getFiles() {
        return files;
    }
    
    public String SerielizarXml(){
        StringWriter writer = new StringWriter();
        JAXBContext context;
        try {
            context = JAXBContext.newInstance(directorio.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.marshal(this, writer);
            return writer.toString();
        } catch (JAXBException ex) {
            Logger.getLogger(directorio.class.getName()).log(Level.SEVERE, null, ex);
            return "Murio!";
        }
//        finally{
////           return "Error no se pudo serializar";   
//        }
//        finally{
////           return "Error no se pudo serializar";   
//        }
        
    }
    
    
}
