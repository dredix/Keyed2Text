/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package keyed2text;

import java.io.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

/**
 *
 * @author auislc
 */
public class Keyed2Text {

    final static short endian = Bin.BIG_ENDIAN;
    final static boolean signed = false;
    public static BufferedWriter out;
    public static int keylength;
    public static int recordsize;
    public static Vector xml;

    public static int byteArrayToInt(byte[] b) {
        int value = 0;
        for (int i = b.length - 1; i >= 0; i--) {
            int shift = i * 8; //(4 - 1 - i) * 8;
            value += (b[i] & 0x000000FF) << shift;
        }
        return value;
    }

    public static int byteArrayToInt(byte[] b, int offset, int length) {
        int value = 0;
        for (int i = length - 1; i >= 0; i--) {
            int shift = i * 8; //(4 - 1 - i) * 8;
            value += (b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        if (args.length < 2) {
            System.out.println("Not enough arguments. Arg[0] full path of target text file\n Arg[1] full path of xml file for parsing document.");
            System.exit(1);
        }
        try {
            out = new BufferedWriter(new FileWriter(args[0]));
            DocumentBuilderFactory documentbuilderfactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentbuilder = documentbuilderfactory.newDocumentBuilder();
            Document doc = documentbuilder.parse(new File(args[1]));
            String type, path, header = "";
            int pos, size;
            NodeList KeyedFile = doc.getElementsByTagName("KeyedFile");
            Element keyedfile = (Element) KeyedFile.item(0);
            path = keyedfile.getAttribute("path");
            keylength = Integer.parseInt(keyedfile.getAttribute("keySize"));
            recordsize = Integer.parseInt(keyedfile.getAttribute("recordSize"));
            NodeList nodelist = doc.getElementsByTagName("DefaultRecord");
            NodeList nodelist1 = doc.getElementsByTagName("Field");
            xml = new Vector();
            for (int k1 = 0; k1 < nodelist1.getLength(); k1++) {
                String info[] = new String[3];
                Element element = (Element) nodelist1.item(k1);
                if (element.getParentNode().equals(nodelist.item(0))) {
                    pos = Integer.parseInt(element.getAttribute("position"));
                    size = pos + Integer.parseInt(element.getAttribute("size"));
                    type = element.getAttribute("type");
                    header = header + (element.getAttribute("name")).toString() + '\t';
                    info[0] = Integer.toString(pos);
                    info[1] = Integer.toString(size);
                    info[2] = type;
                    xml.add(info);
                }
            }
            if (header.endsWith("\t")) {
                header = header.substring(0, header.length() - 1);
            }
            out.write(header);
            out.newLine();
            out.flush();
            lukeFileWalker(path);
            out.close();
        } catch (Exception e) {
            System.out.println("Exception 1");
            e.printStackTrace();
        }

    }

    public static void lukeFileWalker(String path) {
        String dataId = "", testcase1 = "";
        int cont = 0;
        while (testcase1.length() < keylength * 2) {
            testcase1 = "00" + testcase1;
        }
        try {
            RandomAccessFile file = new RandomAccessFile(path, "r");
            Bin bin = new Bin(file);
            bin.setSigned(signed);
            bin.setEndian(Bin.LITTLE_ENDIAN);
            bin.movePointer();
            long len = bin.binaryLenght();
            long i = 516;
            long reg = 0;
            byte binaryData[] = new byte[recordsize];
            while (i < len) {
                while (reg < 512 && reg + recordsize <= len) {
                    dataId = getData(i, bin, keylength);
                    if (dataId.equals(testcase1)) {
                    } else {
                        cont++;
                        bin.alignBack(i);
                        binaryData = bin.readFullyReg(binaryData);
                        RequestKeyedFile(binaryData, i, cont);
                    }
                    i += recordsize;
                    reg += recordsize;
                    if (reg + recordsize > 512) {
                        i += 512 - reg;
                        reg = 512;
                    }
                }
                reg = 0;
            }
            file.close();
            System.out.println("total :" + cont);
        } catch (Exception e) {
            System.out.println("Exception 2");
            e.printStackTrace();
        }

    }

    public static void RequestKeyedFile(byte binaryData[], long offset, int cont) {
        try {
            Iterator it = xml.iterator();
            String data[], hex;
            StringBuffer result = new StringBuffer();
            int pos, end;
            while (it.hasNext()) {
                data = (String[]) it.next();
                pos = Integer.parseInt(data[0]);
                end = Integer.parseInt(data[1]);
                if (data[2].equals("ascii")) {
                    for (int p = pos; p < end; p++) {
                        result.append((char) binaryData[p]);
                    }
                } else if (data[2].equals("bits")) {
                    for (int p = end - 1; p >= pos; p--) {
                        result.append(lPad(Integer.toBinaryString(
                                binaryData[p] & 0xFF), 8, '0'));
                    }
                } else if (data[2].equals("int")) {
                    result.append(byteArrayToInt(binaryData, pos, end - pos));
                } else {
                    for (int p = pos; p < end; p++) {
                        hex = Integer.toHexString(binaryData[p] & 0xff);
                        if (hex.length() == 1) {
                            result.append("0");
                        }
                        result.append(hex);
                    }
                }
                result.append('\t');
            }
            if (result.charAt(result.length() - 1) == '\t') {
                result.deleteCharAt(result.length() - 1);
            }
            if (result.indexOf("\0") >= 0) {
                System.out.println("Stream contains null character: " + offset + ", " + cont);
            }
            it = null;
            out.write(result.toString());
            out.newLine();
            out.flush();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    static String getData(long i, Bin bin, int tam) throws java.io.IOException {
        StringBuffer data = new StringBuffer();
        try {
            String hex = "";

            for (int j = 0; j < tam; j++) {
                bin.alignBack(i);
                hex = Bin.decimal2hex(bin.readByte());
                if (hex.length() == 1) {
                    data.append("0");
                }
                data.append(hex);
                i++;
            }
            return data.toString();
        } catch (Exception e) {
            System.out.println("Exception 3");
            e.printStackTrace();
            return data.toString();
        }
    }

    public static String lPad(String str, int length, char c) {
        StringBuffer sb = new StringBuffer(length);
        sb.append(str);
        while (sb.length() < length) {
            sb.insert(0, c);
        }
        return sb.toString();
    }

    public static int hex2decimal(String s) {
        String hex = "0123456789ABCDEF";
        s = s.toUpperCase();
        int i = 0;
        for (int j = 0; j < s.length(); j++) {
            char c = s.charAt(j);
            int k = hex.indexOf(c);
            i = 16 * i + k;
        }

        return i;
    }
}
