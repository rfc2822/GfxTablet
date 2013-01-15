package com.gimpusers.xorgtablet;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.Contacts.Data;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

// see protocol.h in xf86-networktablet driver for details about the protocol


public class XorgClient implements Runnable {
	LinkedBlockingQueue<XEvent> motionQueue = new LinkedBlockingQueue<XEvent>();
	LinkedBlockingQueue<XEvent> getQueue() { return motionQueue; }
	
	InetAddress destAddress;
	SharedPreferences preferences;
	XConfigurationEvent lastConfiguration = null;

	XorgClient(SharedPreferences preferences) {
		this.preferences = preferences;
	}
	
	boolean configureNetworking() {
		try {
			String hostName = preferences.getString(SettingsActivity.KEY_PREF_HOST, "127.0.0.1");
			destAddress = InetAddress.getByName(hostName);
			
			if (lastConfiguration != null)
				motionQueue.add(lastConfiguration);
	
		} catch (UnknownHostException e) {
			destAddress = null;
			return false;
		}
		return true;
	}
	
	@Override
	public void run() {
		try {
			DatagramSocket socket = new DatagramSocket();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			
			while (true) {
				XEvent event = motionQueue.take();
				
				if (destAddress == null)
					continue;
				
				if (event.getClass() == XConfigurationEvent.class)
					lastConfiguration = (XConfigurationEvent)event;
				else if (event.getClass() == XDisconnectEvent.class)
					break;
			
				byte[] data = event.toByteArray();
				DatagramPacket pkt = new DatagramPacket(data, data.length, destAddress, 40117);
				socket.send(pkt);

				baos.reset();			
			}
		} catch (Exception e) {
			Log.e("XorgTablet", "motionQueue failed: " + e.getMessage());
		}
	}
}
