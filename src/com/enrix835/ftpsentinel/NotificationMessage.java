package com.enrix835.ftpsentinel;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class NotificationMessage {
	
	private Context context;
	
	NotificationMessage(Context context) {
		this.context = context;
	}
	
	public void show(String title, String brief, String message, int ID) {
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.ic_ftps, title, System.currentTimeMillis());
	    
		Intent notificationIntent = new Intent(context, FtpsentinelActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
			
		notification.setLatestEventInfo(context, brief, message, contentIntent);
		notificationManager.notify(ID, notification);
	}

}
