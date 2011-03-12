/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.techingen.openfireplugins;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;


import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;

import com.mailgun.*;

import com.techingen.twentyat.packetinterceptor.data.HibernateUtil;
import com.techingen.twentyat.packetinterceptor.data.Message;
import com.techingen.twentyat.packetinterceptor.data.Recipient;
import com.techingen.twentyat.packetinterceptor.data.TwentyatUser;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
/**
 *
 * @author root
 */
public class PacketInterceptorPlugin implements Plugin,PacketInterceptor  {
    //Logger logger = Logger.getLogger("MyLogger");
    File  fileInitialize,fileIntercept;
    PrintWriter outInitialize,outIntercept;
    HibernateUtil hibernateUtil;
    SessionFactory sessionFactory;
public PacketInterceptorPlugin(){}
public void initializePlugin(PluginManager manager, File pluginDirectory) {
   InterceptorManager.getInstance().addInterceptor(this);     //logger.log(Level.ALL, "init");
   try{

   fileIntercept=new File("/home/openfire/intercept.log");
   outIntercept=new PrintWriter(fileIntercept);
   outIntercept.println("Created File");
 
    fileInitialize=new File("/home/openfire/initialize.log");
    outInitialize=new PrintWriter(fileInitialize);
    outInitialize.println("Initialized Plugin");
    Mailgun.init("key-9smdw6vjbobqwiwdd7");
    outInitialize.println("MailGun Initialized");
    outInitialize.close();

    hibernateUtil=new HibernateUtil();
    sessionFactory=hibernateUtil.getSessionFactory();

   
    }
   catch(Exception e){}
   
}

public void destroyPlugin() {
        // Your code goes here
   InterceptorManager.getInstance().removeInterceptor(this);
    outIntercept.close();
   
}
public void interceptPacket(org.xmpp.packet.Packet packet,Session session,boolean incoming,boolean processed) throws PacketRejectedException{
   boolean isFromFieldAvailable=false,isToFieldAvailable=false,isThreadFieldAvailable=false,isMessageFieldAvailable=false;
   String threadId=null,message=null,to=null,from=null;

   outIntercept.println("ID:"+packet.getID());
   if(packet.getTo()!=null){
        isToFieldAvailable=true;
        to=packet.getTo().toString();
        outIntercept.println("To:"+to);
       }
   else{
       isToFieldAvailable=false;
       outIntercept.println("To:Null");
       }
   if(packet.getFrom()!=null){
      from=packet.getFrom().toString();
      outIntercept.println("From:"+from);
      isFromFieldAvailable=true;
    }
   else{
       outIntercept.println("From:Null");
       isFromFieldAvailable=false;
    }
   if(packet.getElement().elementText("thread")!=null){
       threadId=packet.getElement().elementText("thread");
        outIntercept.println("Thread:"+threadId);
        isThreadFieldAvailable=true;
       }
   else{
       outIntercept.println("Thread:Null");
       isThreadFieldAvailable=false;
       }
   if(packet.getElement().elementText("body")!=null){
       isMessageFieldAvailable=true;
       message=packet.getElement().elementText("body");
   }
   else{
       isMessageFieldAvailable=false;
       message=null;
   }
   outIntercept.println(packet.toXML());

if(isFromFieldAvailable && isToFieldAvailable && isThreadFieldAvailable && isMessageFieldAvailable){
    int messageId=-1;
    try{
    if(!isUserActive(to)){
        TwentyatUser userFrom,userTo;
        userFrom=getTwentyAtUser(from);
        userTo=getTwentyAtUser(to);
        if(userFrom==null)
            outIntercept.println("From:"+from +" is null");
        else
            outIntercept.println("From:"+userFrom.getFirstName()+" "+userFrom.getLastName());

        if(userTo==null)
            outIntercept.println("To:"+to +" is null");
        else
            outIntercept.println("From:"+userTo.getFirstName()+" "+userTo.getLastName());

        if(userTo!=null && userFrom!=null){
            outIntercept.println("Inserting Message into Db:"+message);
            messageId=insertMessage(userFrom,userTo,threadId,message);
            outIntercept.println("Inserted Message into Db:"+String.valueOf(messageId)+":"+message);

        }

        if(userTo!=null){
            outIntercept.println("Intercept:Sending Mail to "+userTo.getEmail()+":MessageId:"+String.valueOf(messageId)+":Message:"+message);
            sendMail("john@avinazz.mailgun.org",userTo.getEmail(),messageId,message);
            outIntercept.println("Intercept:Sent Mail to "+userTo.getEmail()+":MessageId:"+String.valueOf(messageId)+":Message:"+message);
        }
        else
            outIntercept.println("Error:Recipient is unknown");
        }
    //else do nothing
    }catch(Exception ex){
        outIntercept.println("Intercept:Exception sending email to :"+to+":"+ex.getMessage());
        ex.printStackTrace(outIntercept);
    }
  }
 }

private void sendMail(String from,String to, int messageId, String message){
    try {
            MailgunMessage.sendText(from,to,"<MessageId#"+String.valueOf(messageId)+">",message);
            //"avinazz.mailgun.org"
    }
    catch(Exception e){
        outIntercept.println("SendMail:Exception trying to send mail to :"+to);
        e.printStackTrace(outIntercept);
    }

  }
private boolean isUserActive(String username){
    outIntercept.println("isUserActive:"+username);
    TwentyatUser twentyAtUser=getTwentyAtUser(username);
    if(twentyAtUser!=null){
        outIntercept.println("isUserActive:"+username+":"+String.valueOf(twentyAtUser.getIsActive()));
        return twentyAtUser.getIsActive();
    }
    else{
        outIntercept.println("isUserActive:"+username+":Unknown User");
        //Do Nothing if user not found
        return true;
    }
    
}
private int insertMessage(TwentyatUser from,TwentyatUser to,String threadId,String messageText){
    org.hibernate.Session session=sessionFactory.openSession();
    Transaction transaction=session.beginTransaction();
    //to=to.split("@")[0];
    //from=from.split("@")[0];
    try{
    outIntercept.println("Before Logging Packet to Db");

    Message message=new Message();
    message.setTwentyatUser(from);
    message.setDatetime(new Date());
    message.setMessageText(messageText);
    Recipient recepient=new Recipient();
    recepient.setTwentyatUser(to);
    recepient.setMessage(message);
    message.getRecipients().add(recepient);
    session.persist(message);
    transaction.commit();
    outIntercept.println("Successfully Logged Packet to Db");
    return message.getMessageId();

    }
    catch(HibernateException he){
     transaction.rollback();
     outIntercept.println("Exception Logging Packet to Db");
     he.printStackTrace(outIntercept);
     return -1;
    }
    
}
private TwentyatUser getTwentyAtUser(String username){
    username=username.split("@")[0];
    outIntercept.println("getTwentyAtUser:"+username);
    org.hibernate.Session session=sessionFactory.openSession();
    String hql="from TwentyatUser where username='"+username+"'";
    outIntercept.println("HQL:"+hql);
    Query query=session.createQuery(hql);
    List<TwentyatUser>twentyAtUsers=query.list();
    Iterator<TwentyatUser>iTwentyAtUsers=twentyAtUsers.iterator();
    TwentyatUser twentyatUser=null;
    if(iTwentyAtUsers.hasNext()){
        twentyatUser=iTwentyAtUsers.next();
        outIntercept.println("Successfully Retrieved User from Db:username"+username+":"+twentyatUser.getFirstName()+" "+twentyatUser.getLastName()+":"+twentyatUser.getEmail());
    }
    else{
        outIntercept.println("No Such User:"+username);
        twentyatUser=null;
    }
    return twentyatUser;
    
 

}
}
