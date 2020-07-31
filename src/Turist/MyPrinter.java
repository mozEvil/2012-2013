
package Turist;

/**
 *
 * @author mozevil
 */

import java.awt.Font;
import java.awt.Graphics;
import java.awt.PrintJob;

public class MyPrinter extends javax.swing.JFrame {
    
    private PrintJob pjob = null;
    private Graphics pg = null;
    private int x = 0;
    private String font_name = "SansSerif";
    private int font_size = 12;
    
    int margin_left = 20;        
    int margin_top = 25;
    
    
    public MyPrinter() {
        pjob = getToolkit().getPrintJob(this, "Printer", null);
        if (pjob == null) return;
        pg = pjob.getGraphics();
        pg.setFont(new Font(font_name, Font.PLAIN, font_size));
    }
    
    public void setFont(String name, int size) {
        if (pg == null) return;
        font_name = name;
        font_size = size;
        pg.setFont(new Font(font_name, Font.PLAIN, font_size));
    }
    
    public void addString(String str) {
        if (pg == null) return;
        pg.drawString(str, margin_left, margin_top + font_size*(x++));
    }
    
    public void addStringPart2(String str, int margin_left) {
        if (pg == null) return;
        pg.drawString(str, margin_left, margin_top + font_size*(x-1));
    }
    
    public void startPrint() {
        if (pg == null) return;
        pg.dispose();              
        pjob.end(); 
        x = 0;
    }
 
}
