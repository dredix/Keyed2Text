/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package keyed2text;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * @author dredix
 */
public class Keyed2Text {

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        // Check command line arguments. If invalid, print syntax and exit.
        if (args.length != 2) {
            Syntax();
            return;
        }
        if (Utils.isNullOrWhiteSpace(args[0]) || Utils.isNullOrWhiteSpace(args[1])) {
            Syntax();
            return;
        }
        try {
            // Run the conversion
            KFileConverter.convert(args[0], args[1]);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Keyed2Text.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(Keyed2Text.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Keyed2Text.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private static void Syntax() {
        System.out.println("Syntax: java -jar Keyed2Text.jar <input_xml> <output_txt>");
    }
}
