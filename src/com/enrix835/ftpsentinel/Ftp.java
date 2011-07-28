package com.enrix835.ftpsentinel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class Ftp {
	
	FTPClient nFtp;
	private String username, password, host;
	private int port;
	private String welcomeMessage;
	private int replyCode;
	
	Ftp(String username, String password, String host, int port) {
		this.username = username;
		this.password = password;
		this.host = host;
		this.port = port;
	}
	
	public boolean connect(boolean getWelcomeMsg) {
		nFtp = new FTPClient();
		try {
			nFtp.connect(host, port);
		} catch (SocketException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		try {
			nFtp.login(username, password);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		replyCode = nFtp.getReplyCode();

	    if(!FTPReply.isPositiveCompletion(replyCode)) {
	        try {
				nFtp.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
	      }
		
		if(getWelcomeMsg) {
			welcomeMessage = nFtp.getReplyString();
		}
		return true;
	}
	
	public String getWelcomeMessage() {
		return welcomeMessage;
	}
	
	public void getFileList(String outFilename) {
		try {
			FTPFile [] ftpList = nFtp.listFiles(null);
			BufferedWriter out = new BufferedWriter(new FileWriter(outFilename));
			for(FTPFile ftpFile : ftpList) {
				out.write(ftpFile.getName() + " | " + 
						  ftpFile.getSize() + " | " + 
						  ftpFile.getUser() + " | " + 
						  ftpFile.getGroup());
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int checkUpdates(Utils utils, String newFileList, String fileList) {
		getFileList(newFileList);
		return utils.filesEqual(fileList, newFileList);
	}
	
	public void disconnect() {
		try {
			nFtp.logout();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			nFtp.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isConnected() {
		return nFtp.isConnected();
	}
}
