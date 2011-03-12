package com.mailgun;

import java.io.IOException;
import java.util.ArrayList;
import java.net.HttpURLConnection;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


/**
 *
 * Mailbox captures all mail arriving to it's address.
 * Mail will be stored on the server and can be later accessed via IMAP or POP3 protocols.  
 *  
 * Mailbox has several properties:
 *
 * alex@gmail.com
 *  ^      ^
 *  |      |
 * user    domain
 *
 * and a password
 *
 * user and domain can not be changed for an existing mailbox.
*/
public class Mailbox extends MailgunResourceUpsertable {
	String user;
	String domain;
    String password;
    String size;
	
	/**
	 * Constructs an empty mailbox.
	 */
	public Mailbox() {
	}

	/**
	 * Construct the mailbox.
	 * @param user the user part of a mailbox address<br>
	 * @param domain the domain part of a mailbox address
	 * @param password the password to log into mailbox via IMAP or POP3
	 */
	public Mailbox(String user, String domain, String password) {
		 this.user = user;
		 this.domain = domain;
         this.password = password;
	}

	public String getUser() { 
		return this.user; 
	}

	public void setUser(String user) { 
		this.user = user;
	}
	
	public String getDomain() { 
		return this.domain; 
	}

	public void setDomain(String domain) { 
		this.domain = domain; 
	}

	public void setPassword(String password) { 
		this.password = password; 
	}

	public String getSize() { 
		return this.size; 
	}

	/**
	 * Insert new mailbox or update the mailbox with the new password if it already exists.
	 * @param mailbox
	 * @throws IOException
	 * @throws XMLStreamException
	 * @see	MailgunResourceUpsertable 
	 */
	public static void upsert(Mailbox mailbox) throws IOException, XMLStreamException {
		upsert(mailboxInfo.collectionName, mailbox);
	}

    /**
     * Upsert mailboxes contained in a csv string:
     *   
     * john@domain.com, password
     * doe@domain.com, password2         
     *
	 * @param mailboxes string with csv contents
	 * @throws IOException
	 */
    public static void upsertFromCsv(String mailboxes) throws IOException {
        HttpURLConnection conn = Mailgun.createConnection(apiUrl + "mailboxes.txt?api_key=" + apiKey, "POST");
		conn.setRequestProperty("Content-Type", "text/plain");
		conn.setRequestProperty("Accept", "text/plain");
		conn.connect();
		conn.getOutputStream().write(mailboxes.getBytes("utf-8"));
        checkHTTPCode(conn);
    }
	
	/**
	 * Find mailbox by Id
	 * 
	 * @return Found mailbox. Never returns null, throws IOException "404" instead.
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	public static Mailbox find(String Id) throws IOException, XMLStreamException {
		return findById(Mailbox.class, Id);
	}

	/**
	 * Find all mailboxes.
	 *
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	public static ArrayList<Mailbox> find() throws IOException, XMLStreamException {
		return findImpl(Mailbox.class, null);
	}
	
		
	@Override	
	protected MailgunResource.ResourceInfo getResourceInfo() {
		return mailboxInfo;
	}
	static MailgunResource.ResourceInfo mailboxInfo = new MailgunResource.ResourceInfo("mailboxes", "mailbox");	
	
	@Override
	protected void writeInnerXml(XMLStreamWriter xw) throws XMLStreamException {
		super.writeInnerXml(xw);
		writeValue(xw, "rest_id", user + "@" + domain);
		writeValue(xw, "user", user);		
		writeValue(xw, "domain", domain);
		writeValue(xw, "password", password);
	}
	
	@Override
	protected boolean onReadTag(String tag, String text, String type) {
		if (super.onReadTag(tag, text, type))
			return true;		
		if ("user".equals(tag))
			user = text;
		else if ("domain".equals(tag))
			domain = text;
        else if ("size".equals(tag))
            size = text;
		else
			return false;
		return true;
	}	
}
