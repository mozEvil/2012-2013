
package Turist;

/**
 *
 * @author mozevil
 */
import java.util.Date;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
 
// Отправка простого сообщения с типом контента "text/plain"
public class myMail {
/*    // Сюда необходимо подставить адрес получателя сообщения
    String to = "test@mozevil.ru";
    String from = "foton@mozevil.ru";
    // Сюда необходимо подставить SMTP сервер, используемый для отправки
    String host = "mail.mozevil.ru";
    String subject = "";
    
    String username = "foton@mozevil.ru";
    String password = "foton";
    
    private String login = "<login>";
    private String psw = "<parol>";
 */   
    Session session = null;
 /*   
    public myMail(){
        // Создание свойств, получение сессии
        Properties props = new Properties();

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", host);
        //props.put("mail.smtp.port", "25");
        
        //Session session = Session.getInstance(props);
        session = Session.getInstance(props,
            new javax.mail.Authenticator() {
            @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
        });        
    }
  */  
    public myMail(final String username, final String password, String host){
        // Создание свойств, получение сессии
        Properties props = new Properties();

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", host);
        //props.put("mail.smtp.port", "25");
        
        //Session session = Session.getInstance(props);
        session = Session.getInstance(props,
            new javax.mail.Authenticator() {
            @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
        });        
    }
 /*
    public String sendSMS(String[] phone, String mes) { 
        try {
            StringBuilder phn = new StringBuilder();
            // Создание объекта сообщения
            Message msg = new MimeMessage(session);
 
            // Установка атрибутов сообщения
            msg.setFrom(new InternetAddress(from));
            InternetAddress[] address = {new InternetAddress(to)};
            msg.setRecipients(Message.RecipientType.TO, address);
            msg.setSubject(subject);
            msg.setSentDate(new Date());
            
            int i = 0;
            while(i < phone.length) phn.append(phone[i++]).append(",");
            String phones = phn.substring(0, phn.length() - 1);
            //phones = phones.substring(0, phones.length() - 1);
            
            // Установка тела сообщения
            String text = login + ":" + psw + "::::" + phones + ":" + mes;
            msg.setText(text);
 
            // Отправка сообщения
            Transport.send(msg);
            return "Отправлено!";
        }
        catch (MessagingException ex) {
            return ex.getMessage();
        }
        //<login>:<psw>::::<phones>:<mes>
        //<login>:<psw>:<id>:<time>,<tz>:<translit>,<format>,<sender>,<test>:<phones>:<mes>
    }
  */  
    public String sendSMS(String from, String to, String subject, String[] phone, String mes, String login, String psw) { 
        try {
            StringBuilder phn = new StringBuilder();
            // Создание объекта сообщения
            Message msg = new MimeMessage(session);
 
            // Установка атрибутов сообщения
            msg.setFrom(new InternetAddress(from));
            InternetAddress[] address = {new InternetAddress(to)};
            msg.setRecipients(Message.RecipientType.TO, address);
            msg.setSubject(subject);
            msg.setSentDate(new Date());
            
            int i = 0;
            while(i < phone.length) phn.append(phone[i++]).append(",");
            String phones = phn.substring(0, phn.length() - 1);
            //phones = phones.substring(0, phones.length() - 1);
            
            // Установка тела сообщения
            String text = login + ":" + psw + "::::" + phones + ":" + mes;
            msg.setText(text);
 
            // Отправка сообщения
            Transport.send(msg);
            return "Отправлено!";
        }
        catch (MessagingException ex) {
            return ex.getMessage();
        }
        //<login>:<psw>::::<phones>:<mes>
        //<login>:<psw>:<id>:<time>,<tz>:<translit>,<format>,<sender>,<test>:<phones>:<mes>
    }
   /* 
    public String send(String to, String subject, String text) { 
        try {
            // Создание объекта сообщения
            Message msg = new MimeMessage(session);
 
            // Установка атрибутов сообщения
            msg.setFrom(new InternetAddress(from));
            InternetAddress[] address = {new InternetAddress(to)};
            msg.setRecipients(Message.RecipientType.TO, address);
            msg.setSubject(subject);
            msg.setSentDate(new Date());
 
            // Установка тела сообщения
            msg.setText(text);
 
            // Отправка сообщения
            Transport.send(msg);
            return "Отправлено!";
        }
        catch (MessagingException ex) {
            return ex.getMessage();
        }
    }
    */
    public String send(String from, String to, String subject, String text) { 
        try {
            // Создание объекта сообщения
            Message msg = new MimeMessage(session);
 
            // Установка атрибутов сообщения
            msg.setFrom(new InternetAddress(from));
            InternetAddress[] address = {new InternetAddress(to)};
            msg.setRecipients(Message.RecipientType.TO, address);
            msg.setSubject(subject);
            msg.setSentDate(new Date());
 
            // Установка тела сообщения
            msg.setText(text);
 
            // Отправка сообщения
            Transport.send(msg);
            return "Отправлено!";
        }
        catch (MessagingException ex) {
            return ex.getMessage();
        }
    }
    
}