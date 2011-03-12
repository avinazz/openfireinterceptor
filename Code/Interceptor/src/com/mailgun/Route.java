package com.mailgun;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


/**
 * Route represents the basic rule: the message for particular recipient R is forwarded to destination/callback D.
 * Route has 2 properties: pattern and destination. The pair (pattern, destination) must be unique 
 */
public class Route extends MailgunResourceUpsertable {
	String pattern;
	String destination;
	
	/**
	 * Constructs empty route.
	 */
	public Route() {
	}

	/**
	 * Construct the route.
	 * @param pattern	the pattern for matching the recipient<br>
     * There are 4 types of patterns: <br>
     * <ol> 
     * <li> '*' - match all </li>
     * <li> exact string match, case-sensitive (foo&at;bar.com)</li>
     * <li> a domain pattern, i.e. a string like "*&at;example.com" - matches all emails going to example.com </li>
     * <li> a regular expression</li>
     * </ol> 
	 * @param destination	where to forward the message. Can be:
	 * <ul>
	 * <li>An email address</li>
	 * <li>HTTP/HTTPS URL. A message will be HTTP POSTed there.</li>
	 * </ul> 
	 */
	public Route(String pattern, String destination) {
		 this.pattern = pattern;
		 this.destination = destination;
	}

	public String getPattern() { 
		return this.pattern; 
	}
	public void setPattern(String pattern) { 
		this.pattern = pattern; 
	}
	
	public String getDestination() { 
		return this.destination; 
	}
	public void setDestination(String destination) { 
		this.destination = destination; 
	}

	/**
	 * Insert new route or do nothing if route with same pattern and destination already exist.
	 * @param route
	 * @throws IOException
	 * @throws XMLStreamException
	 * @see	MailgunResourceUpsertable 
	 */
	public static void upsert(Route route) throws IOException, XMLStreamException {
		upsert(routeInfo.collectionName, route);
	}
	
	/**
	 * Find route by Id
	 * 
	 * @return Found route. Never return null, throw IOException "404" instead.
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	public static Route find(String Id) throws IOException, XMLStreamException {
		return findById(Route.class, Id);
	}

	/**
	 * Find all routes.
	 *
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	public static ArrayList<Route> find() throws IOException, XMLStreamException {
		return findImpl(Route.class, null);
	}
	
		
	@Override	
	protected MailgunResource.ResourceInfo getResourceInfo() {
		return routeInfo;
	}
	static MailgunResource.ResourceInfo routeInfo = new MailgunResource.ResourceInfo("routes", "route");	
	
	@Override
	protected void writeInnerXml(XMLStreamWriter xw) throws XMLStreamException {
		super.writeInnerXml(xw);
		writeValue(xw, "pattern", pattern);		
		writeValue(xw, "destination", destination);
	}
	
	@Override
	protected boolean onReadTag(String tag, String text, String type) {
		if (super.onReadTag(tag, text, type))
			return true;		
		if ("pattern".equals(tag))
			pattern = text;
		else if ("destination".equals(tag))
			destination = text;
		else
			return false;
		return true;
	}
	
}
