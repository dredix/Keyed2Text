/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package keyed2text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


/**
 *
 * @author dredix
 */
public class KFileDescriptor {

    private String fileName;
    private int keyLength;
    private int recordSize;
    private KFileField[] fields;
    

    public KFileDescriptor(String filename) throws
            ParserConfigurationException, SAXException, IOException  {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        parseDoc(db.parse(new File(filename)));
    }

    public KFileDescriptor(Document doc) {
        parseDoc(doc);
    }

    public final void parseDoc(Document doc) {
        if (doc == null) {
            throw new IllegalArgumentException("Xml document cannot be null");
        }
        Element elem = (Element) doc.getElementsByTagName("KeyedFile").item(0);

        fileName = elem.getAttribute("path");
        if (fileName == null || "".equals(fileName)) {
            throw new RuntimeException("Keyed File name cannot be empty");
        }
        
        keyLength = Integer.parseInt(elem.getAttribute("keySize"));
        if (keyLength <= 0) {
            throw new RuntimeException("Key length must be a positive integer");
        }
        recordSize = Integer.parseInt(elem.getAttribute("recordSize"));
        if (recordSize <= 0) {
            throw new RuntimeException("Record size must be a positive integer");
        }

        Node parent = doc.getElementsByTagName("DefaultRecord").item(0);
        NodeList xFields = doc.getElementsByTagName("Field");
        int n = xFields.getLength();
        ArrayList al = new ArrayList(n);
        KFileField kff;
        for (int k = 0; k < n; k++) {
            elem = (Element) xFields.item(k);
            if (elem.getParentNode().equals(parent))
            {
                kff = new KFileField();
                kff.name = elem.getAttribute("name");
                kff.start = Integer.parseInt(elem.getAttribute("position"));
                kff.size = Integer.parseInt(elem.getAttribute("size"));
                kff.end = kff.start + kff.size;
                kff.type = elem.getAttribute("type");
                al.add(kff);
                kff = null;
            }
        }
        Collections.sort(al);
        fields = new KFileField[al.size()];
        al.toArray(fields);
    }

    public String getFileName() {
        return fileName;
    }

    public int getKeyLength() {
        return keyLength;
    }

    public int getRecordSize() {
        return recordSize;
    }

    public KFileField[] getFields() {
        return fields;
    }
    
    public KFileField getField(int index) {
        return fields[index];
    }

    public int getFieldCount() {
        return fields.length;
    }

}
