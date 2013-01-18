package com.gimpusers.xorgtablet;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.SharedPreferences;
import android.util.Log;

// see xf86-networktablet on Github for details about the protocol


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
			String hostName = preferences.getString(SettingsActivity.KEY_PREF_HOST, "unknown.invalid");
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
				
				// save resolution, even if not sending it
				if (event.getClass() == XConfigurationEvent.class)
					lastConfiguration = (XConfigurationEvent)event;
				// graceful shutdown
				else if (event.getClass() == XDisconnectEvent.class)
					break;
				
				if (destAddress == null)		// no valid destination host
					continue;
			
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
