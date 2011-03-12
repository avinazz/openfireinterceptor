package com.mailgun;

import java.util.HashMap;

@SuppressWarnings("serial")
class RequestOptions extends HashMap<String, String> {
	
	public RequestOptions add(String name, Object value) {
		if (value != null)
			this.put(name, value.toString());
		return this;		
	}
	
	public String urlencode() {
		try {
			return Utils.urlencode(this);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return "urlencode. Internal bug";
		}
	}
}
