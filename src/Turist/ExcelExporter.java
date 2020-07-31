
package Turist;

/**
 *
 * @author mozevil
 */
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import jxl.CellView;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.write.*;

public class ExcelExporter {

    void fillData(JTable table, File file) {

        try {

            WritableWorkbook workbook1 = Workbook.createWorkbook(file);
            WritableSheet sheet1 = workbook1.createSheet("Лист 1", 0); 
            
            CellView cv = new CellView();
            cv.setAutosize(true);
            
            //установка шрифта
            WritableFont arial12 = new WritableFont(WritableFont.ARIAL, 12, WritableFont.NO_BOLD);
            WritableFont arial12_Bold = new WritableFont(WritableFont.ARIAL, 12, WritableFont.BOLD);
            WritableCellFormat cellFormat = new WritableCellFormat(arial12);
            cellFormat.setAlignment(Alignment.LEFT);
            cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
            
            WritableCellFormat cellFormat_Bold = new WritableCellFormat(arial12_Bold);
            cellFormat_Bold.setAlignment(Alignment.CENTRE);
            cellFormat_Bold.setBorder(Border.ALL, BorderLineStyle.THIN);
            
            TableModel model = table.getModel();

            for (int i = 0; i < model.getColumnCount(); i++) {
                Label column = new Label(i, 0, model.getColumnName(i), cellFormat_Bold);
                sheet1.addCell(column);
            }
            for (int i = 0; i < model.getRowCount(); i++) {
                for (int j = 0; j < model.getColumnCount(); j++) {
                    Label row = new Label(j, i + 1, model.getValueAt(i, j) + "", cellFormat);
                    sheet1.addCell(row);
                    
                }               
            }
            for (int i = 0; i < model.getColumnCount(); i++) {
                sheet1.setColumnView(i, cv);
            }
            
            workbook1.write();
            workbook1.close();
            
        } catch (IOException | WriteException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }
}
