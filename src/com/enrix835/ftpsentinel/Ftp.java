package com.enrix835.ftpsentinel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class Ftp {
	
	FTPClient nFtp;
	private String username, password, host, directory;
	private int port;
	private String welcomeMessage;
	private int replyCode;
	
	Ftp(String username, String password, String host, int port, String directory) {
		this.username = username;
		this.password = password;
		this.host = host;
		this.port = port;
		this.directory = directory;
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
			FTPFile [] ftpList = nFtp.listFiles(!directory.equals("/") ? directory : null);
			BufferedWriter out = new BufferedWriter(new FileWriter(outFilename));
			/* first line is the name of the directory */
			out.write("dir:" + directory + "\n\n");
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
		String previousWorkingDir = null;
		String newWorkingDir = null;
		
		getFileList(newFileList);
		
		try {
			previousWorkingDir = new BufferedReader(new FileReader(fileList)).readLine();
			newWorkingDir = new BufferedReader(new FileReader(newFileList)).readLine();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/* if the user selected to monitor a different directory... */
		
		if(!previousWorkingDir.equals(newWorkingDir)) {
			getFileList(fileList);
		}
		
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
