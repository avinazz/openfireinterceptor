package com.mailgun;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import sun.misc.BASE64Encoder;


/**
 * Mailgun.init() lets you initialize the library. 
 * You need API Key. You may provide API URL if for some reason it differs from standard.
**/
public class Mailgun {
	
	/**
	 * Initialize the library with standard MailGun API URL
	 * 
	 * @param apiKey	Your API Key
	 */
	public static void init(String apiKey) {
		init(apiKey, "https://mailgun.net/api/");
	}
	
	/**
	 * Initialize the library.  
	 * 
	 * @param apiUrl	Base URL of MailGun API, such as http://v1.mailgun.net/api
	 * @param apiKey	Your API Key
	 **/
	public static void init(String apiKey, String apiUrl) {
		if (!apiUrl.endsWith("/"))
			apiUrl += "/";
		Mailgun.apiUrl = apiUrl;
		Mailgun.apiKey = apiKey;
	    BASE64Encoder enc = new sun.misc.BASE64Encoder();
	    Mailgun.authHeader = enc.encode(("api_key:" + apiKey).getBytes());
	}


	protected static HttpURLConnection createConnection(String uri, String method) throws IOException {
		// Note about Connection: keep-alive header.
		// http://www.io.com/~maus/HttpKeepAlive.html
		//
		// On the client side, Java abstracts the notion of a keepalive request away from 
		// the programmer. The HttpURLConnection class implements keepalive automatically, 
		// without intervention being either required or possible on the part of the programmer. 
		// It does this through an internal cache of client connections, which is maintained 
		// as one of the implementation details of the class. Incidentally, this means that 
		// keepalive is an implementation detail of the Javasoft class library, which may or 
		// may not be available in other class libraries.				
		URL url = new URL(uri);
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setRequestProperty("Authorization", "Basic "+ authHeader);
		connection.setRequestMethod(method);
		connection.setDoOutput(true);
		connection.setDoInput(true);
		return connection;
	}

	protected static HttpURLConnection createXmlConnection(String uri, String method) throws IOException {
		HttpURLConnection connection = createConnection(uri, method);
		connection.setRequestProperty("Content-Type","text/xml");
		connection.setRequestProperty("Accept","text/xml, text/plain");
		return connection;
	}
	
	protected static void checkHTTPCode(HttpURLConnection connection) throws IOException {
		int code = connection.getResponseCode();
		if (code < 200 || 299 < code) {
			connection.disconnect();
			// Attempt to call connection.getInputStream will raise exception, but
			// hides HTTP message. Mailgun provides error description there, use it. 
			throw new IOException(String.format("%d %s", code, connection.getResponseMessage()));
		}		
	}

	protected static String apiUrl;
	protected static String apiKey;
	protected static String authHeader;	
}
