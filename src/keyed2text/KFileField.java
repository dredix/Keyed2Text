/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package keyed2text;

/**
 *
 * @author dredix
 */
public class KFileField implements Comparable {

    public String name;
    public int size;
    public int start;
    public int end;
    public String type;

    public int compareTo(Object o) {
        return this.start - ((KFileField) o).start;
    }

}
