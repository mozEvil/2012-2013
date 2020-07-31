
package Turist;


import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
 
/**
 *
 * @author mutagen, edited mozEvil
 */
public class Cfg {
      
    public static Properties getProperties(File f) {
        try {
            InputStream is = new FileInputStream(f);
            Properties p = new Properties();
            p.load(is);
            return p;
         } catch (Exception ex) {
            Logger.getLogger(Cfg.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
            return null;
        }
    }
 
    public static void saveProperties(File f, Properties p) {
        try {
            OutputStream os = new FileOutputStream(f);
            p.store(os, null);
        } catch (Exception ex) {
            Logger.getLogger(Cfg.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }
}