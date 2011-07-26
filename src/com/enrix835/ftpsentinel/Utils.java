package com.enrix835.ftpsentinel;

import java.io.File;
import java.net.InetAddress;
import java.util.Date;
import java.util.regex.*;

public class Utils {
	
	public String GetIP(String host) {
		try {
			InetAddress ipAddress = InetAddress.getByName(host);
			return ipAddress.getHostAddress();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean isIPAddress(String ip) {
		Pattern p = Pattern.compile("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b");
		Matcher m = p.matcher(ip);
		return m.matches();
	}
	
	public String getFileDate(String filename) {
		return (new Date((new File(filename).lastModified()))).toString();
	}
	
	public int filesEqual(String filename, String fileList) {
		if((new File(filename).length()) > ((new File(fileList).length()))) {
			return 1; // removed files
		} else if((new File(filename).length()) < ((new File(fileList).length()))) {
			return 2; // added files
		} else {
			return 0; // equal
		}
	}
	
}