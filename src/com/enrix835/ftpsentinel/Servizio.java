package com.enrix835.ftpsentinel;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import com.enrix835.ftpsentinel.R;

public class Servizio extends Service {
	
	private Resources res;
	private Ftp newFtp;
	private SharedPreferences prefs;
	private String hostname;
	private String username;
	private String password; 
	private int port;
	private int interval; 
	private boolean getWelcomeMsg;
	private String fileList; 
	private String newFileList;
	
	private Utils utils = new Utils();
	private Timer timer = new Timer();
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;

	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.i("ftpsentinel", "service created");
		
		res = getResources();
		prefs = getSharedPreferences("ftpdata", Context.MODE_PRIVATE);
		hostname = prefs.getString("hostname", "");
		username = prefs.getString("username", "");
		password = prefs.getString("password", "");
		port = prefs.getInt("port", 21);
		interval = prefs.getInt("interval", 30000);
		getWelcomeMsg = prefs.getBoolean("welcome", true);
		fileList = prefs.getString("fileList", "/sdcard/.ftpsentinelList");
		newFileList = prefs.getString("newFileList", "/sdcard/.ftpsentinelList.new");
		
		utils = new Utils();
		timer = new Timer();
		
		String IP = !utils.isIPAddress(hostname) ? utils.GetIP(hostname) : hostname;
		
		newFtp = new Ftp(username, password, IP, port);
        newFtp.connect(getWelcomeMsg);
        
        if(newFtp.getWelcomeMessage() != null) {
        	Toast.makeText(getBaseContext(), newFtp.getWelcomeMessage(), Toast.LENGTH_LONG).show();
        }
	}

	@Override
	public void onDestroy() {
		if(newFtp.isConnected() == true) {
			newFtp.disconnect();
			if(timer != null) {
				timer.cancel();
			}
			super.onDestroy();
			Toast.makeText(getBaseContext(), res.getString(R.string.disconnected), Toast.LENGTH_LONG).show();
			Log.i("ftpsentinel", "service destroyed");
		} else {
			Toast.makeText(getBaseContext(), res.getString(R.string.notConnected), Toast.LENGTH_LONG).show();
		}
	}
	
	public void DisconnectAndDestroy() {
		
	}
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
        /* the first time ftpsentinel will get the
         * main fileList
         */
		boolean isFirstTime = intent.getExtras().getBoolean("isFirstTime"); 
        if(isFirstTime) {
        	newFtp.getFileList(fileList);
        }
		startCheckService();
	}
	
	public void checkForUpdates() {
		int value;
		if(((value = newFtp.checkUpdates(utils, newFileList, fileList)) != 0)) {
				showNotification(res.getString(R.string.updatedDetected), 
					res.getString(R.string.filesNotification) + " " +
					(value == 1 ? res.getString(R.string.filesRemovedNotification) : 
					res.getString(R.string.filesAddedNotification)) + " " +
					res.getString(R.string.orChangedFilesNotification), 
					res.getString(R.string.fileChanged) + " " + hostname, 1);
		}
		if(!(new File(newFileList)).delete() && !(new File(fileList).isFile())) {
			Toast.makeText(getBaseContext(), res.getString(R.string.IOException), Toast.LENGTH_LONG).show();
		}
	}
	
	public void startCheckService() {
	
		timer = new Timer();
		
		final Handler update = new Handler() {
			public void dispatchMessage (Message msg) {
				super.dispatchMessage(msg);
				checkForUpdates();
			}
		};
		
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				try {
					update.sendEmptyMessage(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 0, interval);
	}
	
	void showNotification(String title, String brief, String message, int ID) {	
		NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.ic_ftps, title, System.currentTimeMillis());
	    
		Intent notificationIntent = new Intent(this, Servizio.class);
		PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(), 0, notificationIntent, android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
			
		notification.setLatestEventInfo(getBaseContext(), brief, message, contentIntent);
		notificationManager.notify(ID, notification);
	}
	
}
