
package Turist;

/**
 *
 * @author mozevil
 */
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;


public class DB {
 /*   
    private String user = "root";
    private String pass = "root";
    private String host = "127.0.0.1:3306";
    private String base = "foton"; 
    private String url = "jdbc:mysql://" + host + "/" + base + "?useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8";
  */
    private Connection conn = null;
    private Statement st = null;
    
  /*  public DB() {
        try {
            Class.forName ("com.mysql.jdbc.Driver").newInstance ();
            conn = DriverManager.getConnection (url, user, pass);
            st = conn.createStatement();
            
        } catch (InstantiationException | IllegalAccessException | SQLException | ClassNotFoundException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex);
        }
    } */
    public DB(String user, String pass, String host, String base) {
        String url = "jdbc:mysql://" + host + "/" + base + "?useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8";
        try {
            Class.forName ("com.mysql.jdbc.Driver").newInstance ();
            conn = DriverManager.getConnection (url, user, pass);
            st = conn.createStatement();
            
        } catch (InstantiationException | IllegalAccessException | SQLException | ClassNotFoundException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }
    
    public Connection getConnection() {        
        return conn;
    }
    
    public Statement getStatement() {
        return st;
    }
    
    public ResultSet executeQuery(String query) {
        try {
            return st.executeQuery(query);
            
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
            return null;
        }
    }
    
    public boolean executeUpdate(String query) {
        try {
            st.executeUpdate(query);
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
            return false;
        }
    }
    
    public void close() {
        try {
            if (st != null) st.close();
            if (conn != null) conn.close();            
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }
}

class DBT_main {

    String id;
    String prodavets;
    String turoperator;
    //String tur_num;
    //String dogovor_num;
    String tur_name;
   // String u_firma;
    //String u_post;
    //String u_fam;
//    String u_name;
//    String u_otche;
//    String u_recvizit;
//    String fizik;
    String f_fam;
    String f_name;
    String f_otche;
    String f_passport;
    String f_address;
    String phone_dom;
    String phone_rab;
    String phone_mob;
    String email;
    String sms;
    String spam;
    String tur1_fio;
    String tur1_bd;
    String tur1_passport;
    String tur1_adr;
    String tur2_fio;
    String tur2_bd;
    String tur2_passport;
    String tur2_adr;
    String tur3_fio;
    String tur3_bd;
    String tur3_passport;
    String tur3_adr;
    String tur4_fio;
    String tur4_bd;
    String tur4_passport;
    String tur4_adr;
    String tur5_fio;
    String tur5_bd;
    String tur5_passport;
    String tur5_adr;
    String tur_date_s;
    String tur_date_po;
    String tur_begin;
    String tur_punkt;
    String tur_end;
    String bilet_cat;
    String h_name;
    String h_nomer;
    String food;
    String gid;
    String visa;
    String strah;
    String heal;
    String transfer;
    String excurs;
    String service;
    String price;
    String sale_date;
    String seller;
    //String file_name;
    //String path;
    String edited;
    String edited_count;
    String pokupatel;
    String tur1_price;
    String tur2_price;
    String tur3_price;
    String tur4_price;
    String tur5_price;
    String procent;
    String last_price;
    
}

class DBT_managers {
    
    String id;
    String prodavets;
    String name;
    String dog_name;
    
}

class DBT_prodavets {
    
    String id;
    String name;
    String fullname;
    String inn;
    String kpp;
    String ogrn;
    String address_u;
    String address_f;
    String okved;
    String okpo;
    String okato;
    String oktmo;
    String okogu;
    String okfs;
    String okopf;
    String phone;
    String fax;
    String email;
    String director;
    String r_schet;
    String bank;
    String k_schet;
    String bik;
    String dog_director;
}

class DBT_turoperator {
    
    String id;
    String name;
    String fullname;
    String reestr;
    String address_u;
    String address_p;
    String ogrn;
    String inn;
    String sposob;
    String razmer;
    String dogovor;
    String srok;
    String org_name;
    String org_address;
}

class DBT_turs {
    
    String id;
    String turoperator;
    String name;
    String begin;
    String punkt;
    String end;
    String bilet;
    String hotel;
    String room;
    String food;
    String transfer;
    String gid;
    String visa;
    String strah;
    String heal;
    String excurs;
    String service;
}

class DBT_turagent {
    
    String id;
    String name;
    String fullname;
    String manager;
    String address;
    String phone;
    String r_schet;
    String k_schet;
    String bank;
    String inn;
    String kpp;
    String bik;
}