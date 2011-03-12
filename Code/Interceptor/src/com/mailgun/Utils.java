package com.mailgun;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

class Utils {
	
	public static String urlencode(Map<String, String> params) throws UnsupportedEncodingException {
		if (params == null)
			return "";
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			String value = entry.getValue();
			if (value != null && value.length() > 0)
			{
				sb.append(entry.getKey());
				sb.append("=");
				sb.append(URLEncoder.encode(value, "utf-8"));
				sb.append("&");
			}
		}
		return sb.toString();
	}

	public static int parseInt(String text, String type) {
		if ("integer".equals(type))
			return Integer.valueOf(text).intValue();
		return 0;
	}
	
	public static boolean parseBoolean(String text, String type) {
		if ("boolean".equals(type))
			return Boolean.valueOf(text).booleanValue();
		return false;
	}
}
