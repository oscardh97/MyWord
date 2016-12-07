/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package labo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author oscarito
 */

@XmlRootElement(name="file")
public class file {
    String name;

    public file(String name) {
        this.name = name;
    }

    public file() {
    }
    
    
    @XmlElement
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

}