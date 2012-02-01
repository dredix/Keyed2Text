/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package keyed2text;

/**
 *
 * @author dredix
 */
public class Utils {

    public static String lPad(String str, int length, char c) {
        StringBuilder sb = new StringBuilder(length);
        sb.append(str);
        while (sb.length() < length) {
            sb.insert(0, c);
        }
        return sb.toString();
    }

    public static String rPad(String str, int length, char c) {
        StringBuilder sb = new StringBuilder(length);
        sb.append(str);
        while (sb.length() < length) {
            sb.append(c);
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

    public static String strToHex(String str) {
        char[] carr = str.toCharArray();
        StringBuilder sb = new StringBuilder(str.length() * 2);
        for (int i = 0; i < carr.length; i++) {
            sb.append(Integer.toHexString((int) carr[i] & 0xFF));
            sb.append(' ');
        }
        return sb.toString();
    }
    
    public static boolean isNullOrWhiteSpace(String str) {
        return (str == null || "".equals(str) || "".equals(str.trim()));
    }

}
