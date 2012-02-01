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

    public static boolean isNullOrWhiteSpace(String str) {
        return (str == null || "".equals(str) || "".equals(str.trim()));
    }

}
