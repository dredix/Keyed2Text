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
 * Performs the conversion from keyed format to tab delimited text file.
 * @author dredix
 */
public class KFileConverter {

    public static final char SEP = '\t';

    public static void convert(String kfDescPath, String textFilePath)
            throws IOException, ParserConfigurationException, SAXException {

        // Get the file descriptor from the XML file.
        KFileDescriptor kfd = new KFileDescriptor(kfDescPath);

        // Validate that keyed file and output text file are not the same.
        String keyedFilename = new File(kfd.getFileName()).getCanonicalPath();
        String textFilename = new File(textFilePath).getCanonicalPath();
        if (keyedFilename.equals(textFilename)) {
            throw new IllegalArgumentException("Output text file cannot be the same input keyed file");
        }

        // Get record data and make sure is correct.
        int kl = kfd.getKeyLength();
        int rs = kfd.getRecordSize();
        if (kl <= 0 || rs <= 0 || kfd.getFieldCount() <= 0) {
            throw new IllegalArgumentException("Invalid input data");
        }

        // Create appropriate file handlers.
        RandomAccessFile file = new RandomAccessFile(keyedFilename, "r");
        BufferedWriter bw = new BufferedWriter(new FileWriter(textFilename));

        // Write the header to the text file.
        writeHeader(kfd, bw);

        // Setup the null key, so valid records can be identified.
        byte[] nullKey = new byte[kl];
        byte[] readKey = new byte[kl]; // record key
        byte[] record = new byte[rs];  // record contents

        // Start cycling through the file.
        long len = file.length();
        long i = 516;
        long reg = 0;
        while (i < len) {
            while (reg < 512 && reg + rs <= len) {
                // Read the key of the current record.
                file.seek(i);
                file.readFully(readKey);
                // If a valid (non null) key is found, read the record and
                // write the text equivalent.
                if (!Arrays.equals(nullKey, readKey)) {
                    file.seek(i);
                    file.readFully(record);
                    writeToText(kfd, record, bw);
                }
                // Reposition the file pointer.
                i += rs;
                reg += rs;
                if (reg + rs > 512) {
                    i += 512 - reg;
                    reg = 512;
                }
            }
            reg = 0;
        }
        // Close everything.
        bw.flush();
        bw.close();
        file.close();
    }

    /**
     * Write a record to the text files. Individual fields are converted 
     * according to the data on the keyed file descriptor.
     */
    private static void writeToText(KFileDescriptor kf, byte[] record, BufferedWriter bw) throws IOException {
        StringBuilder sb = new StringBuilder();
        KFileField field;
        // Convert each field to text and append it to a resulting string.
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
                // TODO: Int conversion is not implemented correctly. 
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
        // Delete the last separator on the line. At this stage Which is better:
        // The char deletion or adding a condition to the char insertion.
        if (sb.charAt(sb.length() - 1) == SEP) {
            sb.deleteCharAt(sb.length() - 1);
        }
        // Don't write the results if the stream contains a null character, as
        // it usually indicates a deleted record.
        if (sb.indexOf("\0") >= 0) {
            return;
        }
        // Write the converted line to the output buffer
        bw.write(sb.toString());
        bw.newLine();
    }

    /**
     * Writes the header to the text files, by concatenating the field names.
     */
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
