/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EDITOR;

import static EDITOR.myWord.read;
import java.io.IOException;
import java.io.StringReader;
import java.util.Base64;
import java.util.Stack;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author oscarito
 */
public class FILE extends Thread {
    private int creationUser;
    private int id;
    private String actualBlock;
    private String nameFile;
    private JSONArray blocks = new JSONArray();
    private JTextPane txtDocument;
    private boolean isOpen;
    
    public FILE(int id, int creationUser, String nameFile, JTextPane txtDocument) {
        this.creationUser = creationUser;
        this.id = id;
        this.nameFile = nameFile;
        this.txtDocument = txtDocument;
        this.bindEvents();
        isOpen = true;
        start();
    }

    public FILE(int creationUser, JTextPane txtDocument) {
        this.creationUser = creationUser;
        this.txtDocument = txtDocument;
        this.bindEvents();
        isOpen = true;
        start();
    }
        
    public FILE(int id, int creationUser, String name, JSONArray blocks, JTextPane txtDocument) {
        this.creationUser = creationUser;
        this.id = id;
        this.nameFile = nameFile;
        this.blocks = blocks;
        this.txtDocument = txtDocument;
        this.bindEvents();
        isOpen = true;
        start();
    }
    public void run() {
        System.out.println("@TEXT " + this.txtDocument.getText());
        while (isOpen) {
            try {
                this.updateBlock();
                sleep(5000);
            } catch (Exception e) {
            }
        }
//        this.updateBlock();
    }
    public void setCreationUser(int creationUser) {
        this.creationUser = creationUser;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNameFile(String nameFile) {
        this.nameFile = nameFile;
    }

    public void setBlocks(JSONArray blocks) {
        this.blocks = blocks;
    }

    public void setTxtDocument(JTextPane txtDocument) {
        this.txtDocument = txtDocument;
    }
    
    
    public JSONObject toJSON() {
        JSONObject retVal = new JSONObject();
        try {
            this.blocks = this.updateJSON();
            String textBase64 = null;
            try {
                textBase64 = Base64.getEncoder().encodeToString(this.blocks.toJSONString().getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
            retVal.put("ID", id);
    //        retVal.put("CREATION_USER", this.creationUser);
            retVal.put("CONTENT", textBase64);
            retVal.put("NAME", nameFile);
            System.out.println("@FILE = " + retVal.toJSONString());
            read("updateFile", retVal);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }
    
    public void updateBlock() {
        try {
            JSONObject retVal = new JSONObject();
//            this.blocks = this.updateJSON();
            String textBase64 = null;
            JSONArray blocks = this.getBlockById(actualBlock);
            if (blocks.size() == 0) return;
            
            System.out.println("@ID_BLOCK " + actualBlock);
            System.out.println("BLOCK +++" + ((JSONObject)blocks.get(0)).toJSONString());
            try {
                textBase64 = Base64.getEncoder().encodeToString(((JSONObject)blocks.get(0)).toJSONString().getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
            retVal.put("idFile", this.id);
            retVal.put("id", this.actualBlock);
    //        retVal.put("CREATION_USER", this.creationUser);
            retVal.put("content", textBase64);
//            retVal.put("NAME", nameFile);
//            System.out.println("@BLOCK = " + retVal.toJSONString());
//            read("updateFile", retVal);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
                                         
    public JSONArray updateJSON () {
        return this.getBlocks(-1, null);
    }
    
    public JSONArray getBlockById(String idBlock) {
//        this.actualBlock = idBlock;
        return this.getBlocks(-1, idBlock);
    }
    
    public JSONArray getBlockByPosition(int pos) {
        return this.getBlocks(pos, null);
    }
    
    public JSONArray getBlocks (int pos, String idBlock) {
        JSONArray retBlocks = new JSONArray();
        //Creating a new editor document
        
        HTMLEditorKit htmlKit = new HTMLEditorKit();
//        HTMLDocument htmlDoc = (HTMLDocument) 
//        HTMLDocument htmlDoc = (HTMLDocument) htmlKit.createDefaultDocument();
        HTMLDocument htmlDoc = (HTMLDocument) this.txtDocument.getDocument();
//        StyledDocument doc = (StyledDocument) this.txtDocument.getDocument();
        try {
            htmlKit.read(new StringReader(this.txtDocument.getText()), htmlDoc, 0);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        
        //Getting all blocks 
        ElementIterator iterator = new ElementIterator(htmlDoc);
        Element element;
        int totalBlocks = 0;
        
        //Iterate each block and get all attributes
        while ((element = iterator.next()) != null) {
            
            JSONObject newBlock = new JSONObject();
            AttributeSet as = element.getAttributes();
            
            Object name = as.getAttribute(StyleConstants.NameAttribute);
            String idBlockHTML = (String)as.getAttribute("idblock");
            System.out.println("@idBlockHTML ---> " + idBlockHTML);
            System.out.println("TEST   ---> " + as.getAttribute("idblock"));
            MutableAttributeSet asNew = new SimpleAttributeSet(as.copyAttributes());
            int idUser = as.getAttribute("idUser") != null ? (int)as.getAttribute("idUser") : -1;
            
            int startBlock = element.getStartOffset();
            int endBlock = element.getEndOffset();
            if ((name == HTML.Tag.CONTENT || name == HTML.Tag.P) && (idBlock == null || idBlock.equals(idBlockHTML)) 
                    && ((pos == -1) || (pos >= startBlock && pos < endBlock))) {
                
//                if (pos != 0 && pos >= startBlock && pos < endBlock) {
//                    System.out.println("<content>" + startBlock);
//                    System.out.println("POS: " + pos);
//                    System.out.println("idUser -> " + as.getAttribute("idUser"));
//                    System.out.println("</content>" + endBlock);
                    try {
                        System.out.println("@BLOCK = " + htmlDoc.getText(startBlock, endBlock - startBlock));
                    } catch (Exception e) {
                    }
//                }
                
                int count = element.getElementCount();
                JSONArray lineas = new JSONArray();
                for (int i = 0; i < count; i++) {
                    Element child = element.getElement(i);
                    int startOffset = child.getStartOffset();
                    int endOffset = child.getEndOffset();
                    int length = endOffset - startOffset;
                    try {
                        lineas.add(this.getHTMLLine(child, htmlDoc.getText(startOffset, length)));
                    } catch (Exception e) {
                        System.err.println(e);
                    }
                }
                newBlock.put("idUser", idUser);
                
                
                
//                System.out.println("@ID = " + idBlockHTML);
                if (lineas.size() > 0) {
                    if (idBlockHTML == null) {
                        System.out.println("idBlockHTML === null");
                        idBlockHTML = UUID.randomUUID().toString() + totalBlocks;
                        asNew.addAttribute("idblock", idBlockHTML);
                        htmlDoc.setParagraphAttributes(startBlock, endBlock - startBlock - 1, asNew, true);
                    }
                    newBlock.put("content", lineas);
                    newBlock.put("id", idBlockHTML);
                    retBlocks.add(newBlock);
                }
                totalBlocks++;
                if (idBlock != null || pos != -1) {
                    this.actualBlock = idBlockHTML;
//                    System.out.println("@retBlocks " + retBlocks.toJSONString());
                    return retBlocks;
                }
            }
        }
//        this.txtDocument.setDocument(htmlDoc);
//        System.out.println("@retBlocks " + retBlocks.toJSONString());
        return retBlocks;
    }
    private JSONObject getHTMLLine(Element element, String text) {
        JSONObject retVal = new JSONObject();
        Stack<String> tags = new Stack<String>();
        JSONObject properties = new JSONObject();
//        DefaultStyledDocument doc = (DefaultStyledDocument) element.getDocument();
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
//        System.out.println("TEXT ---> " + text);
        retVal.put("style", properties);
        String textBase64 = Base64.getEncoder().encodeToString(text.getBytes());
        retVal.put("text", textBase64);
//        System.out.println("@MyTag2 " + retVal.toJSONString());
        try {
            String test = retVal.get("text").toString();
//            System.out.println("@Decoding textBase64: [" + textBase64 + "] " + textBase64);
//            System.out.println(new String(Base64.getDecoder().decode(test)));
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        return retVal;
    }
    
    public void renderText(JSONArray blocks) {
        String text = "";
        this.txtDocument.setContentType("text/html");
        this.txtDocument.setText("<html></html>");
        for (int i = 0; i < blocks.size(); i++) {
            JSONObject block = (JSONObject)blocks.get(i);
            JSONArray lines = (JSONArray)block.get("content");
            text += "<p";
            try {
                text += " idblock=\"" + block.get("id").toString() + "\">";
            } catch (Exception e) {
            }
            for (int j = 0; j < lines.size(); j++) {
//                text += lines.get(i);
                JSONObject line = (JSONObject)lines.get(j);
                String textLine = new String(Base64.getDecoder().decode(line.get("text").toString()));
                System.out.println(textLine);
                text += textLine;
            }
            text += "</p>";
            try {
                
                MutableAttributeSet asNew = new SimpleAttributeSet();
                asNew.addAttribute("id", block.get("id").toString());
                this.txtDocument.getDocument().insertString(this.txtDocument.getDocument().getLength(), text, asNew);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        this.txtDocument.setText(text);
       
//        this.txtDocument.getDocument().insertString(id, text, a);
        System.out.println("@TEXT(1) = " + this.txtDocument.getText());
        updateJSON();
        
//        HTMLDocument htmlDoc = (HTMLDocument) this.txtDocument.getDocument();
        System.out.println("@TEXT = " + this.txtDocument.getText());
        start();
    }
    public void bindEvents() {
        FILE _self = this;
        this.txtDocument.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                _self.getBlockByPosition(_self.txtDocument.getCaretPosition());
            }
        });
    }
}
