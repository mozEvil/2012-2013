
package Turist;

import com.lowagie.text.rtf.document.RtfDocument;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import net.sourceforge.rtf.RTFTemplate;
import net.sourceforge.rtf.helper.RTFTemplateBuilder;



/**
 *
 * @author mozevil
 */
public class RTF {
    private RTFTemplateBuilder builder = null;
    private RTFTemplate rtfTemplate = null;
            
    public RTF() {
        try {
            // 1. Get default RTFtemplateBuilder
            builder = RTFTemplateBuilder.newRTFTemplateBuilder();            

            // 2. Get RTFtemplate with default Implementation of template engine (Velocity) 
            rtfTemplate = builder.newRTFTemplate();    
            
        } catch (Exception ex) {
            Logger.getLogger(RTF.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex);
        }
    }
    
    public void putDate(String rtfSource, String rtfTarget, DBT_main main, DBT_prodavets prodavets){
        if (rtfTemplate == null) return;
        //String rtfTarget = baseDir() + "out/" + field[1] + ".rtf";
        try {
            // 3. Set the RTF model source 
            rtfTemplate.setTemplate(new File(rtfSource)); 

            String seller = famIO(main.seller);
            String pokupatel = main.f_fam.trim() + " " + main.f_name.trim().substring(0, 1) + "." + main.f_otche.trim().substring(0, 1) + ".";
            String price_text = Num.toString(Integer.parseInt(main.last_price));
            String date = reFormateDate(main.sale_date, "dd.MM.yyyy", "dd.MMMM.yyyy");
            StringTokenizer stk = new StringTokenizer(date, ".");
            String s_d = stk.nextToken();
            String s_m = stk.nextToken();
            String s_g = stk.nextToken();
            
            // 4. Put the context
            rtfTemplate.put("id", rus(main.id));
            rtfTemplate.put("prodavets", rus(main.prodavets));
            rtfTemplate.put("turoperator", rus(main.turoperator));
            rtfTemplate.put("tur_name", rus(main.tur_name));
            rtfTemplate.put("f_fam", rus(main.f_fam));
            rtfTemplate.put("f_name", rus(main.f_name));
            rtfTemplate.put("f_otche", rus(main.f_otche));
            rtfTemplate.put("f_passport", rus(main.f_passport));
            rtfTemplate.put("f_address", rus(main.f_address));
            if (!main.phone_dom.equalsIgnoreCase("")) {rtfTemplate.put("phone_dom", rus(main.phone_dom));} else {rtfTemplate.put("phone_dom", rus("нет"));}
            if (!main.phone_rab.equalsIgnoreCase("")) {rtfTemplate.put("phone_rab", rus(main.phone_rab));} else {rtfTemplate.put("phone_rab", rus("нет"));}
            if (!main.phone_mob.equalsIgnoreCase("")) {rtfTemplate.put("phone_mob", rus(main.phone_mob));} else {rtfTemplate.put("phone_mob", rus("нет"));}
            rtfTemplate.put("tur1_fio", rus(main.tur1_fio)); // "\\par"+
            if (!main.tur1_passport.equalsIgnoreCase("")) rtfTemplate.put("tur1_passport", rus("паспорт: "+ main.tur1_passport));
            if (!main.tur1_bd.equalsIgnoreCase("") && !main.tur1_bd.equalsIgnoreCase("null")) rtfTemplate.put("tur1_bd", rus("дата рождения: " + main.tur1_bd));
            
            if (!main.tur2_fio.equalsIgnoreCase("")) {
                rtfTemplate.put("tur2_fio", "\\par "+rus(main.tur2_fio));
                if (!main.tur2_passport.equalsIgnoreCase("")) rtfTemplate.put("tur2_passport", rus("паспорт: "+ main.tur2_passport));
                if (!main.tur2_bd.equalsIgnoreCase("") && !main.tur2_bd.equalsIgnoreCase("null")) rtfTemplate.put("tur2_bd", rus("дата рождения: " + main.tur2_bd));
                
            }
            if (!main.tur3_fio.equalsIgnoreCase("")) {
                rtfTemplate.put("tur3_fio", "\\par "+rus(main.tur3_fio));
                if (!main.tur3_passport.equalsIgnoreCase("")) rtfTemplate.put("tur3_passport", rus("паспорт: "+ main.tur3_passport));
                if (!main.tur3_bd.equalsIgnoreCase("") && !main.tur3_bd.equalsIgnoreCase("null")) rtfTemplate.put("tur3_bd", rus("дата рождения: " + main.tur3_bd));
            }
            if (!main.tur4_fio.equalsIgnoreCase("")) {
                rtfTemplate.put("tur4_fio", "\\par "+rus(main.tur4_fio));
                if (!main.tur4_passport.equalsIgnoreCase("")) rtfTemplate.put("tur4_passport", rus("паспорт: "+ main.tur4_passport));
                if (!main.tur4_bd.equalsIgnoreCase("") && !main.tur4_bd.equalsIgnoreCase("null")) rtfTemplate.put("tur4_bd", rus("дата рождения: " + main.tur4_bd));
            }
            if (!main.tur5_fio.equalsIgnoreCase("")) {
                rtfTemplate.put("tur5_fio", "\\par "+rus(main.tur5_fio));
                if (!main.tur5_passport.equalsIgnoreCase("")) rtfTemplate.put("tur5_passport", rus("паспорт: "+ main.tur5_passport));
                if (!main.tur5_bd.equalsIgnoreCase("") && !main.tur5_bd.equalsIgnoreCase("null")) rtfTemplate.put("tur5_bd", rus("дата рождения: " + main.tur5_bd));
            }
            
            rtfTemplate.put("tur_date_s", rus(main.tur_date_s));
            rtfTemplate.put("tur_date_po", rus(main.tur_date_po));
            rtfTemplate.put("tur_begin", rus(main.tur_begin));
            rtfTemplate.put("tur_punkt", rus(main.tur_punkt));
            rtfTemplate.put("tur_end", rus(main.tur_end));
            rtfTemplate.put("bilet_cat", rus(main.bilet_cat));
            rtfTemplate.put("h_name", rus(main.h_name));
            rtfTemplate.put("h_nomer", rus(main.h_nomer));
            rtfTemplate.put("food", rus(main.food));
            rtfTemplate.put("transfer", rus(main.transfer));
            rtfTemplate.put("gid", rus(main.gid));
            rtfTemplate.put("visa", rus(main.visa));
            rtfTemplate.put("strah", rus(main.strah));
            rtfTemplate.put("heal", rus(main.heal));
            rtfTemplate.put("excurs", rus(main.excurs));
            rtfTemplate.put("service", rus(main.service));
            rtfTemplate.put("price", rus(main.last_price));
            rtfTemplate.put("price_text", rus(price_text));
            rtfTemplate.put("sale_date", rus(main.sale_date));
            rtfTemplate.put("seller", rus(seller));
            rtfTemplate.put("pokupatel", rus(pokupatel));
            rtfTemplate.put("s_d", rus(s_d));
            rtfTemplate.put("s_m", rus(s_m));
            rtfTemplate.put("s_g", rus(s_g));
            //jLabel_price_word.getText();
            rtfTemplate.put("prodavets_name", rus(prodavets.name));
            rtfTemplate.put("prodavets_address_u", rus(prodavets.address_u));
            rtfTemplate.put("prodavets_address_f", rus(prodavets.address_f));
            rtfTemplate.put("prodavets_phone", rus(prodavets.phone));
            rtfTemplate.put("prodavets_fax", rus(prodavets.fax));
            //rtfTemplate.put("", rus(""));
            
            // 5. Merge the RTF source model and the context  
            rtfTemplate.merge(rtfTarget);
            
        } catch (Exception ex) {
            Logger.getLogger(RTF.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex);
        }
    }

    public void putDate_dogovor(String rtfSource, String rtfTarget, DBT_main main, DBT_prodavets prodavets, DBT_managers managers, DBT_turoperator turoperator){
        if (rtfTemplate == null) return;
        //String rtfTarget = baseDir() + "out/" + field[1] + ".rtf";
        try {
            // 3. Set the RTF model source 
            rtfTemplate.setTemplate(new File(rtfSource)); 

            String seller = famIO(main.seller);
            String pokupatel = main.f_fam + " " + main.f_name.substring(0, 1) + "." + main.f_otche.substring(0, 1) + ".";
            String price_text = Num.toString(Integer.parseInt(main.last_price));
            String date = reFormateDate(main.sale_date, "dd.MM.yyyy", "dd.MMMM.yyyy");
            StringTokenizer stk = new StringTokenizer(date, ".");
            String s_d = stk.nextToken();
            String s_m = stk.nextToken();
            String s_g = stk.nextToken();
            String director_io = famIO(prodavets.director);
            
           
            Date day_s = getDate(main.tur_date_s);
            Date day_po = getDate(main.tur_date_po);
            long day_count = day_po.getTime() - day_s.getTime();
            
            int day_srok = (int) (day_count / (24 * 60 * 60 * 1000));
            
            // 4. Put the context
            rtfTemplate.put("id", rus(main.id));
            rtfTemplate.put("prodavets", rus(main.prodavets));
            rtfTemplate.put("turoperator", rus(main.turoperator));
            rtfTemplate.put("tur_name", rus(main.tur_name));
            rtfTemplate.put("f_fam", rus(main.f_fam));
            rtfTemplate.put("f_name", rus(main.f_name));
            rtfTemplate.put("f_otche", rus(main.f_otche));
            rtfTemplate.put("f_passport", rus(main.f_passport));
            rtfTemplate.put("f_address", rus(main.f_address));
            rtfTemplate.put("phone_dom", rus(main.phone_dom));
            rtfTemplate.put("phone_rab", rus(main.phone_rab));
            rtfTemplate.put("phone_mob", rus(main.phone_mob));
            rtfTemplate.put("f_email", rus(main.email));
            rtfTemplate.put("tur_date_s", rus(main.tur_date_s));
            rtfTemplate.put("tur_date_po", rus(main.tur_date_po));
            rtfTemplate.put("tur_begin", rus(main.tur_begin));
            rtfTemplate.put("tur_punkt", rus(main.tur_punkt));
            rtfTemplate.put("tur_end", rus(main.tur_end));
            rtfTemplate.put("bilet_cat", rus(main.bilet_cat));
            rtfTemplate.put("h_name", rus(main.h_name));
            rtfTemplate.put("h_nomer", rus(main.h_nomer));
            rtfTemplate.put("food", rus(main.food));
            rtfTemplate.put("transfer", rus(main.transfer));
            rtfTemplate.put("gid", rus(main.gid));
            rtfTemplate.put("visa", rus(main.visa));
            rtfTemplate.put("strah", rus(main.strah));
            rtfTemplate.put("heal", rus(main.heal));
            rtfTemplate.put("excurs", rus(main.excurs));
            rtfTemplate.put("service", rus(main.service));
            rtfTemplate.put("price", rus(main.last_price));
            rtfTemplate.put("price_text", rus(price_text));
            rtfTemplate.put("sale_date", rus(main.sale_date));
            rtfTemplate.put("seller", rus(seller));
            rtfTemplate.put("pokupatel", rus(pokupatel));
            rtfTemplate.put("s_d", rus(s_d));
            rtfTemplate.put("s_m", rus(s_m));
            rtfTemplate.put("s_g", rus(s_g));
            rtfTemplate.put("manager", rus(main.seller));
            
            rtfTemplate.put("dog_director", rus(prodavets.dog_director));
            rtfTemplate.put("director_io", rus(director_io));
            rtfTemplate.put("prodavets_fullname", rus(prodavets.fullname));
            rtfTemplate.put("prodavets_name", rus(prodavets.name));
            rtfTemplate.put("prodavets_inn", rus(prodavets.inn));
            rtfTemplate.put("prodavets_kpp", rus(prodavets.kpp));
            rtfTemplate.put("prodavets_ogrn", rus(prodavets.ogrn));
            rtfTemplate.put("prodavets_address_u", rus(prodavets.address_u));
            rtfTemplate.put("prodavets_address_f", rus(prodavets.address_f));
            if (!prodavets.okved.equalsIgnoreCase("")) {
                rtfTemplate.put("okved", rus("ОКВЭД" + prodavets.okved) + "\\par");
                rtfTemplate.put("okpo", rus("ОКПО " + prodavets.okpo));
                rtfTemplate.put("okato", rus(" ОКАТО " + prodavets.okato) + "\\par");
                rtfTemplate.put("oktmo", rus("ОКТМО " + prodavets.oktmo));
                rtfTemplate.put("okogu", rus(" ОКОГУ " + prodavets.okogu));
                rtfTemplate.put("okfs", rus(" ОКФС " + prodavets.okfs) + "\\par");
                rtfTemplate.put("okopf", rus("ОКОПФ " + prodavets.okopf) + "\\par");
            }
            rtfTemplate.put("prodavets_phone", rus(prodavets.phone));
            rtfTemplate.put("prodavets_fax", rus(prodavets.fax));
            rtfTemplate.put("prodavets_email", rus(prodavets.email));
            rtfTemplate.put("prodavets_director", rus(prodavets.director));
            rtfTemplate.put("prodavets_r_schet", rus(prodavets.r_schet));
            rtfTemplate.put("prodavets_bank", rus(prodavets.bank));
            rtfTemplate.put("prodavets_k_schet", rus(prodavets.k_schet));
            rtfTemplate.put("prodavets_bik", rus(prodavets.bik));
            
            rtfTemplate.put("turoperator_name", rus(turoperator.name));
            rtfTemplate.put("turoperator_fullname", rus(turoperator.fullname));
            rtfTemplate.put("turoperator_reestr", rus(turoperator.reestr));
            rtfTemplate.put("turoperator_address_u", rus(turoperator.address_u));
            rtfTemplate.put("turoperator_address_p", rus(turoperator.address_p));
            rtfTemplate.put("turoperator_ogrn", rus(turoperator.ogrn));
            rtfTemplate.put("turoperator_inn", rus(turoperator.inn));
            rtfTemplate.put("turoperator_sposob", rus(turoperator.sposob));
            rtfTemplate.put("turoperator_razmer", rus(turoperator.razmer));
            rtfTemplate.put("turoperator_dogovor", rus(turoperator.dogovor));
            rtfTemplate.put("turoperator_srok", rus(turoperator.srok));
            rtfTemplate.put("turoperator_org_name", rus(turoperator.org_name));
            rtfTemplate.put("turoperator_org_address", rus(turoperator.org_address));
            
            rtfTemplate.put("mngr_dog_name", rus(managers.dog_name));
            rtfTemplate.put("day_srok", day_srok);
            
            
            //jLabel_price_word.getText();
            
            //rtfTemplate.put("", rus(""));
            
            // 5. Merge the RTF source model and the context  
            rtfTemplate.merge(rtfTarget);
            
        } catch (Exception ex) {
            Logger.getLogger(RTF.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex);
        }
    }
    
    public void putDate_PKO_A(String rtfSource, String rtfTarget, DBT_main main, DBT_turagent turagent){
        if (rtfTemplate == null) return;
        
        try {
            // 3. Set the RTF model source 
            rtfTemplate.setTemplate(new File(rtfSource)); 

            String seller = famIO(main.seller);
            String price_text = Num.toString(Integer.parseInt(main.last_price));
            String date = reFormateDate(main.sale_date, "dd.MM.yyyy", "dd.MMMM.yyyy");
            StringTokenizer stk = new StringTokenizer(date, ".");
            String s_d = stk.nextToken();
            String s_m = stk.nextToken();
            String s_g = stk.nextToken();
            StringTokenizer token = new StringTokenizer(main.tur1_fio);
            String tur1_fam = token.nextToken();
            
            // 4. Put the context
            rtfTemplate.put("id", rus(main.id));
            rtfTemplate.put("prodavets", rus(main.prodavets));
            rtfTemplate.put("tur_name", rus(main.tur_name));
            rtfTemplate.put("price", rus(main.last_price));
            rtfTemplate.put("price_text", rus(price_text));
            rtfTemplate.put("sale_date", rus(main.sale_date));
            rtfTemplate.put("seller", rus(seller));
            rtfTemplate.put("pokupatel", rus(turagent.fullname));
            rtfTemplate.put("s_d", rus(s_d));
            rtfTemplate.put("s_m", rus(s_m));
            rtfTemplate.put("s_g", rus(s_g));
            rtfTemplate.put("tur_date_s", rus(main.tur_date_s));
            rtfTemplate.put("tur1_fam", rus(tur1_fam));
            
            // 5. Merge the RTF source model and the context  
            rtfTemplate.merge(rtfTarget);
            
        } catch (Exception ex) {
            Logger.getLogger(RTF.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex);
        }
    }
    
    public void putDate_Podtverzhdenie(String rtfSource, String rtfTarget, DBT_main main, DBT_turagent turagent){
        if (rtfTemplate == null) return;
        
        try {
            // 3. Set the RTF model source 
            rtfTemplate.setTemplate(new File(rtfSource)); 

            String tur_date_s = reFormateDate(main.tur_date_s, "dd.MM.yyyy", "dd.MM.yy");
            String tur_date_po = reFormateDate(main.tur_date_po, "dd.MM.yyyy", "dd.MM.yy");
            String sale_date = reFormateDate(main.sale_date, "dd.MM.yyyy", "dd.MM.yy");
            String pay_do = "";
            
            Date day_s = getDate(main.tur_date_s);
            Date day_po = getDate(main.tur_date_po);
            long day_count = day_po.getTime() - day_s.getTime();            
            int day_srok = (int) (day_count / (24 * 60 * 60 * 1000));
            String day_night = day_srok + " н.";
            
            String n1 = "1";
            String n2 = main.tur2_fio.equalsIgnoreCase("") ? "" : "2";
            String n3 = main.tur3_fio.equalsIgnoreCase("") ? "" : "3";
            String n4 = main.tur4_fio.equalsIgnoreCase("") ? "" : "4";
            String n5 = main.tur5_fio.equalsIgnoreCase("") ? "" : "5";
            
            String transfer1 = main.transfer;
            String transfer2 = n2.equalsIgnoreCase("") ? "" : main.transfer;
            String transfer3 = n3.equalsIgnoreCase("") ? "" : main.transfer;
            String transfer4 = n4.equalsIgnoreCase("") ? "" : main.transfer;
            String transfer5 = n5.equalsIgnoreCase("") ? "" : main.transfer;
            
            String excurs1 = main.excurs;
            String excurs2 = n2.equalsIgnoreCase("") ? "" : main.excurs;
            String excurs3 = n3.equalsIgnoreCase("") ? "" : main.excurs;
            String excurs4 = n4.equalsIgnoreCase("") ? "" : main.excurs;
            String excurs5 = n5.equalsIgnoreCase("") ? "" : main.excurs;
            
            String strah1 = main.strah;
            String strah2 = n2.equalsIgnoreCase("") ? "" : main.strah;
            String strah3 = n3.equalsIgnoreCase("") ? "" : main.strah;
            String strah4 = n4.equalsIgnoreCase("") ? "" : main.strah;
            String strah5 = n5.equalsIgnoreCase("") ? "" : main.strah;
            
            String manager = famIO(main.seller);
            String tur1_famio = famIO(main.tur1_fio);
            String tur2_famio = n2.equalsIgnoreCase("") ? "" : famIO(main.tur2_fio);
            String tur3_famio = n3.equalsIgnoreCase("") ? "" : famIO(main.tur3_fio);
            String tur4_famio = n4.equalsIgnoreCase("") ? "" : famIO(main.tur4_fio);
            String tur5_famio = n5.equalsIgnoreCase("") ? "" : famIO(main.tur5_fio);
            
            String tur1_passport = main.tur1_passport;
            String tur2_passport = n2.equalsIgnoreCase("") ? "" : main.tur2_passport;
            String tur3_passport = n3.equalsIgnoreCase("") ? "" : main.tur3_passport;
            String tur4_passport = n4.equalsIgnoreCase("") ? "" : main.tur4_passport;
            String tur5_passport = n5.equalsIgnoreCase("") ? "" : main.tur5_passport;
            
            String tur1_bd = main.tur1_bd;
            String tur2_bd = n2.equalsIgnoreCase("") ? "" : main.tur2_bd;
            String tur3_bd = n3.equalsIgnoreCase("") ? "" : main.tur3_bd;
            String tur4_bd = n4.equalsIgnoreCase("") ? "" : main.tur4_bd;
            String tur5_bd = n5.equalsIgnoreCase("") ? "" : main.tur5_bd;            
            
            // 4. Put the context
            rtfTemplate.put("id", rus(main.id));
            rtfTemplate.put("prodavets", rus(main.prodavets));
            rtfTemplate.put("turagent", rus(turagent.fullname));
            rtfTemplate.put("ta_address", rus(turagent.address));
            rtfTemplate.put("ta_manager", rus(turagent.manager));
            rtfTemplate.put("ta_phone", rus(turagent.phone));
            
            //написать условия
            rtfTemplate.put("n1", n1);
            rtfTemplate.put("n2", n2);
            rtfTemplate.put("n3", n3);
            rtfTemplate.put("n4", n4);
            rtfTemplate.put("n5", n5);
            
            rtfTemplate.put("tur1_fio", rus(main.tur1_fio));
            rtfTemplate.put("tur2_fio", rus(main.tur2_fio));
            rtfTemplate.put("tur3_fio", rus(main.tur3_fio));
            rtfTemplate.put("tur4_fio", rus(main.tur4_fio));
            rtfTemplate.put("tur5_fio", rus(main.tur5_fio));
            
            rtfTemplate.put("tur_name", rus(main.tur_name));
            rtfTemplate.put("tur_date_s", tur_date_s);
            rtfTemplate.put("tur_date_po", tur_date_po);
            
            rtfTemplate.put("tur_punkt", rus(main.tur_punkt));
            rtfTemplate.put("hotel", rus(main.h_name));
            rtfTemplate.put("room", rus(main.h_nomer));
            rtfTemplate.put("day_night", rus(day_night));
            rtfTemplate.put("food", rus(main.food));
            
            rtfTemplate.put("tur1_famio", rus(tur1_famio));
            rtfTemplate.put("tur2_famio", rus(tur2_famio));
            rtfTemplate.put("tur3_famio", rus(tur3_famio));
            rtfTemplate.put("tur4_famio", rus(tur4_famio));
            rtfTemplate.put("tur5_famio", rus(tur5_famio));
            
            //написать условия
            rtfTemplate.put("tur1_passport", rus(tur1_passport));
            rtfTemplate.put("tur2_passport", rus(tur2_passport));
            rtfTemplate.put("tur3_passport", rus(tur3_passport));
            rtfTemplate.put("tur4_passport", rus(tur4_passport));
            rtfTemplate.put("tur5_passport", rus(tur5_passport));
            
            rtfTemplate.put("tur1_bd", rus(tur1_bd));
            rtfTemplate.put("tur2_bd", rus(tur2_bd));
            rtfTemplate.put("tur3_bd", rus(tur3_bd));
            rtfTemplate.put("tur4_bd", rus(tur4_bd));
            rtfTemplate.put("tur5_bd", rus(tur5_bd));
            
            rtfTemplate.put("transfer1", rus(transfer1));
            rtfTemplate.put("transfer2", rus(transfer2));
            rtfTemplate.put("transfer3", rus(transfer3));
            rtfTemplate.put("transfer4", rus(transfer4));
            rtfTemplate.put("transfer5", rus(transfer5));
                
            rtfTemplate.put("excurs1", rus(excurs1));
            rtfTemplate.put("excurs2", rus(excurs2));
            rtfTemplate.put("excurs3", rus(excurs3));
            rtfTemplate.put("excurs4", rus(excurs4));
            rtfTemplate.put("excurs5", rus(excurs5));
                
            rtfTemplate.put("strah1", rus(strah1));
            rtfTemplate.put("strah2", rus(strah2));
            rtfTemplate.put("strah3", rus(strah3));
            rtfTemplate.put("strah4", rus(strah4));
            rtfTemplate.put("strah5", rus(strah5));
                
            rtfTemplate.put("tur1_price", rus(main.tur1_price));
            rtfTemplate.put("tur2_price", rus(main.tur2_price));
            rtfTemplate.put("tur3_price", rus(main.tur3_price));
            rtfTemplate.put("tur4_price", rus(main.tur4_price));
            rtfTemplate.put("tur5_price", rus(main.tur5_price));
            
            rtfTemplate.put("price", rus(main.price));
            rtfTemplate.put("last_price", rus(main.last_price));
            rtfTemplate.put("procent", rus(main.procent + "%"));
            
            rtfTemplate.put("manager", rus(manager));
            rtfTemplate.put("sale_date", rus(sale_date));
            rtfTemplate.put("pay_do", rus(pay_do));
            

            // 5. Merge the RTF source model and the context  
            rtfTemplate.merge(rtfTarget);
            
        } catch (Exception ex) {
            Logger.getLogger(RTF.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex);
        }
    }
    
    
    public static String rus(String str) throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new RtfDocument().filterSpecialChar(baos, str, true, true);
        return new String(baos.toByteArray());
    }
    
    public String famIO (String in) {
        StringTokenizer stk = new StringTokenizer(in);
        String fam = stk.nextToken();
        String name = stk.nextToken();
        String otche = stk.nextToken();
        String out = fam + " " + name.substring(0, 1) + "." + otche.substring(0, 1) + ".";
        return out;
    }
    
    public String yes_no(String number){
        //if (number.equalsIgnoreCase("NULL")) return "Нет";
        if (number.equalsIgnoreCase("1")) return "Да";
        return "Нет";
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
     
     public String reFormateDate(String strDate, String fromFormat, String toFormat){
        if (strDate == null) return "NULL";
        if (strDate.toString().equalsIgnoreCase("")) return "NULL";
        try { 
            SimpleDateFormat myDateFormat = new SimpleDateFormat(fromFormat); //Задали шаблон входящей строки  
            Date date = myDateFormat.parse(strDate); //получили дату
            myDateFormat.applyPattern(toFormat); //изменили шаблон
            return myDateFormat.format(date);
        } catch (ParseException ex) {  
            JOptionPane.showMessageDialog(null, ex);
            return null;
        }
    }
     
    public Date getDate(String strDate) {
        if (strDate == null) return null;
        if (strDate.toString().equalsIgnoreCase("")) return null;
        try { 
            SimpleDateFormat myDateFormat = new SimpleDateFormat("dd.MM.yyyy");
            Date date = myDateFormat.parse(strDate);
            return date;
         } catch (ParseException ex) {  
            JOptionPane.showMessageDialog(null, ex);
            return null;
        }
    }
     
}
