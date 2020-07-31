
package Turist;

/**
 *
 * @author mozevil
 */
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.StringTokenizer;
import jxl.CellView;
import jxl.SheetSettings;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.VerticalAlignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.*;
import jxl.write.*;
import jxl.write.biff.RowsExceededException;
 
public class ExcelJob {
 
	private static WritableWorkbook workbook; // переменная рабочей книги
	public static WritableSheet sheet;
 
        public static void excelSchet(String excelTarget, DBT_main main, DBT_prodavets prodavets, DBT_turagent turagent) throws WriteException {
		WorkbookSettings ws = new WorkbookSettings();
		ws.setLocale(new Locale("ru", "RU"));
 
		try {
                    //имя и путь файла
                    workbook = Workbook.createWorkbook(new File(excelTarget), ws);
                    //название листа
                    sheet = workbook.createSheet("Счет", 0);	
                    //ширина 2-го столбца
                    CellView cv = new CellView();
                    cv.setSize(10000);
                    sheet.setColumnView(1, cv);
                    //??
                  //  SheetSettings ss = new SheetSettings(sheet);
               //     ss.setScaleFactor(500);
                //    ss.setOrientation(PageOrientation.LANDSCAPE);
                    //-----------------------------------------
                    //установка шрифта
                    WritableFont arial_10 = new WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD);
                    
                    //форматы ячеек
                    WritableCellFormat text = new WritableCellFormat(arial_10);
                    WritableCellFormat text_centre = new WritableCellFormat(arial_10);
                    WritableCellFormat table_text_left = new WritableCellFormat(arial_10);
                    WritableCellFormat table_text_centre = new WritableCellFormat(arial_10);
                    WritableCellFormat table_number = new WritableCellFormat(arial_10);
                    WritableCellFormat table_head = new WritableCellFormat(arial_10);
                    WritableCellFormat table_foot_text = new WritableCellFormat(arial_10);
                    WritableCellFormat table_foot_number = new WritableCellFormat(arial_10);
                    
                    
                    //выравнивание
                    text.setAlignment(Alignment.LEFT);
                    text_centre.setAlignment(Alignment.CENTRE);
                    table_text_left.setAlignment(Alignment.LEFT);
                    table_text_centre.setAlignment(Alignment.CENTRE);
                    table_number.setAlignment(Alignment.CENTRE);
                    table_head.setAlignment(Alignment.CENTRE);
                    table_foot_text.setAlignment(Alignment.LEFT);
                    table_foot_number.setAlignment(Alignment.CENTRE);
                    
                    text.setVerticalAlignment(VerticalAlignment.CENTRE);
                    text_centre.setVerticalAlignment(VerticalAlignment.CENTRE);
                    table_text_left.setVerticalAlignment(VerticalAlignment.CENTRE);
                    table_text_centre.setVerticalAlignment(VerticalAlignment.CENTRE);
                    table_number.setVerticalAlignment(VerticalAlignment.CENTRE);
                    table_head.setVerticalAlignment(VerticalAlignment.CENTRE);
                    table_foot_text.setVerticalAlignment(VerticalAlignment.CENTRE);
                    table_foot_number.setVerticalAlignment(VerticalAlignment.CENTRE);
                    
                    //перенос по словам если не помещается
                    table_text_left.setWrap(true);
                   
                    //рисуем рамку
                    table_text_left.setBorder(Border.ALL, BorderLineStyle.THIN);
                    table_text_centre.setBorder(Border.ALL, BorderLineStyle.THIN);
                    table_number.setBorder(Border.ALL, BorderLineStyle.THIN);
                    table_head.setBorder(Border.ALL, BorderLineStyle.MEDIUM);
                    table_foot_text.setBorder(Border.ALL, BorderLineStyle.MEDIUM);
                    table_foot_number.setBorder(Border.ALL, BorderLineStyle.MEDIUM);
                    
                    //
                    String tur1_fio = famIO(main.tur1_fio);
                    String tur2_fio = main.tur2_fio.equalsIgnoreCase("") ? "" : famIO(main.tur2_fio);
                    String tur3_fio = main.tur3_fio.equalsIgnoreCase("") ? "" : famIO(main.tur3_fio);
                    String tur4_fio = main.tur4_fio.equalsIgnoreCase("") ? "" : famIO(main.tur4_fio);
                    String tur5_fio = main.tur5_fio.equalsIgnoreCase("") ? "" : famIO(main.tur5_fio);
                    
                    String procent = "-" + (Integer.parseInt(main.price) - Integer.parseInt(main.last_price));
                    
                    //добавления в ячейки
                    //new Label(№ столбца, № строки, "значение", формат ячейки); //нумерация строк и столбцой начинается с 0
                    int i = 0; // номер строки
                    i++;
                    sheet.addCell(new Label(0, i++, "Продавец: " + prodavets.name, text));
                    sheet.addCell(new Label(0, i++, "Адрес: " + prodavets.address_f, text));
                    sheet.addCell(new Label(0, i++, "Расчетный счет: " + prodavets.r_schet, text));
                    sheet.addCell(new Label(0, i++, "Кор. счет: " + prodavets.k_schet, text));                    
                    sheet.addCell(new Label(0, i++, "Банк: " + prodavets.bank, text));
                    sheet.addCell(new Label(0, i++, "ИНН: " + prodavets.inn, text));
                    sheet.addCell(new Label(0, i++, "КПП: " + prodavets.kpp, text));
                    sheet.addCell(new Label(0, i++, "БИК: " + prodavets.bik, text));
                    i++;
                    sheet.addCell(new Label(0, i++, "Покупатель: " + turagent.fullname, text));                    
                    sheet.addCell(new Label(0, i++, "Адрес: " + turagent.address, text));
                    sheet.addCell(new Label(0, i++, "Расчетный счет: " + turagent.r_schet, text));
                    sheet.addCell(new Label(0, i++, "Кор. счет: " + turagent.k_schet, text));                    
                    sheet.addCell(new Label(0, i++, "Банк: " + turagent.bank, text));
                    sheet.addCell(new Label(0, i++, "ИНН: " + turagent.inn, text));
                    sheet.addCell(new Label(0, i++, "КПП: " + turagent.kpp, text));
                    sheet.addCell(new Label(0, i++, "БИК: " + turagent.bik, text));
                    i++;
                     //объединить ячейки  mergeCells(int col1, int row1, int col2, int row2) 
                    sheet.mergeCells(0, i, 5, i);
                    sheet.addCell(new Label(0, i++, "СЧЕТ № " + main.id + " от " + main.sale_date, text_centre));
                    i++;
                    sheet.addCell(new Label(0, i, "№", table_head));
                    sheet.addCell(new Label(1, i, "Наименование", table_head));
                    sheet.addCell(new Label(2, i, "Ед. изм.", table_head));
                    sheet.addCell(new Label(3, i, "Кол-во", table_head));
                    sheet.addCell(new Label(4, i, "Цена", table_head));
                    sheet.addCell(new Label(5, i++, "Сумма", table_head));
                    int no = 1;
                    sheet.addCell(new Label(0, i, "" + no++, table_text_centre));
                    sheet.addCell(new Label(1, i, main.tur_name + ", " + main.tur_date_s + ", " + tur1_fio, table_text_left));
                    sheet.addCell(new Label(2, i, "ед.", table_text_centre));
                    sheet.addCell(new Label(3, i, "1", table_text_centre));
                    sheet.addCell(new Label(4, i, main.tur1_price, table_number));
                    sheet.addCell(new Label(5, i++, main.tur1_price, table_number));
                    
                    if(!tur2_fio.equalsIgnoreCase("")){
                        sheet.addCell(new Label(0, i, "" + no++, table_text_centre));
                        sheet.addCell(new Label(1, i, main.tur_name + ", " + main.tur_date_s + ", " + tur2_fio, table_text_left));
                        sheet.addCell(new Label(2, i, "ед.", table_text_centre));
                        sheet.addCell(new Label(3, i, "1", table_text_centre));
                        sheet.addCell(new Label(4, i, main.tur2_price, table_number));
                        sheet.addCell(new Label(5, i++, main.tur2_price, table_number));
                    }
                    if(!tur3_fio.equalsIgnoreCase("")){
                        sheet.addCell(new Label(0, i, "" + no++, table_text_centre));
                        sheet.addCell(new Label(1, i, main.tur_name + ", " + main.tur_date_s + ", " + tur3_fio, table_text_left));
                        sheet.addCell(new Label(2, i, "ед.", table_text_centre));
                        sheet.addCell(new Label(3, i, "1", table_text_centre));
                        sheet.addCell(new Label(4, i, main.tur3_price, table_number));
                        sheet.addCell(new Label(5, i++, main.tur3_price, table_number));
                    }
                    if(!tur4_fio.equalsIgnoreCase("")){
                        sheet.addCell(new Label(0, i, "" + no++, table_text_centre));
                        sheet.addCell(new Label(1, i, main.tur_name + ", " + main.tur_date_s + ", " + tur4_fio, table_text_left));
                        sheet.addCell(new Label(2, i, "ед.", table_text_centre));
                        sheet.addCell(new Label(3, i, "1", table_text_centre));
                        sheet.addCell(new Label(4, i, main.tur4_price, table_number));
                        sheet.addCell(new Label(5, i++, main.tur4_price, table_number));
                    }
                    if(!tur5_fio.equalsIgnoreCase("")){
                        sheet.addCell(new Label(0, i, "" + no++, table_text_centre));
                        sheet.addCell(new Label(1, i, main.tur_name + ", " + main.tur_date_s + ", " + tur5_fio, table_text_left));
                        sheet.addCell(new Label(2, i, "ед.", table_text_centre));
                        sheet.addCell(new Label(3, i, "1", table_text_centre));
                        sheet.addCell(new Label(4, i, main.tur5_price, table_number));
                        sheet.addCell(new Label(5, i++, main.tur5_price, table_number));
                    }
                    sheet.addCell(new Label(0, i, "" + no, table_text_centre));
                    sheet.addCell(new Label(1, i, "Агентское вознаграждение", table_text_left));
                    sheet.addCell(new Label(2, i, "шт.", table_text_centre));
                    sheet.addCell(new Label(3, i, main.procent + "%", table_text_centre));
                    sheet.addCell(new Label(4, i, procent, table_number));
                    sheet.addCell(new Label(5, i++, procent, table_number));
                    
                    sheet.addCell(new Label(0, i, "Итого:", table_foot_text));
                    sheet.addCell(new Label(1, i, "", table_foot_text));
                    sheet.addCell(new Label(2, i, "", table_foot_text));
                    sheet.addCell(new Label(3, i, "", table_foot_text));
                    sheet.addCell(new Label(4, i, "", table_foot_text));
                    sheet.addCell(new Label(5, i++, main.last_price, table_foot_number));
                    i++;
                    sheet.addCell(new Label(0, i++, "Сумма прописью: " + Num.toString(Integer.parseInt(main.last_price)) + " рублей 00 копеек. Без НДС.", text));
                    i++;
                    sheet.addCell(new Label(0, i, "Руководитель предприятия:", text));
                    sheet.addCell(new Label(3, i, "Бухгалтер:", text));
                    
                   
                    
		} catch (IOException e) {
		}
 
		try {
			workbook.write();
			workbook.close();
		} catch (IOException | WriteException e) {
		}
 
	}
        
        public static String famIO (String in) {
        StringTokenizer stk = new StringTokenizer(in);
        String fam = stk.nextToken();
        String name = stk.nextToken();
        String otche = stk.nextToken();
        String out = fam + " " + name.substring(0, 1) + "." + otche.substring(0, 1) + ".";
        return out;
    }
 
}
