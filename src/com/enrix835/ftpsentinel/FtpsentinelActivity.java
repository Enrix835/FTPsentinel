package com.enrix835.ftpsentinel;

import java.io.File;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

public class FtpsentinelActivity extends PreferenceActivity {
	
	public final String FTP_DATA = "ftpdata";
	
	public boolean getWelcomeMsg;
	
	public EditTextPreference hostEdit;
    public EditTextPreference usernameEdit;
    public EditTextPreference passwordEdit;
    public EditTextPreference portEdit;
    public CheckBoxPreference welcomeCheck;
    public EditTextPreference timerButton;
    public Preference connectButton;
    public Preference fileListButton;
    public Preference manualCheckButton;
    public Preference logoutButton;
    
    public Resources res;
    
    public Ftp newFtp;
    
    public Utils utils = new Utils();
    
    public String dateFile;
    public String hostname;
    
    public String externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
    public final String fileList = externalStoragePath + "/.ftpsentinelList";
    public final String newFileList = externalStoragePath + "/.ftpsentinelList.new";
    public int timer;
    public Servizio checkService;
    
    public Intent intent;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
		boolean isFirstTime = false;
		
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.layout);
        
        intent = new Intent(this, Servizio.class);
        res = getResources();

        dateFile = utils.getFileDate(fileList);
        
        hostEdit = (EditTextPreference) findPreference("host");
        usernameEdit = (EditTextPreference) findPreference("username");
        passwordEdit = (EditTextPreference) findPreference("password");
        portEdit = (EditTextPreference) findPreference("port");
        
        welcomeCheck = (CheckBoxPreference) findPreference("welcomeCheck");
        
        timerButton = (EditTextPreference) findPreference("timerButton");
        
        connectButton = (Preference) findPreference("connectButton");
		connectButton.setOnPreferenceClickListener(connectListener);
		
		fileListButton = (Preference) findPreference("getListButton");
		fileListButton.setSummary(res.getString(R.string.nextUpdates) + res.getString(R.string.getNewFileListSummary)+ " " + dateFile);
		fileListButton.setOnPreferenceClickListener(fileListListener);
		
		manualCheckButton = (Preference) findPreference("manualCheckButton");
		manualCheckButton.setOnPreferenceClickListener(manualCheckListener);
		
		logoutButton = (Preference) findPreference("disconnectButton");
		logoutButton.setOnPreferenceClickListener(logoutListener);
		
		if(!(new File(fileList).isFile())){
			createInfoDialog(res.getString(R.string.firstTime), res.getString(R.string.firstTimeSummary), "OK");
			isFirstTime = true;
		}
		intent.putExtra("isFirstTime", isFirstTime);
    }

	OnPreferenceClickListener connectListener = new OnPreferenceClickListener() {
		
		private String hostname;
		private String username;
		private String password;
		private int port;

		public boolean onPreferenceClick(Preference preference) {
			
	        SharedPreferences prefs = getSharedPreferences(FTP_DATA, Context.MODE_PRIVATE);
	        SharedPreferences.Editor editor = prefs.edit();
	        
			hostname = hostEdit.getText();
			username = usernameEdit.getText();
			password = passwordEdit.getText();
			port = Integer.parseInt(portEdit.getText());
			timer = Integer.parseInt(timerButton.getText());
			
			if(welcomeCheck.isChecked()) {
				getWelcomeMsg = true;
			}
			
			if(hostname == "" || username == "" || password == "") {
				Toast.makeText(getBaseContext(), R.string.allEntry, Toast.LENGTH_LONG).show();
			} else {
				if(timer < 60000) {
					Toast.makeText(getBaseContext(), R.string.warningLowInterval, Toast.LENGTH_LONG).show();
				}
				editor.putString("hostname", hostname);
				editor.putString("username", username);
				editor.putString("password", password);
				editor.putInt("port", port);
				editor.putInt("interval", timer);
				editor.putBoolean("welcome", getWelcomeMsg);
				editor.putString("fileList", fileList);
				editor.putString("newFileList", newFileList);
				editor.commit();
				
				startService(intent);
			}
			return true;
		}

	};
	
	OnPreferenceClickListener fileListListener = new OnPreferenceClickListener() {

		public boolean onPreferenceClick(Preference preference) {
			String host, username, password;
			SharedPreferences prefs = getSharedPreferences(FTP_DATA, Context.MODE_PRIVATE);
			int port;
			
			username = prefs.getString("username", "");
			password = prefs.getString("password", "");
			host = prefs.getString("hostname", "");
			port = prefs.getInt("port", 21);
			
			Ftp nFtp = new Ftp(username, password, host, port);
			nFtp.connect(false);
			nFtp.getFileList(fileList);
			Log.i("ftpsentinel", "getting new file list...");
			nFtp.disconnect();
			return true;
		}
		
	};
	
	OnPreferenceClickListener manualCheckListener = new OnPreferenceClickListener() {
		
		public boolean onPreferenceClick(Preference preference) {
			String host, username, password;
			SharedPreferences prefs = getSharedPreferences(FTP_DATA, Context.MODE_PRIVATE);
			int port, value;
			
			username = prefs.getString("username", "");
			password = prefs.getString("password", "");
			host = prefs.getString("hostname", "");
			port = prefs.getInt("port", 21);
			
			Ftp nFtp = new Ftp(username, password, host, port);
			nFtp.connect(false);
			
			if((value = nFtp.checkUpdates(utils, newFileList, fileList)) != 0) {
				showNotification(res.getString(R.string.updatedDetected), 
						res.getString(R.string.filesNotification) + " " +
						(value == 1 ? res.getString(R.string.filesRemovedNotification) : 
						res.getString(R.string.filesAddedNotification)) + " " +
						res.getString(R.string.orChangedFilesNotification), 
						res.getString(R.string.fileChanged) + " " + host, 1);
			}
			
			(new File(newFileList)).delete();
			nFtp.disconnect();
			return true;
		}
		
	};
	
	OnPreferenceClickListener logoutListener = new OnPreferenceClickListener() {
		
		public boolean onPreferenceClick(Preference preference) {
		   /* TODO:
			*  check if service is running
			*/
			stopService(intent);
			return true;
		} 
		
	};
	
	/* TODO:
	 *  put showNotification() in Utils class
	 */
	void showNotification(String title, String brief, String message, int ID) {	
		NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.ic_ftps, title, System.currentTimeMillis());
	    
		Intent notificationIntent = new Intent(this, FtpsentinelActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(), 0, notificationIntent, android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
			
		notification.setLatestEventInfo(getBaseContext(), brief, message, contentIntent);
		notificationManager.notify(ID, notification);
	}
	
	public void createInfoDialog(String title, String message, String button) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)
	       .setCancelable(false)
	       .setPositiveButton(button, new DialogInterface.OnClickListener() {
	    	   public void onClick(DialogInterface dialog, int id) {
	    		   dialog.cancel();
	    		  }
	    });
	    AlertDialog alert = builder.create();
	    alert.setTitle(title);
	    alert.setIcon(R.drawable.ic_ftps);
	    alert.show();
	}
	
}