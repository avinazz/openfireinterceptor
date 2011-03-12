package com.mailgun;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;


/**
 * 
 * Base class for MailGun resources. Provides common methods.
 * <p>
 * <b>Paging of huge collections</b><br>
 * Resources such as Domain, Mailbox, Route have find() function. 
 * Server limits amount of objects returned by 1 request. 
 * <font color="red">To read huge collections, use find(start, count) function.</font>
 * Current limit is 5000.
 * <p>You can verify the limit by calling API REST method "max_index_count". Use browser.
 * Go to $API_BASE_URL/max_index_count, you will be asked for user/password. 
 * Enter "api_key" as user name and your API Key as password. You will see the limit. <br>
 * </p>
 * </p>
 */
public abstract class MailgunResource extends Mailgun {
	
	private String id;
	
	/**
	 * Id of the object (object gets Id after create/save/find operation)
	 */
	public String getId() { 
		return this.id; 
	}
	
	/**
	 * Create new object. Throws exception if object already exist.
	 *  
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	public void create() throws IOException, XMLStreamException {
		update(apiUrl + collectionName() + ".xml", "POST");
	}
	
	/**
	 * Save modifications or create new object
	 * 
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	public void save() throws IOException, XMLStreamException {
		if (id != null && id.length() > 0)
			update(apiUrl + collectionName() + "/" + id + ".xml", "PUT");
		else
			create();
	}

	/**
	 * Delete the resource. It is safe to delete non-existing resource, no error will occur. 
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	public void delete() throws IOException, XMLStreamException {
		if (id == null || id.length() == 0)
			throw new IOException("Cannot delete object with empty id. Forgot to find() ?");
		String uri = apiUrl + collectionName() + "/" + this.id + ".xml";
		HttpURLConnection connection = createXmlConnection(uri, "DELETE");
		checkHTTPCode(connection);
		connection.disconnect();
	}
		
	protected void update(String uri, String method) throws IOException, XMLStreamException {
		// Write directly to connection stream. It collects all data in internal buffer
		// and sends over network only when program calls connection.getResponse().
		// Thus, java automatically sends Content-Length header required for POST.
		HttpURLConnection connection = createXmlConnection(uri, method);
		XMLStreamWriter xw = writeStartDocument((connection.getOutputStream()));
		writeXml(xw);
		xw.writeEndDocument();
		xw.close();
		readThis(connection);
		connection.disconnect();
	}

	
	protected static void callCollectionMethod(
		String collName, String collMethod, String httpMethod, byte[] data) 
		throws IOException 
	{
		String uri = apiUrl + collName + "/"+ collMethod + ".xml";
		HttpURLConnection connection = createXmlConnection(uri, httpMethod);
		connection.getOutputStream().write(data);
		checkHTTPCode(connection);
		connection.disconnect();
	}
	
	/////////////////////////////////////////////////////////////////////////
	// finders	
	
	protected static <T extends MailgunResource> 
	T findById(Class<T> klass, String Id) throws IOException, XMLStreamException {
		T obj = newInstance(klass);
		String uri = apiUrl + obj.collectionName() + "/" + Id + ".xml";
		HttpURLConnection connection = createXmlConnection(uri, "GET");
		obj.readThis(connection);
		return obj;
	}
	
	protected static <T extends MailgunResource> 
	ArrayList<T> findImpl(Class<T> klass, int start, int count, String sortBy) 
		throws IOException, XMLStreamException 
	{ 
		return findImpl(klass, start, count, sortBy, new RequestOptions());
	}
	
	protected static <T extends MailgunResource> 
	ArrayList<T> findImpl(Class<T> klass, int start, int count, String sort, RequestOptions options) 
		throws IOException, XMLStreamException 
	{
		options.add("start", start).add("count", count).add("sort", sort);
		return findImpl(klass, options);
	}
	
	protected static <T extends MailgunResource> 
	ArrayList<T> findImpl(Class<T> klass, RequestOptions options) 
		throws IOException, XMLStreamException 
	{
		String collName = newInstance(klass).collectionName();
		String uri = apiUrl + collName + ".xml?" + (options != null ? options.urlencode() : "");
		HttpURLConnection connection = createXmlConnection(uri, "GET");
		if (canReadXmlResponse(connection))
			return readList(klass, getXMLReader(connection));
		else
			return new ArrayList<T>();
	}	
	
	/////////////////////////////////////////////////////////////////////////
	// toXmlString family

	/**
	 * Returns XML representation of resource
	 */
	public String toXmlString() throws XMLStreamException {
		try {
			return toXmlStream().toString("utf-8");
		}
		catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
			return "toXmlString(). Internal bug." ;
		}

	}

	/**
	 * Returns XML representation of resource, as byte array, utf-8 encoded. 
	 */
	public byte[] toXmlByteArray() throws XMLStreamException {
		return toXmlStream().toByteArray();
	}

	/**
	 * Returns XML representation of resource, as Stream, utf-8 encoded. 
	 */	
	public ByteArrayOutputStream toXmlStream() throws XMLStreamException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		XMLOutputFactory f = XMLOutputFactory.newInstance();
		XMLStreamWriter xw = f.createXMLStreamWriter(buf, "utf-8");			
		writeXml(xw);
		xw.close();
		return buf;
	}
	
	/**
	 * Writes XML representation of the object to XML stream
	 * @param xw	XML stream to write to
	 * @throws XMLStreamException
	 */
	public void writeXml(XMLStreamWriter xw) throws XMLStreamException {
		xw.writeStartElement(resourceName());
		if (id != null && id.length() > 0)
			writeValue(xw, "id", id);
		writeInnerXml(xw);
		xw.writeEndElement();	
	}
	
	/////////////////////////////////////////////////////////////////////////
	// Functions for subclasses
	
	protected MailgunResource() {
	}
	
	static class ResourceInfo {
		public String collectionName;
		public String resourceName;
		
		public ResourceInfo(String collection, String resource) {
			collectionName = collection;
			resourceName = resource;
		}
	}
	
	protected abstract ResourceInfo getResourceInfo();
	
	protected String collectionName() {
		return getResourceInfo().collectionName;
	}

	protected String resourceName() {
		return getResourceInfo().resourceName;
	}	
	
	
	protected XMLStreamWriter writeStartDocument(OutputStream ostream) throws XMLStreamException {
		XMLOutputFactory fo = XMLOutputFactory.newInstance();
		XMLStreamWriter xw = fo.createXMLStreamWriter(ostream, "utf-8");
		xw.writeStartDocument("utf-8", "1.0");
		return xw;
	}	


	/////////////////////////////////////////////////////////////////////////
	// OVERRIDABLES
	
	// type can be 'integer', 'boolean', 'nil'
	protected boolean onReadTag(String tag, String text, String type) {
		if ("id".equals(tag))
			this.id = text;
		else
			return false;
		return true;
	}	
	
	protected void writeInnerXml(XMLStreamWriter xw) throws XMLStreamException {
		if (id != null && id.length() > 0)
			writeValue(xw, "id", id);
	}
	
	/////////////////////////////////////////////////////////////////////////
	// Response parsers
	
	protected static boolean canReadXmlResponse(HttpURLConnection connection) throws IOException {
		checkHTTPCode(connection);
		return connection.getContentLength() > 0 && connection.getContentType().startsWith("text/xml"); 
	}
	
	protected static XMLEventReader getXMLReader(HttpURLConnection connection) 
		throws IOException, XMLStreamException 
	{
		XMLInputFactory fi = XMLInputFactory.newInstance();		
		return fi.createXMLEventReader(connection.getInputStream());
	}
	
	@SuppressWarnings("unchecked")
	protected static <E extends MailgunResource> ArrayList<E> readList(
			Class<E> klass, XMLEventReader xr) throws IOException, XMLStreamException 
	{
		String resourceElement = newInstance(klass).getResourceInfo().resourceName;		
		ArrayList<E> list = new ArrayList<E>(512);
		while (xr.hasNext()) {
			// find first <collectionName type="array"> 
		    XMLEvent event = xr.nextEvent();
		    if (event.isStartElement()) {
		    	Attribute typeAttr = event.asStartElement().getAttributeByName(new QName("type"));
		    	if (typeAttr != null && "array".equals(typeAttr.getValue())) {
					// for every <resourceName> do readThis()		    		
		    		while (xr.hasNext()) {
		    		    event = xr.nextEvent();
		    		    if (event.isStartElement() && 
		    		    	event.asStartElement().getName().getLocalPart().equals(resourceElement)) 
		    		    {
		    		    	MailgunResource obj = newInstance(klass);
		    		    	obj.readThis(xr);
		    		    	list.add((E)obj);
		    		    }
		    		}
		    	}
		    }
		}
		return list;
	}	
	
	protected void readThis(HttpURLConnection connection) throws XMLStreamException, IOException {
		if (canReadXmlResponse(connection))
			readThis(getXMLReader(connection));
	}
	
	// read all tags till </resourceName> tag or end of stream
	// xr must be positioned after <resourceName>, i.e. on 1st inner tag
	// on exit, xr is positioned after </resourceName> closing tag
	protected void readThis(XMLEventReader xr) throws XMLStreamException {
		String tag = null, text = null, type = null;
		while (xr.hasNext()) {
		    XMLEvent event = xr.nextEvent();
		    if (event.isStartElement()) {
		    	tag = event.asStartElement().getName().getLocalPart();
		    	text = type = null;
				Attribute a = event.asStartElement().getAttributeByName(new QName("type"));
				if (a != null)
					type = a.getValue();
				else {
					a = event.asStartElement().getAttributeByName(new QName("nil"));
					if (a != null)
						type = "nil";					
				}
		    }
		    else if (event.isCharacters())
		    	text = event.asCharacters().getData();
		    else if (event.isEndElement()) {
		    	onReadTag(tag, text, type);		    	
		    	tag = type = text = null;
		    	if (event.asEndElement().getName().getLocalPart().equals(resourceName()))
		    		return;
		    }		    
		}		
	}
	

	/////////////////////////////////////////////////////////////////////////
	// HELPERS
	
	protected static <T extends MailgunResource> T newInstance(Class<T> klass) throws IOException {
		try {
			return klass.newInstance();
		} 
		catch (Exception e) {
			throw new IOException("Mailgun bug. Cannot create object " + klass.getName() + 
					". Stack trace: " + e.getStackTrace());
		}
	}
	
	protected static void writeValue(XMLStreamWriter xw, String tag, Object value) 
		throws XMLStreamException 
	{
		if (value != null) {
			xw.writeStartElement(tag);
			if (value.getClass().isAssignableFrom(int.class) || value.getClass().isAssignableFrom(long.class))
				xw.writeAttribute("type", "integer");
			if (value.getClass().isAssignableFrom(boolean.class)) {
				xw.writeAttribute("type", "boolean");
				value = (Boolean)value ? "True" : "False";
			}
			
			xw.writeCharacters(value.toString());
			xw.writeEndElement();
		}
		// else - do not write "nil=true" attribute, Mailgun doesn't need it
	}

}
