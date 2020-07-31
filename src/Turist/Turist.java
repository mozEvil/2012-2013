package Turist;

import com.toedter.calendar.JDayChooser;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import jxl.write.WriteException;
/**
 *
 * @author mozevil
 */
public class Turist extends javax.swing.JFrame {
    // Настройки
    private static String dbs_user;
    private static String dbs_pass;
    private static String dbs_host;
    private static String dbs_base;
    //Data Base
    private String db_user;
    private String db_pass;
    private String db_host;
    private String db_base;
    //----
    //SMS
    private String sms_smtp_user;
    private String sms_smtp_pass;
    private String sms_smtp;
    private String sms_to;
    private String sms_from;
    private String sms_spam_login;
    private String sms_spam_pass;
    //----
    //Mail
    private String mail_smtp_user;
    private String mail_smtp_pass;
    private String mail_smtp;
    private String mail_from;
    //------------
    
    SendMailThread smt;
    StatusProcessThread spt;
    Thread sm;
    Thread sp;
    DB db2;
    //Properties properties;
    
    public Turist() {
        initComponents();
        setProp(new File("properties.cfg"));
        db = new DB(db_user, db_pass, db_host, db_base);
        db2 = new DB(db_user, db_pass, db_host, db_base);
        tableUpdate();
        //отдельный поток для периодического обновления таблицы последних клиентов
        updateTableThread utt = new updateTableThread();
        Thread ut = new Thread(utt);
        ut.start();
        //------------------------------------------------------------------------
        jday = jDayChooser;
      /*  MyRenderer render = new MyRenderer(); //выравнивание текста в таблице по центру
        for (int i = 0; i < jTable2.getColumnModel().getColumnCount(); i++){
            jTable2.getColumnModel().getColumn(i).setCellRenderer(render);
        }*/
        jLabel_tur_num.setVisible(false);
        monthReport();
    }
    private void setProp(File file){
        Properties properties = Cfg.getProperties(file);
        if (properties == null) properties = Cfg.getProperties(new File("default.cfg"));
        if (properties == null) {
            JOptionPane.showMessageDialog(null, "Отсутсвуют файлы настройки.\nПрограмма будет закрыта.");
            System.exit(0);
        }
        
        db_user = properties.getProperty("db_user");
        db_pass = properties.getProperty("db_pass");
        db_host = properties.getProperty("db_host");
        db_base = properties.getProperty("db_base");
        
        dbs_user = db_user;
        dbs_pass = db_pass;
        dbs_host = db_host;
        dbs_base = db_base;
        
        sms_to = properties.getProperty("sms_to");
        sms_from = properties.getProperty("sms_from");
        sms_smtp = properties.getProperty("sms_smtp");
        sms_smtp_user = properties.getProperty("sms_smtp_user");
        sms_smtp_pass = properties.getProperty("sms_smtp_pass");
        sms_spam_login = properties.getProperty("sms_spam_login");
        sms_spam_pass = properties.getProperty("sms_spam_pass");
        
        mail_from = properties.getProperty("mail_from");
        mail_smtp = properties.getProperty("mail_smtp");
        mail_smtp_user = properties.getProperty("mail_smtp_user");
        mail_smtp_pass = properties.getProperty("mail_smtp_pass");
    }
    
    private void monthReport(){
        
        Date date = new Date();
        int d = date.getDate(); // 1 - 31
        int m = date.getMonth(); //0 - 11
        int y = date.getYear();
        //if (d != 1) return;
       
        String date_s = reFormateDateForSQL(new Date(y, m-1, 1));
        String date_po = reFormateDateForSQL(new Date(y, m, 0));
        
        File reportLastMonth = new File("Отчеты/Ежемесячный отчет/"+ reFormateDate(new Date(y, m-1, 1),"yyyy.MM(MMMM)") + ".xls");
        if (reportLastMonth.exists()) return;
        
        DefaultTableModel model = new DefaultTableModel();
        jTable_report.setModel(model);
        model.addColumn("№");
        model.addColumn("Фамилия");
        model.addColumn("Телефон");
        model.addColumn("Email");
        model.addColumn("Дата поездки");
        model.addColumn("Стоимость");
        model.addColumn("Туроператор");
        model.addColumn("Продавец");
        model.addColumn("Покупатель");
        model.addColumn("Тур");
        model.addColumn("Откуда");
        model.addColumn("Пункт");
        model.addColumn("Менеджер");
        model.addColumn("Дата продажи");
       
        String query = "SELECT * FROM main WHERE DATE(sale_date) >= " + date_s + " AND DATE(sale_date) <= " + date_po;
        ResultSet rs = db.executeQuery(query);
        try {
            int size = 0;
            while(rs.next()) {
                model.addRow(new Object[14]);
                jTable_report.setValueAt(size+1, size, 0);
                jTable_report.setValueAt(rs.getString("f_fam") + " " + rs.getString("f_name") + " " + rs.getString("f_otche"), size, 1);
                jTable_report.setValueAt(rs.getString("phone_dom") + ", " + rs.getString("phone_rab") + ", " + rs.getString("phone_mob"), size, 2);
                jTable_report.setValueAt(rs.getString("email"), size, 3);
                jTable_report.setValueAt(reFormateDateForTable(rs.getString("tur_date_s")) + " - " + reFormateDateForTable(rs.getString("tur_date_po")), size, 4);
                jTable_report.setValueAt(rs.getString("last_price"), size, 5);
                jTable_report.setValueAt(rs.getString("turoperator"), size, 6);
                jTable_report.setValueAt(rs.getString("prodavets"), size, 7);
                jTable_report.setValueAt(rs.getString("pokupatel"), size, 8);
                jTable_report.setValueAt(rs.getString("tur_name"), size, 9);
                jTable_report.setValueAt(rs.getString("tur_begin"), size, 10);
                jTable_report.setValueAt(rs.getString("tur_punkt"), size, 11);
                jTable_report.setValueAt(rs.getString("seller"), size, 12);
                jTable_report.setValueAt(rs.getString("sale_date"), size, 13);
                size++;
            }
            int price_all = 0;
            for(int i=0; i<model.getRowCount(); i++){
                price_all += Integer.parseInt(jTable_report.getValueAt(i, 5).toString());
            }
            model.addRow(new Object[14]);
            for(int i=0; i<14; i++){ 
                jTable_report.setValueAt("", size, i);
            }
            jTable_report.setValueAt("Сумма: " + price_all, size, 5);
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
        
       // String today = reFormateDate(new Date(), "yyyy.MM.dd");
        ExcelExporter excel = new ExcelExporter();
        //excel.fillData(jTable_report, new File("Отчеты/Ежемесячный отчет/"+ y + "." + m +".xls"));
        excel.fillData(jTable_report, reportLastMonth);
    }
    

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFrame_new = new javax.swing.JFrame();
        jPanel5 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jTextField_f_otche = new javax.swing.JTextField();
        jTextField_f_name = new javax.swing.JTextField();
        jTextField_f_fam = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jTextField_f_passport = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTextArea_f_address = new javax.swing.JTextArea();
        jLabel13 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jTextField_phone_dom = new javax.swing.JTextField();
        jTextField_phone_rab = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField_phone_mob = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextField_email = new javax.swing.JTextField();
        jCheckBox_sms = new javax.swing.JCheckBox();
        jCheckBox_spam = new javax.swing.JCheckBox();
        jPanel6 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        jTextField_tur1_fio = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        jDateChooser_tur1_bd = new com.toedter.calendar.JDateChooser();
        jLabel22 = new javax.swing.JLabel();
        jTextField_tur1_adr = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        jTextField_tur2_fio = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        jDateChooser_tur2_bd = new com.toedter.calendar.JDateChooser();
        jTextField_tur2_adr = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jTextField_tur3_fio = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        jDateChooser_tur3_bd = new com.toedter.calendar.JDateChooser();
        jTextField_tur3_adr = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jTextField_tur4_fio = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        jDateChooser_tur4_bd = new com.toedter.calendar.JDateChooser();
        jTextField_tur4_adr = new javax.swing.JTextField();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jTextField_tur5_fio = new javax.swing.JTextField();
        jLabel33 = new javax.swing.JLabel();
        jDateChooser_tur5_bd = new com.toedter.calendar.JDateChooser();
        jTextField_tur5_adr = new javax.swing.JTextField();
        jLabel34 = new javax.swing.JLabel();
        jLabel55 = new javax.swing.JLabel();
        jTextField_tur1_passport = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel56 = new javax.swing.JLabel();
        jTextField_tur2_passport = new javax.swing.JTextField();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel57 = new javax.swing.JLabel();
        jTextField_tur3_passport = new javax.swing.JTextField();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel58 = new javax.swing.JLabel();
        jTextField_tur4_passport = new javax.swing.JTextField();
        jSeparator4 = new javax.swing.JSeparator();
        jLabel59 = new javax.swing.JLabel();
        jTextField_tur5_passport = new javax.swing.JTextField();
        jLabel71 = new javax.swing.JLabel();
        jTextField_tur1_price = new javax.swing.JTextField();
        jLabel72 = new javax.swing.JLabel();
        jTextField_tur2_price = new javax.swing.JTextField();
        jLabel73 = new javax.swing.JLabel();
        jTextField_tur3_price = new javax.swing.JTextField();
        jLabel74 = new javax.swing.JLabel();
        jTextField_tur4_price = new javax.swing.JTextField();
        jLabel75 = new javax.swing.JLabel();
        jTextField_tur5_price = new javax.swing.JTextField();
        jPanel7 = new javax.swing.JPanel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jDateChooser_tur_date_po = new com.toedter.calendar.JDateChooser();
        jDateChooser_tur_date_s = new com.toedter.calendar.JDateChooser();
        jPanel8 = new javax.swing.JPanel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jTextField_tur_begin = new javax.swing.JTextField();
        jTextField_tur_punkt = new javax.swing.JTextField();
        jTextField_tur_end = new javax.swing.JTextField();
        jPanel9 = new javax.swing.JPanel();
        jLabel40 = new javax.swing.JLabel();
        jComboBox_bilet_cat = new javax.swing.JComboBox();
        jLabel41 = new javax.swing.JLabel();
        jTextField_h_name = new javax.swing.JTextField();
        jLabel42 = new javax.swing.JLabel();
        jComboBox_h_nomer = new javax.swing.JComboBox();
        jLabel43 = new javax.swing.JLabel();
        jComboBox_food = new javax.swing.JComboBox();
        jLabel44 = new javax.swing.JLabel();
        jComboBox_transfer = new javax.swing.JComboBox();
        jCheckBox_visa = new javax.swing.JCheckBox();
        jCheckBox_strah = new javax.swing.JCheckBox();
        jCheckBox_heal = new javax.swing.JCheckBox();
        jSeparator5 = new javax.swing.JSeparator();
        jLabel45 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTextArea_excurs = new javax.swing.JTextArea();
        jLabel46 = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTextArea_service = new javax.swing.JTextArea();
        jCheckBox_gid = new javax.swing.JCheckBox();
        jSeparator7 = new javax.swing.JSeparator();
        jPanel10 = new javax.swing.JPanel();
        jLabel47 = new javax.swing.JLabel();
        jTextField_price = new javax.swing.JTextField();
        jLabel_price_word = new javax.swing.JLabel();
        jLabel49 = new javax.swing.JLabel();
        jDateChooser_sale_date = new com.toedter.calendar.JDateChooser();
        jLabel50 = new javax.swing.JLabel();
        jComboBox_seller = new javax.swing.JComboBox();
        jCheckBox_price = new javax.swing.JCheckBox();
        jLabel64 = new javax.swing.JLabel();
        jComboBox_procent = new javax.swing.JComboBox();
        jLabel67 = new javax.swing.JLabel();
        jTextField_last_price = new javax.swing.JTextField();
        jLabel_last_price_word = new javax.swing.JLabel();
        jSeparator6 = new javax.swing.JSeparator();
        jSeparator8 = new javax.swing.JSeparator();
        jPanel_dogovor = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel60 = new javax.swing.JLabel();
        jComboBox_prodavets = new javax.swing.JComboBox();
        jLabel62 = new javax.swing.JLabel();
        jComboBox_turoperator = new javax.swing.JComboBox();
        jComboBox_tur_name = new javax.swing.JComboBox();
        jLabel63 = new javax.swing.JLabel();
        jComboBox_pokupatel = new javax.swing.JComboBox();
        jPanel14 = new javax.swing.JPanel();
        jButton_new_dogovor = new javax.swing.JButton();
        jButton_new_prilozhenie = new javax.swing.JButton();
        jButton_new_putevka = new javax.swing.JButton();
        jButton_new_pko = new javax.swing.JButton();
        jButton_new_fortuna = new javax.swing.JButton();
        jLabel_tur_num = new javax.swing.JLabel();
        jButton_new_edit = new javax.swing.JButton();
        jLabel_edited_count = new javax.swing.JLabel();
        jButton_new_copy = new javax.swing.JButton();
        jButton_new_schet = new javax.swing.JButton();
        jButton_new_podtverzhdenie = new javax.swing.JButton();
        jButton_new_pko_a = new javax.swing.JButton();
        jButton_new_save = new javax.swing.JButton();
        jButton_new_close = new javax.swing.JButton();
        jFrame_client = new javax.swing.JFrame();
        jPanel4 = new javax.swing.JPanel();
        jLabel_id1 = new javax.swing.JLabel();
        jLabel_data1 = new javax.swing.JLabel();
        jLabel_name1 = new javax.swing.JLabel();
        jLabel_fam1 = new javax.swing.JLabel();
        jLabel_otche1 = new javax.swing.JLabel();
        jLabel_price1 = new javax.swing.JLabel();
        jTextField_name1 = new javax.swing.JTextField();
        jTextField_fam1 = new javax.swing.JTextField();
        jTextField_otche1 = new javax.swing.JTextField();
        jTextField_price1 = new javax.swing.JTextField();
        jFormattedTextField_date1 = new javax.swing.JFormattedTextField();
        jTextField_id1 = new javax.swing.JTextField();
        jButton_client_close = new javax.swing.JButton();
        jButton_client_save = new javax.swing.JButton();
        jButton_edit = new javax.swing.JButton();
        jButton_print = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jFrame_mail = new javax.swing.JFrame();
        jLabel_mail_to = new javax.swing.JLabel();
        jTextField_mail_to = new javax.swing.JTextField();
        jLabel_mail_subject = new javax.swing.JLabel();
        jTextField_mail_subject = new javax.swing.JTextField();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextPane_msg = new javax.swing.JTextPane();
        jButton_mail_start = new javax.swing.JButton();
        jButton_mail_send = new javax.swing.JButton();
        jScrollPane9 = new javax.swing.JScrollPane();
        jTable_spam = new javax.swing.JTable();
        jButton_mail_suspend = new javax.swing.JButton();
        jButton_mail_resume = new javax.swing.JButton();
        jButton_mail_stop = new javax.swing.JButton();
        jProgressBar_mail = new javax.swing.JProgressBar();
        jLabel_mail_process_status = new javax.swing.JLabel();
        jFrame_sms = new javax.swing.JFrame();
        jLabel48 = new javax.swing.JLabel();
        jTextField_sms_nomer = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea_sms_message = new javax.swing.JTextArea();
        jLabel_sms_length = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable_sms = new javax.swing.JTable();
        jLabel_sms_progress = new javax.swing.JLabel();
        jButton_sms_send = new javax.swing.JButton();
        jButton_sms_start = new javax.swing.JButton();
        jButton_new_close1 = new javax.swing.JButton();
        jFrame_search = new javax.swing.JFrame();
        jScrollPane10 = new javax.swing.JScrollPane();
        jTable_search = new javax.swing.JTable();
        jTextField_search = new javax.swing.JTextField();
        jComboBox_search_field = new javax.swing.JComboBox();
        jButton_search_search = new javax.swing.JButton();
        jComboBox_search_FIO = new javax.swing.JComboBox();
        jButton_new_close2 = new javax.swing.JButton();
        jFrame_report = new javax.swing.JFrame();
        jPanel13 = new javax.swing.JPanel();
        jDateChooser_report_s = new com.toedter.calendar.JDateChooser();
        jDateChooser_report_po = new com.toedter.calendar.JDateChooser();
        jLabel52 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jCheckBox_r_fam = new javax.swing.JCheckBox();
        jCheckBox_r_phone = new javax.swing.JCheckBox();
        jCheckBox_r_mail = new javax.swing.JCheckBox();
        jCheckBox_r_price = new javax.swing.JCheckBox();
        jCheckBox_r_manager = new javax.swing.JCheckBox();
        jComboBox_r_manager = new javax.swing.JComboBox();
        jComboBox_r_tur_punkt = new javax.swing.JComboBox();
        jCheckBox_r_tur_punkt = new javax.swing.JCheckBox();
        jCheckBox_r_tur_begin = new javax.swing.JCheckBox();
        jCheckBox_r_tur_name = new javax.swing.JCheckBox();
        jCheckBox_r_prodavets = new javax.swing.JCheckBox();
        jCheckBox_r_turoperator = new javax.swing.JCheckBox();
        jComboBox_r_turoperator = new javax.swing.JComboBox();
        jComboBox_r_prodavets = new javax.swing.JComboBox();
        jComboBox_r_tur_name = new javax.swing.JComboBox();
        jComboBox_r_tur_begin = new javax.swing.JComboBox();
        jButton_report = new javax.swing.JButton();
        jCheckBox_r_all = new javax.swing.JCheckBox();
        jDateChooser_r_tur_date_s = new com.toedter.calendar.JDateChooser();
        jCheckBox_r_tur_data = new javax.swing.JCheckBox();
        jCheckBox_r_pokupatel = new javax.swing.JCheckBox();
        jComboBox_r_pokupatel = new javax.swing.JComboBox();
        jLabel76 = new javax.swing.JLabel();
        jButton_new_close3 = new javax.swing.JButton();
        jFrame_happy = new javax.swing.JFrame();
        jScrollPane11 = new javax.swing.JScrollPane();
        jTable_happy = new javax.swing.JTable();
        jButton_happy_sms = new javax.swing.JButton();
        jButton_happy_mail = new javax.swing.JButton();
        jButton_happy_client = new javax.swing.JButton();
        jButton_new_close4 = new javax.swing.JButton();
        jFrame_prodavets = new javax.swing.JFrame();
        jComboBox_opt_prodavets = new javax.swing.JComboBox();
        jButton_prodavets_new = new javax.swing.JButton();
        jTabbedPane_prodavets = new javax.swing.JTabbedPane();
        jPanel11 = new javax.swing.JPanel();
        jScrollPane12 = new javax.swing.JScrollPane();
        jTable_prodavets = new javax.swing.JTable();
        jButton_prodavets_edit = new javax.swing.JButton();
        jButton_prodavets_save = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton_opt_prodavets_cancel = new javax.swing.JButton();
        jPanel16 = new javax.swing.JPanel();
        jButton_manager_add = new javax.swing.JButton();
        jScrollPane13 = new javax.swing.JScrollPane();
        jTable_managers = new javax.swing.JTable();
        jTextField_manager = new javax.swing.JTextField();
        jTextField_manager_dog = new javax.swing.JTextField();
        jButton_new_close5 = new javax.swing.JButton();
        jFrame_turoperator = new javax.swing.JFrame();
        jComboBox_opt_turoperator = new javax.swing.JComboBox();
        jButton_turoperator_new = new javax.swing.JButton();
        jScrollPane14 = new javax.swing.JScrollPane();
        jTable_opt_turoperator = new javax.swing.JTable();
        jButton12 = new javax.swing.JButton();
        jButton_turoperator_edit = new javax.swing.JButton();
        jButton_turoperator_save = new javax.swing.JButton();
        jButton_opt_turoperator_cancel = new javax.swing.JButton();
        jButton_new_close6 = new javax.swing.JButton();
        jFrame_options = new javax.swing.JFrame();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jTextField_db_user = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextField_db_pass = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jTextField_db_host = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jTextField_db_base = new javax.swing.JTextField();
        jPanel12 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jTextField_mail_smtp_user = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jTextField_mail_smtp_pass = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jTextField_mail_smtp = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jTextField_mail_from = new javax.swing.JTextField();
        jPanel15 = new javax.swing.JPanel();
        jLabel65 = new javax.swing.JLabel();
        jTextField_sms_smtp_user = new javax.swing.JTextField();
        jTextField_sms_smtp_pass = new javax.swing.JTextField();
        jLabel66 = new javax.swing.JLabel();
        jLabel68 = new javax.swing.JLabel();
        jTextField_sms_smtp = new javax.swing.JTextField();
        jTextField_sms_to = new javax.swing.JTextField();
        jLabel69 = new javax.swing.JLabel();
        jLabel51 = new javax.swing.JLabel();
        jTextField_sms_from = new javax.swing.JTextField();
        jLabel53 = new javax.swing.JLabel();
        jTextField_sms_spam_login = new javax.swing.JTextField();
        jLabel61 = new javax.swing.JLabel();
        jTextField_sms_spam_pass = new javax.swing.JTextField();
        jButton_prop_save = new javax.swing.JButton();
        jButton_prop_default = new javax.swing.JButton();
        jButton_new_close10 = new javax.swing.JButton();
        jFrame_report_table = new javax.swing.JFrame();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable_report = new javax.swing.JTable();
        jButton_save_Excel = new javax.swing.JButton();
        jCheckBox_report_open = new javax.swing.JCheckBox();
        jButton_new_close7 = new javax.swing.JButton();
        jFrame_turs = new javax.swing.JFrame();
        jComboBox_opt_turs = new javax.swing.JComboBox();
        jButton_opt_turs_add = new javax.swing.JButton();
        jScrollPane15 = new javax.swing.JScrollPane();
        jTable_turs = new javax.swing.JTable();
        jButton_opt_turs_edit = new javax.swing.JButton();
        jButton_opt_turs_save = new javax.swing.JButton();
        jButton_opt_turs_cancel = new javax.swing.JButton();
        jButton_new_close8 = new javax.swing.JButton();
        jFrame_turagent = new javax.swing.JFrame();
        jComboBox_opt_turagent = new javax.swing.JComboBox();
        jButton_turagent_new = new javax.swing.JButton();
        jScrollPane16 = new javax.swing.JScrollPane();
        jTable_opt_turagent = new javax.swing.JTable();
        jButton13 = new javax.swing.JButton();
        jButton_turagent_edit = new javax.swing.JButton();
        jButton_turagent_save = new javax.swing.JButton();
        jButton_opt_turagent_cancel = new javax.swing.JButton();
        jButton_new_close9 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jDayChooser = new com.toedter.calendar.JDayChooser();
        jPanel3 = new javax.swing.JPanel();
        jButton_new = new javax.swing.JButton();
        jButton_search = new javax.swing.JButton();
        jButton_otchet = new javax.swing.JButton();
        jButton_sms = new javax.swing.JButton();
        jButton_email = new javax.swing.JButton();
        jButton_update = new javax.swing.JButton();
        jButton_happy = new javax.swing.JButton();
        jScrollPane8 = new javax.swing.JScrollPane();
        jTable_main = new javax.swing.JTable();
        jButton_new_close11 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem_opt_prodavets = new javax.swing.JMenuItem();
        jMenuItem_opt_turagent = new javax.swing.JMenuItem();
        jMenuItem_opt_turoperator = new javax.swing.JMenuItem();
        jMenuItem_opt_turs = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();

        jFrame_new.setTitle("Форма клиента");
        jFrame_new.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jFrame_new.setExtendedState(6);
        jFrame_new.setLocationByPlatform(true);
        jFrame_new.setMinimumSize(new java.awt.Dimension(925, 800));
        jFrame_new.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                jFrame_newComponentHidden(evt);
            }
        });

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Покупатель"));

        jLabel14.setText("Фамилия: ");

        jLabel15.setText("Имя: ");

        jLabel16.setText("Отчество: ");

        jTextField_f_otche.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                jTextField_f_otcheCaretUpdate(evt);
            }
        });
        jTextField_f_otche.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextField_f_otcheFocusLost(evt);
            }
        });

        jTextField_f_name.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                jTextField_f_nameCaretUpdate(evt);
            }
        });
        jTextField_f_name.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextField_f_nameFocusLost(evt);
            }
        });

        jTextField_f_fam.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                jTextField_f_famCaretUpdate(evt);
            }
        });
        jTextField_f_fam.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextField_f_famFocusLost(evt);
            }
        });

        jLabel17.setText("Паспорт: ");

        jTextField_f_passport.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                jTextField_f_passportCaretUpdate(evt);
            }
        });
        jTextField_f_passport.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextField_f_passportFocusLost(evt);
            }
        });

        jLabel18.setText("Адрес: ");

        jScrollPane5.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jTextArea_f_address.setColumns(20);
        jTextArea_f_address.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        jTextArea_f_address.setRows(4);
        jTextArea_f_address.setAutoscrolls(false);
        jTextArea_f_address.setMaximumSize(new java.awt.Dimension(164, 60));
        jTextArea_f_address.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                jTextArea_f_addressCaretUpdate(evt);
            }
        });
        jTextArea_f_address.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextArea_f_addressFocusLost(evt);
            }
        });
        jScrollPane5.setViewportView(jTextArea_f_address);

        jLabel13.setText("Телефон (дом.)");

        jLabel19.setText("Телефон (раб.)");

        jLabel4.setText("Телефон (сот.)");

        jLabel5.setText("E-mail:");

        jCheckBox_sms.setToolTipText("Включить в SMS рассылку");

        jCheckBox_spam.setToolTipText("Включить в Email рассылку");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel15, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField_f_fam, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField_f_name, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField_f_otche, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField_f_passport, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField_phone_dom, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField_phone_rab, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jTextField_phone_mob, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jCheckBox_sms))))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField_email, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jCheckBox_spam)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(jTextField_f_fam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(jTextField_f_name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(jTextField_f_otche, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(jTextField_f_passport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel18)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(jTextField_phone_dom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(jTextField_phone_rab, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox_sms)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel4)
                        .addComponent(jTextField_phone_mob, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox_spam)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel5)
                        .addComponent(jTextField_email, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Список туристов:"));

        jLabel20.setText("1: ");

        jLabel21.setText("Дата рождения:");

        jLabel22.setText("Адрес:");

        jLabel23.setText("2: ");

        jLabel24.setText("Дата рождения:");

        jLabel25.setText("Адрес:");

        jLabel26.setText("3: ");

        jLabel27.setText("Дата рождения:");

        jLabel28.setText("Адрес:");

        jLabel29.setText("4: ");

        jLabel30.setText("Дата рождения:");

        jLabel31.setText("Адрес:");

        jLabel32.setText("5: ");

        jLabel33.setText("Дата рождения:");

        jLabel34.setText("Адрес:");

        jLabel55.setText("Паспорт:");

        jLabel56.setText("Паспорт:");

        jLabel57.setText("Паспорт:");

        jLabel58.setText("Паспорт:");

        jLabel59.setText("Паспорт:");

        jLabel71.setText("Цена:");

        jTextField_tur1_price.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                jTextField_tur1_priceCaretUpdate(evt);
            }
        });

        jLabel72.setText("Цена:");

        jTextField_tur2_price.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                jTextField_tur2_priceCaretUpdate(evt);
            }
        });

        jLabel73.setText("Цена:");

        jTextField_tur3_price.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                jTextField_tur3_priceCaretUpdate(evt);
            }
        });

        jLabel74.setText("Цена:");

        jTextField_tur4_price.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                jTextField_tur4_priceCaretUpdate(evt);
            }
        });

        jLabel75.setText("Цена:");

        jTextField_tur5_price.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                jTextField_tur5_priceCaretUpdate(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator4)
                    .addComponent(jSeparator3)
                    .addComponent(jSeparator2)
                    .addComponent(jSeparator1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel20)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel6Layout.createSequentialGroup()
                                        .addComponent(jLabel21)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jDateChooser_tur1_bd, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jTextField_tur1_fio, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(1, 1, 1))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel6Layout.createSequentialGroup()
                                    .addComponent(jLabel22)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jTextField_tur1_adr, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel6Layout.createSequentialGroup()
                                    .addComponent(jLabel55)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jTextField_tur1_passport, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel6Layout.createSequentialGroup()
                                    .addComponent(jLabel71)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(jTextField_tur1_price, javax.swing.GroupLayout.DEFAULT_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel6Layout.createSequentialGroup()
                                    .addGap(20, 20, 20)
                                    .addComponent(jLabel56)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jTextField_tur2_passport, javax.swing.GroupLayout.DEFAULT_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel6Layout.createSequentialGroup()
                                    .addComponent(jLabel23)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel6Layout.createSequentialGroup()
                                            .addComponent(jLabel25)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(jTextField_tur2_adr, javax.swing.GroupLayout.DEFAULT_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel6Layout.createSequentialGroup()
                                            .addComponent(jLabel24)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(jDateChooser_tur2_bd, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(jTextField_tur2_fio, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel72)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jTextField_tur2_price, javax.swing.GroupLayout.DEFAULT_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel6Layout.createSequentialGroup()
                                    .addGap(20, 20, 20)
                                    .addComponent(jLabel57)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jTextField_tur3_passport, javax.swing.GroupLayout.DEFAULT_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel6Layout.createSequentialGroup()
                                    .addComponent(jLabel26)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jTextField_tur3_fio, javax.swing.GroupLayout.DEFAULT_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(jPanel6Layout.createSequentialGroup()
                                            .addComponent(jLabel28)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(jTextField_tur3_adr, javax.swing.GroupLayout.DEFAULT_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel6Layout.createSequentialGroup()
                                            .addComponent(jLabel27)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(jDateChooser_tur3_bd, javax.swing.GroupLayout.DEFAULT_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel73)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jTextField_tur3_price, javax.swing.GroupLayout.DEFAULT_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel6Layout.createSequentialGroup()
                                    .addGap(20, 20, 20)
                                    .addComponent(jLabel58)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jTextField_tur4_passport, javax.swing.GroupLayout.DEFAULT_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel6Layout.createSequentialGroup()
                                    .addComponent(jLabel29)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jTextField_tur4_fio, javax.swing.GroupLayout.DEFAULT_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(jPanel6Layout.createSequentialGroup()
                                            .addComponent(jLabel31)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(jTextField_tur4_adr, javax.swing.GroupLayout.DEFAULT_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel6Layout.createSequentialGroup()
                                            .addComponent(jLabel30)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(jDateChooser_tur4_bd, javax.swing.GroupLayout.DEFAULT_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel74)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jTextField_tur4_price, javax.swing.GroupLayout.DEFAULT_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel6Layout.createSequentialGroup()
                                    .addGap(20, 20, 20)
                                    .addComponent(jLabel59)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jTextField_tur5_passport, javax.swing.GroupLayout.DEFAULT_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel6Layout.createSequentialGroup()
                                        .addComponent(jLabel75)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jTextField_tur5_price, javax.swing.GroupLayout.DEFAULT_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel6Layout.createSequentialGroup()
                                        .addComponent(jLabel32)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jTextField_tur5_fio, javax.swing.GroupLayout.DEFAULT_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(jPanel6Layout.createSequentialGroup()
                                                .addComponent(jLabel34)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jTextField_tur5_adr, javax.swing.GroupLayout.DEFAULT_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel6Layout.createSequentialGroup()
                                                .addComponent(jLabel33)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jDateChooser_tur5_bd, javax.swing.GroupLayout.DEFAULT_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)))))))))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(jTextField_tur1_fio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel21)
                    .addComponent(jDateChooser_tur1_bd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel55)
                    .addComponent(jTextField_tur1_passport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(jTextField_tur1_adr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel71)
                    .addComponent(jTextField_tur1_price, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(jTextField_tur2_fio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel24)
                    .addComponent(jDateChooser_tur2_bd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel56)
                    .addComponent(jTextField_tur2_passport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(jTextField_tur2_adr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel72)
                    .addComponent(jTextField_tur2_price, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26)
                    .addComponent(jTextField_tur3_fio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel27)
                    .addComponent(jDateChooser_tur3_bd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel57)
                    .addComponent(jTextField_tur3_passport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28)
                    .addComponent(jTextField_tur3_adr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel73)
                    .addComponent(jTextField_tur3_price, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29)
                    .addComponent(jTextField_tur4_fio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel30)
                    .addComponent(jDateChooser_tur4_bd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel58)
                    .addComponent(jTextField_tur4_passport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel31)
                    .addComponent(jTextField_tur4_adr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel74)
                    .addComponent(jTextField_tur4_price, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel32)
                    .addComponent(jTextField_tur5_fio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel33)
                    .addComponent(jDateChooser_tur5_bd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel59)
                    .addComponent(jTextField_tur5_passport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel34)
                    .addComponent(jTextField_tur5_adr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel75)
                    .addComponent(jTextField_tur5_price, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Продолжительность поездки"));

        jLabel35.setText("с:");

        jLabel36.setText("по:");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel35)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jDateChooser_tur_date_s, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel36)
                .addGap(12, 12, 12)
                .addComponent(jDateChooser_tur_date_po, javax.swing.GroupLayout.DEFAULT_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jDateChooser_tur_date_po, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel35)
                    .addComponent(jDateChooser_tur_date_s, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel36))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Маршрут пездки и страна (пункт) пребывания"));

        jLabel37.setText("Начало маршрута:");

        jLabel38.setText("Пункт пребывания:");

        jLabel39.setText("Окончание маршрута:");

        jTextField_tur_begin.setText("Тюмень");
        jTextField_tur_begin.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                jTextField_tur_beginCaretUpdate(evt);
            }
        });

        jTextField_tur_end.setText("Тюмень");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel37, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel39, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel38, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField_tur_punkt, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField_tur_end, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField_tur_begin, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel37)
                    .addComponent(jTextField_tur_begin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel38)
                    .addComponent(jTextField_tur_punkt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel39)
                    .addComponent(jTextField_tur_end, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel40.setText("Категория проездного билета:");

        jComboBox_bilet_cat.setEditable(true);
        jComboBox_bilet_cat.setMaximumRowCount(10);
        jComboBox_bilet_cat.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "полный", "плацкарт", "купе", "общий", "эконом", "I класс", "II класс", "бизнес", "автобус" }));

        jLabel41.setText("Гостиница:");

        jLabel42.setText("Номер:");

        jComboBox_h_nomer.setEditable(true);
        jComboBox_h_nomer.setMaximumRowCount(10);
        jComboBox_h_nomer.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "SGL", "DBL", "TRPL", "1-но местн.", "2-х местн.", "3-х местн.", "апартаменты", "другое" }));

        jLabel43.setText("Питание:");

        jComboBox_food.setEditable(true);
        jComboBox_food.setMaximumRowCount(10);
        jComboBox_food.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "BB", "HB", "FB", "ALL", "завтрак", "2-х разовое", "3-х разовое", "без питания", "другое" }));

        jLabel44.setText("Трансфер:");

        jComboBox_transfer.setEditable(true);
        jComboBox_transfer.setMaximumRowCount(10);
        jComboBox_transfer.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Групповой", "Индивидуальный", "Нет" }));

        jCheckBox_visa.setText("Виза     ");
        jCheckBox_visa.setFocusPainted(false);

        jCheckBox_strah.setText("Страховка");
        jCheckBox_strah.setFocusPainted(false);

        jCheckBox_heal.setText("Лечение  ");
        jCheckBox_heal.setFocusPainted(false);

        jLabel45.setText("Экскурсионная программа:");

        jScrollPane6.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jTextArea_excurs.setColumns(20);
        jTextArea_excurs.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        jTextArea_excurs.setRows(5);
        jScrollPane6.setViewportView(jTextArea_excurs);

        jLabel46.setText("Дополнительно оплаченные услуги:");

        jScrollPane7.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jTextArea_service.setColumns(20);
        jTextArea_service.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        jTextArea_service.setRows(4);
        jScrollPane7.setViewportView(jTextArea_service);

        jCheckBox_gid.setText("Наличие руководителя группы туристов");
        jCheckBox_gid.setFocusPainted(false);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator5)
                    .addComponent(jSeparator7)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jCheckBox_visa)
                        .addGap(18, 18, 18)
                        .addComponent(jCheckBox_strah)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jCheckBox_heal))
                    .addComponent(jCheckBox_gid, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel9Layout.createSequentialGroup()
                                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLabel44, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel43, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel41, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel42, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextField_h_name, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jComboBox_h_nomer, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jComboBox_food, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jComboBox_transfer, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel9Layout.createSequentialGroup()
                                .addComponent(jLabel40)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox_bilet_cat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel45)
                            .addComponent(jLabel46))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel40)
                    .addComponent(jComboBox_bilet_cat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel41)
                    .addComponent(jTextField_h_name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel42)
                    .addComponent(jComboBox_h_nomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel43)
                    .addComponent(jComboBox_food, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel44)
                    .addComponent(jComboBox_transfer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox_gid)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox_visa)
                    .addComponent(jCheckBox_strah)
                    .addComponent(jCheckBox_heal))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel45)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel46)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel47.setText("Стоимость путевки:");

        jTextField_price.setText("0");
        jTextField_price.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                jTextField_priceCaretUpdate(evt);
            }
        });

        jLabel_price_word.setText(" ");

        jLabel49.setText("Дата продажи:");

        jDateChooser_sale_date.setDate(new Date());
        jDateChooser_sale_date.setDateFormatString("d MMMM yyyy 'г.'");

        jLabel50.setText("Менеджер:");

        jComboBox_seller.setMaximumRowCount(10);
        jComboBox_seller.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Сошников Е.Н." }));

        jCheckBox_price.setToolTipText("Ввести стоимость вручную");
        jCheckBox_price.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_priceActionPerformed(evt);
            }
        });

        jLabel64.setText("%");

        jComboBox_procent.setMaximumRowCount(11);
        jComboBox_procent.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" }));
        jComboBox_procent.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox_procentItemStateChanged(evt);
            }
        });

        jLabel67.setText("=");

        jTextField_last_price.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                jTextField_last_priceCaretUpdate(evt);
            }
        });

        jLabel_last_price_word.setText(" ");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel50, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel49, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jDateChooser_sale_date, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox_seller, 0, 198, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel10Layout.createSequentialGroup()
                                .addComponent(jLabel47)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField_price, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBox_price))
                            .addGroup(jPanel10Layout.createSequentialGroup()
                                .addComponent(jLabel64)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox_procent, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel67)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField_last_price, javax.swing.GroupLayout.DEFAULT_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel_last_price_word, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSeparator8, javax.swing.GroupLayout.DEFAULT_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel_price_word, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel47)
                        .addComponent(jTextField_price, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jCheckBox_price))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel_price_word)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel64)
                    .addComponent(jComboBox_procent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel67)
                    .addComponent(jTextField_last_price, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel_last_price_word)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel49)
                    .addComponent(jDateChooser_sale_date, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel50)
                    .addComponent(jComboBox_seller, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel_dogovor.setBorder(javax.swing.BorderFactory.createTitledBorder("№ договора: ___"));

        jLabel6.setText("Тур: ");

        jLabel60.setText("Продавец: ");

        jComboBox_prodavets.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox_prodavets.setEnabled(false);
        jComboBox_prodavets.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox_prodavetsItemStateChanged(evt);
            }
        });

        jLabel62.setText("Туроператор:");

        jComboBox_turoperator.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jComboBox_tur_name.setEditable(true);
        jComboBox_tur_name.setEnabled(false);
        jComboBox_tur_name.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox_tur_nameItemStateChanged(evt);
            }
        });

        jLabel63.setText("Покупатель:");

        jComboBox_pokupatel.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Турист" }));
        jComboBox_pokupatel.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox_pokupatelItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel_dogovorLayout = new javax.swing.GroupLayout(jPanel_dogovor);
        jPanel_dogovor.setLayout(jPanel_dogovorLayout);
        jPanel_dogovorLayout.setHorizontalGroup(
            jPanel_dogovorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_dogovorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel_dogovorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel_dogovorLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel60, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox_prodavets, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel_dogovorLayout.createSequentialGroup()
                        .addGroup(jPanel_dogovorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel62, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel_dogovorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBox_tur_name, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox_turoperator, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel_dogovorLayout.createSequentialGroup()
                        .addComponent(jLabel63)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jComboBox_pokupatel, 0, 222, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel_dogovorLayout.setVerticalGroup(
            jPanel_dogovorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_dogovorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel_dogovorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jComboBox_prodavets, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel60, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel_dogovorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jComboBox_pokupatel)
                    .addComponent(jLabel63, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel_dogovorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jComboBox_turoperator)
                    .addComponent(jLabel62, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel_dogovorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jComboBox_tur_name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel14.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButton_new_dogovor.setText("Договор");
        jButton_new_dogovor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_new_dogovorActionPerformed(evt);
            }
        });

        jButton_new_prilozhenie.setText("Приложение");
        jButton_new_prilozhenie.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_new_prilozhenieActionPerformed(evt);
            }
        });

        jButton_new_putevka.setText("Путевка");
        jButton_new_putevka.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_new_putevkaActionPerformed(evt);
            }
        });

        jButton_new_pko.setText("ПКО");
        jButton_new_pko.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_new_pkoActionPerformed(evt);
            }
        });

        jButton_new_fortuna.setText("Фортуна");
        jButton_new_fortuna.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_new_fortunaActionPerformed(evt);
            }
        });

        jLabel_tur_num.setText("...");

        jButton_new_edit.setText("Изменить");
        jButton_new_edit.setEnabled(false);
        jButton_new_edit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_new_editActionPerformed(evt);
            }
        });

        jLabel_edited_count.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel_edited_count.setText("0");

        jButton_new_copy.setText("Копировать");
        jButton_new_copy.setEnabled(false);
        jButton_new_copy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_new_copyActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jButton_new_dogovor)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_new_prilozhenie)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton_new_pko)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton_new_putevka)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton_new_fortuna)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel_tur_num, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(43, 43, 43)
                .addComponent(jLabel_edited_count, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_new_edit)
                .addGap(18, 18, 18)
                .addComponent(jButton_new_copy)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jButton_new_edit)
                .addComponent(jLabel_edited_count)
                .addComponent(jButton_new_copy))
            .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jButton_new_dogovor)
                .addComponent(jButton_new_prilozhenie)
                .addComponent(jButton_new_pko)
                .addComponent(jButton_new_putevka)
                .addComponent(jButton_new_fortuna)
                .addComponent(jLabel_tur_num))
        );

        jButton_new_schet.setText("Счет");
        jButton_new_schet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_new_schetActionPerformed(evt);
            }
        });

        jButton_new_podtverzhdenie.setText("Подтверждение");
        jButton_new_podtverzhdenie.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_new_podtverzhdenieActionPerformed(evt);
            }
        });

        jButton_new_pko_a.setText("ПКО/А");
        jButton_new_pko_a.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_new_pko_aActionPerformed(evt);
            }
        });

        jButton_new_save.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Turist/save_f2.png"))); // NOI18N
        jButton_new_save.setText("Сохранить");
        jButton_new_save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_new_saveActionPerformed(evt);
            }
        });
        jButton_new_save.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jButton_new_savePropertyChange(evt);
            }
        });

        jButton_new_close.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Turist/cancel_f2.png"))); // NOI18N
        jButton_new_close.setText("Закрыть");
        jButton_new_close.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_new_closeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jFrame_newLayout = new javax.swing.GroupLayout(jFrame_new.getContentPane());
        jFrame_new.getContentPane().setLayout(jFrame_newLayout);
        jFrame_newLayout.setHorizontalGroup(
            jFrame_newLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrame_newLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFrame_newLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jFrame_newLayout.createSequentialGroup()
                        .addGroup(jFrame_newLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel_dogovor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jFrame_newLayout.createSequentialGroup()
                                .addGap(16, 16, 16)
                                .addComponent(jButton_new_schet)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton_new_podtverzhdenie)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton_new_pko_a)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jFrame_newLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jFrame_newLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jFrame_newLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jPanel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jPanel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(jFrame_newLayout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addComponent(jButton_new_save)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton_new_close))))
                    .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jFrame_newLayout.setVerticalGroup(
            jFrame_newLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrame_newLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFrame_newLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jFrame_newLayout.createSequentialGroup()
                        .addGroup(jFrame_newLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jFrame_newLayout.createSequentialGroup()
                                .addComponent(jPanel_dogovor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jFrame_newLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jButton_new_schet)
                                    .addComponent(jButton_new_podtverzhdenie)
                                    .addComponent(jButton_new_pko_a)))
                            .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(jFrame_newLayout.createSequentialGroup()
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jFrame_newLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton_new_save)
                            .addComponent(jButton_new_close))
                        .addGap(18, 18, 18)))
                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(33, Short.MAX_VALUE))
        );

        jFrame_client.setTitle("Карточка клиента");
        jFrame_client.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jFrame_client.setExtendedState(6);
        jFrame_client.setLocationByPlatform(true);
        jFrame_client.setMinimumSize(new java.awt.Dimension(700, 350));

        jLabel_id1.setText("id");

        jLabel_data1.setText("Дата");

        jLabel_name1.setText("Имя");

        jLabel_fam1.setText("Фамилия");

        jLabel_otche1.setText("Отчество");

        jLabel_price1.setText("Цена");

        jTextField_name1.setEditable(false);

        jTextField_fam1.setEditable(false);

        jTextField_otche1.setEditable(false);

        jTextField_price1.setEditable(false);

        jFormattedTextField_date1.setEditable(false);
        jFormattedTextField_date1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(new java.text.SimpleDateFormat("yyyy-MM-dd"))));

        jTextField_id1.setEditable(false);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel_otche1)
                                .addComponent(jLabel_fam1, javax.swing.GroupLayout.Alignment.TRAILING))
                            .addComponent(jLabel_name1, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addComponent(jLabel_data1, javax.swing.GroupLayout.Alignment.TRAILING))
                    .addComponent(jLabel_price1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel_id1, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextField_id1)
                    .addComponent(jTextField_name1)
                    .addComponent(jTextField_fam1)
                    .addComponent(jTextField_otche1)
                    .addComponent(jTextField_price1)
                    .addComponent(jFormattedTextField_date1, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_id1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_id1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel_data1)
                    .addComponent(jFormattedTextField_date1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_name1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_name1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_fam1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_fam1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_otche1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_otche1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_price1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_price1))
                .addContainerGap(54, Short.MAX_VALUE))
        );

        jButton_client_close.setText("Закрыть");
        jButton_client_close.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_client_closeActionPerformed(evt);
            }
        });

        jButton_client_save.setText("Сохранить");
        jButton_client_save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_client_saveActionPerformed(evt);
            }
        });

        jButton_edit.setText("Редактировать");

        jButton_print.setText("Печать");
        jButton_print.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_printActionPerformed(evt);
            }
        });

        jButton2.setText("jButton2");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jFrame_clientLayout = new javax.swing.GroupLayout(jFrame_client.getContentPane());
        jFrame_client.getContentPane().setLayout(jFrame_clientLayout);
        jFrame_clientLayout.setHorizontalGroup(
            jFrame_clientLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrame_clientLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFrame_clientLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jFrame_clientLayout.createSequentialGroup()
                        .addComponent(jButton_edit)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton_client_save)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton_client_close)
                        .addGap(68, 68, 68)
                        .addComponent(jButton2))
                    .addGroup(jFrame_clientLayout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton_print)))
                .addContainerGap(262, Short.MAX_VALUE))
        );
        jFrame_clientLayout.setVerticalGroup(
            jFrame_clientLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrame_clientLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFrame_clientLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jFrame_clientLayout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jFrame_clientLayout.createSequentialGroup()
                        .addComponent(jButton_print)
                        .addGap(29, 29, 29)))
                .addGroup(jFrame_clientLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton_client_save)
                    .addComponent(jButton_client_close)
                    .addComponent(jButton_edit)
                    .addComponent(jButton2))
                .addContainerGap(33, Short.MAX_VALUE))
        );

        jFrame_mail.setTitle("Email рассылка");
        jFrame_mail.setExtendedState(6);
        jFrame_mail.setLocationByPlatform(true);
        jFrame_mail.setMinimumSize(new java.awt.Dimension(730, 360));

        jLabel_mail_to.setText("Кому:");

        jLabel_mail_subject.setText("Тема:");

        jScrollPane4.setViewportView(jTextPane_msg);

        jButton_mail_start.setText("Запустить");
        jButton_mail_start.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_mail_startActionPerformed(evt);
            }
        });

        jButton_mail_send.setText("Отправить");
        jButton_mail_send.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_mail_sendActionPerformed(evt);
            }
        });

        jTable_spam.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "email", "Статус"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane9.setViewportView(jTable_spam);

        jButton_mail_suspend.setText("Остановить");
        jButton_mail_suspend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_mail_suspendActionPerformed(evt);
            }
        });

        jButton_mail_resume.setText("Продолжить");
        jButton_mail_resume.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_mail_resumeActionPerformed(evt);
            }
        });

        jButton_mail_stop.setText("Прервать");
        jButton_mail_stop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_mail_stopActionPerformed(evt);
            }
        });

        jProgressBar_mail.setStringPainted(true);

        jLabel_mail_process_status.setText("---");
        jLabel_mail_process_status.setMaximumSize(new java.awt.Dimension(15, 14));
        jLabel_mail_process_status.setMinimumSize(new java.awt.Dimension(15, 14));
        jLabel_mail_process_status.setPreferredSize(new java.awt.Dimension(15, 14));

        javax.swing.GroupLayout jFrame_mailLayout = new javax.swing.GroupLayout(jFrame_mail.getContentPane());
        jFrame_mail.getContentPane().setLayout(jFrame_mailLayout);
        jFrame_mailLayout.setHorizontalGroup(
            jFrame_mailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrame_mailLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFrame_mailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jFrame_mailLayout.createSequentialGroup()
                        .addGroup(jFrame_mailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jFrame_mailLayout.createSequentialGroup()
                                .addComponent(jLabel_mail_subject)
                                .addGap(18, 18, 18)
                                .addComponent(jTextField_mail_subject))
                            .addGroup(jFrame_mailLayout.createSequentialGroup()
                                .addComponent(jLabel_mail_to)
                                .addGap(18, 18, 18)
                                .addComponent(jTextField_mail_to, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))
                            .addComponent(jScrollPane4))
                        .addGap(10, 10, 10)
                        .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jFrame_mailLayout.createSequentialGroup()
                        .addComponent(jButton_mail_send)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton_mail_start)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton_mail_suspend)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton_mail_resume)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton_mail_stop)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel_mail_process_status, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jProgressBar_mail, javax.swing.GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jFrame_mailLayout.setVerticalGroup(
            jFrame_mailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrame_mailLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFrame_mailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jFrame_mailLayout.createSequentialGroup()
                        .addGroup(jFrame_mailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel_mail_to)
                            .addComponent(jTextField_mail_to, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jFrame_mailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel_mail_subject)
                            .addComponent(jTextField_mail_subject, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE))
                    .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jFrame_mailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton_mail_start, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton_mail_send, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton_mail_suspend, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton_mail_resume, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton_mail_stop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jProgressBar_mail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_mail_process_status, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jFrame_sms.setTitle("SMS рассылка");
        jFrame_sms.setExtendedState(6);
        jFrame_sms.setLocationByPlatform(true);
        jFrame_sms.setMinimumSize(new java.awt.Dimension(375, 300));
        jFrame_sms.setPreferredSize(new java.awt.Dimension(375, 300));

        jLabel48.setText("Тел.:");

        jTextArea_sms_message.setColumns(20);
        jTextArea_sms_message.setRows(5);
        jTextArea_sms_message.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                jTextArea_sms_messageCaretUpdate(evt);
            }
        });
        jScrollPane1.setViewportView(jTextArea_sms_message);

        jLabel_sms_length.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel_sms_length.setText(" 70/0");

        jTable_sms.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Телефон:"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane3.setViewportView(jTable_sms);

        jLabel_sms_progress.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel_sms_progress.setText(" ");

        jButton_sms_send.setText("Отправить");
        jButton_sms_send.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_sms_sendActionPerformed(evt);
            }
        });

        jButton_sms_start.setText("Запустить");
        jButton_sms_start.setToolTipText("");
        jButton_sms_start.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_sms_startActionPerformed(evt);
            }
        });

        jButton_new_close1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Turist/cancel_f2.png"))); // NOI18N
        jButton_new_close1.setText("Закрыть");
        jButton_new_close1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_new_close1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jFrame_smsLayout = new javax.swing.GroupLayout(jFrame_sms.getContentPane());
        jFrame_sms.getContentPane().setLayout(jFrame_smsLayout);
        jFrame_smsLayout.setHorizontalGroup(
            jFrame_smsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrame_smsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFrame_smsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jButton_sms_send, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jFrame_smsLayout.createSequentialGroup()
                        .addComponent(jLabel48)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField_sms_nomer, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel_sms_length, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jFrame_smsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jLabel_sms_progress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton_sms_start, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jButton_new_close1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jFrame_smsLayout.setVerticalGroup(
            jFrame_smsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrame_smsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFrame_smsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton_new_close1)
                    .addGroup(jFrame_smsLayout.createSequentialGroup()
                        .addGroup(jFrame_smsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jFrame_smsLayout.createSequentialGroup()
                                .addGroup(jFrame_smsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel48)
                                    .addComponent(jTextField_sms_nomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jFrame_smsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel_sms_length)
                            .addComponent(jLabel_sms_progress))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jFrame_smsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton_sms_send)
                            .addComponent(jButton_sms_start))))
                .addContainerGap(58, Short.MAX_VALUE))
        );

        jFrame_sms.getAccessibleContext().setAccessibleParent(this);

        jFrame_search.setTitle("Поиск");
        jFrame_search.setExtendedState(6);
        jFrame_search.setLocationByPlatform(true);
        jFrame_search.setMinimumSize(new java.awt.Dimension(1000, 500));

        jTable_search.setAutoCreateRowSorter(true);
        jTable_search.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "№ договора", "Туроператор", "Покупатель", "Название тура", "ФИО", "Дата покупки", "Цена", "Начало тура", "Окончание тура"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable_search.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable_searchMouseClicked(evt);
            }
        });
        jScrollPane10.setViewportView(jTable_search);

        jComboBox_search_field.setMaximumRowCount(12);
        jComboBox_search_field.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Показать все", "№ договра", "Туроператор", "Название тура", "Фамилия", "Имя", "Отчество", "Паспрт", "Телефон (дом.)", "Телефон (раб.)", "Телефон (моб.)", "Email" }));

        jButton_search_search.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Turist/search.png"))); // NOI18N
        jButton_search_search.setText("Найти");
        jButton_search_search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_search_searchActionPerformed(evt);
            }
        });

        jComboBox_search_FIO.setMaximumRowCount(25);
        jComboBox_search_FIO.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox_search_FIOItemStateChanged(evt);
            }
        });

        jButton_new_close2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Turist/cancel_f2.png"))); // NOI18N
        jButton_new_close2.setText("Закрыть");
        jButton_new_close2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_new_close2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jFrame_searchLayout = new javax.swing.GroupLayout(jFrame_search.getContentPane());
        jFrame_search.getContentPane().setLayout(jFrame_searchLayout);
        jFrame_searchLayout.setHorizontalGroup(
            jFrame_searchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrame_searchLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFrame_searchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 982, Short.MAX_VALUE)
                    .addGroup(jFrame_searchLayout.createSequentialGroup()
                        .addGroup(jFrame_searchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jFrame_searchLayout.createSequentialGroup()
                                .addComponent(jComboBox_search_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField_search))
                            .addGroup(jFrame_searchLayout.createSequentialGroup()
                                .addComponent(jComboBox_search_FIO, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton_search_search))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jFrame_searchLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton_new_close2)))
                .addContainerGap())
        );
        jFrame_searchLayout.setVerticalGroup(
            jFrame_searchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jFrame_searchLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFrame_searchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton_search_search)
                    .addGroup(jFrame_searchLayout.createSequentialGroup()
                        .addGroup(jFrame_searchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField_search, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox_search_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox_search_FIO, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_new_close2)
                .addContainerGap())
        );

        jFrame_report.setTitle("Отчет");
        jFrame_report.setExtendedState(6);
        jFrame_report.setLocationByPlatform(true);
        jFrame_report.setMinimumSize(new java.awt.Dimension(490, 350));

        jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder("Выберите период и тип отчета"));

        jLabel52.setText("С:");

        jLabel54.setText("По:");

        jButton1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButton1.setText("X");
        jButton1.setMargin(new java.awt.Insets(1, 1, 1, 1));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jCheckBox_r_fam.setSelected(true);
        jCheckBox_r_fam.setText("Фамилия");

        jCheckBox_r_phone.setText("Телефон");

        jCheckBox_r_mail.setText("Email");

        jCheckBox_r_price.setSelected(true);
        jCheckBox_r_price.setText("Стоимость");

        jCheckBox_r_manager.setText("Менеджер");
        jCheckBox_r_manager.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_r_managerActionPerformed(evt);
            }
        });

        jComboBox_r_manager.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Все" }));
        jComboBox_r_manager.setEnabled(false);

        jComboBox_r_tur_punkt.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Все" }));
        jComboBox_r_tur_punkt.setEnabled(false);

        jCheckBox_r_tur_punkt.setText("Пункт");
        jCheckBox_r_tur_punkt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_r_tur_punktActionPerformed(evt);
            }
        });

        jCheckBox_r_tur_begin.setText("Начало");
        jCheckBox_r_tur_begin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_r_tur_beginActionPerformed(evt);
            }
        });

        jCheckBox_r_tur_name.setText("Тур");
        jCheckBox_r_tur_name.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_r_tur_nameActionPerformed(evt);
            }
        });

        jCheckBox_r_prodavets.setText("Продавец");
        jCheckBox_r_prodavets.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_r_prodavetsActionPerformed(evt);
            }
        });

        jCheckBox_r_turoperator.setText("Труоператор");
        jCheckBox_r_turoperator.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_r_turoperatorActionPerformed(evt);
            }
        });

        jComboBox_r_turoperator.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Все" }));
        jComboBox_r_turoperator.setEnabled(false);

        jComboBox_r_prodavets.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Все" }));
        jComboBox_r_prodavets.setEnabled(false);

        jComboBox_r_tur_name.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Все" }));
        jComboBox_r_tur_name.setEnabled(false);

        jComboBox_r_tur_begin.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Все" }));
        jComboBox_r_tur_begin.setEnabled(false);

        jButton_report.setText("Сформировать");
        jButton_report.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_reportActionPerformed(evt);
            }
        });

        jCheckBox_r_all.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jCheckBox_r_all.setText("Выбрать всех");
        jCheckBox_r_all.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_r_allActionPerformed(evt);
            }
        });

        jDateChooser_r_tur_date_s.setEnabled(false);

        jCheckBox_r_tur_data.setText("Дата поездки");
        jCheckBox_r_tur_data.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_r_tur_dataActionPerformed(evt);
            }
        });

        jCheckBox_r_pokupatel.setText("Покупатель");
        jCheckBox_r_pokupatel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_r_pokupatelActionPerformed(evt);
            }
        });

        jComboBox_r_pokupatel.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Все" }));
        jComboBox_r_pokupatel.setEnabled(false);

        jLabel76.setText("С");

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel13Layout.createSequentialGroup()
                        .addComponent(jLabel52, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jDateChooser_report_s, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel54)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jDateChooser_report_po, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton_report, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel13Layout.createSequentialGroup()
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jCheckBox_r_mail, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jCheckBox_r_price, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                                .addComponent(jCheckBox_r_phone, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jCheckBox_r_fam, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jCheckBox_r_all, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jCheckBox_r_tur_data)
                            .addGroup(jPanel13Layout.createSequentialGroup()
                                .addComponent(jLabel76)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jDateChooser_r_tur_date_s, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(5, 5, 5)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel13Layout.createSequentialGroup()
                                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jCheckBox_r_prodavets, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jCheckBox_r_turoperator))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jComboBox_r_prodavets, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jComboBox_r_turoperator, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(jPanel13Layout.createSequentialGroup()
                                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jCheckBox_r_manager, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jCheckBox_r_tur_punkt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jCheckBox_r_tur_begin, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jCheckBox_r_tur_name, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jCheckBox_r_pokupatel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jComboBox_r_pokupatel, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jComboBox_r_tur_begin, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jComboBox_r_tur_punkt, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jComboBox_r_manager, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jComboBox_r_tur_name, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))))
                .addContainerGap())
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton_report))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel13Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jDateChooser_report_po, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jDateChooser_report_s, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel52, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel54, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(17, 17, 17)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox_r_turoperator)
                    .addComponent(jComboBox_r_turoperator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox_r_all))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox_r_prodavets)
                    .addComponent(jComboBox_r_prodavets, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox_r_fam))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox_r_phone)
                    .addComponent(jCheckBox_r_tur_name)
                    .addComponent(jComboBox_r_tur_name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox_r_mail)
                    .addComponent(jCheckBox_r_tur_begin)
                    .addComponent(jComboBox_r_tur_begin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox_r_tur_punkt)
                    .addComponent(jComboBox_r_tur_punkt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox_r_price))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jCheckBox_r_manager)
                        .addComponent(jComboBox_r_manager, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jCheckBox_r_tur_data))
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel76, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jDateChooser_r_tur_date_s, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jCheckBox_r_pokupatel)
                            .addComponent(jComboBox_r_pokupatel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButton_new_close3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Turist/cancel_f2.png"))); // NOI18N
        jButton_new_close3.setText("Закрыть");
        jButton_new_close3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_new_close3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jFrame_reportLayout = new javax.swing.GroupLayout(jFrame_report.getContentPane());
        jFrame_report.getContentPane().setLayout(jFrame_reportLayout);
        jFrame_reportLayout.setHorizontalGroup(
            jFrame_reportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrame_reportLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFrame_reportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jFrame_reportLayout.createSequentialGroup()
                        .addGap(311, 311, 311)
                        .addComponent(jButton_new_close3)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jFrame_reportLayout.setVerticalGroup(
            jFrame_reportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrame_reportLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_new_close3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jFrame_happy.setTitle("Список именинников");
        jFrame_happy.setExtendedState(6);
        jFrame_happy.setLocationByPlatform(true);
        jFrame_happy.setMinimumSize(new java.awt.Dimension(400, 300));

        jTable_happy.setAutoCreateRowSorter(true);
        jTable_happy.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "№ договора", "Дата", "ФИО", "SMS", "Email"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane11.setViewportView(jTable_happy);

        jButton_happy_sms.setText("SMS");
        jButton_happy_sms.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_happy_smsActionPerformed(evt);
            }
        });

        jButton_happy_mail.setText("Email");
        jButton_happy_mail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_happy_mailActionPerformed(evt);
            }
        });

        jButton_happy_client.setText("Карточка клиента");
        jButton_happy_client.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_happy_clientActionPerformed(evt);
            }
        });

        jButton_new_close4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Turist/cancel_f2.png"))); // NOI18N
        jButton_new_close4.setText("Закрыть");
        jButton_new_close4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_new_close4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jFrame_happyLayout = new javax.swing.GroupLayout(jFrame_happy.getContentPane());
        jFrame_happy.getContentPane().setLayout(jFrame_happyLayout);
        jFrame_happyLayout.setHorizontalGroup(
            jFrame_happyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jFrame_happyLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFrame_happyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jFrame_happyLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton_new_close4))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jFrame_happyLayout.createSequentialGroup()
                        .addComponent(jButton_happy_client)
                        .addGap(101, 101, 101)
                        .addComponent(jButton_happy_sms)
                        .addGap(18, 18, 18)
                        .addComponent(jButton_happy_mail)
                        .addGap(0, 50, Short.MAX_VALUE))
                    .addComponent(jScrollPane11, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(15, 15, 15))
        );
        jFrame_happyLayout.setVerticalGroup(
            jFrame_happyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jFrame_happyLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFrame_happyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton_happy_sms)
                    .addComponent(jButton_happy_mail)
                    .addComponent(jButton_happy_client))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton_new_close4)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jFrame_prodavets.setTitle("Продавцы");
        jFrame_prodavets.setExtendedState(6);
        jFrame_prodavets.setLocationByPlatform(true);
        jFrame_prodavets.setMinimumSize(new java.awt.Dimension(410, 450));
        jFrame_prodavets.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                jFrame_prodavetsComponentHidden(evt);
            }
        });

        jComboBox_opt_prodavets.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox_opt_prodavets.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox_opt_prodavetsItemStateChanged(evt);
            }
        });

        jButton_prodavets_new.setText("Новый");
        jButton_prodavets_new.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_prodavets_newActionPerformed(evt);
            }
        });

        jTable_prodavets.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Название", null},
                {"Полное название", null},
                {"ИНН", null},
                {"КПП", null},
                {"ОГРН", null},
                {"Юридический адрес", null},
                {"Фактический адрес", null},
                {"ОКВЭД", null},
                {"ОКПО", null},
                {"ОКАТО", null},
                {"ОКТМО", null},
                {"ОКОГУ", null},
                {"ОКФС", null},
                {"ОКОПФ", null},
                {"Телефон", null},
                {"Факс", null},
                {"E-mail", null},
                {"Директор", null},
                {"Директор для договора", null},
                {"Расчетный счет", null},
                {"Наименование банка", null},
                {"Корреспондентский счет", null},
                {"БИК", null}
            },
            new String [] {
                "Поле", "Данные"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable_prodavets.setColumnSelectionAllowed(true);
        jTable_prodavets.getTableHeader().setReorderingAllowed(false);
        jScrollPane12.setViewportView(jTable_prodavets);
        jTable_prodavets.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jTable_prodavets.getColumnModel().getColumn(0).setMinWidth(150);
        jTable_prodavets.getColumnModel().getColumn(0).setPreferredWidth(150);
        jTable_prodavets.getColumnModel().getColumn(0).setMaxWidth(150);

        jButton_prodavets_edit.setText("Изменить");
        jButton_prodavets_edit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_prodavets_editActionPerformed(evt);
            }
        });

        jButton_prodavets_save.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Turist/save_f2.png"))); // NOI18N
        jButton_prodavets_save.setText("Сохранить");
        jButton_prodavets_save.setEnabled(false);
        jButton_prodavets_save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_prodavets_saveActionPerformed(evt);
            }
        });

        jButton10.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jButton10.setText("Удалить");
        jButton10.setEnabled(false);
        jButton10.setMargin(new java.awt.Insets(1, 1, 1, 1));

        jButton_opt_prodavets_cancel.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jButton_opt_prodavets_cancel.setText("Отменить");
        jButton_opt_prodavets_cancel.setMargin(new java.awt.Insets(1, 1, 1, 1));
        jButton_opt_prodavets_cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_opt_prodavets_cancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton10)
                .addGap(18, 18, 18)
                .addComponent(jButton_opt_prodavets_cancel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 186, Short.MAX_VALUE)
                .addComponent(jButton_prodavets_edit)
                .addGap(18, 18, 18)
                .addComponent(jButton_prodavets_save)
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addComponent(jScrollPane12, javax.swing.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton_prodavets_edit)
                        .addComponent(jButton_prodavets_save))
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton10)
                            .addComponent(jButton_opt_prodavets_cancel))))
                .addGap(23, 23, 23))
        );

        jTabbedPane_prodavets.addTab("Реквизиты", jPanel11);

        jButton_manager_add.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jButton_manager_add.setText("+");
        jButton_manager_add.setMargin(new java.awt.Insets(1, 1, 1, 1));
        jButton_manager_add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_manager_addActionPerformed(evt);
            }
        });

        jTable_managers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Ф.И.О.", "Ф.И.О. для договра"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable_managers.setColumnSelectionAllowed(true);
        jTable_managers.getTableHeader().setReorderingAllowed(false);
        jScrollPane13.setViewportView(jTable_managers);
        jTable_managers.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane13, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 528, Short.MAX_VALUE)
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addComponent(jTextField_manager, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField_manager_dog, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton_manager_add, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField_manager, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField_manager_dog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton_manager_add))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane_prodavets.addTab("Менеджеры", jPanel16);

        jButton_new_close5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Turist/cancel_f2.png"))); // NOI18N
        jButton_new_close5.setText("Закрыть");
        jButton_new_close5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_new_close5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jFrame_prodavetsLayout = new javax.swing.GroupLayout(jFrame_prodavets.getContentPane());
        jFrame_prodavets.getContentPane().setLayout(jFrame_prodavetsLayout);
        jFrame_prodavetsLayout.setHorizontalGroup(
            jFrame_prodavetsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrame_prodavetsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFrame_prodavetsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jFrame_prodavetsLayout.createSequentialGroup()
                        .addComponent(jComboBox_opt_prodavets, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton_prodavets_new))
                    .addComponent(jTabbedPane_prodavets)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jFrame_prodavetsLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton_new_close5)))
                .addContainerGap())
        );
        jFrame_prodavetsLayout.setVerticalGroup(
            jFrame_prodavetsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrame_prodavetsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFrame_prodavetsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox_opt_prodavets, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton_prodavets_new))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTabbedPane_prodavets, javax.swing.GroupLayout.PREFERRED_SIZE, 513, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton_new_close5)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        jFrame_turoperator.setTitle("Туроператоры");
        jFrame_turoperator.setExtendedState(6);
        jFrame_turoperator.setLocationByPlatform(true);
        jFrame_turoperator.setMinimumSize(new java.awt.Dimension(450, 380));
        jFrame_turoperator.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                jFrame_turoperatorComponentHidden(evt);
            }
        });

        jComboBox_opt_turoperator.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox_opt_turoperator.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox_opt_turoperatorItemStateChanged(evt);
            }
        });

        jButton_turoperator_new.setText("Добавить");
        jButton_turoperator_new.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_turoperator_newActionPerformed(evt);
            }
        });

        jTable_opt_turoperator.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Название", null},
                {"Полное название", null},
                {"Реестровый номер", null},
                {"Юридический адрес", null},
                {"Почтовый адрес", null},
                {"ОГРН", null},
                {"ИНН", null},
                {"Способ фин. обеспечения", null},
                {"Размер фин. обеспечения", null},
                {"Договор", null},
                {"Срок действия фин. обеспечения", null},
                {"Организация, пред-шей фин. обеспечение", null},
                {"Адрес организации, пред-шей фин. обеспечение", null}
            },
            new String [] {
                "Поля", "Данные"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable_opt_turoperator.setColumnSelectionAllowed(true);
        jTable_opt_turoperator.getTableHeader().setReorderingAllowed(false);
        jScrollPane14.setViewportView(jTable_opt_turoperator);
        jTable_opt_turoperator.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        jButton12.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jButton12.setText("Удалить");
        jButton12.setEnabled(false);
        jButton12.setMargin(new java.awt.Insets(1, 1, 1, 1));

        jButton_turoperator_edit.setText("Изменить");
        jButton_turoperator_edit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_turoperator_editActionPerformed(evt);
            }
        });

        jButton_turoperator_save.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Turist/save_f2.png"))); // NOI18N
        jButton_turoperator_save.setText("Сохранить");
        jButton_turoperator_save.setEnabled(false);
        jButton_turoperator_save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_turoperator_saveActionPerformed(evt);
            }
        });

        jButton_opt_turoperator_cancel.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jButton_opt_turoperator_cancel.setText("Отменить");
        jButton_opt_turoperator_cancel.setMargin(new java.awt.Insets(1, 1, 1, 1));
        jButton_opt_turoperator_cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_opt_turoperator_cancelActionPerformed(evt);
            }
        });

        jButton_new_close6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Turist/cancel_f2.png"))); // NOI18N
        jButton_new_close6.setText("Закрыть");
        jButton_new_close6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_new_close6ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jFrame_turoperatorLayout = new javax.swing.GroupLayout(jFrame_turoperator.getContentPane());
        jFrame_turoperator.getContentPane().setLayout(jFrame_turoperatorLayout);
        jFrame_turoperatorLayout.setHorizontalGroup(
            jFrame_turoperatorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrame_turoperatorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFrame_turoperatorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jFrame_turoperatorLayout.createSequentialGroup()
                        .addComponent(jComboBox_opt_turoperator, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton_turoperator_new))
                    .addComponent(jScrollPane14, javax.swing.GroupLayout.DEFAULT_SIZE, 642, Short.MAX_VALUE)
                    .addGroup(jFrame_turoperatorLayout.createSequentialGroup()
                        .addComponent(jButton12)
                        .addGap(18, 18, 18)
                        .addComponent(jButton_opt_turoperator_cancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton_turoperator_edit)
                        .addGap(18, 18, 18)
                        .addComponent(jButton_turoperator_save)
                        .addGap(18, 18, 18)
                        .addComponent(jButton_new_close6)))
                .addContainerGap())
        );
        jFrame_turoperatorLayout.setVerticalGroup(
            jFrame_turoperatorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrame_turoperatorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFrame_turoperatorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox_opt_turoperator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton_turoperator_new))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 268, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jFrame_turoperatorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton_turoperator_edit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton_turoperator_save, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton_opt_turoperator_cancel)
                    .addComponent(jButton_new_close6))
                .addGap(127, 127, 127))
        );

        jFrame_options.setTitle("Настройки");
        jFrame_options.setExtendedState(6);
        jFrame_options.setLocationByPlatform(true);
        jFrame_options.setMinimumSize(new java.awt.Dimension(370, 360));

        jLabel2.setText("User");

        jTextField_db_user.setText("root");
        jTextField_db_user.setToolTipText("");

        jLabel3.setText("Password");

        jTextField_db_pass.setText("root");
        jTextField_db_pass.setToolTipText("");

        jLabel7.setText("Host");

        jTextField_db_host.setText("127.0.0.1:3306");
        jTextField_db_host.setToolTipText("");

        jLabel8.setText("Base");

        jTextField_db_base.setText("foton");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField_db_pass)
                    .addComponent(jTextField_db_host, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
                    .addComponent(jTextField_db_base)
                    .addComponent(jTextField_db_user, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextField_db_user, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextField_db_pass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jTextField_db_host, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jTextField_db_base, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(116, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("База данных", jPanel2);

        jLabel9.setText("User");

        jTextField_mail_smtp_user.setText("foton@mozevil.ru");

        jLabel10.setText("Password");

        jTextField_mail_smtp_pass.setText("foton");

        jLabel11.setText("SMTP");

        jTextField_mail_smtp.setText("mail.mozevil.ru");

        jLabel12.setText("From");

        jTextField_mail_from.setText("foton@mozevil.ru");

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField_mail_smtp_pass)
                    .addComponent(jTextField_mail_smtp)
                    .addComponent(jTextField_mail_from, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
                    .addComponent(jTextField_mail_smtp_user, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jTextField_mail_smtp_user, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jTextField_mail_smtp_pass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jTextField_mail_smtp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jTextField_mail_from, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(116, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Email", jPanel12);

        jLabel65.setText("User");

        jTextField_sms_smtp_user.setText("foton@mozevil.ru");

        jTextField_sms_smtp_pass.setText("foton");

        jLabel66.setText("Password");

        jLabel68.setText("SMTP");

        jTextField_sms_smtp.setText("mail.mozevil.ru");

        jTextField_sms_to.setText("test@mozevil.ru");

        jLabel69.setText("To");

        jLabel51.setText("From");

        jTextField_sms_from.setText("foton@mozevil.ru");

        jLabel53.setText("SMSC login");

        jTextField_sms_spam_login.setText("логин СМС центр");

        jLabel61.setText("SMSC pass");

        jTextField_sms_spam_pass.setText("пароль СМС центр");

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel66, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel65, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel68, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel69, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel51, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField_sms_smtp_pass)
                            .addComponent(jTextField_sms_smtp)
                            .addComponent(jTextField_sms_to, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
                            .addComponent(jTextField_sms_smtp_user, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jTextField_sms_from)))
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addComponent(jLabel53)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextField_sms_spam_login))
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addComponent(jLabel61)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextField_sms_spam_pass)))
                .addContainerGap())
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel65)
                    .addComponent(jTextField_sms_smtp_user, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel66)
                    .addComponent(jTextField_sms_smtp_pass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel68)
                    .addComponent(jTextField_sms_smtp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel69)
                    .addComponent(jTextField_sms_to, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel51)
                    .addComponent(jTextField_sms_from, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel53)
                    .addComponent(jTextField_sms_spam_login, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel61)
                    .addComponent(jTextField_sms_spam_pass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("SMS", jPanel15);

        jButton_prop_save.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Turist/save_f2.png"))); // NOI18N
        jButton_prop_save.setText("Сохранить");
        jButton_prop_save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_prop_saveActionPerformed(evt);
            }
        });

        jButton_prop_default.setText("По умолчанию");
        jButton_prop_default.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_prop_defaultActionPerformed(evt);
            }
        });

        jButton_new_close10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Turist/cancel_f2.png"))); // NOI18N
        jButton_new_close10.setText("Закрыть");
        jButton_new_close10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_new_close10ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jFrame_optionsLayout = new javax.swing.GroupLayout(jFrame_options.getContentPane());
        jFrame_options.getContentPane().setLayout(jFrame_optionsLayout);
        jFrame_optionsLayout.setHorizontalGroup(
            jFrame_optionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrame_optionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFrame_optionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1)
                    .addGroup(jFrame_optionsLayout.createSequentialGroup()
                        .addComponent(jButton_prop_default)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton_prop_save)
                        .addGap(18, 18, 18)
                        .addComponent(jButton_new_close10)))
                .addContainerGap())
        );
        jFrame_optionsLayout.setVerticalGroup(
            jFrame_optionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrame_optionsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 268, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jFrame_optionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton_prop_default)
                    .addComponent(jButton_prop_save)
                    .addComponent(jButton_new_close10))
                .addContainerGap(106, Short.MAX_VALUE))
        );

        jFrame_report_table.setExtendedState(6);
        jFrame_report_table.setLocationByPlatform(true);
        jFrame_report_table.setMinimumSize(new java.awt.Dimension(700, 400));

        jTable_report.setAutoCreateRowSorter(true);
        jTable_report.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "№"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane2.setViewportView(jTable_report);

        jButton_save_Excel.setText("Сохранить в Excel");
        jButton_save_Excel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_save_ExcelActionPerformed(evt);
            }
        });

        jCheckBox_report_open.setSelected(true);
        jCheckBox_report_open.setText("Открыть после сохранения");

        jButton_new_close7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Turist/cancel_f2.png"))); // NOI18N
        jButton_new_close7.setText("Закрыть");
        jButton_new_close7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_new_close7ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jFrame_report_tableLayout = new javax.swing.GroupLayout(jFrame_report_table.getContentPane());
        jFrame_report_table.getContentPane().setLayout(jFrame_report_tableLayout);
        jFrame_report_tableLayout.setHorizontalGroup(
            jFrame_report_tableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrame_report_tableLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFrame_report_tableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 619, Short.MAX_VALUE)
                    .addGroup(jFrame_report_tableLayout.createSequentialGroup()
                        .addComponent(jButton_save_Excel)
                        .addGap(18, 18, 18)
                        .addComponent(jCheckBox_report_open)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jFrame_report_tableLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton_new_close7)))
                .addContainerGap())
        );
        jFrame_report_tableLayout.setVerticalGroup(
            jFrame_report_tableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jFrame_report_tableLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFrame_report_tableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton_save_Excel)
                    .addComponent(jCheckBox_report_open))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jButton_new_close7)
                .addGap(31, 31, 31))
        );

        jFrame_turs.setTitle("Туры");
        jFrame_turs.setExtendedState(6);
        jFrame_turs.setLocationByPlatform(true);
        jFrame_turs.setMinimumSize(new java.awt.Dimension(400, 410));
        jFrame_turs.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                jFrame_tursComponentHidden(evt);
            }
        });

        jComboBox_opt_turs.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox_opt_turs.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox_opt_tursItemStateChanged(evt);
            }
        });

        jButton_opt_turs_add.setText("Новый");
        jButton_opt_turs_add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_opt_turs_addActionPerformed(evt);
            }
        });

        jTable_turs.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Туроператор", null},
                {"Название тура", null},
                {"Начало", null},
                {"Пункт", null},
                {"Конец", null},
                {"Билет", null},
                {"Гостиница", null},
                {"Номер", null},
                {"Питание", null},
                {"Трансфер", null},
                {"Гид", null},
                {"Виза", null},
                {"Страховка", null},
                {"Лечение", null},
                {"Экскурсии", null},
                {"Доп. услуги", null}
            },
            new String [] {
                "Поля", "Данные"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable_turs.setColumnSelectionAllowed(true);
        jScrollPane15.setViewportView(jTable_turs);
        jTable_turs.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        jButton_opt_turs_edit.setText("Изменить");
        jButton_opt_turs_edit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_opt_turs_editActionPerformed(evt);
            }
        });

        jButton_opt_turs_save.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Turist/save_f2.png"))); // NOI18N
        jButton_opt_turs_save.setText("Сохранить");
        jButton_opt_turs_save.setEnabled(false);
        jButton_opt_turs_save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_opt_turs_saveActionPerformed(evt);
            }
        });

        jButton_opt_turs_cancel.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jButton_opt_turs_cancel.setText("Отменить");
        jButton_opt_turs_cancel.setMargin(new java.awt.Insets(1, 1, 1, 1));
        jButton_opt_turs_cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_opt_turs_cancelActionPerformed(evt);
            }
        });

        jButton_new_close8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Turist/cancel_f2.png"))); // NOI18N
        jButton_new_close8.setText("Закрыть");
        jButton_new_close8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_new_close8ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jFrame_tursLayout = new javax.swing.GroupLayout(jFrame_turs.getContentPane());
        jFrame_turs.getContentPane().setLayout(jFrame_tursLayout);
        jFrame_tursLayout.setHorizontalGroup(
            jFrame_tursLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrame_tursLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFrame_tursLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane15, javax.swing.GroupLayout.DEFAULT_SIZE, 622, Short.MAX_VALUE)
                    .addGroup(jFrame_tursLayout.createSequentialGroup()
                        .addComponent(jComboBox_opt_turs, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton_opt_turs_add))
                    .addGroup(jFrame_tursLayout.createSequentialGroup()
                        .addComponent(jButton_opt_turs_edit)
                        .addGap(18, 18, 18)
                        .addComponent(jButton_opt_turs_cancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton_opt_turs_save)
                        .addGap(18, 18, 18)
                        .addComponent(jButton_new_close8)))
                .addContainerGap())
        );
        jFrame_tursLayout.setVerticalGroup(
            jFrame_tursLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrame_tursLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFrame_tursLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton_opt_turs_add)
                    .addComponent(jComboBox_opt_turs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane15, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jFrame_tursLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton_opt_turs_edit)
                    .addComponent(jButton_opt_turs_save)
                    .addComponent(jButton_opt_turs_cancel)
                    .addComponent(jButton_new_close8))
                .addContainerGap(29, Short.MAX_VALUE))
        );

        jFrame_turagent.setTitle("Турагенты");
        jFrame_turagent.setExtendedState(6);
        jFrame_turagent.setLocationByPlatform(true);
        jFrame_turagent.setMinimumSize(new java.awt.Dimension(450, 380));
        jFrame_turagent.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                jFrame_turagentComponentHidden(evt);
            }
        });

        jComboBox_opt_turagent.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox_opt_turagent.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox_opt_turagentItemStateChanged(evt);
            }
        });

        jButton_turagent_new.setText("Добавить");
        jButton_turagent_new.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_turagent_newActionPerformed(evt);
            }
        });

        jTable_opt_turagent.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Название", null},
                {"Полное название", null},
                {"Менеджер", null},
                {"Адрес", null},
                {"Телефон", null},
                {"Расчетный счет", null},
                {"Кор. счет", null},
                {"Банк", null},
                {"ИНН", null},
                {"КПП", null},
                {"БИК", null}
            },
            new String [] {
                "Поля", "Данные"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable_opt_turagent.setColumnSelectionAllowed(true);
        jTable_opt_turagent.getTableHeader().setReorderingAllowed(false);
        jScrollPane16.setViewportView(jTable_opt_turagent);
        jTable_opt_turagent.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        jButton13.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jButton13.setText("Удалить");
        jButton13.setEnabled(false);
        jButton13.setMargin(new java.awt.Insets(1, 1, 1, 1));

        jButton_turagent_edit.setText("Изменить");
        jButton_turagent_edit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_turagent_editActionPerformed(evt);
            }
        });

        jButton_turagent_save.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Turist/save_f2.png"))); // NOI18N
        jButton_turagent_save.setText("Сохранить");
        jButton_turagent_save.setEnabled(false);
        jButton_turagent_save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_turagent_saveActionPerformed(evt);
            }
        });

        jButton_opt_turagent_cancel.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jButton_opt_turagent_cancel.setText("Отменить");
        jButton_opt_turagent_cancel.setMargin(new java.awt.Insets(1, 1, 1, 1));
        jButton_opt_turagent_cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_opt_turagent_cancelActionPerformed(evt);
            }
        });

        jButton_new_close9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Turist/cancel_f2.png"))); // NOI18N
        jButton_new_close9.setText("Закрыть");
        jButton_new_close9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_new_close9ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jFrame_turagentLayout = new javax.swing.GroupLayout(jFrame_turagent.getContentPane());
        jFrame_turagent.getContentPane().setLayout(jFrame_turagentLayout);
        jFrame_turagentLayout.setHorizontalGroup(
            jFrame_turagentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrame_turagentLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFrame_turagentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jFrame_turagentLayout.createSequentialGroup()
                        .addComponent(jComboBox_opt_turagent, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton_turagent_new))
                    .addComponent(jScrollPane16, javax.swing.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE)
                    .addGroup(jFrame_turagentLayout.createSequentialGroup()
                        .addComponent(jButton13)
                        .addGap(18, 18, 18)
                        .addComponent(jButton_opt_turagent_cancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton_turagent_edit)
                        .addGap(18, 18, 18)
                        .addComponent(jButton_turagent_save)
                        .addGap(18, 18, 18)
                        .addComponent(jButton_new_close9)))
                .addContainerGap())
        );
        jFrame_turagentLayout.setVerticalGroup(
            jFrame_turagentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrame_turagentLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFrame_turagentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox_opt_turagent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton_turagent_new))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane16, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jFrame_turagentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton_turagent_edit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton_turagent_save, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton_opt_turagent_cancel)
                    .addComponent(jButton_new_close9))
                .addGap(40, 40, 40))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Турист v.1.1.0");
        setExtendedState(6);
        setLocationByPlatform(true);
        setMinimumSize(new java.awt.Dimension(820, 400));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Turist/vitrina.jpg"))); // NOI18N
        jLabel1.setToolTipText("");

        jDayChooser.setToolTipText("dsfsdf");
        jDayChooser.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jDayChooserMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 640, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jDayChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jDayChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );

        jButton_new.setText("Новый");
        jButton_new.setFocusPainted(false);
        jButton_new.setFocusable(false);
        jButton_new.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_newActionPerformed(evt);
            }
        });

        jButton_search.setText("Поиск");
        jButton_search.setFocusPainted(false);
        jButton_search.setFocusable(false);
        jButton_search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_searchActionPerformed(evt);
            }
        });

        jButton_otchet.setText("Отчет");
        jButton_otchet.setFocusPainted(false);
        jButton_otchet.setFocusable(false);
        jButton_otchet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_otchetActionPerformed(evt);
            }
        });

        jButton_sms.setText("SMS");
        jButton_sms.setFocusPainted(false);
        jButton_sms.setFocusable(false);
        jButton_sms.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_smsActionPerformed(evt);
            }
        });

        jButton_email.setText("Email");
        jButton_email.setFocusPainted(false);
        jButton_email.setFocusable(false);
        jButton_email.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_emailActionPerformed(evt);
            }
        });

        jButton_update.setText("60");
        jButton_update.setFocusPainted(false);
        jButton_update.setFocusable(false);
        jButton_update.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_updateActionPerformed(evt);
            }
        });

        jButton_happy.setText("!");
        jButton_happy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_happyActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jButton_new)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_search)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_otchet)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_email)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_sms)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 327, Short.MAX_VALUE)
                .addComponent(jButton_happy)
                .addGap(65, 65, 65)
                .addComponent(jButton_update))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jButton_new, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jButton_search, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jButton_otchet, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jButton_email, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jButton_sms, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jButton_update, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jButton_happy, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jTable_main.setAutoCreateRowSorter(true);
        jTable_main.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "№ договора", "Туроператор", "Покупатель", "Название тура", "ФИО", "Дата покупки", "Стоимость", "Начало тура", "Конец тура"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable_main.getTableHeader().setReorderingAllowed(false);
        jTable_main.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable_mainMouseClicked(evt);
            }
        });
        jScrollPane8.setViewportView(jTable_main);

        jButton_new_close11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Turist/exit_7572.png"))); // NOI18N
        jButton_new_close11.setText("Выход");
        jButton_new_close11.setMargin(new java.awt.Insets(2, 6, 2, 14));
        jButton_new_close11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_new_close11ActionPerformed(evt);
            }
        });

        jMenu1.setText("Файл");

        jMenuItem1.setText("Закрыть");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Настройки");

        jMenuItem2.setText("Опции");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem2);

        jMenuItem_opt_prodavets.setText("Продавцы");
        jMenuItem_opt_prodavets.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_opt_prodavetsActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem_opt_prodavets);

        jMenuItem_opt_turagent.setText("Турагенты");
        jMenuItem_opt_turagent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_opt_turagentActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem_opt_turagent);

        jMenuItem_opt_turoperator.setText("Туроператоры");
        jMenuItem_opt_turoperator.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_opt_turoperatorActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem_opt_turoperator);

        jMenuItem_opt_turs.setText("Туры");
        jMenuItem_opt_turs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_opt_tursActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem_opt_turs);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("Помощь");

        jMenuItem5.setText("Справка");
        jMenu3.add(jMenuItem5);

        jMenuItem6.setText("О программе");
        jMenu3.add(jMenuItem6);

        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                        .addComponent(jButton_new_close11))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane8)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButton_new_close11)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        //выход из программы
        db.close();
        db2.close();
        System.exit(0);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jButton_newActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_newActionPerformed
        //показать окно добавления нового клиента
        jButton_new_save.setEnabled(true);
        SAVE(true);
        jComboBox_prodavets.setEnabled(false);
        if (query_type) {
            query_type = QUERY_INSERT;
            //db.executeUpdate("UPDATE main SET edited = 0 WHERE tur_num = " + jLabel_tur_num.getText()); //удалить
            db.executeUpdate("UPDATE main SET edited = 0 WHERE id = " + jLabel_tur_num.getText());
        }
        clearFields_newClient();   
        changePokupatel();
        jCheckBox_priceActionPerformed(null);
        jFrame_new.setVisible(true);
       
    }//GEN-LAST:event_jButton_newActionPerformed

    private void jButton_updateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_updateActionPerformed
        //ручное обновление списка последних клиентов из БД
        tableUpdate();
    }//GEN-LAST:event_jButton_updateActionPerformed

    private void jButton_client_closeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_client_closeActionPerformed
        //закрыть карточку клиента
        jFrame_client.setVisible(false);
    }//GEN-LAST:event_jButton_client_closeActionPerformed

    private void jButton_client_saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_client_saveActionPerformed
        //старая форма
        
    }//GEN-LAST:event_jButton_client_saveActionPerformed

    private void jButton_printActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_printActionPerformed
        //распечатать карточку клиента
        MyPrinter print = new MyPrinter();
        print.addString("Номер:      " + jTextField_id1.getText());
        print.addString("Дата:         " + jFormattedTextField_date1.getText());
        print.addString("Имя:          " + jTextField_name1.getText());
        print.addString("Фамилия:  " + jTextField_fam1.getText());
        print.addString("Отчество: " + jTextField_otche1.getText());
        print.addString("Цена:        " + jTextField_price1.getText());
        print.startPrint();
        
      /*   PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
        DocPrintJob job = printService.createPrintJob();
        DocFlavor docFlavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
        
        //PrintJob pjob = getToolkit().getPrintJob(this, "Printer", null);
        
       
        DocAttributeSet docAttributes = new HashDocAttributeSet();
	docAttributes.add(OrientationRequested.PORTRAIT);
        
	PrintRequestAttributeSet printAttributes = new HashPrintRequestAttributeSet();
	printAttributes.add(new Copies(1));
            printAttributes.add(MediaSizeName.ISO_A4);
            printAttributes.add(Sides.ONE_SIDED);
            printAttributes.add(Finishings.STAPLE);
        
        try {
            Doc doc = new SimpleDoc(new FileInputStream("C:/1.TXT"), docFlavor, docAttributes);
            job.print(doc, printAttributes);
            
        } catch (FileNotFoundException|PrintException ex) {
            Logger.getLogger(Foton.class.getName()).log(Level.SEVERE, null, ex);
        }
        */
    }//GEN-LAST:event_jButton_printActionPerformed

    private void jButton_mail_sendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_mail_sendActionPerformed
        //отправить одиночное письмо адресату
        jButton_mail_send.setEnabled(false);
        String to = jTextField_mail_to.getText().trim();
        String subject = jTextField_mail_subject.getText().trim();
        String text = jTextPane_msg.getText();
        myMail mail = new myMail(mail_smtp_user, mail_smtp_pass, mail_smtp);
        JOptionPane.showMessageDialog(null, mail.send(mail_from, to, subject, text));
        jButton_mail_send.setEnabled(true);
    }//GEN-LAST:event_jButton_mail_sendActionPerformed

    private void jButton_mail_startActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_mail_startActionPerformed
        // Запустить Email рассылку
        if (!jButton_mail_start.isEnabled()) return;
        int rowCount = jTable_spam.getRowCount();
        if(rowCount == 0) return;
        int i = 0;
        String[] mail_to = new String[rowCount];
        jProgressBar_mail.setValue(0);
        while(i < rowCount){
            mail_to[i] = jTable_spam.getValueAt(i, 0).toString();
            jTable_spam.setValueAt("", i, 1);
            i++;
        }
        
        smt = new SendMailThread(rowCount, mail_to);
        sm = new Thread(smt);
        sm.start();
        
        spt = new StatusProcessThread();
        sp = new Thread(spt);
        sp.start();
        
        jButton_mail_send.setEnabled(false);
        jButton_mail_suspend.setEnabled(true);
        jButton_mail_resume.setEnabled(false);
        jButton_mail_stop.setEnabled(true);
        jButton_mail_start.setEnabled(false);
    }//GEN-LAST:event_jButton_mail_startActionPerformed
    
    private void jButton_emailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_emailActionPerformed
        //mail рассылка
        try {
            if (jButton_mail_start.isEnabled()) {
                jTextField_mail_to.setText("");
                // Получить писок подписчиков на СПАМ
                ResultSet rs = db.executeQuery("SELECT email FROM main WHERE spam = 1");
                DefaultTableModel dtm = (DefaultTableModel) jTable_spam.getModel();
                while(dtm.getRowCount() > 0){
                    dtm.removeRow(dtm.getRowCount() - 1);
                }
                jTable_spam.setModel(dtm);
                while(rs.next()) {
                    dtm.addRow(new String[] {rs.getString("email"), ""});
                }
                dtm.fireTableDataChanged();
                jButton_mail_send.setEnabled(true);
                jButton_mail_suspend.setEnabled(false);
                jButton_mail_resume.setEnabled(false);
                jButton_mail_stop.setEnabled(false);
                jButton_mail_start.setEnabled(true);
            }
            jFrame_mail.setVisible(true);
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }//GEN-LAST:event_jButton_emailActionPerformed

    private void jButton_smsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_smsActionPerformed
        //SMS
        try {
            jTextField_sms_nomer.setText("");
            // Получить писок подписчиков на СПАМ
            ResultSet rs = db.executeQuery("SELECT phone_mob FROM main WHERE sms = 1");
            DefaultTableModel dtm = (DefaultTableModel) jTable_sms.getModel();
            while(dtm.getRowCount() > 0){
                dtm.removeRow(dtm.getRowCount() - 1);
            }
            jTable_sms.setModel(dtm);
            while(rs.next()) {
                dtm.addRow(new String[] {rs.getString("phone_mob")});
            }
            dtm.fireTableDataChanged();
            jFrame_sms.setVisible(true);
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }//GEN-LAST:event_jButton_smsActionPerformed

    private void jDayChooserMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jDayChooserMouseClicked
        // Клик по календарику        
      //  System.out.println("click");
    }//GEN-LAST:event_jDayChooserMouseClicked

    private void jButton_otchetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_otchetActionPerformed
        // Отчет
        jFrame_report.setVisible(true);
        
        /*      JOptionPane.showMessageDialog(null, baseDir());
        String rtfSource = baseDir() + "templates/Putevka.rtf";
        String rtfTarget = baseDir() + "out/" + jLabel_tur_num.getText() + ".rtf";
        RTF rtf = new RTF();
        rtf.putDate(rtfSource, rtfTarget, getFields_newClient());
        try {
            java.awt.Desktop.getDesktop().open(new File(rtfTarget));
        } catch (IOException ex) {
            Logger.getLogger(Foton.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex);
        }
*/
    }//GEN-LAST:event_jButton_otchetActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // печать 2
        
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton_new_saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_new_saveActionPerformed
        // Сохранить в базу нового клиента / изменить
        if (!jButton_new_save.isEnabled()) return;
        String status = checkFields();
        if(!status.equalsIgnoreCase("ok")) {
            JOptionPane.showMessageDialog(null, "Не заполнено поле: " + status);
            return;
        }
        try {
            if (!db.executeUpdate(fields_newClient_2(query_type))) return;
            if(query_type){ //edit
                ResultSet rs = db2.executeQuery("SELECT edited_count FROM main WHERE id = " + jLabel_tur_num.getText());
                rs.next();
                jLabel_edited_count.setText(rs.getString("edited_count"));
            } else { //insert
                ResultSet rs = db2.executeQuery("SELECT id FROM main ORDER BY id DESC LIMIT 1");
                rs.next();
                int id;
                id = rs.getInt("id");
                jLabel_tur_num.setText("" + id);
                jPanel_dogovor.setBorder(javax.swing.BorderFactory.createTitledBorder("№ договора: " + id));
            }
            query_type = QUERY_INSERT;
            jButton_new_save.setEnabled(false);
            SAVE(false);
            tableUpdate();
            //------------------сохраняем документы-----
            String rtfSource;
            String rtfTarget;
            String excelTarget;
            String t_name = jLabel_tur_num.getText() + "_" + jTextField_f_fam.getText() + "_" + jLabel_edited_count.getText() + ".rtf"; 
            String a_name = jLabel_tur_num.getText() + "_" + jComboBox_pokupatel.getSelectedItem() + "_" + jLabel_edited_count.getText();
            String dot_rtf = ".rtf";
            String dot_xls = ".xls";
            
            RTF rtf = new RTF();
            
            if(jComboBox_pokupatel.getSelectedItem().toString().equalsIgnoreCase("Турист")){
                //шаблоны для туристов
                //Договор
                rtfSource = "templates/dogovor.rtf";
                rtfTarget = "Данные о клиентах/Договора/d" + t_name;
                rtf.putDate_dogovor(rtfSource, rtfTarget, getFields_newClient_2(), getFields_newClient_prodavets(), getFields_newClient_managers(), getFields_newClient_turoperator());

                //Приложение
                rtfSource = "templates/prilozhenie.rtf";
                rtfTarget = "Данные о клиентах/Приложения/p" + t_name;
                rtf.putDate(rtfSource, rtfTarget, getFields_newClient_2(), getFields_newClient_prodavets());

                //ПКО
                rtfSource = "templates/pko.rtf";
                rtfTarget = "Данные о клиентах/ПКО/k" + t_name;
                rtf.putDate(rtfSource, rtfTarget, getFields_newClient_2(), getFields_newClient_prodavets());

                //Путевка
                rtfSource = "templates/putevka.rtf";
                rtfTarget = "Данные о клиентах/Путевки/v" + t_name;
                rtf.putDate(rtfSource, rtfTarget, getFields_newClient_2(), getFields_newClient_prodavets());

                //Фортуна
                rtfSource = "templates/fortuna.rtf";
                rtfTarget = "Данные о клиентах/Фортуна/f" + t_name;
                rtf.putDate(rtfSource, rtfTarget, getFields_newClient_2(), getFields_newClient_prodavets());
            } else {
                //шаблоны для агентов
                //ПКО
                rtfSource = "templates/pko_a.rtf";
                rtfTarget = "Данные о клиентах/ПКО_А/ka" + a_name + dot_rtf;
                rtf.putDate_PKO_A(rtfSource, rtfTarget, getFields_newClient_2(), getFields_newClient_turagent());
                
                //Подтверждение
                rtfSource = "templates/podtverzhdenie.rtf";
                rtfTarget = "Данные о клиентах/Подтверждения/b" + a_name + dot_rtf;
                rtf.putDate_Podtverzhdenie(rtfSource, rtfTarget, getFields_newClient_2(), getFields_newClient_turagent());
                
                //Счет
                try {
                    excelTarget = "Данные о клиентах/Счета/s" + a_name + dot_xls;
                    ExcelJob.excelSchet(excelTarget, getFields_newClient_2(), getFields_newClient_prodavets(), getFields_newClient_turagent());
                } catch (WriteException ex) {
                    Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }//GEN-LAST:event_jButton_new_saveActionPerformed

    private void jButton_new_editActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_new_editActionPerformed
        // Редактировать карточку клиента из формы нового клиента
        if (!jButton_new_edit.isEnabled()) return;
        try { 
           // ResultSet rs = db.executeQuery("SELECT edited FROM main WHERE tur_num = " + jLabel_tur_num.getText()); // delete
            ResultSet rs = db.executeQuery("SELECT edited FROM main WHERE id = " + jLabel_tur_num.getText());
            rs.next();
            if (rs.getInt("edited") == 0) {
               // db.executeUpdate("UPDATE main SET edited = 1 WHERE tur_num = " + jLabel_tur_num.getText()); //del
                db.executeUpdate("UPDATE main SET edited = 1 WHERE id = " + jLabel_tur_num.getText()); 
                query_type = QUERY_UPDATE;
                
                String c1 = jComboBox_prodavets.getSelectedItem() + "";
                String c2 = jComboBox_turoperator.getSelectedItem() + "";
                String c3 = jComboBox_seller.getSelectedItem() + "";
                String c4 = jComboBox_pokupatel.getSelectedItem() + "";
                String c5 = jComboBox_procent.getSelectedItem().toString().trim();
                
                jComboBox_prodavets.removeAllItems();
                jComboBox_turoperator.removeAllItems();
                jComboBox_seller.removeAllItems();
                jComboBox_pokupatel.removeAllItems();
                jComboBox_pokupatel.addItem("Турист");
                jComboBox_procent.removeAllItems();
                
                boolean item_not_found = true;
                for(int i = 0; i<=15; i++) { 
                    jComboBox_procent.addItem("" + i);
                    if(c5.equalsIgnoreCase("" + i)) item_not_found = false;
                }
                if (item_not_found) jComboBox_procent.addItem("" + c5);
                jComboBox_procent.setSelectedItem(c5);
                
                rs = db.executeQuery("SELECT name FROM prodavets ORDER BY id");
                while(rs.next()) {
                    jComboBox_prodavets.addItem(rs.getString("name"));
                }
                if(c1 != null && !c1.equalsIgnoreCase("null")) jComboBox_prodavets.setSelectedItem(c1);
                //----------------------------------------------------
                rs = db.executeQuery("SELECT name FROM turoperator ORDER BY id");
                while(rs.next()) {
                    jComboBox_turoperator.addItem(rs.getString("name"));
                }
                if(c2 != null && !c2.equalsIgnoreCase("null")) jComboBox_turoperator.setSelectedItem(c2);
                //----------------------------------------------------
                rs = db.executeQuery("SELECT name FROM managers WHERE prodavets = '" + jComboBox_prodavets.getSelectedItem() + "' ORDER BY id");
                while(rs.next()) {
                    jComboBox_seller.addItem(rs.getString("name"));
                }
                if(c3 != null && !c3.equalsIgnoreCase("null")) jComboBox_seller.setSelectedItem(c3);
                //----------------------------------------------------
                rs = db.executeQuery("SELECT name FROM turagent ORDER BY id");
                while(rs.next()) {
                    jComboBox_pokupatel.addItem(rs.getString("name"));
                }
                if(c4 != null && !c4.equalsIgnoreCase("null")) jComboBox_pokupatel.setSelectedItem(c4);
                //----------------------------------------------------
                jButton_new_save.setEnabled(true);
                SAVE(true);
                changePokupatel();
                jCheckBox_priceActionPerformed(null);
            } else {
                JOptionPane.showMessageDialog(null, "Данная запись уже кем-то редактируется.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
        
    }//GEN-LAST:event_jButton_new_editActionPerformed

    private void jButton_new_savePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jButton_new_savePropertyChange
   /*     //когда меняется свойство кнопки СОХРАНИТЬ
        //блокировка полей для ввода на форме нового клиента
        
      //  jTextArea_u_recvizit.setEditable(jButton_new_save.isEnabled());
        jTextArea_f_address.setEditable(jButton_new_save.isEnabled());
      //  jTextField_u_firma.setEditable(jButton_new_save.isEnabled());
//        jTextField_u_post.setEditable(jButton_new_save.isEnabled());
//        jTextField_u_fam.setEditable(jButton_new_save.isEnabled());
//        jTextField_u_name.setEditable(jButton_new_save.isEnabled());
//        jTextField_u_otche.setEditable(jButton_new_save.isEnabled());
//        jCheckBox_fizik.setEnabled(jButton_new_save.isEnabled());
        jTextField_f_fam.setEditable(jButton_new_save.isEnabled());
        jTextField_f_name.setEditable(jButton_new_save.isEnabled());
        jTextField_f_otche.setEditable(jButton_new_save.isEnabled());
        jTextField_f_passport.setEditable(jButton_new_save.isEnabled());
        jTextField_phone_dom.setEditable(jButton_new_save.isEnabled());
        jTextField_phone_rab.setEditable(jButton_new_save.isEnabled());
        jTextField_phone_mob.setEditable(jButton_new_save.isEnabled());
        jTextField_email.setEditable(jButton_new_save.isEnabled());
        jCheckBox_spam.setEnabled(jButton_new_save.isEnabled());
        jCheckBox_sms.setEnabled(jButton_new_save.isEnabled());
        
        jComboBox_tur_name.setEnabled(jButton_new_save.isEnabled());
        jCheckBox_gid.setEnabled(jButton_new_save.isEnabled());
        jTextField_tur1_adr.setEditable(jButton_new_save.isEnabled());
        jTextField_tur2_adr.setEditable(jButton_new_save.isEnabled());
        jTextField_tur3_adr.setEditable(jButton_new_save.isEnabled());
        jTextField_tur4_adr.setEditable(jButton_new_save.isEnabled());
        jTextField_tur5_adr.setEditable(jButton_new_save.isEnabled());
        jTextField_tur1_fio.setEditable(jButton_new_save.isEnabled());
        jTextField_tur2_fio.setEditable(jButton_new_save.isEnabled());
        jTextField_tur3_fio.setEditable(jButton_new_save.isEnabled());
        jTextField_tur4_fio.setEditable(jButton_new_save.isEnabled());
        jTextField_tur5_fio.setEditable(jButton_new_save.isEnabled());
        jDateChooser_tur1_bd.setEnabled(jButton_new_save.isEnabled());
        jDateChooser_tur2_bd.setEnabled(jButton_new_save.isEnabled());
        jDateChooser_tur3_bd.setEnabled(jButton_new_save.isEnabled());
        jDateChooser_tur4_bd.setEnabled(jButton_new_save.isEnabled());
        jDateChooser_tur5_bd.setEnabled(jButton_new_save.isEnabled());
        jDateChooser_tur_date_po.setEnabled(jButton_new_save.isEnabled());
        jDateChooser_tur_date_s.setEnabled(jButton_new_save.isEnabled());
        jTextField_tur_begin.setEditable(jButton_new_save.isEnabled());
        jTextField_tur_end.setEditable(jButton_new_save.isEnabled());
        jTextField_tur_punkt.setEditable(jButton_new_save.isEnabled());
        jComboBox_bilet_cat.setEnabled(jButton_new_save.isEnabled());
        jTextField_h_name.setEditable(jButton_new_save.isEnabled());
        jComboBox_h_nomer.setEnabled(jButton_new_save.isEnabled());
        jComboBox_food.setEnabled(jButton_new_save.isEnabled());
        jCheckBox_visa.setEnabled(jButton_new_save.isEnabled());
        jCheckBox_strah.setEnabled(jButton_new_save.isEnabled());
        jCheckBox_heal.setEnabled(jButton_new_save.isEnabled());
        jComboBox_transfer.setEnabled(jButton_new_save.isEnabled());
        jTextArea_excurs.setEditable(jButton_new_save.isEnabled());
        jTextArea_service.setEditable(jButton_new_save.isEnabled());
        jTextField_price.setEditable(jButton_new_save.isEnabled());
        jDateChooser_sale_date.setEnabled(jButton_new_save.isEnabled());
        jComboBox_seller.setEnabled(jButton_new_save.isEnabled());
        
        jButton_new_edit.setEnabled(!jButton_new_save.isEnabled());
//        jButton_new_addtur.setEnabled(!jButton_new_save.isEnabled());
//        jButton_new_print.setEnabled(!jButton_new_save.isEnabled());
        
        jButton_new_dogovor.setEnabled(!jButton_new_save.isEnabled());
        jButton_new_fortuna.setEnabled(!jButton_new_save.isEnabled());
        jButton_new_pko.setEnabled(!jButton_new_save.isEnabled());
        jButton_new_prilozhenie.setEnabled(!jButton_new_save.isEnabled());
        jButton_new_putevka.setEnabled(!jButton_new_save.isEnabled());
        
        jComboBox_prodavets.setEnabled(jButton_new_save.isEnabled());
        jComboBox_turoperator.setEnabled(jButton_new_save.isEnabled());
        
        jTextField_tur1_passport.setEditable(jButton_new_save.isEnabled());
        jTextField_tur2_passport.setEditable(jButton_new_save.isEnabled());
        jTextField_tur3_passport.setEditable(jButton_new_save.isEnabled());
        jTextField_tur4_passport.setEditable(jButton_new_save.isEnabled());
        jTextField_tur5_passport.setEditable(jButton_new_save.isEnabled());
        
        jTextField_tur1_price.setEditable(jButton_new_save.isEnabled());
        jTextField_tur2_price.setEditable(jButton_new_save.isEnabled());
        jTextField_tur3_price.setEditable(jButton_new_save.isEnabled());
        jTextField_tur4_price.setEditable(jButton_new_save.isEnabled());
        jTextField_tur5_price.setEditable(jButton_new_save.isEnabled());
        
        jTextField_last_price.setEditable(jButton_new_save.isEnabled());
        
        jComboBox_pokupatel.setEnabled(jButton_new_save.isEnabled());
        jComboBox_procent.setEnabled(jButton_new_save.isEnabled());
        jCheckBox_price.setEnabled(jButton_new_save.isEnabled());
        JOptionPane.showMessageDialog(null, "Свойства кнопки сохранить изменились");*/
    }//GEN-LAST:event_jButton_new_savePropertyChange

    private void jFrame_newComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jFrame_newComponentHidden
        // когда закрывается окно добавления нового клиента (неважно по крестику или по кнопке)
        //JOptionPane.showMessageDialog(null, "BINGO!!!");
        jButton_new_save.setEnabled(true);
        SAVE(true);
        jComboBox_prodavets.setEnabled(false);
        if (query_type) {
            query_type = QUERY_INSERT;
            //db.executeUpdate("UPDATE main SET edited = 0 WHERE tur_num = " + jLabel_tur_num.getText()); //заменить
            db.executeUpdate("UPDATE main SET edited = 0 WHERE id = " + jLabel_tur_num.getText()); 
        }
    }//GEN-LAST:event_jFrame_newComponentHidden

    private void jTable_mainMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable_mainMouseClicked
        // клик по главной таблице №2
        int row = jTable_main.getSelectedRow();
        String id = jTable_main.getValueAt(row, 0).toString();
        if (!id.equalsIgnoreCase("")) {
            jFrame_newComponentHidden(null);
           // ResultSet rs = db.executeQuery("SELECT * FROM main WHERE tur_num = " + id); //заменить
            ResultSet rs = db.executeQuery("SELECT * FROM main WHERE id = " + id);
            fillFields_newClient_2(rs);
            jFrame_new.setVisible(true);
            jButton_new_save.setEnabled(false);
            SAVE(false);
        }
        
    }//GEN-LAST:event_jTable_mainMouseClicked

    private void jButton_mail_suspendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_mail_suspendActionPerformed
        // Остановить Email рассылку
        sm.suspend();
        sp.suspend();
        jButton_mail_send.setEnabled(true);
        jButton_mail_suspend.setEnabled(false);
        jButton_mail_resume.setEnabled(true);
        jButton_mail_stop.setEnabled(true);
    }//GEN-LAST:event_jButton_mail_suspendActionPerformed

    private void jButton_mail_resumeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_mail_resumeActionPerformed
        // Продолжить Email рассылку
        sm.resume();
        sp.resume();
        jButton_mail_send.setEnabled(false);
        jButton_mail_suspend.setEnabled(true);
        jButton_mail_resume.setEnabled(false);
        jButton_mail_stop.setEnabled(true);
    }//GEN-LAST:event_jButton_mail_resumeActionPerformed

    private void jButton_mail_stopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_mail_stopActionPerformed
        // Прервать Email рассылку
        sm.stop();
        sp.stop();
        jProgressBar_mail.setMaximum(100);
        jProgressBar_mail.setValue(0);
        jButton_mail_send.setEnabled(true);
        jButton_mail_suspend.setEnabled(false);
        jButton_mail_resume.setEnabled(false);
        jButton_mail_stop.setEnabled(false);
        jButton_mail_start.setEnabled(true);
    }//GEN-LAST:event_jButton_mail_stopActionPerformed

    private void jTextArea_sms_messageCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jTextArea_sms_messageCaretUpdate
        // набор смски
        int sms_length = jTextArea_sms_message.getText().length();
        int sms_count = 0;
        if (sms_length > 0 && sms_length <= 70) sms_count = 1;
        if (sms_length > 70 && sms_length%67 != 0) sms_count = sms_length / 67 + 1;
        if (sms_length > 70 && sms_length%67 == 0) sms_count = sms_length / 67;
        
        if (sms_length > 70) {
            jLabel_sms_length.setText(67*sms_count - sms_length + "/" + sms_count);
        } else {
            jLabel_sms_length.setText(70 - sms_length + "/" + sms_count);
        }
    }//GEN-LAST:event_jTextArea_sms_messageCaretUpdate

    private void jButton_sms_sendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_sms_sendActionPerformed
        // Отправить одиночную смску
        String[] phone = new String[1];
        phone[0] = jTextField_sms_nomer.getText().trim();  
        String message = jTextArea_sms_message.getText();
        
        myMail mail = new myMail(sms_smtp_user, sms_smtp_pass, sms_smtp);
        JOptionPane.showMessageDialog(null, mail.sendSMS(sms_from, sms_to, "", phone, message, sms_spam_login, sms_spam_pass));
    }//GEN-LAST:event_jButton_sms_sendActionPerformed

    private void jButton_sms_startActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_sms_startActionPerformed
        // Запустить SMS рассылку
        int row_count = jTable_sms.getRowCount();
        if (row_count == 0) return;
        int i = 0;
        String[] phone = new String[row_count];
        String message = jTextArea_sms_message.getText();
        while(i < row_count){
            phone[i] = jTable_sms.getValueAt(i, 0).toString();
            i++;
        }
        myMail mail = new myMail(sms_smtp_user, sms_smtp_pass, sms_smtp);
        JOptionPane.showMessageDialog(null, mail.sendSMS(sms_from, sms_to, "", phone, message, sms_spam_login, sms_spam_pass));
    }//GEN-LAST:event_jButton_sms_startActionPerformed
boolean combo_search = false;
    private void jButton_searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_searchActionPerformed
        // Открыть форму поиска
        jFrame_search.setVisible(true);
        combo_search = false;
        jComboBox_search_FIO.removeAllItems();
        //jComboBox_search_FIO.addItem("Ф.И.О.");
        String query = "SELECT f_fam, f_name, f_otche FROM main ORDER BY f_fam";
        ResultSet rs = db.executeQuery(query);
        LinkedHashSet hashSet = new LinkedHashSet();
        try {
            while(rs.next()) {
                hashSet.add((rs.getString("f_fam") + " " + rs.getString("f_name") + " " + rs.getString("f_otche")).trim());
            }
            Iterator itr = hashSet.iterator();
            while(itr.hasNext()){
                jComboBox_search_FIO.addItem(itr.next());
            }
            combo_search = true;
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }//GEN-LAST:event_jButton_searchActionPerformed

    private void jButton_search_searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_search_searchActionPerformed
        // Найти  
        String search = "'" + jTextField_search.getText().trim() + "'";
        String last = "";
        switch(jComboBox_search_field.getSelectedIndex()){
            //case 0: field = "tur_num";      break;// заменить на: case 0: field = "id";      break;
            //case 1: field = "dogovor_num";  break;// заменить на: case 1: field = "turoperator";  break;
            case 0:  last = "";                                 break;
            case 1:  last = " WHERE id = " + search;            break;
            case 2:  last = " WHERE turoperator = " + search;   break;
            case 3:  last = " WHERE tur_name = " + search;      break;
            case 4:  last = " WHERE f_fam = " + search;         break;
            case 5:  last = " WHERE f_name = " + search;        break;
            case 6:  last = " WHERE f_otche = " + search;       break;
            case 7:  last = " WHERE f_passport = " + search;    break;
            case 8:  last = " WHERE phone_dom = " + search;     break;
            case 9:  last = " WHERE phone_rab = " + search;     break;
            case 10: last = " WHERE phone_mob = " + search;     break;
            case 11: last = " WHERE email = " + search;         break;
        }
       // String query = "SELECT tur_num, dogovor_num, tur_name, f_fam, f_name, f_otche, sale_date, price, tur_date_s, tur_date_po FROM main WHERE " + field + "=" + search; //заменить
        String query = "SELECT id, turoperator, pokupatel, tur_name, f_fam, f_name, f_otche, sale_date, last_price, tur_date_s, tur_date_po FROM main" + last;
        //изменить таблицу
        ResultSet rs = db.executeQuery(query);
        DefaultTableModel dtm = (DefaultTableModel) jTable_search.getModel();
        while(dtm.getRowCount() > 0){
            dtm.removeRow(dtm.getRowCount() - 1);
        }
        jTable_search.setModel(dtm);
      
        try {
            while(rs.next()) {
                dtm.addRow(new String[] {
                    rs.getString("id"),
                    rs.getString("turoperator"),
                    rs.getString("pokupatel"),
                    rs.getString("tur_name"),
                    rs.getString("f_fam") + " " + rs.getString("f_name") + " " + rs.getString("f_otche"),
                    reFormateDateForTable(rs.getString("sale_date")),
                    rs.getString("last_price"),
                    reFormateDateForTable(rs.getString("tur_date_s")),
                    reFormateDateForTable(rs.getString("tur_date_po")),
                });
            }
            dtm.fireTableDataChanged();
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
        
    }//GEN-LAST:event_jButton_search_searchActionPerformed

    private void jTable_searchMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable_searchMouseClicked
        // Клик по таблице поиска
        int row = jTable_search.getSelectedRow();
        String id = jTable_search.getValueAt(row, 0).toString();
        if (!id.equalsIgnoreCase("")) {
            jFrame_newComponentHidden(null);
            //ResultSet rs = db.executeQuery("SELECT * FROM main WHERE tur_num = " + id); //
            ResultSet rs = db.executeQuery("SELECT * FROM main WHERE id = " + id);
            fillFields_newClient_2(rs);
            jFrame_new.setVisible(true);
            jButton_new_save.setEnabled(false);
            SAVE(false);
        }
    }//GEN-LAST:event_jTable_searchMouseClicked

    private void jButton_happyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_happyActionPerformed
        // Нажали кнопку happy
       // jButton_happy.setEnabled(birthday_today);
        jFrame_happy.setVisible(true);
        
        DefaultTableModel dtm = (DefaultTableModel) jTable_happy.getModel();
        while(dtm.getRowCount() > 0){
            dtm.removeRow(dtm.getRowCount() - 1);
        }
        jTable_happy.setModel(dtm);
       
        try {///tur_num заменить на id
            /////////
            ResultSet rs = db.executeQuery("SELECT id, tur1_fio, tur1_bd, phone_mob, email FROM main WHERE EXTRACT(MONTH FROM tur1_bd) = " + (new Date().getMonth()+1));
            while(rs.next()){
                dtm.addRow(new String[] {
                    rs.getString("id"),
                    reFormateDateForTable(rs.getString("tur1_bd")),
                    rs.getString("tur1_fio"),
                    rs.getString("phone_mob"),
                    rs.getString("email"),
                });
            }
            rs = db.executeQuery("SELECT id, tur2_fio, tur2_bd, phone_mob, email FROM main WHERE EXTRACT(MONTH FROM tur2_bd) = " + (new Date().getMonth()+1));
            while(rs.next()){
                dtm.addRow(new String[] {
                    rs.getString("id"),
                    reFormateDateForTable(rs.getString("tur2_bd")),
                    rs.getString("tur2_fio"),
                    rs.getString("phone_mob"),
                    rs.getString("email"),
                });
            }
            rs = db.executeQuery("SELECT id, tur3_fio, tur3_bd, phone_mob, email FROM main WHERE EXTRACT(MONTH FROM tur3_bd) = " + (new Date().getMonth()+1));
            while(rs.next()){
                dtm.addRow(new String[] {
                    rs.getString("id"),
                    reFormateDateForTable(rs.getString("tur3_bd")),
                    rs.getString("tur3_fio"),
                    rs.getString("phone_mob"),
                    rs.getString("email"),
                });
            }
            rs = db.executeQuery("SELECT id, tur4_fio, tur4_bd, phone_mob, email FROM main WHERE EXTRACT(MONTH FROM tur4_bd) = " + (new Date().getMonth()+1));
            while(rs.next()){
                dtm.addRow(new String[] {
                    rs.getString("id"),
                    reFormateDateForTable(rs.getString("tur4_bd")),
                    rs.getString("tur4_fio"),
                    rs.getString("phone_mob"),
                    rs.getString("email"),
                });
            }
            rs = db.executeQuery("SELECT id, tur5_fio, tur5_bd, phone_mob, email FROM main WHERE EXTRACT(MONTH FROM tur5_bd) = " + (new Date().getMonth()+1));
            while(rs.next()){
                dtm.addRow(new String[] {
                    rs.getString("id"),
                    reFormateDateForTable(rs.getString("tur5_bd")),
                    rs.getString("tur5_fio"),
                    rs.getString("phone_mob"),
                    rs.getString("email"),
                });
            }
            
            dtm.fireTableDataChanged();
            jTable_happy.getRowSorter().toggleSortOrder(1);
            
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }    
        
    }//GEN-LAST:event_jButton_happyActionPerformed

    private void jButton_happy_smsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_happy_smsActionPerformed
        // SMS на форме Happy
        int row = jTable_happy.getSelectedRow();
        if (row == -1) return;
        String sms = jTable_happy.getValueAt(row, 3) + "";
        if(sms.equalsIgnoreCase("") || sms.equalsIgnoreCase("null")) return;
        jTextField_sms_nomer.setText(sms);
        jFrame_sms.setVisible(true);
    }//GEN-LAST:event_jButton_happy_smsActionPerformed

    private void jButton_happy_mailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_happy_mailActionPerformed
        // Email на форме Happy
        int row = jTable_happy.getSelectedRow();
        if (row == -1) return;
        String mail = jTable_happy.getValueAt(row, 4) + "";
        if(mail.equalsIgnoreCase("") || mail.equalsIgnoreCase("null")) return;
        jTextField_mail_to.setText(mail);
        jFrame_mail.setVisible(true);
        if (jButton_mail_send.isEnabled()) {
            jButton_mail_suspend.setEnabled(false);
            jButton_mail_resume.setEnabled(false);
            jButton_mail_stop.setEnabled(false);
            jButton_mail_start.setEnabled(true);
        }
    }//GEN-LAST:event_jButton_happy_mailActionPerformed

    private void jButton_happy_clientActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_happy_clientActionPerformed
        // happy client
        int row = jTable_happy.getSelectedRow();
        if (row == -1) return;
        String id = jTable_happy.getValueAt(row, 0).toString();
        if (!id.equalsIgnoreCase("") || id.equalsIgnoreCase("null")) {
            jFrame_newComponentHidden(null);
            ResultSet rs = db.executeQuery("SELECT * FROM main WHERE id = " + id);
            fillFields_newClient_2(rs);
            jFrame_new.setVisible(true);
            jButton_new_save.setEnabled(false);
            SAVE(false);
        }
    }//GEN-LAST:event_jButton_happy_clientActionPerformed

    private void jMenuItem_opt_prodavetsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_opt_prodavetsActionPerformed
        // открыть форму настроек продавца
        jFrame_prodavets.setVisible(true);
        jButton_opt_prodavets_cancel.setVisible(false);
        jComboBox_opt_prodavets.setEnabled(false);
        jComboBox_opt_prodavets.removeAllItems();
        String query = "SELECT name FROM prodavets ORDER BY id";
        ResultSet rs = db.executeQuery(query);
        try {
            while (rs.next()) {
                jComboBox_opt_prodavets.addItem(rs.getString("name"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex);
        }
        jComboBox_opt_prodavets.setEnabled(true);
        jComboBox_opt_prodavetsItemStateChanged(null);
    }//GEN-LAST:event_jMenuItem_opt_prodavetsActionPerformed

    private void jMenuItem_opt_turoperatorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_opt_turoperatorActionPerformed
        // открыть форму настроек туроператоров
        jFrame_turoperator.setVisible(true);
        jComboBox_opt_turoperator.setEnabled(false);
        jComboBox_opt_turoperator.removeAllItems();
        String query = "SELECT name FROM turoperator ORDER BY id";
        ResultSet rs = db.executeQuery(query);
        try {
            while (rs.next()) {
                jComboBox_opt_turoperator.addItem(rs.getString("name"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex);
        }
        jComboBox_opt_turoperator.setEnabled(true);
        jButton_opt_turoperator_cancel.setVisible(false);
        jComboBox_opt_turoperatorItemStateChanged(null);
    }//GEN-LAST:event_jMenuItem_opt_turoperatorActionPerformed

    private void jComboBox_search_FIOItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox_search_FIOItemStateChanged
        // ПОИСК, комбобокс ФИО на панели поиска
        if (!combo_search) {
            return;
        }
        String fio = jComboBox_search_FIO.getSelectedItem().toString();
        StringTokenizer stk = new StringTokenizer(fio);
        String fam = "'" + stk.nextToken() + "'";
        String name = "'" + stk.nextToken() + "'";
        String otche = "'" + stk.nextToken() + "'";

        String query = "SELECT id, turoperator, pokupatel, tur_name, f_fam, f_name, f_otche, sale_date, last_price, tur_date_s, tur_date_po FROM main "
                + "WHERE f_fam = " + fam + " AND f_name=" + name + " AND f_otche=" + otche + "";
        ResultSet rs = db.executeQuery(query);
        DefaultTableModel dtm = (DefaultTableModel) jTable_search.getModel();
        while (dtm.getRowCount() > 0) {
            dtm.removeRow(dtm.getRowCount() - 1);
        }
        jTable_search.setModel(dtm);
        try {
            while (rs.next()) {
                dtm.addRow(new String[]{
                            rs.getString("id"),
                            rs.getString("turoperator"),
                            rs.getString("pokupatel"),
                            rs.getString("tur_name"),
                            rs.getString("f_fam") + " " + rs.getString("f_name") + " " + rs.getString("f_otche"),
                            reFormateDateForTable(rs.getString("sale_date")),
                            rs.getString("last_price"),
                            reFormateDateForTable(rs.getString("tur_date_s")),
                            reFormateDateForTable(rs.getString("tur_date_po")),});
            }
            dtm.fireTableDataChanged();
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }//GEN-LAST:event_jComboBox_search_FIOItemStateChanged

    private void jComboBox_prodavetsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox_prodavetsItemStateChanged
        // Смена продавца на форме клиента
        if (!jComboBox_prodavets.isEnabled()) return;
        //JOptionPane.showMessageDialog(null, "ffs");
        jComboBox_seller.removeAllItems();
        String query = "SELECT name FROM managers WHERE prodavets = '" + jComboBox_prodavets.getSelectedItem() + "' ORDER BY id";
        ResultSet rs = db2.executeQuery(query);
        try {
            while (rs.next()) {
                jComboBox_seller.addItem(rs.getString("name"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jComboBox_prodavetsItemStateChanged

    private void jComboBox_opt_prodavetsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox_opt_prodavetsItemStateChanged
        // опции продавец выбор продавца// когда меняется комбобокс с продавцем
        if (!jComboBox_opt_prodavets.isEnabled()) return;
        try {
            String query = "SELECT * FROM prodavets WHERE name = '" + jComboBox_opt_prodavets.getSelectedItem() + "'";
            ResultSet rs = db2.executeQuery(query);
            while (rs.next()) {
                jTable_prodavets.setValueAt(rs.getString("name"), 0, 1);
                jTable_prodavets.setValueAt(rs.getString("fullname"), 1, 1);
                jTable_prodavets.setValueAt(rs.getString("inn"), 2, 1);
                jTable_prodavets.setValueAt(rs.getString("kpp"), 3, 1);
                jTable_prodavets.setValueAt(rs.getString("ogrn"), 4, 1);
                jTable_prodavets.setValueAt(rs.getString("address_u"), 5, 1);
                jTable_prodavets.setValueAt(rs.getString("address_f"), 6, 1);
                jTable_prodavets.setValueAt(rs.getString("okved"), 7, 1);
                jTable_prodavets.setValueAt(rs.getString("okpo"), 8, 1);
                jTable_prodavets.setValueAt(rs.getString("okato"), 9, 1);
                jTable_prodavets.setValueAt(rs.getString("oktmo"), 10, 1);
                jTable_prodavets.setValueAt(rs.getString("okogu"), 11, 1);
                jTable_prodavets.setValueAt(rs.getString("okfs"), 12, 1);
                jTable_prodavets.setValueAt(rs.getString("okopf"), 13, 1);
                jTable_prodavets.setValueAt(rs.getString("phone"), 14, 1);
                jTable_prodavets.setValueAt(rs.getString("fax"), 15, 1);
                jTable_prodavets.setValueAt(rs.getString("email"), 16, 1);
                jTable_prodavets.setValueAt(rs.getString("director"), 17, 1);
                jTable_prodavets.setValueAt(rs.getString("r_schet"), 19, 1);
                jTable_prodavets.setValueAt(rs.getString("bank"), 20, 1);
                jTable_prodavets.setValueAt(rs.getString("k_schet"), 21, 1);
                jTable_prodavets.setValueAt(rs.getString("bik"), 22, 1);
                jTable_prodavets.setValueAt(rs.getString("dog_director"), 18, 1);
                //-----и т.д. остальные поля
            }//-----------------------------
            DefaultTableModel dtm = (DefaultTableModel) jTable_managers.getModel();
            while (dtm.getRowCount() > 0) {
                dtm.removeRow(dtm.getRowCount() - 1);
            }
            jTable_managers.setModel(dtm);
            query = "SELECT name, dog_name FROM managers WHERE prodavets = '" + jComboBox_opt_prodavets.getSelectedItem() + "' ORDER BY id";
            rs = db.executeQuery(query);
            while (rs.next()) {
                dtm.addRow(new String[]{rs.getString("name"),rs.getString("dog_name")});
            }
            dtm.fireTableDataChanged();
            //-----------------------
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }//GEN-LAST:event_jComboBox_opt_prodavetsItemStateChanged

    private void jComboBox_opt_turoperatorItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox_opt_turoperatorItemStateChanged
        // выбор туорператора на панели опций
        if (!jComboBox_opt_turoperator.isEnabled()) return;
        try {
            String query = "SELECT * FROM turoperator WHERE name = '" + jComboBox_opt_turoperator.getSelectedItem() + "'";
            ResultSet rs = db2.executeQuery(query);
            while (rs.next()) {
                jTable_opt_turoperator.setValueAt(rs.getString("name"), 0, 1);
                jTable_opt_turoperator.setValueAt(rs.getString("fullname"), 1, 1);
                jTable_opt_turoperator.setValueAt(rs.getString("reestr"), 2, 1);
                jTable_opt_turoperator.setValueAt(rs.getString("address_u"), 3, 1);
                jTable_opt_turoperator.setValueAt(rs.getString("address_p"), 4, 1);
                jTable_opt_turoperator.setValueAt(rs.getString("ogrn"), 5, 1);
                jTable_opt_turoperator.setValueAt(rs.getString("inn"), 6, 1);
                jTable_opt_turoperator.setValueAt(rs.getString("sposob"), 7, 1);
                jTable_opt_turoperator.setValueAt(rs.getString("razmer"), 8, 1);
                jTable_opt_turoperator.setValueAt(rs.getString("dogovor"), 9, 1);
                jTable_opt_turoperator.setValueAt(rs.getString("srok"), 10, 1);
                jTable_opt_turoperator.setValueAt(rs.getString("org_name"), 11, 1);
                jTable_opt_turoperator.setValueAt(rs.getString("org_address"), 12, 1);
                //--и т.д. остальные поля
            }//--------------------------
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }//GEN-LAST:event_jComboBox_opt_turoperatorItemStateChanged

    private void jButton_manager_addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_manager_addActionPerformed
        // добавить менеджера
        String manager = jTextField_manager.getText();
        String manager_dog = jTextField_manager_dog.getText();
        if (manager.equalsIgnoreCase("") || manager == null) return;
        if (manager_dog.equalsIgnoreCase("") || manager_dog == null) return;
        String prodavets = jComboBox_opt_prodavets.getSelectedItem().toString();
        db.executeUpdate("INSERT INTO managers VALUES(null, '" + prodavets + "','" + manager + "','" + manager_dog + "');");
        jTextField_manager.setText("");
        jTextField_manager_dog.setText("");
        jComboBox_opt_prodavetsItemStateChanged(null);
    }//GEN-LAST:event_jButton_manager_addActionPerformed

    private void jButton_prodavets_newActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_prodavets_newActionPerformed
        // Добавить нового продавца
        jComboBox_opt_prodavets.setEnabled(false);
        jComboBox_opt_prodavets.removeAllItems();
        jButton_prodavets_new.setEnabled(false);
        jButton_prodavets_save.setEnabled(true);
        jButton_prodavets_edit.setEnabled(false);
        jTabbedPane_prodavets.setSelectedIndex(0);
        jButton_opt_prodavets_cancel.setVisible(true);
        for (int i = 0; i < 23; i++){
            jTable_prodavets.setValueAt("", i, 1);
        }
       
    }//GEN-LAST:event_jButton_prodavets_newActionPerformed

    private void jButton_prodavets_saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_prodavets_saveActionPerformed
        // Сохранить продавца
        DBT_prodavets prodavets = new DBT_prodavets();
        
        prodavets.name = "'" + jTable_prodavets.getValueAt(0, 1).toString() + "',";
        prodavets.fullname = "'" + jTable_prodavets.getValueAt(1, 1).toString() + "',";
        prodavets.inn = "'" + jTable_prodavets.getValueAt(2, 1).toString() + "',";
        prodavets.kpp = "'" + jTable_prodavets.getValueAt(3, 1).toString() + "',";
        prodavets.ogrn = "'" + jTable_prodavets.getValueAt(4, 1).toString() + "',";
        prodavets.address_u = "'" + jTable_prodavets.getValueAt(5, 1).toString() + "',";
        prodavets.address_f = "'" + jTable_prodavets.getValueAt(6, 1).toString() + "',";
        prodavets.okved = "'" + jTable_prodavets.getValueAt(7, 1).toString() + "',";
        prodavets.okpo = "'" + jTable_prodavets.getValueAt(8, 1).toString() + "',";
        prodavets.okato = "'" + jTable_prodavets.getValueAt(9, 1).toString() + "',";
        prodavets.oktmo = "'" + jTable_prodavets.getValueAt(10, 1).toString() + "',";
        prodavets.okogu = "'" + jTable_prodavets.getValueAt(11, 1).toString() + "',";
        prodavets.okfs = "'" + jTable_prodavets.getValueAt(12, 1).toString() + "',";
        prodavets.okopf = "'" + jTable_prodavets.getValueAt(13, 1).toString() + "',";
        prodavets.phone = "'" + jTable_prodavets.getValueAt(14, 1).toString() + "',";
        prodavets.fax = "'" + jTable_prodavets.getValueAt(15, 1).toString() + "',";
        prodavets.email = "'" + jTable_prodavets.getValueAt(16, 1).toString() + "',";
        prodavets.director = "'" + jTable_prodavets.getValueAt(17, 1).toString() + "',";
        prodavets.r_schet = "'" + jTable_prodavets.getValueAt(19, 1).toString() + "',";
        prodavets.bank = "'" + jTable_prodavets.getValueAt(20, 1).toString() + "',";
        prodavets.k_schet = "'" + jTable_prodavets.getValueAt(21, 1).toString() + "',";
        prodavets.bik = "'" + jTable_prodavets.getValueAt(22, 1).toString() + "',";
        prodavets.dog_director = "'" + jTable_prodavets.getValueAt(18, 1).toString() + "'";
        
        if (jComboBox_opt_prodavets.getItemCount() == 0) {
            // Добавить нового продавца
            db.executeUpdate("INSERT INTO prodavets VALUES(null," + prodavets.name + prodavets.fullname
                    + prodavets.inn + prodavets.kpp + prodavets.ogrn
                    + prodavets.address_u + prodavets.address_f + prodavets.okved
                    + prodavets.okpo + prodavets.okato + prodavets.oktmo + prodavets.okogu
                    + prodavets.okfs + prodavets.okopf + prodavets.phone + prodavets.fax
                    + prodavets.email + prodavets.director + prodavets.r_schet + prodavets.bank
                    + prodavets.k_schet + prodavets.bik + prodavets.dog_director + ");");
        } else {
            //Изменить существующего продавца
            db.executeUpdate("UPDATE prodavets SET "
                    + "name = " + prodavets.name 
                    + "fullname = " + prodavets.fullname
                    + "inn = " + prodavets.inn
                    + "kpp = " + prodavets.kpp
                    + "ogrn = " + prodavets.ogrn
                    + "address_u = " + prodavets.address_u
                    + "address_f = " + prodavets.address_f
                    + "okved = " + prodavets.okved
                    + "okpo = " + prodavets.okpo
                    + "okato = " + prodavets.okato
                    + "oktmo = " + prodavets.oktmo
                    + "okogu = " + prodavets.okogu 
                    + "okfs = " + prodavets.okfs
                    + "okopf = " + prodavets.okopf
                    + "phone = " + prodavets.phone
                    + "fax = " + prodavets.fax
                    + "email = " + prodavets.email
                    + "director = " + prodavets.director
                    + "r_schet = " + prodavets.r_schet
                    + "bank = " + prodavets.bank
                    + "k_schet = " + prodavets.k_schet
                    + "bik = " + prodavets.bik
                    + "dog_director = " + prodavets.dog_director
                    + " WHERE name = '" + jComboBox_opt_prodavets.getSelectedItem() + "'");
            
            db.executeUpdate("UPDATE managers SET prodavets = '" + jTable_prodavets.getValueAt(0, 1).toString() + "' WHERE prodavets = '" + jComboBox_opt_prodavets.getSelectedItem() + "'");
        }
        jButton_prodavets_new.setEnabled(true);
        jComboBox_opt_prodavets.setEnabled(true);
        jButton_prodavets_save.setEnabled(false);
        jButton_prodavets_edit.setEnabled(true);
        jButton_opt_prodavets_cancel.setVisible(false);
        jMenuItem_opt_prodavetsActionPerformed(null);
    }//GEN-LAST:event_jButton_prodavets_saveActionPerformed

    private void jButton_prodavets_editActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_prodavets_editActionPerformed
        // изменить продавца
        jButton_opt_prodavets_cancel.setVisible(true);
        jComboBox_opt_prodavets.setEnabled(false);
        jButton_prodavets_new.setEnabled(false);
        jButton_prodavets_save.setEnabled(true);
        jButton_prodavets_edit.setEnabled(false);
    }//GEN-LAST:event_jButton_prodavets_editActionPerformed

    private void jButton_turoperator_newActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_turoperator_newActionPerformed
        // дОБАВИТЬ ТУРОПЕРАТОРА
        jButton_turoperator_new.setEnabled(false);
        jButton_turoperator_edit.setEnabled(false);
        jButton_turoperator_save.setEnabled(true);
        jComboBox_opt_turoperator.removeAllItems();
        jComboBox_opt_turoperator.setEnabled(false);
        jButton_opt_turoperator_cancel.setVisible(true);
        for (int i = 0; i < 13; i++){
            jTable_opt_turoperator.setValueAt("", i, 1);
        }
    }//GEN-LAST:event_jButton_turoperator_newActionPerformed

    private void jButton_turoperator_editActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_turoperator_editActionPerformed
        // Изменить туроператора:
        jButton_turoperator_new.setEnabled(false);
        jButton_turoperator_edit.setEnabled(false);
        jButton_turoperator_save.setEnabled(true);
        jComboBox_opt_turoperator.setEnabled(false);
        jButton_opt_turoperator_cancel.setVisible(true);
    }//GEN-LAST:event_jButton_turoperator_editActionPerformed

    private void jButton_turoperator_saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_turoperator_saveActionPerformed
        // Сохранить туроператора
            DBT_turoperator turoperator = new DBT_turoperator();
            
            turoperator.name = "'" + jTable_opt_turoperator.getValueAt(0, 1).toString() + "',";
            turoperator.fullname = "'" + jTable_opt_turoperator.getValueAt(1, 1).toString() + "',";
            turoperator.reestr = "'" + jTable_opt_turoperator.getValueAt(2, 1).toString() + "',";
            turoperator.address_u = "'" + jTable_opt_turoperator.getValueAt(3, 1).toString() + "',";
            turoperator.address_p = "'" + jTable_opt_turoperator.getValueAt(4, 1).toString() + "',";
            turoperator.ogrn = "'" + jTable_opt_turoperator.getValueAt(5, 1).toString() + "',";
            turoperator.inn = "'" + jTable_opt_turoperator.getValueAt(6, 1).toString() + "',";
            turoperator.sposob = "'" + jTable_opt_turoperator.getValueAt(7, 1).toString() + "',";
            turoperator.razmer = "'" + jTable_opt_turoperator.getValueAt(8, 1).toString() + "',";
            turoperator.dogovor = "'" + jTable_opt_turoperator.getValueAt(9, 1).toString() + "',";
            turoperator.srok = "'" + jTable_opt_turoperator.getValueAt(10, 1).toString() + "',";
            turoperator.org_name = "'" + jTable_opt_turoperator.getValueAt(11, 1).toString() + "',";
            turoperator.org_address = "'" + jTable_opt_turoperator.getValueAt(12, 1).toString() + "'";
            
        if (jComboBox_opt_turoperator.getItemCount() == 0) {
            // Добавить нового туроператора
            db.executeUpdate("INSERT INTO turoperator VALUES(null," + turoperator.name + turoperator.fullname + turoperator.reestr 
                    + turoperator.address_u + turoperator.address_p + turoperator.ogrn + turoperator.inn + turoperator.sposob 
                    + turoperator.razmer + turoperator.dogovor + turoperator.srok + turoperator.org_name + turoperator.org_address
                    + ");");
        } else {
            //Изменить существующего туроператора
            db.executeUpdate("UPDATE turoperator SET "
                    + "name = " + turoperator.name  
                    + "fullname = " + turoperator.fullname
                    + "reestr = " + turoperator.reestr
                    + "address_u = " + turoperator.address_u
                    + "address_p = " + turoperator.address_p
                    + "ogrn = " + turoperator.ogrn
                    + "inn = " + turoperator.inn
                    + "sposob = " + turoperator.sposob
                    + "razmer = " + turoperator.razmer
                    + "dogovor = " + turoperator.dogovor
                    + "srok = " + turoperator.srok
                    + "org_name = " + turoperator.org_name
                    + "org_address = " + turoperator.org_address
                    + " WHERE name = '" + jComboBox_opt_turoperator.getSelectedItem() + "'");
        }
        jButton_turoperator_new.setEnabled(true);
        jButton_turoperator_edit.setEnabled(true);
        jButton_turoperator_save.setEnabled(false);
        jComboBox_opt_turoperator.setEnabled(true);
        jButton_opt_turoperator_cancel.setVisible(false);
        jMenuItem_opt_turoperatorActionPerformed(null);
    }//GEN-LAST:event_jButton_turoperator_saveActionPerformed

    private void jButton_opt_prodavets_cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_opt_prodavets_cancelActionPerformed
        // отменить ввод нового продавца или редактирование старого
        jButton_prodavets_new.setEnabled(true);
        jComboBox_opt_prodavets.setEnabled(true);
        jButton_prodavets_save.setEnabled(false);
        jButton_prodavets_edit.setEnabled(true);
        jButton_opt_prodavets_cancel.setVisible(false);
        jMenuItem_opt_prodavetsActionPerformed(null);
    }//GEN-LAST:event_jButton_opt_prodavets_cancelActionPerformed

    private void jButton_opt_turoperator_cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_opt_turoperator_cancelActionPerformed
        // отменить ввод нового или редактир. старого туроператора
        jButton_turoperator_new.setEnabled(true);
        jButton_turoperator_edit.setEnabled(true);
        jButton_turoperator_save.setEnabled(false);
        jComboBox_opt_turoperator.setEnabled(true);
        jButton_opt_turoperator_cancel.setVisible(false);
        jMenuItem_opt_turoperatorActionPerformed(null);
    }//GEN-LAST:event_jButton_opt_turoperator_cancelActionPerformed

    private void jFrame_prodavetsComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jFrame_prodavetsComponentHidden
        // закрывается окно продавцев
        jButton_prodavets_new.setEnabled(true);
        jComboBox_opt_prodavets.setEnabled(true);
        jButton_prodavets_save.setEnabled(false);
        jButton_prodavets_edit.setEnabled(true);
        jButton_opt_prodavets_cancel.setVisible(false);
    }//GEN-LAST:event_jFrame_prodavetsComponentHidden

    private void jFrame_turoperatorComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jFrame_turoperatorComponentHidden
        // закрывается окно туроператоров
        jButton_turoperator_new.setEnabled(true);
        jButton_turoperator_edit.setEnabled(true);
        jButton_turoperator_save.setEnabled(false);
        jComboBox_opt_turoperator.setEnabled(true);
        jButton_opt_turoperator_cancel.setVisible(false);
    }//GEN-LAST:event_jFrame_turoperatorComponentHidden

    private void jButton_new_dogovorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_new_dogovorActionPerformed
        // Печать Договр
        String rtfTarget_putevka = "Данные о клиентах/Договора/d" + jLabel_tur_num.getText() + "_" + jTextField_f_fam.getText() + "_" + jLabel_edited_count.getText() + ".rtf";
   /*
        String rtfSource_putevka = "templates/dogovor.rtf";
        
        RTF rtf = new RTF();
        rtf.putDate_dogovor(rtfSource_putevka, rtfTarget_putevka, getFields_newClient_2(), getFields_newClient_prodavets(), getFields_newClient_managers(), getFields_newClient_turoperator());
    */
         try{
            java.awt.Desktop.getDesktop().open(new File(rtfTarget_putevka));
           
        } catch (Exception ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }//GEN-LAST:event_jButton_new_dogovorActionPerformed

    private void jButton_new_prilozhenieActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_new_prilozhenieActionPerformed
        // Печать Приложение добавить Продавца
        String rtfTarget_putevka = "Данные о клиентах/Приложения/p" + jLabel_tur_num.getText() + "_" + jTextField_f_fam.getText() + "_" + jLabel_edited_count.getText() + ".rtf";
    /*
        String rtfSource_putevka = "templates/prilozhenie.rtf";
        
        RTF rtf = new RTF();
        rtf.putDate(rtfSource_putevka, rtfTarget_putevka, getFields_newClient_2(), getFields_newClient_prodavets());
     */
         try{
            java.awt.Desktop.getDesktop().open(new File(rtfTarget_putevka));
           
        } catch (Exception ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }//GEN-LAST:event_jButton_new_prilozhenieActionPerformed

    private void jButton_new_pkoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_new_pkoActionPerformed
        // Печать ПКО
        String rtfTarget_putevka = "Данные о клиентах/ПКО/k" + jLabel_tur_num.getText() + "_" + jTextField_f_fam.getText() + "_" + jLabel_edited_count.getText() + ".rtf";
   /*
        String rtfSource_putevka = "templates/pko.rtf";
        
        RTF rtf = new RTF();
        rtf.putDate(rtfSource_putevka, rtfTarget_putevka, getFields_newClient_2(), getFields_newClient_prodavets());
     */
         try{
            java.awt.Desktop.getDesktop().open(new File(rtfTarget_putevka));
           
        } catch (Exception ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }//GEN-LAST:event_jButton_new_pkoActionPerformed

    private void jButton_new_putevkaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_new_putevkaActionPerformed
        // Печать Путевка  добавить поле Продавец
        String rtfTarget_putevka = "Данные о клиентах/Путевки/v" + jLabel_tur_num.getText() + "_" + jTextField_f_fam.getText() + "_" + jLabel_edited_count.getText() + ".rtf";
    /*
        String rtfSource_putevka = "templates/putevka.rtf";
        
        RTF rtf = new RTF();
        rtf.putDate(rtfSource_putevka, rtfTarget_putevka, getFields_newClient_2(), getFields_newClient_prodavets());
     */
         try{
            java.awt.Desktop.getDesktop().open(new File(rtfTarget_putevka));
           
        } catch (Exception ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }//GEN-LAST:event_jButton_new_putevkaActionPerformed

    private void jButton_new_fortunaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_new_fortunaActionPerformed
        // Печать Фортуна
        String rtfTarget_putevka = "Данные о клиентах/Фортуна/f" + jLabel_tur_num.getText() + "_" + jTextField_f_fam.getText() + "_" + jLabel_edited_count.getText() + ".rtf";
   /* 
        String rtfSource_putevka = "templates/fortuna.rtf";
        
        RTF rtf = new RTF();
        rtf.putDate(rtfSource_putevka, rtfTarget_putevka, getFields_newClient_2(), getFields_newClient_prodavets());
     */
         try{
            java.awt.Desktop.getDesktop().open(new File(rtfTarget_putevka));
           
        } catch (Exception ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }//GEN-LAST:event_jButton_new_fortunaActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        // Открыть окно опций
        jTextField_db_user.setText(db_user);
        jTextField_db_pass.setText(db_pass);
        jTextField_db_host.setText(db_host);
        jTextField_db_base.setText(db_base);

        jTextField_mail_from.setText(mail_from);
        jTextField_mail_smtp.setText(mail_smtp);
        jTextField_mail_smtp_user.setText(mail_smtp_user);
        jTextField_mail_smtp_pass.setText(mail_smtp_pass);       

        jTextField_sms_to.setText(sms_to);
        jTextField_sms_from.setText(sms_from);
        jTextField_sms_smtp.setText(sms_smtp);
        jTextField_sms_smtp_user.setText(sms_smtp_user);
        jTextField_sms_smtp_pass.setText(sms_smtp_pass);
        jTextField_sms_spam_login.setText(sms_spam_login);
        jTextField_sms_spam_pass.setText(sms_spam_pass);
   
        jFrame_options.setVisible(true);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jButton_prop_saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_prop_saveActionPerformed
        // Сохранить опции
        Properties prop = new Properties();
        prop.put("db_user",jTextField_db_user.getText());
        prop.put("db_pass",jTextField_db_pass.getText());
        prop.put("db_host",jTextField_db_host.getText());
        prop.put("db_base",jTextField_db_base.getText());
        prop.put("mail_from",jTextField_mail_from.getText());
        prop.put("mail_smtp",jTextField_mail_smtp.getText());
        prop.put("mail_smtp_user",jTextField_mail_smtp_user.getText());
        prop.put("mail_smtp_pass",jTextField_mail_smtp_pass.getText());
        prop.put("sms_to",jTextField_sms_to.getText());
        prop.put("sms_from",jTextField_sms_from.getText());
        prop.put("sms_smtp",jTextField_sms_smtp.getText());
        prop.put("sms_smtp_user",jTextField_sms_smtp_user.getText());
        prop.put("sms_smtp_pass",jTextField_sms_smtp_pass.getText());
        prop.put("sms_spam_login",jTextField_sms_spam_login.getText());
        prop.put("sms_spam_pass",jTextField_sms_spam_pass.getText());
        
        Cfg.saveProperties(new File("properties.cfg"), prop);
        setProp(new File("properties.cfg"));
    }//GEN-LAST:event_jButton_prop_saveActionPerformed

    private void jButton_prop_defaultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_prop_defaultActionPerformed
        // Загрузить настройки по умолчанию
        setProp(new File("default.cfg"));
        jMenuItem2ActionPerformed(null);
    }//GEN-LAST:event_jButton_prop_defaultActionPerformed

    private void jButton_reportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_reportActionPerformed
        // открыть таблицу отчета
        DefaultTableModel model = new DefaultTableModel();
        jTable_report.setModel(model);
        model.addColumn("№");
        
        if(jCheckBox_r_fam.isSelected()) model.addColumn("Фамилия");
        if(jCheckBox_r_phone.isSelected()) model.addColumn("Телефон");
        if(jCheckBox_r_mail.isSelected()) model.addColumn("Email");
        if(jCheckBox_r_tur_data.isSelected()) model.addColumn("Дата поездки");
        if(jCheckBox_r_price.isSelected()) model.addColumn("Стоимость");
        
        if(jCheckBox_r_turoperator.isSelected()) model.addColumn("Туроператор");
        if(jCheckBox_r_prodavets.isSelected()) model.addColumn("Продавец");
        if(jCheckBox_r_pokupatel.isSelected()) model.addColumn("Покупатель");
        if(jCheckBox_r_tur_name.isSelected()) model.addColumn("Тур");
        if(jCheckBox_r_tur_begin.isSelected()) model.addColumn("Откуда");
        if(jCheckBox_r_tur_punkt.isSelected()) model.addColumn("Пункт");
        if(jCheckBox_r_manager.isSelected()) model.addColumn("Менеджер");
        
        if (model.getColumnCount() == 1) {
            JOptionPane.showMessageDialog(null, "Выберите параметры отчета.");
            return;
        }
   
        String date_s = reFormateDateForSQL(jDateChooser_report_s.getDate());
        String date_po = reFormateDateForSQL(jDateChooser_report_po.getDate());
        
        String period = "";
        if(!date_s.equalsIgnoreCase("null") && !date_po.equalsIgnoreCase("null")) period = "WHERE DATE(sale_date) >= " + date_s + " AND DATE(sale_date) <= " + date_po;
        if(!date_s.equalsIgnoreCase("null") && date_po.equalsIgnoreCase("null")) period = "WHERE DATE(sale_date) >= " + date_s;
        if(date_s.equalsIgnoreCase("null") && !date_po.equalsIgnoreCase("null")) period = "WHERE DATE(sale_date) <= " + date_po;
        
        String turoperator = jComboBox_r_turoperator.getSelectedItem().toString();
        if(turoperator.equalsIgnoreCase("Все")) {
            turoperator = "";
        } else {
            if(period.equalsIgnoreCase("")) turoperator = "WHERE turoperator = '" + turoperator + "'";
            else turoperator = " AND turoperator = '" + turoperator + "'";
        }
        
        String prodavets = jComboBox_r_prodavets.getSelectedItem().toString();
        if(prodavets.equalsIgnoreCase("Все")) {
            prodavets = "";
        } else {
            if(period.equalsIgnoreCase("") && turoperator.equalsIgnoreCase("")) 
                prodavets = "WHERE prodavets = '" + prodavets + "'";
            else prodavets = " AND prodavets = '" + prodavets + "'";
        }
        
        String tur_name = jComboBox_r_tur_name.getSelectedItem().toString();
        if(tur_name.equalsIgnoreCase("Все")) {
            tur_name = "";
        } else {
            if(period.equalsIgnoreCase("") && turoperator.equalsIgnoreCase("") 
                    && prodavets.equalsIgnoreCase("")) tur_name = "WHERE tur_name = '" + tur_name + "'";
            else tur_name = " AND tur_name = '" + tur_name + "'";
        }
        
        String tur_begin = jComboBox_r_tur_begin.getSelectedItem().toString();
        if(tur_begin.equalsIgnoreCase("Все")) {
            tur_begin = "";
        } else {
            if(period.equalsIgnoreCase("") && turoperator.equalsIgnoreCase("") 
                    && prodavets.equalsIgnoreCase("") && tur_name.equalsIgnoreCase("")) 
                tur_begin = "WHERE tur_begin = '" + tur_begin + "'";
            else tur_begin = " AND tur_begin = '" + tur_begin + "'";
        }
        
        String tur_punkt = jComboBox_r_tur_punkt.getSelectedItem().toString();
        if(tur_punkt.equalsIgnoreCase("Все")) {
            tur_punkt = "";
        } else {
            if(period.equalsIgnoreCase("") && turoperator.equalsIgnoreCase("") 
                    && prodavets.equalsIgnoreCase("") && tur_name.equalsIgnoreCase("") 
                    && tur_begin.equalsIgnoreCase("")) 
                tur_punkt = "WHERE tur_punkt = '" + tur_punkt + "'";
            else tur_punkt = " AND tur_punkt = '" + tur_punkt + "'";
        }
        
        String seller = jComboBox_r_manager.getSelectedItem().toString();
        if(seller.equalsIgnoreCase("Все")) {
            seller = "";
        } else {
            if(period.equalsIgnoreCase("") && turoperator.equalsIgnoreCase("") 
                    && prodavets.equalsIgnoreCase("") && tur_name.equalsIgnoreCase("") 
                    && tur_begin.equalsIgnoreCase("") && tur_punkt.equalsIgnoreCase("")) 
                seller = "WHERE seller = '" + seller + "'";
            else seller = " AND seller = '" + seller + "'";
        }
        
        String pokupatel = jComboBox_r_pokupatel.getSelectedItem().toString();
        if(pokupatel.equalsIgnoreCase("Все")) {
            pokupatel = "";
        } else if(pokupatel.equalsIgnoreCase("Только туристы")) {
            if(period.equalsIgnoreCase("") && turoperator.equalsIgnoreCase("") 
                    && prodavets.equalsIgnoreCase("") && tur_name.equalsIgnoreCase("") 
                    && tur_begin.equalsIgnoreCase("") && tur_punkt.equalsIgnoreCase("")
                    && seller.equalsIgnoreCase("")) 
                pokupatel = "WHERE pokupatel = 'Турист'";
            else pokupatel = " AND pokupatel = 'Турист'";
        } else if(pokupatel.equalsIgnoreCase("Только агенты")) {
            if(period.equalsIgnoreCase("") && turoperator.equalsIgnoreCase("") 
                    && prodavets.equalsIgnoreCase("") && tur_name.equalsIgnoreCase("") 
                    && tur_begin.equalsIgnoreCase("") && tur_punkt.equalsIgnoreCase("")
                    && seller.equalsIgnoreCase("")) 
                pokupatel = "WHERE pokupatel != 'Турист'";
            else pokupatel = " AND pokupatel != 'Турист'";
        } else {
            if(period.equalsIgnoreCase("") && turoperator.equalsIgnoreCase("") 
                    && prodavets.equalsIgnoreCase("") && tur_name.equalsIgnoreCase("") 
                    && tur_begin.equalsIgnoreCase("") && tur_punkt.equalsIgnoreCase("")
                    && seller.equalsIgnoreCase("")) 
                pokupatel = "WHERE pokupatel = '" + pokupatel + "'";
            else pokupatel = " AND pokupatel = '" + pokupatel + "'";
        }
        
        String tur_date_s = "";
        if(jCheckBox_r_tur_data.isSelected()) {
            tur_date_s = reFormateDateForSQL(jDateChooser_r_tur_date_s.getDate());
            if(tur_date_s.equalsIgnoreCase("null")) {
                tur_date_s = " ORDER BY tur_date_s";
            } else {
                if(period.equalsIgnoreCase("") && turoperator.equalsIgnoreCase("") 
                        && prodavets.equalsIgnoreCase("") && tur_name.equalsIgnoreCase("") 
                        && tur_begin.equalsIgnoreCase("") && tur_punkt.equalsIgnoreCase("")
                        && seller.equalsIgnoreCase("") && pokupatel.equalsIgnoreCase(""))
                    tur_date_s = "WHERE DATE(tur_date_s) >= " + tur_date_s + " ORDER BY tur_date_s";
                else tur_date_s = " AND DATE(tur_date_s) >= " + tur_date_s + " ORDER BY tur_date_s";
            }
        }         
        
        String query = "SELECT * FROM main " + period + turoperator + prodavets + tur_name + tur_begin + tur_punkt + seller + pokupatel + tur_date_s;
        //JOptionPane.showMessageDialog(null, query);
        ResultSet rs = db.executeQuery(query);
        int size = 0;
        int col = model.getColumnCount();
        try {
            while(rs.next()) {
                int column = 0;
                size++;
                model.addRow(new Object[col]);
                jTable_report.setValueAt(zero(size), size-1, column++);
                if(jCheckBox_r_fam.isSelected()) jTable_report.setValueAt(rs.getString("f_fam") + " " + rs.getString("f_name") + " " + rs.getString("f_otche"), size-1, column++);
                if(jCheckBox_r_phone.isSelected()) jTable_report.setValueAt(rs.getString("phone_dom") + ", " + rs.getString("phone_rab") + ", " + rs.getString("phone_mob"), size-1, column++);
                if(jCheckBox_r_mail.isSelected()) jTable_report.setValueAt(rs.getString("email"), size-1, column++);
                if(jCheckBox_r_tur_data.isSelected()) jTable_report.setValueAt(reFormateDateForTable(rs.getString("tur_date_s")) + " - " + reFormateDateForTable(rs.getString("tur_date_po")), size-1, column++);
                if(jCheckBox_r_price.isSelected()) jTable_report.setValueAt(rs.getString("last_price"), size-1, column++);
                if(jCheckBox_r_turoperator.isSelected()) jTable_report.setValueAt(rs.getString("turoperator"), size-1, column++);
                if(jCheckBox_r_prodavets.isSelected()) jTable_report.setValueAt(rs.getString("prodavets"), size-1, column++);
                if(jCheckBox_r_pokupatel.isSelected()) jTable_report.setValueAt(rs.getString("pokupatel"), size-1, column++);
                if(jCheckBox_r_tur_name.isSelected()) jTable_report.setValueAt(rs.getString("tur_name"), size-1, column++);
                if(jCheckBox_r_tur_begin.isSelected()) jTable_report.setValueAt(rs.getString("tur_begin"), size-1, column++);
                if(jCheckBox_r_tur_punkt.isSelected()) jTable_report.setValueAt(rs.getString("tur_punkt"), size-1, column++);
                if(jCheckBox_r_manager.isSelected()) jTable_report.setValueAt(rs.getString("seller"), size-1, column++);
            
            }
           if(jCheckBox_r_price.isSelected()) {
                int pc = -1;
                for(int i=0; i<col; i++){
                    if(model.getColumnName(i).equalsIgnoreCase("Стоимость")) pc = i;
                }
                if (pc != -1){
                        int price_all = 0;
                        for(int i=0; i<model.getRowCount(); i++){
                            int pri;
                            try {
                                pri = Integer.parseInt(jTable_report.getValueAt(i, pc).toString());
                            } catch (Exception e) {
                                pri = 0;
                            }
                            price_all += pri;
                        }
                    model.addRow(new Object[col]);
                    for(int i=0; i<col; i++){
                        jTable_report.setValueAt("", size, i);
                    }
                    jTable_report.setValueAt("Сумма: " + price_all, size, pc);
                }
           }
           
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
        
        jFrame_report_table.setVisible(true);
    }//GEN-LAST:event_jButton_reportActionPerformed

    private void jButton_save_ExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_save_ExcelActionPerformed
        //Сохранить таблицу отчета в Excel
        String today = reFormateDate(new Date(), "yyyy.MM.dd - HH.mm.ss");
        ExcelExporter excel = new ExcelExporter();
        excel.fillData(jTable_report, new File("Отчеты/"+ today +".xls"));
        if(jCheckBox_report_open.isSelected()) {
            try {
                java.awt.Desktop.getDesktop().open(new File("Отчеты/"+ today +".xls"));
            } catch (IOException ex) {
                Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null, ex.getMessage());
            }
        }
    }//GEN-LAST:event_jButton_save_ExcelActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        jDateChooser_report_s.setDate(null);
        jDateChooser_report_po.setDate(null);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jCheckBox_r_turoperatorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox_r_turoperatorActionPerformed
        jComboBox_r_turoperator.removeAllItems();
        jComboBox_r_turoperator.addItem("Все");
        ResultSet rs = db.executeQuery("SELECT name FROM turoperator");
        try {
            while (rs.next()) {
                jComboBox_r_turoperator.addItem(rs.getString("name"));                
            }
            jComboBox_r_turoperator.setEnabled(jCheckBox_r_turoperator.isSelected());
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }//GEN-LAST:event_jCheckBox_r_turoperatorActionPerformed

    private void jCheckBox_r_prodavetsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox_r_prodavetsActionPerformed
        jComboBox_r_prodavets.removeAllItems();
        jComboBox_r_prodavets.addItem("Все");
        ResultSet rs = db.executeQuery("SELECT name FROM prodavets");
        try {
            while (rs.next()) {
                jComboBox_r_prodavets.addItem(rs.getString("name"));                
            }
            jComboBox_r_prodavets.setEnabled(jCheckBox_r_prodavets.isSelected());
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }//GEN-LAST:event_jCheckBox_r_prodavetsActionPerformed

    private void jCheckBox_r_tur_nameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox_r_tur_nameActionPerformed
        jComboBox_r_tur_name.removeAllItems();
        jComboBox_r_tur_name.addItem("Все");
        ResultSet rs = db.executeQuery("SELECT tur_name FROM main");
        LinkedHashSet hashSet = new LinkedHashSet();
        try {
            while (rs.next()) {
                 hashSet.add(rs.getString("tur_name"));
            }
            Iterator itr = hashSet.iterator();
            while(itr.hasNext()){
                jComboBox_r_tur_name.addItem(itr.next());
            }
            jComboBox_r_tur_name.setEnabled(jCheckBox_r_tur_name.isSelected());
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }//GEN-LAST:event_jCheckBox_r_tur_nameActionPerformed

    private void jCheckBox_r_tur_beginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox_r_tur_beginActionPerformed
        jComboBox_r_tur_begin.removeAllItems();
        jComboBox_r_tur_begin.addItem("Все");
        ResultSet rs = db.executeQuery("SELECT tur_begin FROM main");
        LinkedHashSet hashSet = new LinkedHashSet();
        try {
            while (rs.next()) {
                 hashSet.add(rs.getString("tur_begin"));
            }
            Iterator itr = hashSet.iterator();
            while(itr.hasNext()){
                jComboBox_r_tur_begin.addItem(itr.next());
            }
            jComboBox_r_tur_begin.setEnabled(jCheckBox_r_tur_begin.isSelected());
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }//GEN-LAST:event_jCheckBox_r_tur_beginActionPerformed

    private void jCheckBox_r_tur_punktActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox_r_tur_punktActionPerformed
        jComboBox_r_tur_punkt.removeAllItems();
        jComboBox_r_tur_punkt.addItem("Все");
        ResultSet rs = db.executeQuery("SELECT tur_punkt FROM main");
        LinkedHashSet hashSet = new LinkedHashSet();
        try {
            while (rs.next()) {
                 hashSet.add(rs.getString("tur_punkt"));
            }
            Iterator itr = hashSet.iterator();
            while(itr.hasNext()){
                jComboBox_r_tur_punkt.addItem(itr.next());
            }
            jComboBox_r_tur_punkt.setEnabled(jCheckBox_r_tur_punkt.isSelected());
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }//GEN-LAST:event_jCheckBox_r_tur_punktActionPerformed

    private void jCheckBox_r_managerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox_r_managerActionPerformed
        jComboBox_r_manager.removeAllItems();
        jComboBox_r_manager.addItem("Все");
        ResultSet rs = db.executeQuery("SELECT name FROM managers");
        LinkedHashSet hashSet = new LinkedHashSet();
        try {
            while (rs.next()) {
                 hashSet.add(rs.getString("name"));
            }
            Iterator itr = hashSet.iterator();
            while(itr.hasNext()){
                jComboBox_r_manager.addItem(itr.next());
            }
            jComboBox_r_manager.setEnabled(jCheckBox_r_manager.isSelected());
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }//GEN-LAST:event_jCheckBox_r_managerActionPerformed

    private void jCheckBox_r_allActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox_r_allActionPerformed
        jCheckBox_r_fam.setSelected(jCheckBox_r_all.isSelected());
        jCheckBox_r_phone.setSelected(jCheckBox_r_all.isSelected());
        jCheckBox_r_mail.setSelected(jCheckBox_r_all.isSelected());
        jCheckBox_r_price.setSelected(jCheckBox_r_all.isSelected());
        jCheckBox_r_tur_data.setSelected(jCheckBox_r_all.isSelected());
        jDateChooser_r_tur_date_s.setEnabled(jCheckBox_r_all.isSelected());
        jDateChooser_r_tur_date_s.setDate(null);
        
        jCheckBox_r_turoperator.setSelected(jCheckBox_r_all.isSelected());
        jCheckBox_r_turoperatorActionPerformed(null);
        jCheckBox_r_prodavets.setSelected(jCheckBox_r_all.isSelected());
        jCheckBox_r_prodavetsActionPerformed(null);
        jCheckBox_r_tur_name.setSelected(jCheckBox_r_all.isSelected());
        jCheckBox_r_tur_nameActionPerformed(null);
        jCheckBox_r_tur_begin.setSelected(jCheckBox_r_all.isSelected());
        jCheckBox_r_tur_beginActionPerformed(null);
        jCheckBox_r_tur_punkt.setSelected(jCheckBox_r_all.isSelected());
        jCheckBox_r_tur_punktActionPerformed(null);
        jCheckBox_r_manager.setSelected(jCheckBox_r_all.isSelected());
        jCheckBox_r_managerActionPerformed(null);
        jCheckBox_r_pokupatel.setSelected(jCheckBox_r_all.isSelected());
        jCheckBox_r_pokupatelActionPerformed(null);
        
    }//GEN-LAST:event_jCheckBox_r_allActionPerformed

    private void jComboBox_tur_nameItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox_tur_nameItemStateChanged
        // Выбор тура на форме нового клиента
        if(!jComboBox_tur_name.isEnabled()) return;
        if(jComboBox_tur_name.getSelectedItem() == null) return;
        if(jComboBox_tur_name.getSelectedItem().toString().equalsIgnoreCase("")){
            clear_tur();
        } else {
            fill_tur(db.executeQuery("SELECT * FROM turs WHERE name = '" + jComboBox_tur_name.getSelectedItem().toString() + "'"));
        }
    }//GEN-LAST:event_jComboBox_tur_nameItemStateChanged

    private void jComboBox_opt_tursItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox_opt_tursItemStateChanged
        // выбор тура на панели опций
        if (!jComboBox_opt_turs.isEnabled()) return;
        jTable_turs.selectAll();
        jTable_turs.setRowSelectionInterval(0, 0);
        try {
            String query = "SELECT * FROM turs WHERE name = '" + jComboBox_opt_turs.getSelectedItem() + "'";
            ResultSet rs = db2.executeQuery(query);
            while (rs.next()) {
                jTable_turs.setValueAt(rs.getString("turoperator"), 0, 1);
                jTable_turs.setValueAt(rs.getString("name"), 1, 1);
                jTable_turs.setValueAt(rs.getString("begin"), 2, 1);
                jTable_turs.setValueAt(rs.getString("punkt"), 3, 1);
                jTable_turs.setValueAt(rs.getString("end"), 4, 1);
                jTable_turs.setValueAt(rs.getString("bilet"), 5, 1);
                jTable_turs.setValueAt(rs.getString("hotel"), 6, 1);
                jTable_turs.setValueAt(rs.getString("room"), 7, 1);
                jTable_turs.setValueAt(rs.getString("food"), 8, 1);
                jTable_turs.setValueAt(rs.getString("transfer"), 9, 1);
                jTable_turs.setValueAt(parseSQL_bool(rs.getString("gid")), 10, 1);
                jTable_turs.setValueAt(parseSQL_bool(rs.getString("visa")), 11, 1);
                jTable_turs.setValueAt(parseSQL_bool(rs.getString("strah")), 12, 1);
                jTable_turs.setValueAt(parseSQL_bool(rs.getString("heal")), 13, 1);
                jTable_turs.setValueAt(rs.getString("excurs"), 14, 1);
                jTable_turs.setValueAt(rs.getString("service"), 15, 1);
                //--и т.д. остальные поля
            }//--------------------------
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jComboBox_opt_tursItemStateChanged

    private void jMenuItem_opt_tursActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_opt_tursActionPerformed
        // открыть окно туров
        jFrame_turs.setVisible(true);
        jComboBox_opt_turs.setEnabled(false);
        jComboBox_opt_turs.removeAllItems();
        JComboBox comboBox = new JComboBox();
        String query = "SELECT name FROM turs ORDER BY id";
        ResultSet rs = db.executeQuery(query);
        try {
            while (rs.next()) {
                jComboBox_opt_turs.addItem(rs.getString("name"));
            }
            rs = db.executeQuery("SELECT name FROM turoperator ORDER BY id");
            while(rs.next()) {
                comboBox.addItem(rs.getString("name"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
        
        EachRowEditor rowEditor = new EachRowEditor(jTable_turs);        
        EachRowRenderer rowRenderer = new EachRowRenderer();
        
        rowEditor.setEditorAt(0, new DefaultCellEditor(comboBox));
        rowEditor.setEditorAt(10, new CheckBoxCellEditor());
        rowEditor.setEditorAt(11, new CheckBoxCellEditor());
        rowEditor.setEditorAt(12, new CheckBoxCellEditor());
        rowEditor.setEditorAt(13, new CheckBoxCellEditor());
        rowRenderer.add(10, new CWCheckBoxRenderer());
        rowRenderer.add(11, new CWCheckBoxRenderer());
        rowRenderer.add(12, new CWCheckBoxRenderer());
        rowRenderer.add(13, new CWCheckBoxRenderer());
        
        jTable_turs.getColumn("Данные").setCellEditor(rowEditor);
        jTable_turs.getColumn("Данные").setCellRenderer(rowRenderer);
        
        jComboBox_opt_turs.setEnabled(true);
        jButton_opt_turs_cancel.setVisible(false);
        jComboBox_opt_tursItemStateChanged(null);
    }//GEN-LAST:event_jMenuItem_opt_tursActionPerformed

    private void jButton_opt_turs_addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_opt_turs_addActionPerformed
        // новый тур
        jButton_opt_turs_add.setEnabled(false);
        jButton_opt_turs_edit.setEnabled(false);
        jButton_opt_turs_save.setEnabled(true);
        jComboBox_opt_turs.removeAllItems();
        jComboBox_opt_turs.setEnabled(false);
        jButton_opt_turs_cancel.setVisible(true);
        
        jTable_turs.setValueAt("Туроператор", 0, 1);
        jTable_turs.setValueAt("", 1, 1);
        jTable_turs.setValueAt("", 2, 1);
        jTable_turs.setValueAt("", 3, 1);
        jTable_turs.setValueAt("", 4, 1);
        jTable_turs.setValueAt("", 5, 1);
        jTable_turs.setValueAt("", 6, 1);
        jTable_turs.setValueAt("", 7, 1);
        jTable_turs.setValueAt("", 8, 1);
        jTable_turs.setValueAt("", 9, 1);
        jTable_turs.setValueAt(false, 10, 1);
        jTable_turs.setValueAt(false, 11, 1);
        jTable_turs.setValueAt(false, 12, 1);
        jTable_turs.setValueAt(false, 13, 1);
        jTable_turs.setValueAt("", 14, 1);
        jTable_turs.setValueAt("", 15, 1);
        
        jTable_turs.selectAll();
        jTable_turs.setRowSelectionInterval(0, 0);
    }//GEN-LAST:event_jButton_opt_turs_addActionPerformed

    private void jButton_opt_turs_cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_opt_turs_cancelActionPerformed
        //отменить тур
        jButton_opt_turs_add.setEnabled(true);
        jButton_opt_turs_edit.setEnabled(true);
        jButton_opt_turs_save.setEnabled(false);
        jComboBox_opt_turs.setEnabled(true);
        jButton_opt_turs_cancel.setVisible(false);
        jTable_turs.selectAll();
        jTable_turs.setRowSelectionInterval(0, 0);
        jMenuItem_opt_tursActionPerformed(null);
    }//GEN-LAST:event_jButton_opt_turs_cancelActionPerformed

    private void jButton_opt_turs_editActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_opt_turs_editActionPerformed
        // Изменить тур
        jButton_opt_turs_add.setEnabled(false);
        jButton_opt_turs_edit.setEnabled(false);
        jButton_opt_turs_save.setEnabled(true);
        jComboBox_opt_turs.setEnabled(false);
        jButton_opt_turs_cancel.setVisible(true);
    }//GEN-LAST:event_jButton_opt_turs_editActionPerformed

    private void jButton_opt_turs_saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_opt_turs_saveActionPerformed
        // Сохранить тур
        DBT_turs turs = new DBT_turs();

        turs.turoperator = "'" + jTable_turs.getValueAt(0, 1).toString() + "',";
        turs.name = "'" + jTable_turs.getValueAt(1, 1).toString() + "',";
        turs.begin = "'" + jTable_turs.getValueAt(2, 1).toString() + "',";
        turs.punkt = "'" + jTable_turs.getValueAt(3, 1).toString() + "',";
        turs.end = "'" + jTable_turs.getValueAt(4, 1).toString() + "',";
        turs.bilet = "'" + jTable_turs.getValueAt(5, 1).toString() + "',";
        turs.hotel = "'" + jTable_turs.getValueAt(6, 1).toString() + "',";
        turs.room = "'" + jTable_turs.getValueAt(7, 1).toString() + "',";
        turs.food = "'" + jTable_turs.getValueAt(8, 1).toString() + "',";
        turs.transfer = "'" + jTable_turs.getValueAt(9, 1).toString() + "',";
        turs.gid = "'" + parseSQL_string((Boolean)jTable_turs.getValueAt(10, 1)) + "',";
        turs.visa = "'" + parseSQL_string((Boolean)jTable_turs.getValueAt(11, 1)) + "',";
        turs.strah = "'" + parseSQL_string((Boolean)jTable_turs.getValueAt(12, 1)) + "',";
        turs.heal = "'" + parseSQL_string((Boolean)jTable_turs.getValueAt(13, 1)) + "',";
        turs.excurs = "'" + jTable_turs.getValueAt(14, 1).toString() + "',";
        turs.service = "'" + jTable_turs.getValueAt(15, 1).toString() + "'";
        boolean good;
        if (jComboBox_opt_turs.getItemCount() == 0) {
            // Добавить нового туроператора
            good = db.executeUpdate("INSERT INTO turs VALUES(null," 
                    + turs.turoperator
                    + turs.name 
                    + turs.begin
                    + turs.punkt
                    + turs.end
                    + turs.bilet
                    + turs.hotel
                    + turs.room 
                    + turs.food 
                    + turs.transfer
                    + turs.gid
                    + turs.visa 
                    + turs.strah
                    + turs.heal 
                    + turs.excurs
                    + turs.service
                    + ");");
        } else {
            //Изменить существующего туроператора
            good = db.executeUpdate("UPDATE turs SET "
                    + "turoperator = " + turs.turoperator
                    + "name = " + turs.name
                    + "begin = " + turs.begin
                    + "punkt = " + turs.punkt
                    + "end = " + turs.end
                    + "bilet = " + turs.bilet
                    + "hotel = " + turs.hotel
                    + "room = " + turs.room 
                    + "food = " + turs.food 
                    + "transfer = " + turs.transfer
                    + "gid = " + turs.gid
                    + "visa = " + turs.visa 
                    + "strah = " + turs.strah
                    + "heal = " + turs.heal 
                    + "excurs = " + turs.excurs
                    + "service = " + turs.service
                    + " WHERE name = '" + jComboBox_opt_turs.getSelectedItem() + "'");
        }
        if(good){
            jButton_opt_turs_add.setEnabled(true);
            jButton_opt_turs_edit.setEnabled(true);
            jButton_opt_turs_save.setEnabled(false);
            jComboBox_opt_turs.setEnabled(true);
            jButton_opt_turs_cancel.setVisible(false);
            jMenuItem_opt_tursActionPerformed(null);
        }
    }//GEN-LAST:event_jButton_opt_turs_saveActionPerformed

    private void jFrame_tursComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jFrame_tursComponentHidden
       // закрывается окно туров
        jButton_opt_turs_add.setEnabled(true);
        jButton_opt_turs_edit.setEnabled(true);
        jButton_opt_turs_save.setEnabled(false);
        jComboBox_opt_turs.setEnabled(true);
        jButton_opt_turoperator_cancel.setVisible(false);
    }//GEN-LAST:event_jFrame_tursComponentHidden

    private void jTextField_tur_beginCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jTextField_tur_beginCaretUpdate
        if(!jButton_new_save.isEnabled()) return;
        jTextField_tur_end.setText(jTextField_tur_begin.getText().trim());        
    }//GEN-LAST:event_jTextField_tur_beginCaretUpdate

    private void jTextField_f_famCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jTextField_f_famCaretUpdate
        if(!jButton_new_save.isEnabled()) return;
        jTextField_tur1_fio.setText(jTextField_f_fam.getText().trim() + " " + jTextField_f_name.getText().trim() + " " + jTextField_f_otche.getText().trim());
    }//GEN-LAST:event_jTextField_f_famCaretUpdate

    private void jTextField_f_famFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField_f_famFocusLost
        if(!jButton_new_save.isEnabled()) return;
        jTextField_f_fam.setText(jTextField_f_fam.getText().trim());
    }//GEN-LAST:event_jTextField_f_famFocusLost

    private void jTextField_f_nameCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jTextField_f_nameCaretUpdate
        if(!jButton_new_save.isEnabled()) return;
        jTextField_tur1_fio.setText(jTextField_f_fam.getText().trim() + " " + jTextField_f_name.getText().trim() + " " + jTextField_f_otche.getText().trim());
    }//GEN-LAST:event_jTextField_f_nameCaretUpdate

    private void jTextField_f_otcheCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jTextField_f_otcheCaretUpdate
        if(!jButton_new_save.isEnabled()) return;
        jTextField_tur1_fio.setText(jTextField_f_fam.getText().trim() + " " + jTextField_f_name.getText().trim() + " " + jTextField_f_otche.getText().trim());
    }//GEN-LAST:event_jTextField_f_otcheCaretUpdate

    private void jTextField_f_nameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField_f_nameFocusLost
        if(!jButton_new_save.isEnabled()) return;
        jTextField_f_name.setText(jTextField_f_name.getText().trim());
    }//GEN-LAST:event_jTextField_f_nameFocusLost

    private void jTextField_f_otcheFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField_f_otcheFocusLost
        if(!jButton_new_save.isEnabled()) return;
        jTextField_f_otche.setText(jTextField_f_otche.getText().trim());
    }//GEN-LAST:event_jTextField_f_otcheFocusLost

    private void jTextField_f_passportCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jTextField_f_passportCaretUpdate
        if(!jButton_new_save.isEnabled()) return;
        jTextField_tur1_passport.setText(jTextField_f_passport.getText().trim());
    }//GEN-LAST:event_jTextField_f_passportCaretUpdate

    private void jTextField_f_passportFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField_f_passportFocusLost
        if(!jButton_new_save.isEnabled()) return;
        jTextField_f_passport.setText(jTextField_f_passport.getText().trim());
    }//GEN-LAST:event_jTextField_f_passportFocusLost

    private void jTextArea_f_addressCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jTextArea_f_addressCaretUpdate
        if(!jButton_new_save.isEnabled()) return;
        jTextField_tur1_adr.setText(jTextArea_f_address.getText().trim());
    }//GEN-LAST:event_jTextArea_f_addressCaretUpdate

    private void jTextArea_f_addressFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea_f_addressFocusLost
        if(!jButton_new_save.isEnabled()) return;
        jTextArea_f_address.setText(jTextArea_f_address.getText().trim());
    }//GEN-LAST:event_jTextArea_f_addressFocusLost

    private void jCheckBox_r_tur_dataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox_r_tur_dataActionPerformed
        jDateChooser_r_tur_date_s.setEnabled(jCheckBox_r_tur_data.isSelected());
        jDateChooser_r_tur_date_s.setDate(null);
    }//GEN-LAST:event_jCheckBox_r_tur_dataActionPerformed

    private void jComboBox_opt_turagentItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox_opt_turagentItemStateChanged
        // меняется комбобокс турагент на форме опций
        if (!jComboBox_opt_turagent.isEnabled()) return;
        try {
            String query = "SELECT * FROM turagent WHERE name = '" + jComboBox_opt_turagent.getSelectedItem() + "'";
            ResultSet rs = db2.executeQuery(query);
            while (rs.next()) {
                jTable_opt_turagent.setValueAt(rs.getString("name"), 0, 1);
                jTable_opt_turagent.setValueAt(rs.getString("fullname"), 1, 1);
                jTable_opt_turagent.setValueAt(rs.getString("manager"), 2, 1);
                jTable_opt_turagent.setValueAt(rs.getString("address"), 3, 1);
                jTable_opt_turagent.setValueAt(rs.getString("phone"), 4, 1);
                jTable_opt_turagent.setValueAt(rs.getString("r_schet"), 5, 1);
                jTable_opt_turagent.setValueAt(rs.getString("k_schet"), 6, 1);
                jTable_opt_turagent.setValueAt(rs.getString("bank"), 7, 1);
                jTable_opt_turagent.setValueAt(rs.getString("inn"), 8, 1);
                jTable_opt_turagent.setValueAt(rs.getString("kpp"), 9, 1);
                jTable_opt_turagent.setValueAt(rs.getString("bik"), 10, 1);
                //--и т.д. остальные поля
            }//--------------------------
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }//GEN-LAST:event_jComboBox_opt_turagentItemStateChanged

    private void jButton_turagent_newActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_turagent_newActionPerformed
        // дОБАВИТЬ ТУРагента
        jButton_turagent_new.setEnabled(false);
        jButton_turagent_edit.setEnabled(false);
        jButton_turagent_save.setEnabled(true);
        jComboBox_opt_turagent.removeAllItems();
        jComboBox_opt_turagent.setEnabled(false);
        jButton_opt_turagent_cancel.setVisible(true);
        for (int i = 0; i < 11; i++){
            jTable_opt_turagent.setValueAt("", i, 1);
        }
    }//GEN-LAST:event_jButton_turagent_newActionPerformed

    private void jButton_turagent_editActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_turagent_editActionPerformed
         // Изменить турагента:
        jButton_turagent_new.setEnabled(false);
        jButton_turagent_edit.setEnabled(false);
        jButton_turagent_save.setEnabled(true);
        jComboBox_opt_turagent.setEnabled(false);
        jButton_opt_turagent_cancel.setVisible(true);
    }//GEN-LAST:event_jButton_turagent_editActionPerformed

    private void jButton_turagent_saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_turagent_saveActionPerformed
        // Сохранить турагента
            DBT_turagent turagent = new DBT_turagent();
            
            turagent.name = "'" + jTable_opt_turagent.getValueAt(0, 1).toString() + "',";
            turagent.fullname = "'" + jTable_opt_turagent.getValueAt(1, 1).toString() + "',";
            turagent.manager = "'" + jTable_opt_turagent.getValueAt(2, 1).toString() + "',";
            turagent.address = "'" + jTable_opt_turagent.getValueAt(3, 1).toString() + "',";
            turagent.phone = "'" + jTable_opt_turagent.getValueAt(4, 1).toString() + "',";
            turagent.r_schet = "'" + jTable_opt_turagent.getValueAt(5, 1).toString() + "',";
            turagent.k_schet = "'" + jTable_opt_turagent.getValueAt(6, 1).toString() + "',";
            turagent.bank = "'" + jTable_opt_turagent.getValueAt(7, 1).toString() + "',";
            turagent.inn = "'" + jTable_opt_turagent.getValueAt(8, 1).toString() + "',";
            turagent.kpp = "'" + jTable_opt_turagent.getValueAt(9, 1).toString() + "',";
            turagent.bik = "'" + jTable_opt_turagent.getValueAt(10, 1).toString() + "'";
            
        if (jComboBox_opt_turagent.getItemCount() == 0) {
            // Добавить нового туроператора
            db.executeUpdate("INSERT INTO turagent VALUES("
                    + "null," 
                    + turagent.name 
                    + turagent.fullname 
                    + turagent.manager 
                    + turagent.address 
                    + turagent.phone 
                    + turagent.r_schet 
                    + turagent.k_schet 
                    + turagent.bank 
                    + turagent.inn 
                    + turagent.kpp 
                    + turagent.bik 
                    + ");");
        } else {
            //Изменить существующего туроператора
            db.executeUpdate("UPDATE turagent SET "
                    + "name = " + turagent.name  
                    + "fullname = " + turagent.fullname
                    + "manager = " + turagent.manager
                    + "address = " + turagent.address
                    + "phone = " + turagent.phone
                    + "r_schet = " + turagent.r_schet
                    + "k_schet = " + turagent.k_schet
                    + "bank = " + turagent.bank
                    + "inn = " + turagent.inn
                    + "kpp = " + turagent.kpp
                    + "bik = " + turagent.bik
                    + " WHERE name = '" + jComboBox_opt_turagent.getSelectedItem() + "'");
        }
        jButton_turagent_new.setEnabled(true);
        jButton_turagent_edit.setEnabled(true);
        jButton_turagent_save.setEnabled(false);
        jComboBox_opt_turagent.setEnabled(true);
        jButton_opt_turagent_cancel.setVisible(false);
        jMenuItem_opt_turagentActionPerformed(null);
    }//GEN-LAST:event_jButton_turagent_saveActionPerformed

    private void jButton_opt_turagent_cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_opt_turagent_cancelActionPerformed
         // отменить ввод нового или редактир. старого турagenta
        jButton_turagent_new.setEnabled(true);
        jButton_turagent_edit.setEnabled(true);
        jButton_turagent_save.setEnabled(false);
        jComboBox_opt_turagent.setEnabled(true);
        jButton_opt_turagent_cancel.setVisible(false);
        jMenuItem_opt_turagentActionPerformed(null);
    }//GEN-LAST:event_jButton_opt_turagent_cancelActionPerformed

    private void jFrame_turagentComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jFrame_turagentComponentHidden
        // закрывается окно турagenta
        jButton_turagent_new.setEnabled(true);
        jButton_turagent_edit.setEnabled(true);
        jButton_turagent_save.setEnabled(false);
        jComboBox_opt_turagent.setEnabled(true);
        jButton_opt_turagent_cancel.setVisible(false);
    }//GEN-LAST:event_jFrame_turagentComponentHidden

    private void jMenuItem_opt_turagentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_opt_turagentActionPerformed
        // Открываем окно турагентов
        jFrame_turagent.setVisible(true);
        jComboBox_opt_turagent.setEnabled(false);
        jComboBox_opt_turagent.removeAllItems();
        String query = "SELECT name FROM turagent ORDER BY id";
        ResultSet rs = db.executeQuery(query);
        try {
            while (rs.next()) {
                jComboBox_opt_turagent.addItem(rs.getString("name"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
        jComboBox_opt_turagent.setEnabled(true);
        jButton_opt_turagent_cancel.setVisible(false);
        jComboBox_opt_turagentItemStateChanged(null);
    }//GEN-LAST:event_jMenuItem_opt_turagentActionPerformed

    private void jCheckBox_r_pokupatelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox_r_pokupatelActionPerformed
        //Отчет чекбокс покупатель
        jComboBox_r_pokupatel.removeAllItems();
        jComboBox_r_pokupatel.addItem("Все");
        jComboBox_r_pokupatel.addItem("Только туристы");
        jComboBox_r_pokupatel.addItem("Только агенты");
        ResultSet rs = db.executeQuery("SELECT name FROM turagent");
        LinkedHashSet hashSet = new LinkedHashSet();
        try {
            while (rs.next()) {
                 hashSet.add(rs.getString("name"));
            }
            Iterator itr = hashSet.iterator();
            while(itr.hasNext()){
                jComboBox_r_pokupatel.addItem(itr.next());
            }
            jComboBox_r_pokupatel.setEnabled(jCheckBox_r_pokupatel.isSelected());
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }//GEN-LAST:event_jCheckBox_r_pokupatelActionPerformed

    private void jComboBox_pokupatelItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox_pokupatelItemStateChanged
        // смена значения комбобокса ПОКУПАТЕЛЬ на форме нового клиента
        if (!jComboBox_pokupatel.isEnabled()) return;
        changePokupatel();
        jComboBox_procentItemStateChanged(null);
    }//GEN-LAST:event_jComboBox_pokupatelItemStateChanged

    private void jCheckBox_priceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox_priceActionPerformed
        // нажимаем на чекбокс цена
        jTextField_price.setEditable(jCheckBox_price.isSelected());
        jTextField_tur1_price.setEditable(!jCheckBox_price.isSelected());
        jTextField_tur2_price.setEditable(!jCheckBox_price.isSelected());
        jTextField_tur3_price.setEditable(!jCheckBox_price.isSelected());
        jTextField_tur4_price.setEditable(!jCheckBox_price.isSelected());
        jTextField_tur5_price.setEditable(!jCheckBox_price.isSelected());
      
        if(jTextField_price.isEditable()){
            jTextField_tur1_price.setText("");
            jTextField_tur2_price.setText("");
            jTextField_tur3_price.setText("");
            jTextField_tur4_price.setText("");
            jTextField_tur5_price.setText("");
        }
    }//GEN-LAST:event_jCheckBox_priceActionPerformed

    private void jButton_new_closeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_new_closeActionPerformed
       jFrame_new.setVisible(false);
    }//GEN-LAST:event_jButton_new_closeActionPerformed

    private void jTextField_priceCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jTextField_priceCaretUpdate
       try {
            jLabel_price_word.setText(Num.toString(Integer.parseInt(jTextField_price.getText())));
            jTextField_price.setForeground(Color.BLACK);
            if(jComboBox_procent.getSelectedItem().toString().equalsIgnoreCase("0")) jTextField_last_price.setText(jTextField_price.getText());
            jComboBox_procentItemStateChanged(null);
            
        } catch (Exception ex) {
            jTextField_price.setForeground(Color.red);
            //JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }//GEN-LAST:event_jTextField_priceCaretUpdate

    private void jTextField_tur1_priceCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jTextField_tur1_priceCaretUpdate
        // цена 1
        if(!jTextField_tur1_price.isEditable() || !jTextField_tur1_price.isEnabled()) return;
        int price1 = 0;
        int price2 = 0;
        int price3 = 0;
        int price4 = 0;
        int price5 = 0;
        try {
            if(!jTextField_tur1_price.getText().trim().equalsIgnoreCase(""))
                price1 = Integer.parseInt(jTextField_tur1_price.getText());
            jTextField_tur1_price.setForeground(Color.black);
        } catch (Exception ex) {
            jTextField_tur1_price.setForeground(Color.red);
        }
        try {
            if(!jTextField_tur2_price.getText().trim().equalsIgnoreCase(""))
                price2 = Integer.parseInt(jTextField_tur2_price.getText());
            jTextField_tur2_price.setForeground(Color.black);
        } catch (Exception ex) {
            jTextField_tur2_price.setForeground(Color.red);
        }
        try {
            if(!jTextField_tur3_price.getText().trim().equalsIgnoreCase(""))
                price3 = Integer.parseInt(jTextField_tur3_price.getText());
            jTextField_tur3_price.setForeground(Color.black);
        } catch (Exception ex) {
            jTextField_tur3_price.setForeground(Color.red);
        }
        try {
            if(!jTextField_tur4_price.getText().trim().equalsIgnoreCase(""))
                price4 = Integer.parseInt(jTextField_tur4_price.getText());    
            jTextField_tur4_price.setForeground(Color.black);
        } catch (Exception ex) {
            jTextField_tur4_price.setForeground(Color.red);
        }
        try {
            if(!jTextField_tur5_price.getText().trim().equalsIgnoreCase(""))
                price5 = Integer.parseInt(jTextField_tur5_price.getText());
            jTextField_tur5_price.setForeground(Color.black);
        } catch (Exception ex) {
            jTextField_tur5_price.setForeground(Color.red);
        }
        jTextField_price.setText("" + (price1 + price2 + price3 + price4 + price5));
    }//GEN-LAST:event_jTextField_tur1_priceCaretUpdate

    private void jTextField_tur2_priceCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jTextField_tur2_priceCaretUpdate
        // цена 2
        if(!jTextField_tur2_price.isEditable() || !jTextField_tur2_price.isEnabled()) return;
        int price1 = 0;
        int price2 = 0;
        int price3 = 0;
        int price4 = 0;
        int price5 = 0;
        try {
            if(!jTextField_tur1_price.getText().trim().equalsIgnoreCase(""))
                price1 = Integer.parseInt(jTextField_tur1_price.getText());
            jTextField_tur1_price.setForeground(Color.black);
        } catch (Exception ex) {
            jTextField_tur1_price.setForeground(Color.red);
        }
        try {
            if(!jTextField_tur2_price.getText().trim().equalsIgnoreCase(""))
                price2 = Integer.parseInt(jTextField_tur2_price.getText());
            jTextField_tur2_price.setForeground(Color.black);
        } catch (Exception ex) {
            jTextField_tur2_price.setForeground(Color.red);
        }
        try {
            if(!jTextField_tur3_price.getText().trim().equalsIgnoreCase(""))
                price3 = Integer.parseInt(jTextField_tur3_price.getText());
            jTextField_tur3_price.setForeground(Color.black);
        } catch (Exception ex) {
            jTextField_tur3_price.setForeground(Color.red);
        }
        try {
            if(!jTextField_tur4_price.getText().trim().equalsIgnoreCase(""))
                price4 = Integer.parseInt(jTextField_tur4_price.getText());    
            jTextField_tur4_price.setForeground(Color.black);
        } catch (Exception ex) {
            jTextField_tur4_price.setForeground(Color.red);
        }
        try {
            if(!jTextField_tur5_price.getText().trim().equalsIgnoreCase(""))
                price5 = Integer.parseInt(jTextField_tur5_price.getText());
            jTextField_tur5_price.setForeground(Color.black);
        } catch (Exception ex) {
            jTextField_tur5_price.setForeground(Color.red);
        }
        jTextField_price.setText("" + (price1 + price2 + price3 + price4 + price5));
    }//GEN-LAST:event_jTextField_tur2_priceCaretUpdate

    private void jTextField_tur3_priceCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jTextField_tur3_priceCaretUpdate
        // цена 3
        if(!jTextField_tur3_price.isEditable() || !jTextField_tur3_price.isEnabled()) return;
        int price1 = 0;
        int price2 = 0;
        int price3 = 0;
        int price4 = 0;
        int price5 = 0;
        try {
            if(!jTextField_tur1_price.getText().trim().equalsIgnoreCase(""))
                price1 = Integer.parseInt(jTextField_tur1_price.getText());
            jTextField_tur1_price.setForeground(Color.black);
        } catch (Exception ex) {
            jTextField_tur1_price.setForeground(Color.red);
        }
        try {
            if(!jTextField_tur2_price.getText().trim().equalsIgnoreCase(""))
                price2 = Integer.parseInt(jTextField_tur2_price.getText());
            jTextField_tur2_price.setForeground(Color.black);
        } catch (Exception ex) {
            jTextField_tur2_price.setForeground(Color.red);
        }
        try {
            if(!jTextField_tur3_price.getText().trim().equalsIgnoreCase(""))
                price3 = Integer.parseInt(jTextField_tur3_price.getText());
            jTextField_tur3_price.setForeground(Color.black);
        } catch (Exception ex) {
            jTextField_tur3_price.setForeground(Color.red);
        }
        try {
            if(!jTextField_tur4_price.getText().trim().equalsIgnoreCase(""))
                price4 = Integer.parseInt(jTextField_tur4_price.getText());    
            jTextField_tur4_price.setForeground(Color.black);
        } catch (Exception ex) {
            jTextField_tur4_price.setForeground(Color.red);
        }
        try {
            if(!jTextField_tur5_price.getText().trim().equalsIgnoreCase(""))
                price5 = Integer.parseInt(jTextField_tur5_price.getText());
            jTextField_tur5_price.setForeground(Color.black);
        } catch (Exception ex) {
            jTextField_tur5_price.setForeground(Color.red);
        }
        jTextField_price.setText("" + (price1 + price2 + price3 + price4 + price5));
    }//GEN-LAST:event_jTextField_tur3_priceCaretUpdate

    private void jTextField_tur4_priceCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jTextField_tur4_priceCaretUpdate
        // цена 4
        if(!jTextField_tur4_price.isEditable() || !jTextField_tur4_price.isEnabled()) return;
        int price1 = 0;
        int price2 = 0;
        int price3 = 0;
        int price4 = 0;
        int price5 = 0;
        try {
            if(!jTextField_tur1_price.getText().trim().equalsIgnoreCase(""))
                price1 = Integer.parseInt(jTextField_tur1_price.getText());
            jTextField_tur1_price.setForeground(Color.black);
        } catch (Exception ex) {
            jTextField_tur1_price.setForeground(Color.red);
        }
        try {
            if(!jTextField_tur2_price.getText().trim().equalsIgnoreCase(""))
                price2 = Integer.parseInt(jTextField_tur2_price.getText());
            jTextField_tur2_price.setForeground(Color.black);
        } catch (Exception ex) {
            jTextField_tur2_price.setForeground(Color.red);
        }
        try {
            if(!jTextField_tur3_price.getText().trim().equalsIgnoreCase(""))
                price3 = Integer.parseInt(jTextField_tur3_price.getText());
            jTextField_tur3_price.setForeground(Color.black);
        } catch (Exception ex) {
            jTextField_tur3_price.setForeground(Color.red);
        }
        try {
            if(!jTextField_tur4_price.getText().trim().equalsIgnoreCase(""))
                price4 = Integer.parseInt(jTextField_tur4_price.getText());    
            jTextField_tur4_price.setForeground(Color.black);
        } catch (Exception ex) {
            jTextField_tur4_price.setForeground(Color.red);
        }
        try {
            if(!jTextField_tur5_price.getText().trim().equalsIgnoreCase(""))
                price5 = Integer.parseInt(jTextField_tur5_price.getText());
            jTextField_tur5_price.setForeground(Color.black);
        } catch (Exception ex) {
            jTextField_tur5_price.setForeground(Color.red);
        }
        jTextField_price.setText("" + (price1 + price2 + price3 + price4 + price5));
    }//GEN-LAST:event_jTextField_tur4_priceCaretUpdate

    private void jTextField_tur5_priceCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jTextField_tur5_priceCaretUpdate
        // цена 5
        if(!jTextField_tur5_price.isEditable() || !jTextField_tur5_price.isEnabled()) return;
        int price1 = 0;
        int price2 = 0;
        int price3 = 0;
        int price4 = 0;
        int price5 = 0;
        try {
            if(!jTextField_tur1_price.getText().trim().equalsIgnoreCase(""))
                price1 = Integer.parseInt(jTextField_tur1_price.getText());
            jTextField_tur1_price.setForeground(Color.black);
        } catch (Exception ex) {
            jTextField_tur1_price.setForeground(Color.red);
        }
        try {
            if(!jTextField_tur2_price.getText().trim().equalsIgnoreCase(""))
                price2 = Integer.parseInt(jTextField_tur2_price.getText());
            jTextField_tur2_price.setForeground(Color.black);
        } catch (Exception ex) {
            jTextField_tur2_price.setForeground(Color.red);
        }
        try {
            if(!jTextField_tur3_price.getText().trim().equalsIgnoreCase(""))
                price3 = Integer.parseInt(jTextField_tur3_price.getText());
            jTextField_tur3_price.setForeground(Color.black);
        } catch (Exception ex) {
            jTextField_tur3_price.setForeground(Color.red);
        }
        try {
            if(!jTextField_tur4_price.getText().trim().equalsIgnoreCase(""))
                price4 = Integer.parseInt(jTextField_tur4_price.getText());    
            jTextField_tur4_price.setForeground(Color.black);
        } catch (Exception ex) {
            jTextField_tur4_price.setForeground(Color.red);
        }
        try {
            if(!jTextField_tur5_price.getText().trim().equalsIgnoreCase(""))
                price5 = Integer.parseInt(jTextField_tur5_price.getText());
            jTextField_tur5_price.setForeground(Color.black);
        } catch (Exception ex) {
            jTextField_tur5_price.setForeground(Color.red);
        }
        jTextField_price.setText("" + (price1 + price2 + price3 + price4 + price5));
    }//GEN-LAST:event_jTextField_tur5_priceCaretUpdate

    private void jComboBox_procentItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox_procentItemStateChanged
        // проценты на главной форме
        if(!jComboBox_procent.isEnabled()) return;
        
        double price = 0;
        double procent = 0;
        double last_price = 0;
        try {
            price = Double.parseDouble(jTextField_price.getText());
            procent = Double.parseDouble(jComboBox_procent.getSelectedItem().toString());
        } catch (Exception e) {
            //System.out.println(e.getMessage());
        }
        last_price = price - ((price/100)*procent);
        jTextField_last_price.setText("" + Math.round(last_price));
    }//GEN-LAST:event_jComboBox_procentItemStateChanged

    private void jTextField_last_priceCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jTextField_last_priceCaretUpdate
        //проценты текстом
        try {
            jLabel_last_price_word.setText(Num.toString(Integer.parseInt(jTextField_last_price.getText())));            
        } catch (Exception e) {
            //System.out.println(e.getMessage());
        }
    }//GEN-LAST:event_jTextField_last_priceCaretUpdate

    private void jButton_new_close1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_new_close1ActionPerformed
       jFrame_sms.setVisible(false);
    }//GEN-LAST:event_jButton_new_close1ActionPerformed

    private void jButton_new_close2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_new_close2ActionPerformed
        jFrame_search.setVisible(false);
    }//GEN-LAST:event_jButton_new_close2ActionPerformed

    private void jButton_new_close3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_new_close3ActionPerformed
        jFrame_report.setVisible(false);
    }//GEN-LAST:event_jButton_new_close3ActionPerformed

    private void jButton_new_close4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_new_close4ActionPerformed
        jFrame_happy.setVisible(false);
    }//GEN-LAST:event_jButton_new_close4ActionPerformed

    private void jButton_new_close5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_new_close5ActionPerformed
       jFrame_prodavets.setVisible(false);
    }//GEN-LAST:event_jButton_new_close5ActionPerformed

    private void jButton_new_close6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_new_close6ActionPerformed
        jFrame_turoperator.setVisible(false);
    }//GEN-LAST:event_jButton_new_close6ActionPerformed

    private void jButton_new_close7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_new_close7ActionPerformed
        jFrame_report_table.setVisible(false);
    }//GEN-LAST:event_jButton_new_close7ActionPerformed

    private void jButton_new_close8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_new_close8ActionPerformed
        jFrame_turs.setVisible(false);
    }//GEN-LAST:event_jButton_new_close8ActionPerformed

    private void jButton_new_close9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_new_close9ActionPerformed
        jFrame_turagent.setVisible(false);
    }//GEN-LAST:event_jButton_new_close9ActionPerformed

    private void jButton_new_close10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_new_close10ActionPerformed
        jFrame_options.setVisible(false);
    }//GEN-LAST:event_jButton_new_close10ActionPerformed

    private void jButton_new_close11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_new_close11ActionPerformed
       jMenuItem1ActionPerformed(null);
    }//GEN-LAST:event_jButton_new_close11ActionPerformed

    private void jButton_new_schetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_new_schetActionPerformed
        // Открыть счет
        String a_name = jLabel_tur_num.getText() + "_" + jComboBox_pokupatel.getSelectedItem() + "_" + jLabel_edited_count.getText();
        String excelTarget = "Данные о клиентах/Счета/s" + a_name + ".xls";
        
        try{
            java.awt.Desktop.getDesktop().open(new File(excelTarget));
            
        } catch (Exception ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }//GEN-LAST:event_jButton_new_schetActionPerformed

    private void jButton_new_podtverzhdenieActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_new_podtverzhdenieActionPerformed
        // Открыть подтверждение
        String a_name = jLabel_tur_num.getText() + "_" + jComboBox_pokupatel.getSelectedItem() + "_" + jLabel_edited_count.getText();
        String rtfTarget = "Данные о клиентах/Подтверждения/b" + a_name + ".rtf";
        
        try{
            java.awt.Desktop.getDesktop().open(new File(rtfTarget));
            
        } catch (Exception ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }//GEN-LAST:event_jButton_new_podtverzhdenieActionPerformed

    private void jButton_new_pko_aActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_new_pko_aActionPerformed
        // Открыть ПКО_А
        String a_name = jLabel_tur_num.getText() + "_" + jComboBox_pokupatel.getSelectedItem() + "_" + jLabel_edited_count.getText();
        String rtfTarget = "Данные о клиентах/ПКО_А/ka" + a_name + ".rtf";

        try{
            java.awt.Desktop.getDesktop().open(new File(rtfTarget));
            
        } catch (Exception ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }//GEN-LAST:event_jButton_new_pko_aActionPerformed

    private void jButton_new_copyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_new_copyActionPerformed
        // копируем анкету клиента
        jButton_new_save.setEnabled(true);
        SAVE(true);
        jComboBox_prodavets.setEnabled(false);
        if (query_type) {
            query_type = QUERY_INSERT;
            //db.executeUpdate("UPDATE main SET edited = 0 WHERE tur_num = " + jLabel_tur_num.getText()); //удалить
            db.executeUpdate("UPDATE main SET edited = 0 WHERE id = " + jLabel_tur_num.getText());
        }
        clearFields_copyClient();   
        changePokupatel();
        jCheckBox_priceActionPerformed(null);
        //jFrame_new.setVisible(true);
    }//GEN-LAST:event_jButton_new_copyActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Turist.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {                
                    new Turist().setVisible(true);
                    birthday_calendar();
            }
        });
        
    }
    
    public boolean parseSQL_bool(String result){
        if (result.equalsIgnoreCase("")) return false;
        if (result.equalsIgnoreCase("null")) return false;
        if (result.equalsIgnoreCase("0")) return false;
        return true;        
    }
    public String parseSQL_string(boolean bool){
        if (bool) return "1";
        return "0";
    }
    
    /**
     * подсвечивает на календарике дни рождения клиентов
     */
    static void birthday_calendar() {
        dbs = new DB(dbs_user, dbs_pass, dbs_host, dbs_base); 
        Component[] days = jday.getDayPanel().getComponents();
        days[5].setForeground(jday.getSundayForeground()); //суббота красненьким
        int i = 7;
        while(!days[i].isShowing()){
            i++;
        }
        int day_null = i-1;    
        int birthday;
       birthday_today = false;
        try {
        ResultSet rs = dbs.executeQuery("SELECT tur1_bd FROM main WHERE EXTRACT(MONTH FROM tur1_bd) = " + (new Date().getMonth()+1));
            while(rs.next()){
                birthday = rs.getDate("tur1_bd").getDate();                
                days[day_null + birthday].setForeground(new Color(30, 225, 30));  //зеленый
                if (birthday == new Date().getDate()){
                    days[day_null + birthday].setForeground(new Color(244, 66, 244)); //розовый
                    birthday_today = true;
                }
            }
            rs = dbs.executeQuery("SELECT tur2_bd FROM main WHERE EXTRACT(MONTH FROM tur2_bd) = " + (new Date().getMonth()+1));
            while(rs.next()){
                birthday = rs.getDate("tur2_bd").getDate();                
                days[day_null + birthday].setForeground(new Color(30, 225, 30));  //зеленый
                if (birthday == new Date().getDate()){
                    days[day_null + birthday].setForeground(new Color(244, 66, 244)); //розовый
                    birthday_today = true;
                }
            }
            rs = dbs.executeQuery("SELECT tur3_bd FROM main WHERE EXTRACT(MONTH FROM tur3_bd) = " + (new Date().getMonth()+1));
            while(rs.next()){
                birthday = rs.getDate("tur3_bd").getDate();                
                days[day_null + birthday].setForeground(new Color(30, 225, 30));  //зеленый
                if (birthday == new Date().getDate()){
                    days[day_null + birthday].setForeground(new Color(244, 66, 244)); //розовый
                    birthday_today = true;
                }
            }
            rs = dbs.executeQuery("SELECT tur4_bd FROM main WHERE EXTRACT(MONTH FROM tur4_bd) = " + (new Date().getMonth()+1));
            while(rs.next()){
                birthday = rs.getDate("tur4_bd").getDate();                
                days[day_null + birthday].setForeground(new Color(30, 225, 30));  //зеленый
                if (birthday == new Date().getDate()){
                    days[day_null + birthday].setForeground(new Color(244, 66, 244)); //розовый
                    birthday_today = true;
                }
            }
            rs = dbs.executeQuery("SELECT tur5_bd FROM main WHERE EXTRACT(MONTH FROM tur5_bd) = " + (new Date().getMonth()+1));
            while(rs.next()){
                birthday = rs.getDate("tur5_bd").getDate();                
                days[day_null + birthday].setForeground(new Color(30, 225, 30));  //зеленый
                if (birthday == new Date().getDate()){
                    days[day_null + birthday].setForeground(new Color(244, 66, 244)); //розовый
                    birthday_today = true;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        } finally {
            if(birthday_today){
                JOptionPane.showMessageDialog(null, "Сегодня день рождения у нашего клиента!");
               /* int happy = JOptionPane.showOptionDialog(null, 
                        "Сегодня день рождение у нашего клиента!", "Поздравить?", 
                        JOptionPane.YES_NO_CANCEL_OPTION, 
                        JOptionPane.INFORMATION_MESSAGE, null,  
                        new Object[] { "SMS", "Email", "Закрыть"}, null
                );
                switch (happy){
                    case 0: break;
                    case 1: JOptionPane.showMessageDialog(null, "Email"); break;
                    case 2: break;
                }*/
            }
            dbs.close();
        }
    }                 
    
    public class MyRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            return this;
        }
    }
    public class MyRendererHappy extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value.toString().equalsIgnoreCase(reFormateDate(new Date(), "dd.MM.yyyy"))) {
                setBackground(Color.GREEN);
                // !!!!  работает на колонку а не на строчку
            }
            return this;
        }
    }
    
    private void tableUpdate() {
        DB dbu = new DB(db_user, db_pass, db_host, db_base);
        try {
              ResultSet rs = dbu.executeQuery("SELECT * "
                    + "FROM main WHERE DATE(tur_date_po) >= " + reFormateDateForSQL(new Date()) + " ORDER BY tur_date_s");
             
            count = 0;           
            DefaultTableModel dtm = (DefaultTableModel) jTable_main.getModel();
            while(dtm.getRowCount() > 0){
                dtm.removeRow(dtm.getRowCount() - 1);
            }
            jTable_main.setAutoCreateRowSorter(false);
            jTable_main.setModel(dtm);
            dtm.fireTableDataChanged();
            while(rs.next()) {
                dtm.addRow(new String[] {
                   // rs.getString("tur_num"), //заменить и добавить один столбец в таблицу
                    rs.getString("id"),
                    rs.getString("turoperator"),
                    rs.getString("pokupatel"),
                    rs.getString("tur_name"),
                    rs.getString("f_fam") + " " + rs.getString("f_name") + " " + rs.getString("f_otche"),
                    reFormateDateForTable(rs.getString("sale_date")),
                    rs.getString("last_price"),
                    reFormateDateForTable(rs.getString("tur_date_s")),
                    reFormateDateForTable(rs.getString("tur_date_po")),
                });
            } 
            jTable_main.setAutoCreateRowSorter(true);
            dtm.fireTableDataChanged();
            
            
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
            //db = new DB();
        }
        dbu.close();
        
    }
    
    public class updateTableThread implements Runnable {
        @Override
        public void run() {
            count = 0;
            while (true){
                jButton_update.setText(count + "");
                if (count == 60){ 
                    tableUpdate(); 
                } else {                        
                    try {
                        Thread.sleep(1000); //задержка 1с
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
                        JOptionPane.showMessageDialog(null, ex.getMessage());
                    }
                }
                count++;
            }
        }
    }
    
    
    public class SendMailThread implements Runnable {
        myMail mail = new myMail(mail_smtp_user, mail_smtp_pass, mail_smtp);
        String subject = "" + jTextField_mail_subject.getText().trim();
        String text = "" + jTextPane_msg.getText();
        int mail_count;
        String[] mail_to;
        public SendMailThread(int mail_count, String[] mail_to) {
            this.mail_count = mail_count;
            this.mail_to = mail_to;
            jProgressBar_mail.setMaximum(mail_count);
        }
        @Override
        public void run() {
            int i = 0;
            while (i < mail_count){
                try {
                    jTable_spam.setValueAt(mail.send(mail_from, mail_to[i], subject, text), i, 1);
                    jProgressBar_mail.setValue(++i);
                    Thread.sleep(1000); 
                    //1000 = 1 sec
                } catch (InterruptedException ex) {
                    Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            }
            sp.stop();
            jButton_mail_suspend.setEnabled(false);
            jButton_mail_resume.setEnabled(false);
            jButton_mail_stop.setEnabled(false);
            jButton_mail_start.setEnabled(true);
            jButton_mail_send.setEnabled(true);
            JOptionPane.showMessageDialog(null, "Рассылка завершена.");
        }
    }
    
    public class StatusProcessThread implements Runnable {
        @Override
        public void run() {
            while (true){
               try {
                    Thread.sleep(100); 
                    jLabel_mail_process_status.setText("---");
                    Thread.sleep(100); 
                    jLabel_mail_process_status.setText("\\");
                    Thread.sleep(100); 
                    jLabel_mail_process_status.setText("|");
                    Thread.sleep(100); 
                    jLabel_mail_process_status.setText("/");
                    //1000 = 1 sec
                } catch (InterruptedException ex) {
                    Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            }
        }
    }
    
    public Date getDateFromSQL(String strDate) {
        if (strDate == null) return null;
        if (strDate.toString().equalsIgnoreCase("")) return null;
        try { 
            SimpleDateFormat myDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = myDateFormat.parse(strDate);
        return date;
         } catch (ParseException ex) {  
            JOptionPane.showMessageDialog(null, ex.getMessage());
            return null;
        }
    }
    
    public String reFormateDate(String strDate, String fromFormat, String toFormat){
        if (strDate == null) return "NULL";
        if (strDate.toString().equalsIgnoreCase("")) return "NULL";
        try { 
            SimpleDateFormat myDateFormat = new SimpleDateFormat(fromFormat); //Задали шаблон входящей строки  
            Date date = myDateFormat.parse(strDate); //получили дату
            myDateFormat.applyPattern(toFormat); //изменили шаблон
            return myDateFormat.format(date);
        } catch (ParseException ex) {  
            JOptionPane.showMessageDialog(null, ex.getMessage());
            return null;
        }
    }
    public String reFormateDateForTable(String strDateSQL){
        if (strDateSQL == null) return "";
        if (strDateSQL.toString().equalsIgnoreCase("")) return "";
        try { 
            SimpleDateFormat myDateFormat = new SimpleDateFormat("yyyy-MM-dd"); //Задали шаблон входящей строки  
            Date date = myDateFormat.parse(strDateSQL); //получили дату
            myDateFormat.applyPattern("dd.MM.yyyy"); //изменили шаблон
            return myDateFormat.format(date);
        } catch (ParseException ex) {  
            JOptionPane.showMessageDialog(null, ex.getMessage());
            return "";
        }
    }  
    
    public String reFormateDate(Date date, String toFormat){
        if (date == null) return "NULL";
        if (date.toString().equalsIgnoreCase("")) return "NULL";
        SimpleDateFormat myDateFormat = new SimpleDateFormat(toFormat); 
        return myDateFormat.format(date);
    }
    
    public String reFormateDateForSQL(Date date){
        if (date == null) return "NULL";
        if (date.toString().equalsIgnoreCase("")) return "NULL";
        SimpleDateFormat myDateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
        return "'" + myDateFormat.format(date) + "'";
    }
    
    public String baseDir(){
        final ProtectionDomain pd = getClass().getProtectionDomain();
        final CodeSource cs = pd.getCodeSource();

        if (cs == null) {
            //code from class loaded by Bootsrap class loader;
            return "";
        } else {
            final URL location = cs.getLocation();
            String loc = location + "";
            loc = loc.substring(6, loc.length() - 9); // (File:/)....../(Foton.jar)
            return loc;
        }
    }
    
    //очистить поля тура
    public void clear_tur(){
        jTextField_tur_begin.setText("Тюмень");
        jTextField_tur_punkt.setText("");
        jTextField_tur_end.setText("Тюмень");
        jTextField_h_name.setText("");
        
        jComboBox_h_nomer.removeAllItems();
        jComboBox_h_nomer.addItem("SGL");
        jComboBox_h_nomer.addItem("DBL");
        jComboBox_h_nomer.addItem("TRPL");
        jComboBox_h_nomer.addItem("1-но местн.");
        jComboBox_h_nomer.addItem("2-х местн.");
        jComboBox_h_nomer.addItem("3-х местн.");
        jComboBox_h_nomer.addItem("апартаменты");
        jComboBox_h_nomer.addItem("другое");
        jComboBox_h_nomer.setSelectedIndex(0);
        
        jComboBox_bilet_cat.removeAllItems();
        jComboBox_bilet_cat.addItem("полный");
        jComboBox_bilet_cat.addItem("плацкарт");
        jComboBox_bilet_cat.addItem("купе");
        jComboBox_bilet_cat.addItem("общий");
        jComboBox_bilet_cat.addItem("эконом");
        jComboBox_bilet_cat.addItem("I класс");
        jComboBox_bilet_cat.addItem("II класс");
        jComboBox_bilet_cat.addItem("бизнес");
        jComboBox_bilet_cat.addItem("автобус");
        jComboBox_bilet_cat.setSelectedIndex(0);
        
        jComboBox_food.removeAllItems();
        jComboBox_food.addItem("BB");
        jComboBox_food.addItem("HB");
        jComboBox_food.addItem("FB");
        jComboBox_food.addItem("ALL");
        jComboBox_food.addItem("завтрак");
        jComboBox_food.addItem("2-х разовое");
        jComboBox_food.addItem("3-х разовое");
        jComboBox_food.addItem("без питания");
        jComboBox_food.addItem("другое");
        jComboBox_food.setSelectedIndex(0);   
        
        jComboBox_transfer.removeAllItems();
        jComboBox_transfer.addItem("Групповой");
        jComboBox_transfer.addItem("Индивидуальный");
        jComboBox_transfer.addItem("Нет");
        jComboBox_transfer.setSelectedIndex(0);
        
        jCheckBox_gid.setSelected(false);
        jCheckBox_visa.setSelected(false);
        jCheckBox_strah.setSelected(false);
        jCheckBox_heal.setSelected(false);
        jTextArea_excurs.setText("");
        jTextArea_service.setText("");
    }
    
    //заполнить поля тура
    public void fill_tur(ResultSet rs){
        DBT_turs turs = new DBT_turs();
        try {
            while(rs.next()){
                turs.turoperator= rs.getString("turoperator");
                turs.name       = rs.getString("name");
                turs.begin      = rs.getString("begin");
                turs.punkt      = rs.getString("punkt");
                turs.end        = rs.getString("end");
                turs.bilet      = rs.getString("bilet");
                turs.hotel      = rs.getString("hotel");
                turs.room       = rs.getString("room");
                turs.food       = rs.getString("food");
                turs.transfer   = rs.getString("transfer");
                turs.gid        = rs.getString("gid");
                turs.visa       = rs.getString("visa");
                turs.strah      = rs.getString("strah");
                turs.heal       = rs.getString("heal");
                turs.excurs     = rs.getString("excurs");
                turs.service    = rs.getString("service");
                
                jComboBox_turoperator.setSelectedItem(turs.turoperator);
                jTextField_tur_begin.setText(turs.begin);
                jTextField_tur_punkt.setText(turs.punkt);
                jTextField_tur_end.setText(turs.end);
                jTextField_h_name.setText(turs.hotel);
                jComboBox_h_nomer.removeAllItems();
                jComboBox_h_nomer.addItem(turs.room);
                jComboBox_bilet_cat.removeAllItems();
                jComboBox_bilet_cat.addItem(turs.bilet);
                jComboBox_food.removeAllItems();
                jComboBox_food.addItem(turs.food);
                jComboBox_transfer.removeAllItems();
                jComboBox_transfer.addItem(turs.transfer);
                if(turs.gid != null && turs.gid.equalsIgnoreCase("1")) {jCheckBox_gid.setSelected(true);} else {jCheckBox_gid.setSelected(false);}
                if(turs.visa != null && turs.visa.equalsIgnoreCase("1")) {jCheckBox_visa.setSelected(true);} else {jCheckBox_visa.setSelected(false);}
                if(turs.strah != null && turs.strah.equalsIgnoreCase("1")) {jCheckBox_strah.setSelected(true);} else {jCheckBox_strah.setSelected(false);}
                if(turs.heal != null && turs.heal.equalsIgnoreCase("1")) {jCheckBox_heal.setSelected(true);} else {jCheckBox_heal.setSelected(false);}
                jTextArea_excurs.setText(turs.excurs);
                jTextArea_service.setText(turs.service);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }
    
    // очистить поля формы клиента при нажатии на кнопку "новый"
    public void clearFields_newClient(){
        jLabel_tur_num.setText("...");
        jTextField_f_fam.setText("");
        jTextField_f_name.setText("");
        jTextField_f_otche.setText("");
        jTextField_f_passport.setText("");
        jTextArea_f_address.setText("");
        jTextField_phone_dom.setText("");
        jTextField_phone_rab.setText("");
        jTextField_tur1_fio.setText("");
        jDateChooser_tur1_bd.setDate(null);
        jTextField_tur1_adr.setText("");
        jTextField_tur2_fio.setText("");
        jDateChooser_tur2_bd.setDate(null);
        jTextField_tur2_adr.setText("");
        jTextField_tur3_fio.setText("");
        jDateChooser_tur3_bd.setDate(null);
        jTextField_tur3_adr.setText("");
        jTextField_tur4_fio.setText("");
        jDateChooser_tur4_bd.setDate(null);
        jTextField_tur4_adr.setText("");
        jTextField_tur5_fio.setText("");
        jDateChooser_tur5_bd.setDate(null);
        jTextField_tur5_adr.setText("");
        
        jDateChooser_tur_date_s.setDate(null);
        jDateChooser_tur_date_po.setDate(null);
        
        jTextField_tur_begin.setText("Тюмень");
        jTextField_tur_punkt.setText("");
        jTextField_tur_end.setText("Тюмень");
        jTextField_h_name.setText("");
        
        jComboBox_h_nomer.removeAllItems();
        jComboBox_h_nomer.addItem("SGL");
        jComboBox_h_nomer.addItem("DBL");
        jComboBox_h_nomer.addItem("TRPL");
        jComboBox_h_nomer.addItem("1-но местн.");
        jComboBox_h_nomer.addItem("2-х местн.");
        jComboBox_h_nomer.addItem("3-х местн.");
        jComboBox_h_nomer.addItem("апартаменты");
        jComboBox_h_nomer.addItem("другое");
        jComboBox_h_nomer.setSelectedIndex(0);
        
        jComboBox_bilet_cat.removeAllItems();
        jComboBox_bilet_cat.addItem("полный");
        jComboBox_bilet_cat.addItem("плацкарт");
        jComboBox_bilet_cat.addItem("купе");
        jComboBox_bilet_cat.addItem("общий");
        jComboBox_bilet_cat.addItem("эконом");
        jComboBox_bilet_cat.addItem("I класс");
        jComboBox_bilet_cat.addItem("II класс");
        jComboBox_bilet_cat.addItem("бизнес");
        jComboBox_bilet_cat.addItem("автобус");
        jComboBox_bilet_cat.setSelectedIndex(0);
        
        jComboBox_food.removeAllItems();
        jComboBox_food.addItem("BB");
        jComboBox_food.addItem("HB");
        jComboBox_food.addItem("FB");
        jComboBox_food.addItem("ALL");
        jComboBox_food.addItem("завтрак");
        jComboBox_food.addItem("2-х разовое");
        jComboBox_food.addItem("3-х разовое");
        jComboBox_food.addItem("без питания");
        jComboBox_food.addItem("другое");
        jComboBox_food.setSelectedIndex(0);   
        
        jComboBox_transfer.removeAllItems();
        jComboBox_transfer.addItem("Групповой");
        jComboBox_transfer.addItem("Индивидуальный");
        jComboBox_transfer.addItem("Нет");
        jComboBox_transfer.setSelectedIndex(0);
        
        jCheckBox_gid.setSelected(false);
        jCheckBox_visa.setSelected(false);
        jCheckBox_strah.setSelected(false);
        jCheckBox_heal.setSelected(false);
        
        jTextArea_excurs.setText("");
        jTextArea_service.setText("");
        jTextField_price.setText("0");
        jDateChooser_sale_date.setDate(new Date());
        jTextField_phone_mob.setText("");
        jTextField_email.setText("");
        jCheckBox_spam.setSelected(false);
        jCheckBox_sms.setSelected(false);
        //---
        jLabel_edited_count.setText("0");
        jLabel_price_word.setText(Num.toString(0));
        //--
        jTextField_tur1_passport.setText("");
        jTextField_tur2_passport.setText("");
        jTextField_tur3_passport.setText("");
        jTextField_tur4_passport.setText("");
        jTextField_tur5_passport.setText("");
        //--
        jTextField_tur1_price.setText("");
        jTextField_tur2_price.setText("");
        jTextField_tur3_price.setText("");
        jTextField_tur4_price.setText("");
        jTextField_tur5_price.setText("");
        //-
        jCheckBox_price.setSelected(false);
        jTextField_last_price.setText("");
        jLabel_last_price_word.setText(" ");
        //--
        jPanel_dogovor.setBorder(javax.swing.BorderFactory.createTitledBorder("№ договора: ___"));
        
        jComboBox_prodavets.removeAllItems();
        jComboBox_turoperator.removeAllItems();
        jComboBox_seller.removeAllItems();
        jComboBox_tur_name.removeAllItems();
        jComboBox_tur_name.addItem("");
        jComboBox_pokupatel.removeAllItems();
        jComboBox_pokupatel.addItem("Турист");
        //jComboBox_pokupatel.addItem("Агент");
        jComboBox_procent.removeAllItems();
        for(int i = 0; i<=15; i++) { 
            jComboBox_procent.addItem("" + i); 
        } 
        jComboBox_procent.setSelectedIndex(0);
        try {
            String query = "SELECT name FROM prodavets ORDER BY id";
            ResultSet rs = db.executeQuery(query);
            while(rs.next()) {
                jComboBox_prodavets.addItem(rs.getString("name"));
            }//----------------------------------------------------
            query = "SELECT name FROM turoperator ORDER BY id";
            rs = db.executeQuery(query);
            while(rs.next()) {
                jComboBox_turoperator.addItem(rs.getString("name"));
            }//----------------------------------------------------
            query = "SELECT name FROM managers WHERE prodavets = '" + jComboBox_prodavets.getSelectedItem() + "' ORDER BY id";
            rs = db.executeQuery(query);
            while(rs.next()) {
                jComboBox_seller.addItem(rs.getString("name"));
            }//----------------------------------------------------
            query = "SELECT name FROM turs ORDER BY id";
            rs = db.executeQuery(query);
            while(rs.next()) {
                jComboBox_tur_name.addItem(rs.getString("name"));
            }//----------------------------------------------------
            query = "SELECT name FROM turagent ORDER BY id";
            rs = db.executeQuery(query);
            while(rs.next()) {
                jComboBox_pokupatel.addItem(rs.getString("name"));
            }//----------------------------------------------------

            jComboBox_prodavets.setEnabled(true);
            
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
        
    }
    // очистить поля формы клиента при нажатии на кнопку "копировать"
    public void clearFields_copyClient(){
        jLabel_tur_num.setText("...");
        //jTextField_f_fam.setText("");
        //jTextField_f_name.setText("");
        //jTextField_f_otche.setText("");
       // jTextField_f_passport.setText("");
       // jTextArea_f_address.setText("");
      //  jTextField_phone_dom.setText("");
      //  jTextField_phone_rab.setText("");
      //  jTextField_tur1_fio.setText("");
      //  jDateChooser_tur1_bd.setDate(null);
     //   jTextField_tur1_adr.setText("");
     //   jTextField_tur2_fio.setText("");
      //  jDateChooser_tur2_bd.setDate(null);
    //    jTextField_tur2_adr.setText("");
    //    jTextField_tur3_fio.setText("");
     //   jDateChooser_tur3_bd.setDate(null);
      //  jTextField_tur3_adr.setText("");
     //   jTextField_tur4_fio.setText("");
     //   jDateChooser_tur4_bd.setDate(null);
    //    jTextField_tur4_adr.setText("");
     //   jTextField_tur5_fio.setText("");
     //   jDateChooser_tur5_bd.setDate(null);
    //    jTextField_tur5_adr.setText("");
        
        jDateChooser_tur_date_s.setDate(null);
        jDateChooser_tur_date_po.setDate(null);
        
        jTextField_tur_begin.setText("Тюмень");
        jTextField_tur_punkt.setText("");
        jTextField_tur_end.setText("Тюмень");
        jTextField_h_name.setText("");
        
        jComboBox_h_nomer.removeAllItems();
        jComboBox_h_nomer.addItem("SGL");
        jComboBox_h_nomer.addItem("DBL");
        jComboBox_h_nomer.addItem("TRPL");
        jComboBox_h_nomer.addItem("1-но местн.");
        jComboBox_h_nomer.addItem("2-х местн.");
        jComboBox_h_nomer.addItem("3-х местн.");
        jComboBox_h_nomer.addItem("апартаменты");
        jComboBox_h_nomer.addItem("другое");
        jComboBox_h_nomer.setSelectedIndex(0);
        
        jComboBox_bilet_cat.removeAllItems();
        jComboBox_bilet_cat.addItem("полный");
        jComboBox_bilet_cat.addItem("плацкарт");
        jComboBox_bilet_cat.addItem("купе");
        jComboBox_bilet_cat.addItem("общий");
        jComboBox_bilet_cat.addItem("эконом");
        jComboBox_bilet_cat.addItem("I класс");
        jComboBox_bilet_cat.addItem("II класс");
        jComboBox_bilet_cat.addItem("бизнес");
        jComboBox_bilet_cat.addItem("автобус");
        jComboBox_bilet_cat.setSelectedIndex(0);
        
        jComboBox_food.removeAllItems();
        jComboBox_food.addItem("BB");
        jComboBox_food.addItem("HB");
        jComboBox_food.addItem("FB");
        jComboBox_food.addItem("ALL");
        jComboBox_food.addItem("завтрак");
        jComboBox_food.addItem("2-х разовое");
        jComboBox_food.addItem("3-х разовое");
        jComboBox_food.addItem("без питания");
        jComboBox_food.addItem("другое");
        jComboBox_food.setSelectedIndex(0);   
        
        jComboBox_transfer.removeAllItems();
        jComboBox_transfer.addItem("Групповой");
        jComboBox_transfer.addItem("Индивидуальный");
        jComboBox_transfer.addItem("Нет");
        jComboBox_transfer.setSelectedIndex(0);
        
        jCheckBox_gid.setSelected(false);
        jCheckBox_visa.setSelected(false);
        jCheckBox_strah.setSelected(false);
        jCheckBox_heal.setSelected(false);
        
        jTextArea_excurs.setText("");
        jTextArea_service.setText("");
        jTextField_price.setText("0");
        jDateChooser_sale_date.setDate(new Date());
        //jTextField_phone_mob.setText("");
       // jTextField_email.setText("");
       // jCheckBox_spam.setSelected(false);
      //  jCheckBox_sms.setSelected(false);
        //---
        jLabel_edited_count.setText("0");
        jLabel_price_word.setText(Num.toString(0));
        //--
        //jTextField_tur1_passport.setText("");
       // jTextField_tur2_passport.setText("");
      //  jTextField_tur3_passport.setText("");
      //  jTextField_tur4_passport.setText("");
      //  jTextField_tur5_passport.setText("");
        //--
        jTextField_tur1_price.setText("");
        jTextField_tur2_price.setText("");
        jTextField_tur3_price.setText("");
        jTextField_tur4_price.setText("");
        jTextField_tur5_price.setText("");
        //-
        jCheckBox_price.setSelected(false);
        jTextField_last_price.setText("");
        jLabel_last_price_word.setText(" ");
        //--
        jPanel_dogovor.setBorder(javax.swing.BorderFactory.createTitledBorder("№ договора: ___"));
        
        jComboBox_prodavets.removeAllItems();
        jComboBox_turoperator.removeAllItems();
        jComboBox_seller.removeAllItems();
        jComboBox_tur_name.removeAllItems();
        jComboBox_tur_name.addItem("");
        jComboBox_pokupatel.removeAllItems();
        jComboBox_pokupatel.addItem("Турист");
        //jComboBox_pokupatel.addItem("Агент");
        jComboBox_procent.removeAllItems();
        for(int i = 0; i<=15; i++) { 
            jComboBox_procent.addItem("" + i); 
        } 
        jComboBox_procent.setSelectedIndex(0);
        try {
            String query = "SELECT name FROM prodavets ORDER BY id";
            ResultSet rs = db.executeQuery(query);
            while(rs.next()) {
                jComboBox_prodavets.addItem(rs.getString("name"));
            }//----------------------------------------------------
            query = "SELECT name FROM turoperator ORDER BY id";
            rs = db.executeQuery(query);
            while(rs.next()) {
                jComboBox_turoperator.addItem(rs.getString("name"));
            }//----------------------------------------------------
            query = "SELECT name FROM managers WHERE prodavets = '" + jComboBox_prodavets.getSelectedItem() + "' ORDER BY id";
            rs = db.executeQuery(query);
            while(rs.next()) {
                jComboBox_seller.addItem(rs.getString("name"));
            }//----------------------------------------------------
            query = "SELECT name FROM turs ORDER BY id";
            rs = db.executeQuery(query);
            while(rs.next()) {
                jComboBox_tur_name.addItem(rs.getString("name"));
            }//----------------------------------------------------
            query = "SELECT name FROM turagent ORDER BY id";
            rs = db.executeQuery(query);
            while(rs.next()) {
                jComboBox_pokupatel.addItem(rs.getString("name"));
            }//----------------------------------------------------

            jComboBox_prodavets.setEnabled(true);
            
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
        
    }
   
    // Заплнить поля формы клиента
    public void fillFields_newClient_2(ResultSet rs){
        
       DBT_main main = new DBT_main();
        try {
            while(rs.next()){
                main.id = rs.getString("id");
                main.prodavets = rs.getString("prodavets");
                main.turoperator = rs.getString("turoperator");
                main.tur_name = rs.getString("tur_name");
                main.f_fam = rs.getString("f_fam");
                main.f_name = rs.getString("f_name");
                main.f_otche = rs.getString("f_otche");
                main.f_passport = rs.getString("f_passport");
                main.f_address = rs.getString("f_address");
                main.phone_dom = rs.getString("phone_dom");
                main.phone_rab = rs.getString("phone_rab");
                main.phone_mob = rs.getString("phone_mob");
                main.email = rs.getString("email");
                main.sms = rs.getString("sms");
                main.spam = rs.getString("spam");

                main.tur1_fio = rs.getString("tur1_fio");
                main.tur1_bd = rs.getString("tur1_bd");
                main.tur1_passport = rs.getString("tur1_passport");
                main.tur1_adr = rs.getString("tur1_adr");

                main.tur2_fio = rs.getString("tur2_fio");
                main.tur2_bd = rs.getString("tur2_bd");
                main.tur2_passport = rs.getString("tur2_passport");
                main.tur2_adr = rs.getString("tur2_adr");

                main.tur3_fio = rs.getString("tur3_fio");
                main.tur3_bd = rs.getString("tur3_bd");
                main.tur3_passport = rs.getString("tur3_passport");
                main.tur3_adr = rs.getString("tur3_adr");

                main.tur4_fio = rs.getString("tur4_fio");
                main.tur4_bd = rs.getString("tur4_bd");
                main.tur4_passport = rs.getString("tur4_passport");
                main.tur4_adr = rs.getString("tur4_adr");

                main.tur5_fio = rs.getString("tur5_fio");
                main.tur5_bd = rs.getString("tur5_bd");
                main.tur5_passport = rs.getString("tur5_passport");
                main.tur5_adr = rs.getString("tur5_adr");

                main.tur_date_s = rs.getString("tur_date_s");
                main.tur_date_po = rs.getString("tur_date_po");
                main.tur_begin = rs.getString("tur_begin");
                main.tur_punkt = rs.getString("tur_punkt");
                main.tur_end = rs.getString("tur_end");  
                main.bilet_cat = rs.getString("bilet_cat");
                main.h_name = rs.getString("h_name");
                main.h_nomer = rs.getString("h_nomer");
                main.food = rs.getString("food");
                main.gid = rs.getString("gid");
                main.visa = rs.getString("visa");
                main.strah = rs.getString("strah");
                main.heal = rs.getString("heal");
                main.transfer = rs.getString("transfer");
                main.excurs = rs.getString("excurs");
                main.service = rs.getString("service");
                main.price = rs.getString("price");
                main.sale_date = rs.getString("sale_date");
                main.seller = rs.getString("seller");
                main.edited = rs.getString("edited");
                main.edited_count = rs.getString("edited_count");
                
                main.pokupatel = rs.getString("pokupatel");
                main.tur1_price = rs.getString("tur1_price");
                main.tur2_price = rs.getString("tur2_price");
                main.tur3_price = rs.getString("tur3_price");
                main.tur4_price = rs.getString("tur4_price");
                main.tur5_price = rs.getString("tur5_price");
                main.procent = rs.getString("procent");
                main.last_price = rs.getString("last_price");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex);
        }
       
        jPanel_dogovor.setBorder(javax.swing.BorderFactory.createTitledBorder("№ договора: " + main.id));
        jLabel_tur_num.setText(main.id);
        
        jComboBox_pokupatel.setEnabled(false);
        jComboBox_pokupatel.removeAllItems();
        jComboBox_pokupatel.addItem(main.pokupatel);

        jComboBox_procent.setEnabled(false);
        jComboBox_procent.removeAllItems();
        jComboBox_procent.addItem(main.procent);
        
        jComboBox_prodavets.setEnabled(false);
        jComboBox_prodavets.removeAllItems();
        jComboBox_prodavets.addItem(main.prodavets);
        
        jComboBox_turoperator.removeAllItems();
        jComboBox_turoperator.addItem(main.turoperator);
        
        jComboBox_tur_name.setEnabled(false);
        jComboBox_tur_name.removeAllItems();
        jComboBox_tur_name.addItem(main.tur_name);
      
        jTextField_tur1_price.setText(main.tur1_price);
        jTextField_tur2_price.setText(main.tur2_price);
        jTextField_tur3_price.setText(main.tur3_price);
        jTextField_tur4_price.setText(main.tur4_price);
        jTextField_tur5_price.setText(main.tur5_price);
        jTextField_last_price.setText(main.last_price);
        
        jTextField_f_fam.setText(main.f_fam);
        jTextField_f_name.setText(main.f_name);
        jTextField_f_otche.setText(main.f_otche);
        jTextField_f_passport.setText(main.f_passport);
        jTextArea_f_address.setText(main.f_address);
        jTextField_phone_dom.setText(main.phone_dom);
        jTextField_phone_rab.setText(main.phone_rab);
        jTextField_phone_mob.setText(main.phone_mob);
        jTextField_email.setText(main.email);
        if(main.sms != null && main.sms.equalsIgnoreCase("1")) {jCheckBox_sms.setSelected(true);} else {jCheckBox_sms.setSelected(false);}
        if(main.spam != null && main.spam.equalsIgnoreCase("1")) {jCheckBox_spam.setSelected(true);} else {jCheckBox_spam.setSelected(false);}
        
        jTextField_price.setText(main.price);
        jLabel_price_word.setText(Num.toString(Integer.parseInt(main.price)));
        jDateChooser_sale_date.setDate(getDateFromSQL(main.sale_date));
        jComboBox_seller.removeAllItems();
        jComboBox_seller.addItem(main.seller);
        
        jTextField_tur1_fio.setText(main.tur1_fio);
        jDateChooser_tur1_bd.setDate(getDateFromSQL(main.tur1_bd));
        jTextField_tur1_passport.setText(main.tur1_passport);
        jTextField_tur1_adr.setText(main.tur1_adr);
        jTextField_tur2_fio.setText(main.tur2_fio);
        jDateChooser_tur2_bd.setDate(getDateFromSQL(main.tur2_bd));
        jTextField_tur2_passport.setText(main.tur2_passport);
        jTextField_tur2_adr.setText(main.tur2_adr);
        jTextField_tur3_fio.setText(main.tur3_fio);
        jDateChooser_tur3_bd.setDate(getDateFromSQL(main.tur3_bd));
        jTextField_tur3_passport.setText(main.tur3_passport);
        jTextField_tur3_adr.setText(main.tur3_adr);
        jTextField_tur4_fio.setText(main.tur4_fio);
        jDateChooser_tur4_bd.setDate(getDateFromSQL(main.tur4_bd));
        jTextField_tur4_passport.setText(main.tur4_passport);
        jTextField_tur4_adr.setText(main.tur4_adr);
        jTextField_tur5_fio.setText(main.tur5_fio);
        jDateChooser_tur5_bd.setDate(getDateFromSQL(main.tur5_bd));
        jTextField_tur5_passport.setText(main.tur5_passport);
        jTextField_tur5_adr.setText(main.tur5_adr);
        jDateChooser_tur_date_s.setDate(getDateFromSQL(main.tur_date_s));
        jDateChooser_tur_date_po.setDate(getDateFromSQL(main.tur_date_po));
        jTextField_tur_begin.setText(main.tur_begin);
        jTextField_tur_punkt.setText(main.tur_punkt);
        jTextField_tur_end.setText(main.tur_end);
        jTextField_h_name.setText(main.h_name);
         jComboBox_h_nomer.removeAllItems();
         jComboBox_h_nomer.addItem(main.h_nomer);
     //   jComboBox_h_nomer.setSelectedItem(main.h_nomer);////не будет работать, если был введен новый 
         jComboBox_bilet_cat.removeAllItems();
         jComboBox_bilet_cat.addItem(main.bilet_cat);
       // jComboBox_bilet_cat.setSelectedItem(main.bilet_cat); //не будет работать, если был введен новый 
         jComboBox_food.removeAllItems();
         jComboBox_food.addItem(main.food);
       // jComboBox_food.setSelectedItem(main.food);  //не будет работать, если был введен новый 
         jComboBox_transfer.removeAllItems();
         jComboBox_transfer.addItem(main.transfer);
         //  jComboBox_transfer.setSelectedItem(main.transfer); //не будет работать, если был введен новый 
        if(main.gid != null && main.gid.equalsIgnoreCase("1")) {jCheckBox_gid.setSelected(true);} else {jCheckBox_gid.setSelected(false);}
        if(main.visa != null && main.visa.equalsIgnoreCase("1")) {jCheckBox_visa.setSelected(true);} else {jCheckBox_visa.setSelected(false);}
        if(main.strah != null && main.strah.equalsIgnoreCase("1")) {jCheckBox_strah.setSelected(true);} else {jCheckBox_strah.setSelected(false);}
        if(main.heal != null && main.heal.equalsIgnoreCase("1")) {jCheckBox_heal.setSelected(true);} else {jCheckBox_heal.setSelected(false);}
        jTextArea_excurs.setText(main.excurs);
        jTextArea_service.setText(main.service);
        jLabel_edited_count.setText(main.edited_count);
        jCheckBox_price.setSelected(false);
    }
        
    // FALSE - добавить нового клиента // TRUE - редактировать старого клиента
    public String fields_newClient_2(boolean query_type){
            
            DBT_main main = new DBT_main();
            
            main.id = "'" + jLabel_tur_num.getText() + "'";
            main.prodavets = "'" + jComboBox_prodavets.getSelectedItem().toString() + "'";
            main.turoperator = "'" + jComboBox_turoperator.getSelectedItem().toString() + "'";
            main.tur_name = "'" + jComboBox_tur_name.getSelectedItem().toString() + "'";
            main.f_fam = "'" + jTextField_f_fam.getText() + "'";
            main.f_name = "'" + jTextField_f_name.getText() + "'";
            main.f_otche = "'" + jTextField_f_otche.getText() + "'";
            main.f_passport = "'" + jTextField_f_passport.getText() + "'";
            main.f_address = "'" + jTextArea_f_address.getText() + "'";
            main.phone_dom = "'" + jTextField_phone_dom.getText() + "'";
            main.phone_rab = "'" + jTextField_phone_rab.getText() + "'";
            main.phone_mob = "'" + jTextField_phone_mob.getText() + "'";
            main.email = "'" + jTextField_email.getText().trim() + "'";
            main.sms = jCheckBox_sms.isSelected() ? "'1'" : "'0'";
            main.spam = jCheckBox_spam.isSelected() ? "'1'" : "'0'";
            
            main.tur1_fio = "'" + jTextField_tur1_fio.getText() + "'";
            main.tur1_bd = reFormateDateForSQL(jDateChooser_tur1_bd.getDate());
            main.tur1_passport = "'" + jTextField_tur1_passport.getText() + "'";
            main.tur1_adr = "'" + jTextField_tur1_adr.getText() + "'";
            
            main.tur2_fio = "'" + jTextField_tur2_fio.getText() + "'";
            main.tur2_bd = reFormateDateForSQL(jDateChooser_tur2_bd.getDate());
            main.tur2_passport = "'" + jTextField_tur2_passport.getText() + "'";
            main.tur2_adr = "'" + jTextField_tur2_adr.getText() + "'";
                    
            main.tur3_fio = "'" + jTextField_tur3_fio.getText() + "'";
            main.tur3_bd = reFormateDateForSQL(jDateChooser_tur3_bd.getDate());
            main.tur3_passport = "'" + jTextField_tur3_passport.getText() + "'";
            main.tur3_adr = "'" + jTextField_tur3_adr.getText() + "'";
            
            main.tur4_fio = "'" + jTextField_tur4_fio.getText() + "'";
            main.tur4_bd = reFormateDateForSQL(jDateChooser_tur4_bd.getDate());
            main.tur4_passport = "'" + jTextField_tur4_passport.getText() + "'";
            main.tur4_adr = "'" + jTextField_tur4_adr.getText() + "'";
                    
            main.tur5_fio = "'" + jTextField_tur5_fio.getText() + "'";
            main.tur5_bd = reFormateDateForSQL(jDateChooser_tur5_bd.getDate());
            main.tur5_passport = "'" + jTextField_tur5_passport.getText() + "'";
            main.tur5_adr = "'" + jTextField_tur5_adr.getText() + "'";
            
            main.tur_date_s = reFormateDateForSQL(jDateChooser_tur_date_s.getDate());
            main.tur_date_po = reFormateDateForSQL(jDateChooser_tur_date_po.getDate());
            main.tur_begin = "'" + jTextField_tur_begin.getText() + "'";
            main.tur_punkt = "'" + jTextField_tur_punkt.getText() + "'";
            main.tur_end = "'" + jTextField_tur_end.getText() + "'";       
            main.bilet_cat = "'" + jComboBox_bilet_cat.getSelectedItem() + "'";
            main.h_name = "'" + jTextField_h_name.getText() + "'";
            main.h_nomer = "'" + jComboBox_h_nomer.getSelectedItem().toString() + "'";
            main.food = "'" + jComboBox_food.getSelectedItem().toString() + "'";
            main.gid = jCheckBox_gid.isSelected() ? "'1'" : "'0'";
            main.visa = jCheckBox_visa.isSelected() ? "'1'" : "'0'";
            main.strah = jCheckBox_strah.isSelected() ? "'1'" : "'0'";
            main.heal = jCheckBox_heal.isSelected() ? "'1'" : "'0'";
            main.transfer = "'" + jComboBox_transfer.getSelectedItem().toString() + "'";
            main.excurs = "'" + jTextArea_excurs.getText() + "'";
            main.service = "'" + jTextArea_service.getText() + "'";
            main.price = "'" + jTextField_price.getText() + "'";
            main.sale_date = reFormateDateForSQL(jDateChooser_sale_date.getDate());
            main.seller = "'" + jComboBox_seller.getSelectedItem().toString() + "'";
            main.edited = "'0'"; 
            main.edited_count = "'0'";
            String edited_count = "'" + (Integer.parseInt(jLabel_edited_count.getText()) + 1) + "'";
           
            main.pokupatel = "'" + jComboBox_pokupatel.getSelectedItem().toString() + "'";
            main.tur1_price = jTextField_tur1_price.getText().trim().equalsIgnoreCase("") ? "null" : "'" + jTextField_tur1_price.getText().trim() + "'";
            main.tur2_price = jTextField_tur2_price.getText().trim().equalsIgnoreCase("") ? "null" : "'" + jTextField_tur2_price.getText().trim() + "'";
            main.tur3_price = jTextField_tur3_price.getText().trim().equalsIgnoreCase("") ? "null" : "'" + jTextField_tur3_price.getText().trim() + "'";
            main.tur4_price = jTextField_tur4_price.getText().trim().equalsIgnoreCase("") ? "null" : "'" + jTextField_tur4_price.getText().trim() + "'";
            main.tur5_price = jTextField_tur5_price.getText().trim().equalsIgnoreCase("") ? "null" : "'" + jTextField_tur5_price.getText().trim() + "'";
            main.procent = "'" + jComboBox_procent.getSelectedItem().toString() + "'";
            main.last_price = jTextField_last_price.getText().trim().equalsIgnoreCase("") ? "null" : "'" + jTextField_last_price.getText().trim() + "'";
            
            String query;
            if (query_type){
                //UPDATE
                query = "UPDATE main SET "
                        + "prodavets = " + main.prodavets + ","
                        + "turoperator = " + main.turoperator + ","
                        + "tur_name = " + main.tur_name + ","
                        + "f_fam = " + main.f_fam + ","
                        + "f_name = " + main.f_name + ","
                        + "f_otche = " + main.f_otche + ","
                        + "f_passport = " + main.f_passport + ","
                        + "f_address = " + main.f_address + ","
                        + "phone_dom = " + main.phone_dom + ","
                        + "phone_rab = " + main.phone_rab + ","
                        + "phone_mob = " + main.phone_mob + ","
                        + "email = " + main.email + ","
                        + "sms = " + main.sms + ","
                        + "spam = " + main.spam + ","
                        + "tur1_fio = " + main.tur1_fio + ","
                        + "tur1_bd = " + main.tur1_bd + ","
                        + "tur1_passport = " + main.tur1_passport + ","
                        + "tur1_adr = " + main.tur1_adr + ","
                        + "tur2_fio = " + main.tur2_fio + ","
                        + "tur2_bd = " + main.tur2_bd + ","
                        + "tur2_passport = " + main.tur2_passport + ","
                        + "tur2_adr = " + main.tur2_adr + ","
                        + "tur3_fio = " + main.tur3_fio + ","
                        + "tur3_bd = " + main.tur3_bd + ","
                        + "tur3_passport = " + main.tur3_passport + ","
                        + "tur3_adr = " + main.tur3_adr + ","
                        + "tur4_fio = " + main.tur4_fio + ","
                        + "tur4_bd = " + main.tur4_bd + ","
                        + "tur4_passport = " + main.tur4_passport + ","
                        + "tur4_adr = " + main.tur4_adr + ","
                        + "tur5_fio = " + main.tur5_fio + ","
                        + "tur5_bd = " + main.tur5_bd + ","
                        + "tur5_passport = " + main.tur5_passport + ","
                        + "tur5_adr = " + main.tur5_adr + ","
                        + "tur_date_s = " + main.tur_date_s + ","
                        + "tur_date_po = " + main.tur_date_po + ","
                        + "tur_begin = " + main.tur_begin + ","
                        + "tur_punkt = " + main.tur_punkt + ","
                        + "tur_end = " + main.tur_end + ","
                        + "bilet_cat = " + main.bilet_cat + ","
                        + "h_name = " + main.h_name + ","
                        + "h_nomer = " + main.h_nomer + ","
                        + "food = " + main.food + ","
                        + "gid = " + main.gid + ","
                        + "visa = " + main.visa + ","
                        + "strah = " + main.strah + ","
                        + "heal = " + main.heal + ","
                        + "transfer = " + main.transfer + ","
                        + "excurs = " + main.excurs + ","
                        + "service = " + main.service + ","
                        + "price = " + main.price + ","
                        + "sale_date = " + main.sale_date + ","
                        + "seller = " + main.seller + ","
                        + "edited = " + main.edited + ","
                        + "edited_count = " + edited_count + ","
                        + "pokupatel = " + main.pokupatel  + ","
                        + "tur1_price = " + main.tur1_price + ","
                        + "tur2_price = " + main.tur2_price + ","
                        + "tur3_price = " + main.tur3_price + ","
                        + "tur4_price = " + main.tur4_price + ","
                        + "tur5_price = " + main.tur5_price + ","
                        + "procent = " + main.procent + ","
                        + "last_price = " + main.last_price
                        + " WHERE id = " + main.id;
                        
            } else {
                //INSERT           
                query = "INSERT INTO main VALUES ("
                    + "null" + "," 
                    + main.prodavets + "," 
                    + main.turoperator + "," 
                    + main.tur_name + "," 
                    + main.f_fam + "," 
                    + main.f_name + "," 
                    + main.f_otche + "," 
                    + main.f_passport + "," 
                    + main.f_address + "," 
                    + main.phone_dom + "," 
                    + main.phone_rab + "," 
                    + main.phone_mob + "," 
                    + main.email + "," 
                    + main.sms + "," 
                    + main.spam + "," 
                    + main.tur1_fio + "," 
                    + main.tur1_bd + "," 
                    + main.tur1_passport + "," 
                    + main.tur1_adr + "," 
                    + main.tur2_fio + "," 
                    + main.tur2_bd + "," 
                    + main.tur2_passport + "," 
                    + main.tur2_adr + "," 
                    + main.tur3_fio + "," 
                    + main.tur3_bd + "," 
                    + main.tur3_passport + "," 
                    + main.tur3_adr + "," 
                    + main.tur4_fio + "," 
                    + main.tur4_bd + "," 
                    + main.tur4_passport + "," 
                    + main.tur4_adr + "," 
                    + main.tur5_fio + "," 
                    + main.tur5_bd + "," 
                    + main.tur5_passport + "," 
                    + main.tur5_adr + "," 
                    + main.tur_date_s + "," 
                    + main.tur_date_po + "," 
                    + main.tur_begin + "," 
                    + main.tur_punkt + "," 
                    + main.tur_end + "," 
                    + main.bilet_cat + "," 
                    + main.h_name + "," 
                    + main.h_nomer + "," 
                    + main.food + "," 
                    + main.gid + "," 
                    + main.visa + "," 
                    + main.strah + "," 
                    + main.heal + "," 
                    + main.transfer + "," 
                    + main.excurs + ","
                    + main.service + "," 
                    + main.price + "," 
                    + main.sale_date + "," 
                    + main.seller + "," 
                    + main.edited + "," 
                    + main.edited_count + "," 
                    + main.pokupatel + "," 
                    + main.tur1_price + "," 
                    + main.tur2_price + "," 
                    + main.tur3_price + "," 
                    + main.tur4_price + "," 
                    + main.tur5_price + "," 
                    + main.procent + "," 
                    + main.last_price + ");";
            }
            return query;
    }
    
    //получить поля для составления и распечатки договора/путевки    
    DBT_main getFields_newClient_2(){
         
        DBT_main main = new DBT_main();
        
        main.id = jLabel_tur_num.getText();
        main.prodavets = jComboBox_prodavets.getSelectedItem().toString();
        main.turoperator = jComboBox_turoperator.getSelectedItem().toString();
        main.tur_name = jComboBox_tur_name.getSelectedItem().toString();
        main.f_fam = jTextField_f_fam.getText().trim();
        main.f_name = jTextField_f_name.getText().trim();
        main.f_otche = jTextField_f_otche.getText().trim();
        main.f_passport = jTextField_f_passport.getText();
        main.f_address = jTextArea_f_address.getText();
        main.phone_dom = jTextField_phone_dom.getText();
        main.phone_rab = jTextField_phone_rab.getText();
        main.phone_mob = jTextField_phone_mob.getText();
        main.email = jTextField_email.getText().trim();
        main.sms = jCheckBox_sms.isSelected() ? "Да" : "Нет";
        main.spam = jCheckBox_spam.isSelected() ? "Да" : "Нет";

        main.tur1_fio = jTextField_tur1_fio.getText().trim();
        main.tur1_bd = reFormateDate(jDateChooser_tur1_bd.getDate(), "dd.MM.yyyy");
        main.tur1_passport = jTextField_tur1_passport.getText();
        main.tur1_adr = jTextField_tur1_adr.getText();

        main.tur2_fio = jTextField_tur2_fio.getText().trim();
        main.tur2_bd = reFormateDate(jDateChooser_tur2_bd.getDate(), "dd.MM.yyyy");
        main.tur2_passport = jTextField_tur2_passport.getText();
        main.tur2_adr = jTextField_tur2_adr.getText();

        main.tur3_fio = jTextField_tur3_fio.getText().trim();
        main.tur3_bd = reFormateDate(jDateChooser_tur3_bd.getDate(), "dd.MM.yyyy");
        main.tur3_passport = jTextField_tur3_passport.getText();
        main.tur3_adr = jTextField_tur3_adr.getText();

        main.tur4_fio = jTextField_tur4_fio.getText().trim();
        main.tur4_bd = reFormateDate(jDateChooser_tur4_bd.getDate(), "dd.MM.yyyy");
        main.tur4_passport = jTextField_tur4_passport.getText();
        main.tur4_adr = jTextField_tur4_adr.getText();

        main.tur5_fio = jTextField_tur5_fio.getText().trim();
        main.tur5_bd = reFormateDate(jDateChooser_tur5_bd.getDate(), "dd.MM.yyyy");
        main.tur5_passport = jTextField_tur5_passport.getText();
        main.tur5_adr = jTextField_tur5_adr.getText();

        main.tur_date_s = reFormateDate(jDateChooser_tur_date_s.getDate(), "dd.MM.yyyy");
        main.tur_date_po = reFormateDate(jDateChooser_tur_date_po.getDate(), "dd.MM.yyyy");
        main.tur_begin = jTextField_tur_begin.getText().trim();
        main.tur_punkt = jTextField_tur_punkt.getText().trim();
        main.tur_end = jTextField_tur_end.getText().trim();      
        main.bilet_cat = jComboBox_bilet_cat.getSelectedItem().toString().trim();
        main.h_name = jTextField_h_name.getText().trim();
        main.h_nomer = jComboBox_h_nomer.getSelectedItem().toString().trim();
        main.food = jComboBox_food.getSelectedItem().toString().trim();
        main.gid = jCheckBox_gid.isSelected() ? "Да" : "Нет";
        main.visa = jCheckBox_visa.isSelected() ? "Да" : "Нет";
        main.strah = jCheckBox_strah.isSelected() ? "Да" : "Нет";
        main.heal = jCheckBox_heal.isSelected() ? "Да" : "Нет";
        main.transfer = jComboBox_transfer.getSelectedItem().toString().trim();
        main.excurs = jTextArea_excurs.getText().trim();
        main.service = jTextArea_service.getText().trim();
        main.price = jTextField_price.getText();
        main.sale_date = reFormateDate(jDateChooser_sale_date.getDate(), "dd.MM.yyyy");
        main.seller = jComboBox_seller.getSelectedItem().toString();
        main.edited = "0"; 
        main.edited_count = jLabel_edited_count.getText();
        
        main.pokupatel = jComboBox_pokupatel.getSelectedItem().toString();
        main.tur1_price = jTextField_tur1_price.getText().trim();
        main.tur2_price = jTextField_tur2_price.getText().trim();
        main.tur3_price = jTextField_tur3_price.getText().trim();
        main.tur4_price = jTextField_tur4_price.getText().trim();
        main.tur5_price = jTextField_tur5_price.getText().trim();
        main.procent = jComboBox_procent.getSelectedItem().toString();
        main.last_price = jTextField_last_price.getText().trim();
        
        return main;
    }
    
    DBT_prodavets getFields_newClient_prodavets(){
        
        DBT_prodavets prodavets = new DBT_prodavets();
        ResultSet rs = db2.executeQuery("SELECT * FROM prodavets WHERE name = '" + jComboBox_prodavets.getSelectedItem().toString() + "'");
        try {
            rs.next();
            prodavets.id = rs.getString("id");
            prodavets.name = rs.getString("name");
            prodavets.fullname = rs.getString("fullname");
            prodavets.inn = rs.getString("inn");
            prodavets.kpp = rs.getString("kpp"); 
            prodavets.ogrn = rs.getString("ogrn");
            prodavets.address_u = rs.getString("address_u");
            prodavets.address_f = rs.getString("address_f");
            prodavets.okved = rs.getString("okved");
            prodavets.okpo = rs.getString("okpo");
            prodavets.okato = rs.getString("okato");
            prodavets.oktmo = rs.getString("oktmo");
            prodavets.okogu = rs.getString("okogu");
            prodavets.okfs = rs.getString("okfs");
            prodavets.okopf = rs.getString("okopf");
            prodavets.phone = rs.getString("phone");
            prodavets.fax = rs.getString("fax");
            prodavets.email = rs.getString("email");
            prodavets.director = rs.getString("director");
            prodavets.r_schet = rs.getString("r_schet");
            prodavets.bank = rs.getString("bank");
            prodavets.k_schet = rs.getString("k_schet");
            prodavets.bik = rs.getString("bik");
            prodavets.dog_director = rs.getString("dog_director");
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
        }
        return prodavets;
    }
    
    DBT_turoperator getFields_newClient_turoperator(){
        
        DBT_turoperator turoperator = new DBT_turoperator();
        ResultSet rs = db2.executeQuery("SELECT * FROM turoperator WHERE name = '" + jComboBox_turoperator.getSelectedItem().toString() + "'");
        try {
            rs.next();
            turoperator.id = rs.getString("id");
            turoperator.name = rs.getString("name");
            turoperator.fullname = rs.getString("fullname");
            turoperator.reestr = rs.getString("reestr");
            turoperator.address_u = rs.getString("address_u");
            turoperator.address_p = rs.getString("address_p");
            turoperator.ogrn = rs.getString("ogrn");
            turoperator.inn = rs.getString("inn");
            turoperator.sposob = rs.getString("sposob");
            turoperator.razmer = rs.getString("razmer");
            turoperator.dogovor = rs.getString("dogovor");
            turoperator.srok = rs.getString("srok");
            turoperator.org_name = rs.getString("org_name");
            turoperator.org_address = rs.getString("org_address");
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
        }
        return turoperator;
    }
    
    DBT_managers getFields_newClient_managers(){
        
        DBT_managers managers = new DBT_managers();
        ResultSet rs = db2.executeQuery("SELECT * FROM managers WHERE name = '" + jComboBox_seller.getSelectedItem().toString() + "'");
        try {
            rs.next();
            managers.id = rs.getString("id");
            managers.name = rs.getString("name");
            managers.prodavets = rs.getString("prodavets");
            managers.dog_name = rs.getString("dog_name");
            
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
        }
        return managers;
        
    }
    
    DBT_turagent getFields_newClient_turagent(){
        
        DBT_turagent turagent = new DBT_turagent();
        ResultSet rs = db2.executeQuery("SELECT * FROM turagent WHERE name = '" + jComboBox_pokupatel.getSelectedItem().toString() + "'");
        try {
            rs.next();
            turagent.id = rs.getString("id");
            turagent.name = rs.getString("name");
            turagent.fullname = rs.getString("fullname");
            turagent.manager = rs.getString("manager");
            turagent.address = rs.getString("address");
            turagent.phone = rs.getString("phone");
            turagent.r_schet = rs.getString("r_schet");
            turagent.k_schet = rs.getString("k_schet");
            turagent.bank = rs.getString("bank");
            turagent.inn = rs.getString("inn");
            turagent.kpp = rs.getString("kpp");
            turagent.bik = rs.getString("bik");
        } catch (SQLException ex) {
            Logger.getLogger(Turist.class.getName()).log(Level.SEVERE, null, ex);
        }
        return turagent;
    }
    
    public String checkFields(){
        if (jComboBox_tur_name.getSelectedItem() == null) return "Тур";
        if (jComboBox_tur_name.getSelectedItem().toString().equalsIgnoreCase("")) return "Тур";
        if (jTextField_f_fam.getText().trim().equals("")) return "Фамилия";
        if (jTextField_f_name.getText().trim().equals("")) return "Имя";
        if (jTextField_f_otche.getText().trim().equals("")) return "Отчество";
        if (jTextField_f_passport.getText().trim().equals("")) return "Паспорт";
        if (jTextArea_f_address.getText().trim().equals("")) return "Адрес";
        if (jTextField_phone_mob.getText().trim().equals("") 
         && jTextField_phone_rab.getText().trim().equals("")
         && jTextField_phone_dom.getText().trim().equals("")) return "Телефон";   
        if (jDateChooser_tur_date_s.getDate() == null) return "Продолжительность поездки";
        if (jDateChooser_tur_date_po.getDate() == null) return "Продолжительность поездки";
        return "ok";
    }

    public void changePokupatel(){
        if (jComboBox_pokupatel.getItemCount() == 0) return;
        boolean agent = !jComboBox_pokupatel.getSelectedItem().toString().equalsIgnoreCase("Турист");
        if(!agent) {
            jComboBox_procent.setSelectedIndex(0);
            jComboBox_procentItemStateChanged(null);
        }
        jComboBox_procent.setEnabled(agent);
        jTextField_last_price.setEditable(agent);
        
    }
    
    public void SAVE(boolean bool) {
        jButton_new_save.setEnabled(bool);
        //когда меняется свойство кнопки СОХРАНИТЬ
        //блокировка полей для ввода на форме нового клиента
        
      //  jTextArea_u_recvizit.setEditable(jButton_new_save.isEnabled());
        jTextArea_f_address.setEditable(jButton_new_save.isEnabled());
      //  jTextField_u_firma.setEditable(jButton_new_save.isEnabled());
//        jTextField_u_post.setEditable(jButton_new_save.isEnabled());
//        jTextField_u_fam.setEditable(jButton_new_save.isEnabled());
//        jTextField_u_name.setEditable(jButton_new_save.isEnabled());
//        jTextField_u_otche.setEditable(jButton_new_save.isEnabled());
//        jCheckBox_fizik.setEnabled(jButton_new_save.isEnabled());
        jTextField_f_fam.setEditable(jButton_new_save.isEnabled());
        jTextField_f_name.setEditable(jButton_new_save.isEnabled());
        jTextField_f_otche.setEditable(jButton_new_save.isEnabled());
        jTextField_f_passport.setEditable(jButton_new_save.isEnabled());
        jTextField_phone_dom.setEditable(jButton_new_save.isEnabled());
        jTextField_phone_rab.setEditable(jButton_new_save.isEnabled());
        jTextField_phone_mob.setEditable(jButton_new_save.isEnabled());
        jTextField_email.setEditable(jButton_new_save.isEnabled());
        jCheckBox_spam.setEnabled(jButton_new_save.isEnabled());
        jCheckBox_sms.setEnabled(jButton_new_save.isEnabled());
        
        jComboBox_tur_name.setEnabled(jButton_new_save.isEnabled());
        jCheckBox_gid.setEnabled(jButton_new_save.isEnabled());
        jTextField_tur1_adr.setEditable(jButton_new_save.isEnabled());
        jTextField_tur2_adr.setEditable(jButton_new_save.isEnabled());
        jTextField_tur3_adr.setEditable(jButton_new_save.isEnabled());
        jTextField_tur4_adr.setEditable(jButton_new_save.isEnabled());
        jTextField_tur5_adr.setEditable(jButton_new_save.isEnabled());
        jTextField_tur1_fio.setEditable(jButton_new_save.isEnabled());
        jTextField_tur2_fio.setEditable(jButton_new_save.isEnabled());
        jTextField_tur3_fio.setEditable(jButton_new_save.isEnabled());
        jTextField_tur4_fio.setEditable(jButton_new_save.isEnabled());
        jTextField_tur5_fio.setEditable(jButton_new_save.isEnabled());
        jDateChooser_tur1_bd.setEnabled(jButton_new_save.isEnabled());
        jDateChooser_tur2_bd.setEnabled(jButton_new_save.isEnabled());
        jDateChooser_tur3_bd.setEnabled(jButton_new_save.isEnabled());
        jDateChooser_tur4_bd.setEnabled(jButton_new_save.isEnabled());
        jDateChooser_tur5_bd.setEnabled(jButton_new_save.isEnabled());
        jDateChooser_tur_date_po.setEnabled(jButton_new_save.isEnabled());
        jDateChooser_tur_date_s.setEnabled(jButton_new_save.isEnabled());
        jTextField_tur_begin.setEditable(jButton_new_save.isEnabled());
        jTextField_tur_end.setEditable(jButton_new_save.isEnabled());
        jTextField_tur_punkt.setEditable(jButton_new_save.isEnabled());
        jComboBox_bilet_cat.setEnabled(jButton_new_save.isEnabled());
        jTextField_h_name.setEditable(jButton_new_save.isEnabled());
        jComboBox_h_nomer.setEnabled(jButton_new_save.isEnabled());
        jComboBox_food.setEnabled(jButton_new_save.isEnabled());
        jCheckBox_visa.setEnabled(jButton_new_save.isEnabled());
        jCheckBox_strah.setEnabled(jButton_new_save.isEnabled());
        jCheckBox_heal.setEnabled(jButton_new_save.isEnabled());
        jComboBox_transfer.setEnabled(jButton_new_save.isEnabled());
        jTextArea_excurs.setEditable(jButton_new_save.isEnabled());
        jTextArea_service.setEditable(jButton_new_save.isEnabled());
        jTextField_price.setEditable(jButton_new_save.isEnabled());
        jDateChooser_sale_date.setEnabled(jButton_new_save.isEnabled());
        jComboBox_seller.setEnabled(jButton_new_save.isEnabled());
        
        jButton_new_edit.setEnabled(!jButton_new_save.isEnabled());
        jButton_new_copy.setEnabled(!jButton_new_save.isEnabled());
//        jButton_new_addtur.setEnabled(!jButton_new_save.isEnabled());
//        jButton_new_print.setEnabled(!jButton_new_save.isEnabled());
        
        jButton_new_dogovor.setEnabled(!jButton_new_save.isEnabled());
        jButton_new_fortuna.setEnabled(!jButton_new_save.isEnabled());
        jButton_new_pko.setEnabled(!jButton_new_save.isEnabled());
        jButton_new_prilozhenie.setEnabled(!jButton_new_save.isEnabled());
        jButton_new_putevka.setEnabled(!jButton_new_save.isEnabled());
        
        jButton_new_schet.setEnabled(false);
        jButton_new_podtverzhdenie.setEnabled(false);
        jButton_new_pko_a.setEnabled(false);
        
        if(!bool && !jComboBox_pokupatel.getSelectedItem().toString().equalsIgnoreCase("Турист")){
            jButton_new_schet.setEnabled(true);
            jButton_new_podtverzhdenie.setEnabled(true);
            jButton_new_pko_a.setEnabled(true);
            
            jButton_new_dogovor.setEnabled(false);
            jButton_new_fortuna.setEnabled(false);
            jButton_new_pko.setEnabled(false);
            jButton_new_prilozhenie.setEnabled(false);
            jButton_new_putevka.setEnabled(false);
        }
        
        
        jComboBox_prodavets.setEnabled(jButton_new_save.isEnabled());
        jComboBox_turoperator.setEnabled(jButton_new_save.isEnabled());
        
        jTextField_tur1_passport.setEditable(jButton_new_save.isEnabled());
        jTextField_tur2_passport.setEditable(jButton_new_save.isEnabled());
        jTextField_tur3_passport.setEditable(jButton_new_save.isEnabled());
        jTextField_tur4_passport.setEditable(jButton_new_save.isEnabled());
        jTextField_tur5_passport.setEditable(jButton_new_save.isEnabled());
        
        jTextField_tur1_price.setEditable(jButton_new_save.isEnabled());
        jTextField_tur2_price.setEditable(jButton_new_save.isEnabled());
        jTextField_tur3_price.setEditable(jButton_new_save.isEnabled());
        jTextField_tur4_price.setEditable(jButton_new_save.isEnabled());
        jTextField_tur5_price.setEditable(jButton_new_save.isEnabled());
        
        jTextField_last_price.setEditable(jButton_new_save.isEnabled());
        
        jComboBox_pokupatel.setEnabled(jButton_new_save.isEnabled());
        jComboBox_procent.setEnabled(jButton_new_save.isEnabled());
        jCheckBox_price.setEnabled(jButton_new_save.isEnabled());
        
    }
    
    public String zero(int number) {
        String nol = "0000" + number;
        if(nol.length() == 6) return nol.substring(1, nol.length());
        if(nol.length() == 7) return nol.substring(2, nol.length());
        if(nol.length() == 8) return nol.substring(3, nol.length());
        if(nol.length() == 9) return nol.substring(4, nol.length());
        return nol;
    }
    
    
    static boolean birthday_today = false;
    private final boolean QUERY_INSERT = false;
    private final boolean QUERY_UPDATE = true;
    private boolean query_type = QUERY_INSERT;
    private int count = 0;
    private DB db;
    private static DB dbs;
    private static JDayChooser jday;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton_client_close;
    private javax.swing.JButton jButton_client_save;
    private javax.swing.JButton jButton_edit;
    private javax.swing.JButton jButton_email;
    private javax.swing.JButton jButton_happy;
    private javax.swing.JButton jButton_happy_client;
    private javax.swing.JButton jButton_happy_mail;
    private javax.swing.JButton jButton_happy_sms;
    private javax.swing.JButton jButton_mail_resume;
    private javax.swing.JButton jButton_mail_send;
    private javax.swing.JButton jButton_mail_start;
    private javax.swing.JButton jButton_mail_stop;
    private javax.swing.JButton jButton_mail_suspend;
    private javax.swing.JButton jButton_manager_add;
    private javax.swing.JButton jButton_new;
    private javax.swing.JButton jButton_new_close;
    private javax.swing.JButton jButton_new_close1;
    private javax.swing.JButton jButton_new_close10;
    private javax.swing.JButton jButton_new_close11;
    private javax.swing.JButton jButton_new_close2;
    private javax.swing.JButton jButton_new_close3;
    private javax.swing.JButton jButton_new_close4;
    private javax.swing.JButton jButton_new_close5;
    private javax.swing.JButton jButton_new_close6;
    private javax.swing.JButton jButton_new_close7;
    private javax.swing.JButton jButton_new_close8;
    private javax.swing.JButton jButton_new_close9;
    private javax.swing.JButton jButton_new_copy;
    private javax.swing.JButton jButton_new_dogovor;
    private javax.swing.JButton jButton_new_edit;
    private javax.swing.JButton jButton_new_fortuna;
    private javax.swing.JButton jButton_new_pko;
    private javax.swing.JButton jButton_new_pko_a;
    private javax.swing.JButton jButton_new_podtverzhdenie;
    private javax.swing.JButton jButton_new_prilozhenie;
    private javax.swing.JButton jButton_new_putevka;
    private javax.swing.JButton jButton_new_save;
    private javax.swing.JButton jButton_new_schet;
    private javax.swing.JButton jButton_opt_prodavets_cancel;
    private javax.swing.JButton jButton_opt_turagent_cancel;
    private javax.swing.JButton jButton_opt_turoperator_cancel;
    private javax.swing.JButton jButton_opt_turs_add;
    private javax.swing.JButton jButton_opt_turs_cancel;
    private javax.swing.JButton jButton_opt_turs_edit;
    private javax.swing.JButton jButton_opt_turs_save;
    private javax.swing.JButton jButton_otchet;
    private javax.swing.JButton jButton_print;
    private javax.swing.JButton jButton_prodavets_edit;
    private javax.swing.JButton jButton_prodavets_new;
    private javax.swing.JButton jButton_prodavets_save;
    private javax.swing.JButton jButton_prop_default;
    private javax.swing.JButton jButton_prop_save;
    private javax.swing.JButton jButton_report;
    private javax.swing.JButton jButton_save_Excel;
    private javax.swing.JButton jButton_search;
    private javax.swing.JButton jButton_search_search;
    private javax.swing.JButton jButton_sms;
    private javax.swing.JButton jButton_sms_send;
    private javax.swing.JButton jButton_sms_start;
    private javax.swing.JButton jButton_turagent_edit;
    private javax.swing.JButton jButton_turagent_new;
    private javax.swing.JButton jButton_turagent_save;
    private javax.swing.JButton jButton_turoperator_edit;
    private javax.swing.JButton jButton_turoperator_new;
    private javax.swing.JButton jButton_turoperator_save;
    private javax.swing.JButton jButton_update;
    private javax.swing.JCheckBox jCheckBox_gid;
    private javax.swing.JCheckBox jCheckBox_heal;
    private javax.swing.JCheckBox jCheckBox_price;
    private javax.swing.JCheckBox jCheckBox_r_all;
    private javax.swing.JCheckBox jCheckBox_r_fam;
    private javax.swing.JCheckBox jCheckBox_r_mail;
    private javax.swing.JCheckBox jCheckBox_r_manager;
    private javax.swing.JCheckBox jCheckBox_r_phone;
    private javax.swing.JCheckBox jCheckBox_r_pokupatel;
    private javax.swing.JCheckBox jCheckBox_r_price;
    private javax.swing.JCheckBox jCheckBox_r_prodavets;
    private javax.swing.JCheckBox jCheckBox_r_tur_begin;
    private javax.swing.JCheckBox jCheckBox_r_tur_data;
    private javax.swing.JCheckBox jCheckBox_r_tur_name;
    private javax.swing.JCheckBox jCheckBox_r_tur_punkt;
    private javax.swing.JCheckBox jCheckBox_r_turoperator;
    private javax.swing.JCheckBox jCheckBox_report_open;
    private javax.swing.JCheckBox jCheckBox_sms;
    private javax.swing.JCheckBox jCheckBox_spam;
    private javax.swing.JCheckBox jCheckBox_strah;
    private javax.swing.JCheckBox jCheckBox_visa;
    private javax.swing.JComboBox jComboBox_bilet_cat;
    private javax.swing.JComboBox jComboBox_food;
    private javax.swing.JComboBox jComboBox_h_nomer;
    private javax.swing.JComboBox jComboBox_opt_prodavets;
    private javax.swing.JComboBox jComboBox_opt_turagent;
    private javax.swing.JComboBox jComboBox_opt_turoperator;
    private javax.swing.JComboBox jComboBox_opt_turs;
    private javax.swing.JComboBox jComboBox_pokupatel;
    private javax.swing.JComboBox jComboBox_procent;
    private javax.swing.JComboBox jComboBox_prodavets;
    private javax.swing.JComboBox jComboBox_r_manager;
    private javax.swing.JComboBox jComboBox_r_pokupatel;
    private javax.swing.JComboBox jComboBox_r_prodavets;
    private javax.swing.JComboBox jComboBox_r_tur_begin;
    private javax.swing.JComboBox jComboBox_r_tur_name;
    private javax.swing.JComboBox jComboBox_r_tur_punkt;
    private javax.swing.JComboBox jComboBox_r_turoperator;
    private javax.swing.JComboBox jComboBox_search_FIO;
    private javax.swing.JComboBox jComboBox_search_field;
    private javax.swing.JComboBox jComboBox_seller;
    private javax.swing.JComboBox jComboBox_transfer;
    private javax.swing.JComboBox jComboBox_tur_name;
    private javax.swing.JComboBox jComboBox_turoperator;
    private com.toedter.calendar.JDateChooser jDateChooser_r_tur_date_s;
    private com.toedter.calendar.JDateChooser jDateChooser_report_po;
    private com.toedter.calendar.JDateChooser jDateChooser_report_s;
    private com.toedter.calendar.JDateChooser jDateChooser_sale_date;
    private com.toedter.calendar.JDateChooser jDateChooser_tur1_bd;
    private com.toedter.calendar.JDateChooser jDateChooser_tur2_bd;
    private com.toedter.calendar.JDateChooser jDateChooser_tur3_bd;
    private com.toedter.calendar.JDateChooser jDateChooser_tur4_bd;
    private com.toedter.calendar.JDateChooser jDateChooser_tur5_bd;
    private com.toedter.calendar.JDateChooser jDateChooser_tur_date_po;
    private com.toedter.calendar.JDateChooser jDateChooser_tur_date_s;
    private com.toedter.calendar.JDayChooser jDayChooser;
    private javax.swing.JFormattedTextField jFormattedTextField_date1;
    private javax.swing.JFrame jFrame_client;
    private javax.swing.JFrame jFrame_happy;
    private javax.swing.JFrame jFrame_mail;
    private javax.swing.JFrame jFrame_new;
    private javax.swing.JFrame jFrame_options;
    private javax.swing.JFrame jFrame_prodavets;
    private javax.swing.JFrame jFrame_report;
    private javax.swing.JFrame jFrame_report_table;
    private javax.swing.JFrame jFrame_search;
    private javax.swing.JFrame jFrame_sms;
    private javax.swing.JFrame jFrame_turagent;
    private javax.swing.JFrame jFrame_turoperator;
    private javax.swing.JFrame jFrame_turs;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JLabel jLabel69;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel71;
    private javax.swing.JLabel jLabel72;
    private javax.swing.JLabel jLabel73;
    private javax.swing.JLabel jLabel74;
    private javax.swing.JLabel jLabel75;
    private javax.swing.JLabel jLabel76;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel_data1;
    private javax.swing.JLabel jLabel_edited_count;
    private javax.swing.JLabel jLabel_fam1;
    private javax.swing.JLabel jLabel_id1;
    private javax.swing.JLabel jLabel_last_price_word;
    private javax.swing.JLabel jLabel_mail_process_status;
    private javax.swing.JLabel jLabel_mail_subject;
    private javax.swing.JLabel jLabel_mail_to;
    private javax.swing.JLabel jLabel_name1;
    private javax.swing.JLabel jLabel_otche1;
    private javax.swing.JLabel jLabel_price1;
    private javax.swing.JLabel jLabel_price_word;
    private javax.swing.JLabel jLabel_sms_length;
    private javax.swing.JLabel jLabel_sms_progress;
    private javax.swing.JLabel jLabel_tur_num;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem_opt_prodavets;
    private javax.swing.JMenuItem jMenuItem_opt_turagent;
    private javax.swing.JMenuItem jMenuItem_opt_turoperator;
    private javax.swing.JMenuItem jMenuItem_opt_turs;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanel_dogovor;
    private javax.swing.JProgressBar jProgressBar_mail;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane15;
    private javax.swing.JScrollPane jScrollPane16;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane_prodavets;
    private javax.swing.JTable jTable_happy;
    private javax.swing.JTable jTable_main;
    private javax.swing.JTable jTable_managers;
    private javax.swing.JTable jTable_opt_turagent;
    private javax.swing.JTable jTable_opt_turoperator;
    private javax.swing.JTable jTable_prodavets;
    private javax.swing.JTable jTable_report;
    private javax.swing.JTable jTable_search;
    private javax.swing.JTable jTable_sms;
    private javax.swing.JTable jTable_spam;
    private javax.swing.JTable jTable_turs;
    private javax.swing.JTextArea jTextArea_excurs;
    private javax.swing.JTextArea jTextArea_f_address;
    private javax.swing.JTextArea jTextArea_service;
    private javax.swing.JTextArea jTextArea_sms_message;
    private javax.swing.JTextField jTextField_db_base;
    private javax.swing.JTextField jTextField_db_host;
    private javax.swing.JTextField jTextField_db_pass;
    private javax.swing.JTextField jTextField_db_user;
    private javax.swing.JTextField jTextField_email;
    private javax.swing.JTextField jTextField_f_fam;
    private javax.swing.JTextField jTextField_f_name;
    private javax.swing.JTextField jTextField_f_otche;
    private javax.swing.JTextField jTextField_f_passport;
    private javax.swing.JTextField jTextField_fam1;
    private javax.swing.JTextField jTextField_h_name;
    private javax.swing.JTextField jTextField_id1;
    private javax.swing.JTextField jTextField_last_price;
    private javax.swing.JTextField jTextField_mail_from;
    private javax.swing.JTextField jTextField_mail_smtp;
    private javax.swing.JTextField jTextField_mail_smtp_pass;
    private javax.swing.JTextField jTextField_mail_smtp_user;
    private javax.swing.JTextField jTextField_mail_subject;
    private javax.swing.JTextField jTextField_mail_to;
    private javax.swing.JTextField jTextField_manager;
    private javax.swing.JTextField jTextField_manager_dog;
    private javax.swing.JTextField jTextField_name1;
    private javax.swing.JTextField jTextField_otche1;
    private javax.swing.JTextField jTextField_phone_dom;
    private javax.swing.JTextField jTextField_phone_mob;
    private javax.swing.JTextField jTextField_phone_rab;
    private javax.swing.JTextField jTextField_price;
    private javax.swing.JTextField jTextField_price1;
    private javax.swing.JTextField jTextField_search;
    private javax.swing.JTextField jTextField_sms_from;
    private javax.swing.JTextField jTextField_sms_nomer;
    private javax.swing.JTextField jTextField_sms_smtp;
    private javax.swing.JTextField jTextField_sms_smtp_pass;
    private javax.swing.JTextField jTextField_sms_smtp_user;
    private javax.swing.JTextField jTextField_sms_spam_login;
    private javax.swing.JTextField jTextField_sms_spam_pass;
    private javax.swing.JTextField jTextField_sms_to;
    private javax.swing.JTextField jTextField_tur1_adr;
    private javax.swing.JTextField jTextField_tur1_fio;
    private javax.swing.JTextField jTextField_tur1_passport;
    private javax.swing.JTextField jTextField_tur1_price;
    private javax.swing.JTextField jTextField_tur2_adr;
    private javax.swing.JTextField jTextField_tur2_fio;
    private javax.swing.JTextField jTextField_tur2_passport;
    private javax.swing.JTextField jTextField_tur2_price;
    private javax.swing.JTextField jTextField_tur3_adr;
    private javax.swing.JTextField jTextField_tur3_fio;
    private javax.swing.JTextField jTextField_tur3_passport;
    private javax.swing.JTextField jTextField_tur3_price;
    private javax.swing.JTextField jTextField_tur4_adr;
    private javax.swing.JTextField jTextField_tur4_fio;
    private javax.swing.JTextField jTextField_tur4_passport;
    private javax.swing.JTextField jTextField_tur4_price;
    private javax.swing.JTextField jTextField_tur5_adr;
    private javax.swing.JTextField jTextField_tur5_fio;
    private javax.swing.JTextField jTextField_tur5_passport;
    private javax.swing.JTextField jTextField_tur5_price;
    private javax.swing.JTextField jTextField_tur_begin;
    private javax.swing.JTextField jTextField_tur_end;
    private javax.swing.JTextField jTextField_tur_punkt;
    private javax.swing.JTextPane jTextPane_msg;
    // End of variables declaration//GEN-END:variables
}
