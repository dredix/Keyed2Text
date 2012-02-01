/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package keyed2text;

import java.io.*;
import java.util.Arrays;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * @author dredix
 */
public class KFileConverter {

    public static final char SEP = '\t';

    public static void convert(String kfDescPath, String textFilePath)
            throws IOException, ParserConfigurationException, SAXException {

        KFileDescriptor keyedFile = new KFileDescriptor(kfDescPath);
        
        String keyedFilename = new File(keyedFile.getFileName()).getCanonicalPath();
        String textFilename = new File(textFilePath).getCanonicalPath();
        if (keyedFilename.equals(textFilename)) {
            throw new IllegalArgumentException("Output text file cannot be the same input keyed file");
        }

        int kl = keyedFile.getKeyLength();
        int rs = keyedFile.getRecordSize();
        if (kl <= 0 || rs <= 0 || keyedFile.getFieldCount() <= 0) {
            throw new IllegalArgumentException("Invalid input data");
        }
        
        RandomAccessFile file = new RandomAccessFile(keyedFilename, "r");
        BufferedWriter bw = new BufferedWriter(new FileWriter(textFilename));

        writeHeader(keyedFile, bw);

        Bin bin = new Bin(file);
        bin.setSigned(false);
        bin.setEndian(Bin.LITTLE_ENDIAN);
        bin.movePointer();

        byte[] readKey = new byte[kl];
        byte[] nullKey = new byte[kl];
        for (int j = 0; j < kl; j++) {
            nullKey[j] = 0;
        }
        byte[] record = new byte[rs];

        long len = bin.binaryLength();
        long i = 516;
        long reg = 0;
        while (i < len) {
            while (reg < 512 && reg + rs <= len) {
                bin.readBytes(i, readKey);
                if (!Arrays.equals(nullKey, readKey)) {
                    bin.readBytes(i, record);
                    writeToText(keyedFile, record, bw);
                }
                i += rs;
                reg += rs;
                if (reg + rs > 512) {
                    i += 512 - reg;
                    reg = 512;
                }
            }
            reg = 0;
        }
        bw.flush();
        bw.close();
        file.close();
    }

    private static void writeToText(KFileDescriptor kf, byte[] record, BufferedWriter bw) throws IOException {
        StringBuilder sb = new StringBuilder();
        KFileField field;
        for (int k = 0; k < kf.getFieldCount(); k++) {
            field = kf.getField(k);
            if (field.type.equalsIgnoreCase("ascii")) {
                for (int p = field.start; p < field.end; p++) {
                    sb.append((char) record[p]);
                }
            } else if (field.type.equalsIgnoreCase("bits")) {
                for (int p = field.end - 1; p >= field.start; p--) {
                    sb.append(Utils.lPad(Integer.toBinaryString(record[p] & 0xFF), 8, '0'));
                }
            } else if (field.type.equalsIgnoreCase("int")) {
                for (int p = field.end - 1; p >= field.start; p--) {
                    sb.append(Utils.lPad(Integer.toBinaryString(record[p] & 0xFF), 8, '0'));
                }
            } else {
                for (int p = field.start; p < field.end; p++) {
                    String hex = Integer.toHexString(record[p] & 0xff);
                    hex = Utils.lPad(hex, 2, '0');
                    sb.append(hex);
                }
            }
            sb.append(SEP);
        }
        if (sb.charAt(sb.length() - 1) == SEP) {
            sb.deleteCharAt(sb.length() - 1);
        }
        bw.write(sb.toString());
        bw.newLine();
    }

    private static void writeHeader(KFileDescriptor kf, BufferedWriter bw) throws IOException {
        KFileField[] fields = kf.getFields();
        StringBuilder sb = new StringBuilder();
        sb.append(fields[0].name);
        for (int i = 1; i < fields.length; i++) {
            sb.append(SEP).append(fields[i].name);
        }
        bw.write(sb.toString());
        bw.newLine();
    }
}
