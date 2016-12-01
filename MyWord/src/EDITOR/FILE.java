/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EDITOR;

import java.io.StringReader;
import java.util.Base64;
import java.util.Stack;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author oscarito
 */
public class FILE {
    private int creationUser;
    private int id;
    private String name;
    private JSONObject blocks = new JSONObject();
    private JTextPane txtDocument;
    
    public FILE(int id, int creationUser, String name, JTextPane txtDocument) {
        this.creationUser = creationUser;
        this.id = id;
        this.name = name;
        this.txtDocument = txtDocument;
    }

    public FILE(int creationUser, JTextPane txtDocument) {
        this.creationUser = creationUser;
        this.txtDocument = txtDocument;
    }
    
    public FILE(int id, int creationUser, String name, JSONObject blocks, JTextPane txtDocument) {
        this.creationUser = creationUser;
        this.id = id;
        this.name = name;
        this.blocks = blocks;
        this.txtDocument = txtDocument;
    }
    
    public JSONObject toJSON(JTextPane txtDocumento) {
        this.updateJSON();
        JSONObject retVal = new JSONObject();
        retVal.put("id", id);
        retVal.put("creationUser", this.creationUser);
        retVal.put("blocks", this.blocks);
        retVal.put("name", name);
        return retVal;
    }
                                         
    public void updateJSON () {
        JSONObject retVal = new JSONObject();
        HTMLEditorKit htmlKit = new HTMLEditorKit();
        HTMLDocument htmlDoc = (HTMLDocument) htmlKit.createDefaultDocument();
//        setTextAttribute("id", "1");
        try {
            htmlKit.read(new StringReader(this.txtDocument.getText()), htmlDoc, 0);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        // Parse
        System.out.println(this.txtDocument.getText());
        ElementIterator iterator = new ElementIterator(htmlDoc);
        Element element;
        int totalBlocks = 0;
        while ((element = iterator.next()) != null) {
            JSONObject newBlock = new JSONObject();
            AttributeSet as = element.getAttributes();
            Object name = as.getAttribute(StyleConstants.NameAttribute);
            String id = (String)as.getAttribute("id");
            int idUser = as.getAttribute("idUser") != null ? (int)as.getAttribute("idUser") : -1;
            if (name == HTML.Tag.CONTENT || name == HTML.Tag.P) {
//                System.out.println("Id " + id);
                StringBuffer sb = new StringBuffer();
//                System.out.println("NAME  " + name);
//                sb.append(name).append(": ").append(id);
                int count = element.getElementCount();
                JSONArray lineas = new JSONArray();
                for (int i = 0; i < count; i++) {
                    Element child = element.getElement(i);
                    int startOffset = child.getStartOffset();
                    int endOffset = child.getEndOffset();
                    int length = endOffset - startOffset;
                    try {
                        System.out.println("@CONTENT" + htmlDoc.getText(startOffset, length));
                        lineas.add(this.getHTMLBlock(child, htmlDoc.getText(startOffset, length)));
                    } catch (Exception e) {
                        System.err.println(e);
                    }
                }
//                if (idUser == -1) {
//                    idUser = this.creationUser;
//                }
                newBlock.put("idUser", idUser);
                
                if (id == null) {
                    id = idUser + "-" + totalBlocks;
                }
//                System.out.println(id);
                if (lineas.size() > 0) {
                    newBlock.put("content", lineas);
                    if (this.blocks.get(id) != null) {
                        this.blocks.replace(id, newBlock);
                    } else {
                        this.blocks.put(id, newBlock);
                    }
                }
//                System.out.println("TEXT" + sb);
                totalBlocks++;
            }
        }
        System.out.println("@JSON:\t" +  this.blocks.toJSONString());
    }
    private JSONObject getHTMLBlock(Element element, String text) {
        JSONObject retVal = new JSONObject();
        Stack<String> tags = new Stack<String>();
        JSONObject properties = new JSONObject();
        DefaultStyledDocument doc = (DefaultStyledDocument) element.getDocument();
        AttributeSet allAtributes = element.getAttributes();
        
        if (StyleConstants.isBold(allAtributes)) {
            tags.push("b");
        }
        if (StyleConstants.isItalic(allAtributes)) {
            tags.push("i");
        }
        if (StyleConstants.isUnderline(allAtributes)) {
            tags.push("u");
        }
        
        properties.put("fontSize", StyleConstants.getFontSize(allAtributes));
        properties.put("fontFamily", StyleConstants.getFontFamily(allAtributes));
        properties.put("align", StyleConstants.getAlignment(allAtributes));
//        Enumeration atributesNames  = allAtributes.getAttributeNames();
//        System.out.println("@getAttributeCount" + allAtributes.getAttributeCount());
//        DefaultStyledDocument doc = (DefaultStyledDocument) element.getDocument();
//        while (atributesNames.hasMoreElements()) {
//            String tag = atributesNames.nextElement().toString();
//            Object value = allAtributes.getAttribute(tag);
//            System.out.print("@TAG<" + tag + ">");
//            System.out.println(value);
//            
//            if (!tag.equals("name")) {
//                retVal += "<" + tag + ">";
//                tags.push(tag);
//            }
//        }
        
//        System.out.println("@MyTag " + retVal);
//        retVal += text;
        while (tags.size() > 0) {
            String actualTag = tags.pop();
            text = "<" + actualTag + ">" + text + "</" + actualTag + ">";
        }
        retVal.put("style", properties);
        byte[] textBase64 = Base64.getEncoder().encode(text.getBytes());
        retVal.put("text", textBase64);
//        System.out.println("@MyTag2 " + retVal.toJSONString());
        try {
            byte[] test = (byte[])retVal.get("text");
            System.out.println("@Decoding textBase64: [" + textBase64.length + "] " + textBase64);
            System.out.println(new String(Base64.getDecoder().decode((byte[])test)));
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        return retVal;
    }
}
