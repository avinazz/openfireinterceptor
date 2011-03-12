package com.mailgun;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import com.google.gson.Gson;
import java.util.HashMap;

/**
 * Send messages through Mailgun HTTP gateway, applying routing.
 * SMTP sender/recipient specification are in the format most email programs utilize, i.e.:
 * <p>
 *  'Foo Bar' &lt;foo@bar.com&gt; <br> 
 *  "Foo Bar" &lt;foo@bar.com&gt; <br>
 *  Foo Bar &lt;foo@bar.comm&gt; <br>
 *  &lt;foo@bar.com&gt; <br>
 *  foo@bar.com <br>    
 * </p>  
 */
public class MailgunMessage extends Mailgun {

    public static String MAILGUN_TAG = "X-Mailgun-Tag";

	/**
	 * Set options when sending text message
	 */
    public static class Options {

        private HashMap<String, HashMap<String, String>> options = new HashMap<String, HashMap<String, String>>();

        public Options() {
            options.put("headers", new HashMap<String, String>());
        }

        /**
         * Add MIME header to the text message
         * 
         * @param header		header name
         * @param value         header value
         */
        public void setHeader(String header, String value){
            options.get("headers").put(header, value);
        }

        public String toJSON() {
            Gson gson = new Gson();
            return gson.toJson(options);
        }
    }
	
	/**
	 * Send plain-text message. 
	 * 
	 * @param sender		sender specification 
	 * @param recipients	comma- or semicolon-separated list of recipient specifications.
	 * @param subject		message subject (can be empty)
	 * @param text			message text (must be not empty)
	 * @param servername    server that will be sending the message	    
	 * @throws IOException
	 */
	public static void sendText(String sender, String recipients, String subject, String text, String servername, Options options) throws IOException {
		RequestOptions opts = new RequestOptions();
		opts.add("sender", sender).add("recipients", recipients).add("subject", subject);
		opts.add("body", text);
        if(options != null){
            opts.add("options", options.toJSON());
        }

		HttpURLConnection connection = Mailgun.createConnection(messagesUrl("txt", servername), "POST");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("Accept", "text/plain");
		connection.connect();
		connection.getOutputStream().write(opts.urlencode().getBytes("utf-8"));
		checkHTTPCode(connection);
	}
	
	public static void sendText(String sender, String recipients, String subject, String text) throws IOException {
		sendText(sender, recipients, subject, text, "", null);
	}

	public static void sendText(String sender, String recipients, String subject, String text, Options options) throws IOException {
		sendText(sender, recipients, subject, text, "", options);
	}

    /**
     * Send MIME-formatted message "as it is". 
     * 
     * @param sender		sender specification. Mailgun will not add it to message "From" header.		
     * @param recipients	comma- or semicolon-separated list of recipients specifications.
     * @param rawBody		valid MIME message.
     * @throws IOException
     */
	public static void sendRaw(String sender, String recipients, byte[] rawBody, String servername) throws IOException {
		MailgunMessage msg = createRawMessage(sender, recipients, servername);
		msg.beginSendRaw().write(rawBody);
		msg.endSendRaw();
	}
	
	public static void sendRaw(String sender, String recipients, byte[] rawBody) throws IOException {
		sendRaw(sender, recipients, rawBody, "");
	}
	
	
	/**
	 * Use this function to easy send MIME messages constructed by 
	 * <a href="http://java.sun.com/products/javamail/index.jsp">Sun JavaMail API</a>.
	 * <p>
	 * Example: <ssss>
	 * <pre>
	 * javax.mail.Message mimeMsg = ...
	 * MailgunMessage mgMsg = createRawMessage("My Name &lt;me@myhost.com&gt;", "you@yourhost.com");
	 * OutputStream os = mgMsg.beginSendRaw(); // get connection to MailGun
	 * mimeMsg.writeTo(os);                    // write JavaMail message to connection buffer
	 * mgMsg.endSendRaw();                     // send message over network and check server response
	 * </pre>
	 * Note: HttpURLConnection must know size of the message in order to construct HTTP POST request. 
	 * The moment when java.net <i>really</i> sends data over the net is when client asks for server response. 
	 * </p>
	 *  
	 * @param sender		sender specification. Mailgun will not add it to message "From" header.
	 * @param recipients	comma- or semicolon-separated list of recipients specifications.
	 * @return 				MailgunMessage object ready for sending raw mime.
	 */
	public static MailgunMessage createRawMessage(String sender, String recipients, String servername) {
		return new MailgunMessage(sender, recipients, servername);
	}

	public static MailgunMessage createRawMessage(String sender, String recipients) {
		return createRawMessage(sender, recipients, "");
	}
	
	/**
	 * Use this function to easy send MIME messages constructed by 
	 * <a href="http://java.sun.com/products/javamail/index.jsp">Sun JavaMail API</a>. 
	 * Prepare connection to MailGun server and return underlying OutputStream. 
	 * 
	 * @return	OutputStream of underlying HttpURLConnection. Write message content into.
	 * @throws IOException
	 * @see MailgunMessage#createRawMessage
	 */
	public OutputStream beginSendRaw() throws IOException {
		conn = Mailgun.createConnection(messagesUrl("eml", this.servername), "POST");
		conn.setRequestProperty("Content-Type", "text/plain");
		conn.setRequestProperty("Accept", "text/plain");
		conn.connect();
		conn.getOutputStream().write(
				String.format("%s\n%s\n\n", sender, recipients).getBytes("utf-8"));
		return conn.getOutputStream();
	}
	
	/**
	 * Use this function to easy send JavaMail messages.
	 * Send message over network and check server response code.
	 *  
	 * @throws IOException
	 */
	public void endSendRaw() throws IOException {
		if (conn == null)
			return;
		checkHTTPCode(conn);
	}
	
	protected static String messagesUrl(String format, String servername) {
		return apiUrl + "messages." + format + "?servername=" + servername;
	}
	
	
	MailgunMessage(String sender, String recipients, String servername) {
		this.sender = sender;
		this.recipients = recipients;
		this.servername = servername;
	}

	MailgunMessage(String sender, String recipients) {
		this.sender = sender;
		this.recipients = recipients;
		this.servername = "";		
	}
	
	private HttpURLConnection conn;
	private String sender, recipients, servername;
}
