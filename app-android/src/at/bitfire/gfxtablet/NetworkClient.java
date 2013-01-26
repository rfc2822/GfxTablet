package at.bitfire.gfxtablet;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.SharedPreferences;
import android.util.Log;

// see "drivers" directory in Github repository for details about the protocol


public class NetworkClient implements Runnable {
	LinkedBlockingQueue<NetEvent> motionQueue = new LinkedBlockingQueue<NetEvent>();
	LinkedBlockingQueue<NetEvent> getQueue() { return motionQueue; }
	
	InetAddress destAddress;
	SharedPreferences preferences;
	NetConfigurationEvent lastConfiguration = null;

	NetworkClient(SharedPreferences preferences) {
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
			
			while (true) {
				NetEvent event = motionQueue.take();
				
				// save resolution, even if not sending it
				if (event.getClass() == NetConfigurationEvent.class)
					lastConfiguration = (NetConfigurationEvent)event;
				// graceful shutdown
				else if (event.getClass() == NetDisconnectEvent.class)
					break;
				
				if (destAddress == null)		// no valid destination host
					continue;
			
				byte[] data = event.toByteArray();
				DatagramPacket pkt = new DatagramPacket(data, data.length, destAddress, 40117);
				socket.send(pkt);
			}
		} catch (Exception e) {
			Log.e("GfxTablet", "motionQueue failed: " + e.getMessage());
		}
	}
}
