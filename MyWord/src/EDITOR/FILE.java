/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EDITOR;

import static EDITOR.myWord.endpoint;
import java.awt.event.KeyEvent;
import java.io.StringReader;
import java.util.Base64;
import java.util.Stack;
import java.util.UUID;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

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
//        start();
    }

    public FILE(int creationUser, JTextPane txtDocument) {
        this.creationUser = creationUser;
        this.txtDocument = txtDocument;
        this.bindEvents();
        isOpen = true;
//        start();
    }
        
    public FILE(int id, int creationUser, String name, JSONArray blocks, JTextPane txtDocument) {
        this.creationUser = creationUser;
        this.id = id;
        this.nameFile = nameFile;
        this.blocks = blocks;
        this.txtDocument = txtDocument;
        this.bindEvents();
        isOpen = true;
//        start();
    }
    public void run() {
        JSONParser jsonParser = new JSONParser();
        System.out.println("@TEXT " + this.txtDocument.getText());
        while (isOpen) {
            try {
                
                JSONObject object = new JSONObject();
        
                object.put("ID", this.id);
                Object[] response = endpoint("readFileContent", object);
                
                
                try {
                    JSONArray json = (JSONArray)response[1];
                    System.out.println("@@@JSON" + json.toJSONString());
//                    this.renderText((JSONArray)json.get("CONTENT"));
                    this.updateBlock(false);
                    this.blocks = this.updateJSON();
                    this.renderText((JSONArray)jsonParser.parse(((JSONObject)json.get(0)).get("CONTENT").toString()));
                    sleep(5000);
                } catch (Exception ex) {
                    ex.printStackTrace();

        //            Logger.getLogger(PrincipalScreen.class.getName()).log(Level.SEVERE, null, ex);
                }
//                this.updateBlock();
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
            System.out.println("@BLQUES\n" + this.blocks.toJSONString());
            try {
                textBase64 = Base64.getEncoder().encodeToString(this.blocks.toJSONString().getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
            retVal.put("ID", id);
    //        retVal.put("CREATION_USER", this.creationUser);
            retVal.put("CONTENT", textBase64);
//            retVal.put("NAME", nameFile);
            System.out.println("@FILE = " + retVal.toJSONString());
            endpoint("updateFile", retVal);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }
    
    public void updateBlock(boolean unlock) {
        try {
            JSONObject retVal = new JSONObject();
//            this.blocks = this.updateJSON();
            String textBase64 = null;
            
            if (this.actualBlock == null) return;
            
//            JSONArray blocks = this.getBlockById(actualBlock);
            JSONArray blocks = this.getBlockByPosition(this.txtDocument.getCaretPosition());
            System.out.println("EMPTY LINE");
            if (blocks.size() == 0) return;
            
            System.out.println("@ID_BLOCK " + actualBlock);
            System.out.println("BLOCK +++" + ((JSONObject)blocks.get(0)).toJSONString());
//            try {
//                textBase64 = Base64.getEncoder().encodeToString(((JSONObject)blocks.get(0)).toJSONString());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            retVal.put("idFile", this.id);
            retVal.put("id", this.actualBlock);
            retVal.put("unlock", unlock);
    //        retVal.put("CREATION_USER", this.creationUser);
            retVal.put("content", ((JSONArray)((JSONObject)blocks.get(0)).get("content")).toJSONString());
//            retVal.put("NAME", nameFile);
//            System.out.println("@BLOCK = " + retVal.toJSONString());
            endpoint("updateBlock", retVal);
            
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
        System.out.println("@TEXT[172]-------------->...." + this.txtDocument.getText());
        //Creating a new editor document
        
        HTMLEditorKit htmlKit = new HTMLEditorKit();
//        HTMLDocument htmlDoc = (HTMLDocument) this.txtDocument.getDocument();
        HTMLDocument htmlDoc = (HTMLDocument) new HTMLDocument();
        
        try {
            htmlKit.read(new StringReader(this.txtDocument.getText()), htmlDoc, 0);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.toString());
            return new JSONArray();
        }
        
        //Getting all blocks 
        ElementIterator iterator = new ElementIterator(htmlDoc);
        Element element;
        int totalBlocks = 0;
        
        boolean test = false;
        
        //Iterate each block and get all attributes
        while ((element = iterator.next()) != null) {
            JSONObject newBlock = new JSONObject();
            AttributeSet as = element.getAttributes();
            
            Object name = as.getAttribute(StyleConstants.NameAttribute);
            System.out.println("TAG NAME " + name);
            
            if (!test && name == HTML.Tag.BODY) 
                test = true;
            
            if (!test) continue;
            int startBlock = element.getStartOffset();
            int endBlock = element.getEndOffset();
            String idBlockHTML = (String)as.getAttribute("idblock");
            if ((name == HTML.Tag.CONTENT || name == HTML.Tag.P) && (idBlock == null || idBlock.equals(idBlockHTML))
                    && ((pos == -1) || (pos >= startBlock && pos < endBlock))) {
                    System.out.println("@idBlockHTML ---> " + idBlockHTML);
                    System.out.println("TEST   ---> " + as.getAttribute("idblock"));
                    MutableAttributeSet asNew = new SimpleAttributeSet(as.copyAttributes());
                    int idUser = as.getAttribute("idUser") != null ? (int)as.getAttribute("idUser") : -1;
                    
                    try {
//                        is
//                        htmlDoc.replace(idUser, idUser, idBlock, as);
                        System.out.println("@BLOCK = " + htmlDoc.getText(startBlock, endBlock - startBlock));
                    } catch (Exception e) {
                        continue;
                    }
                
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
                
                
//                if (lineas.size() > 0) {
                    if (idBlockHTML == null) {
                        idBlockHTML = UUID.randomUUID().toString() + totalBlocks;
                        asNew.addAttribute("idblock", idBlockHTML);
                        htmlDoc.setParagraphAttributes(startBlock, endBlock - startBlock - 1, asNew, true);
                    }
                    
                    newBlock.put("content", lineas);
                    newBlock.put("id", idBlockHTML);
                    retBlocks.add(newBlock);
//                }
                totalBlocks++;
                if (idBlock != null || pos != -1) {
                    this.actualBlock = idBlockHTML;
                    return retBlocks;
                }
            }
        }
        this.txtDocument.setDocument(htmlDoc);
        
        System.out.println("@TEXT[241]-------------->...." + this.txtDocument.getText());
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
        try {
            JSONParser jsonParser = new JSONParser();
            String text = "";
            this.txtDocument.setContentType("text/html");
            this.txtDocument.setText("<html><head></head><body>");
            for (int i = 0; i < blocks.size(); i++) {
                JSONObject block = (JSONObject)blocks.get(i);

                System.out.println("@this.actualBlock -> " + this.actualBlock);
                System.out.println("@this.block -> " + block.get("id").toString());
                if (this.actualBlock != null) {
                    if (this.actualBlock.equals(block.get("id").toString())) {
                        for (Object myBlock : this.blocks) {
                            JSONObject myBlockJson = (JSONObject)myBlock;

                            if (myBlockJson.get("id").toString().equals(block.get("id").toString())) {
                                block = (JSONObject)jsonParser.parse(myBlockJson.toJSONString());
                                System.out.println("IGUALES!!!!!!!!!!!!!!!!!!!");
                                break;
                            }
                        }
                    }
                }
                JSONArray lines = null;
                try {
                    lines = (JSONArray)block.get("content");
                } catch (Exception e) {
                    lines = (JSONArray)jsonParser.parse(block.get("content").toString());
                }

                text += "<p";
                try {
                    text += " idblock=\"" + block.get("id").toString() + "\">";
                } catch (Exception e) {
                    e.printStackTrace();
                    text += ">\n";
                }
                for (int j = 0; j < lines.size(); j++) {
    //                text += lines.get(i);
                    JSONObject line = (JSONObject)lines.get(j);
    
                    if (line.get("text") == null) {
                        continue;
                    }
                    String textLine = new String(Base64.getDecoder().decode(line.get("text").toString()));
                    System.out.println(textLine);
                    text += textLine;
                }
                text += "</p>";
            }

            this.txtDocument.setText(text + "</body></html>");

    //        this.txtDocument.getDocument().insertString(id, text, a);
    //        System.out.println("@TEXT(1) = " + this.txtDocument.getText());
            updateJSON();
        } catch (Exception Ex) {
            Ex.printStackTrace();
        }
//        HTMLDocument htmlDoc = (HTMLDocument) this.txtDocument.getDocument();
//        System.out.println("@TEXT = " + this.txtDocument.getText());
//        start();
    }
    public void bindEvents() {
        FILE _self = this;
        this.txtDocument.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                try {
                    JSONArray blocks = _self.getBlockByPosition(_self.txtDocument.getCaretPosition());
                    
                    if (blocks.size() == 0) return;
                    JSONObject selectedBlock = (JSONObject)blocks.get(0);
                    System.out.println("@SELECTED_BLOCK\n" + selectedBlock.toJSONString());
                    
                    selectedBlock.put("idFile", _self.id);
                    endpoint("getLock", selectedBlock);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        
        this.txtDocument.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                try {
//                    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
//                        System.out.println("**************ENTER*****************");
//                    }
//                    _self.blocks = _self.updateJSON();
//                    
//                    
//                    JSONArray blocks = _self.getBlockByPosition(_self.txtDocument.getCaretPosition());
//                    
//                    if (blocks.size() == 0) return;
//                    JSONObject selectedBlock = (JSONObject)blocks.get(0);
//                    System.out.println("@SELECTED_BLOCK\n" + selectedBlock.toJSONString());
//                    
//                    selectedBlock.put("idFile", _self.id);
//                    endpoint("getLock", selectedBlock);
//                    System.out.println("KEY PRESS ++++++++++++++++++++++++++++++>" + KeyEvent.VK_ENTER);
                } catch (Exception e) {
                }
            }
        });
    }
}
