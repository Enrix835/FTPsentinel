package com.enrix835.ftpsentinel;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class Alert {
	
	private Context context;
	
	Alert(Context context) {
		this.context = context;
	}
	
	public AlertDialog createAlert(String title, String message, String button) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
		return alert;
	}
}
