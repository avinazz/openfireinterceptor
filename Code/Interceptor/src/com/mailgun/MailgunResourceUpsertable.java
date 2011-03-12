package com.mailgun;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

/**
 * Provides upsert method. 
 * There are 2 differences between upsert() and save(). 
 * Upsert does not throw exception if object already exist. 
 * Upsert does not load Id property of the object. 
 * <p>
 * It ensures that resource exists on the server and does not syncronize client object instance.
 * In order to modify "upserted" object, you need to find() it first.
 * Upsert() is designed to simplify resource creation, when you want to skip existing resources.
 * </p>   
 */
public abstract class MailgunResourceUpsertable extends MailgunResource {

	protected static void upsert(String collectionName,  MailgunResourceUpsertable obj) 
	throws IOException, XMLStreamException {
		MailgunResource.callCollectionMethod(collectionName, "upsert", "POST", 
				obj.toXmlByteArray());
	}
	
}
