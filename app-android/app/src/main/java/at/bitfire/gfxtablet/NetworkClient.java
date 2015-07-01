package at.bitfire.gfxtablet;

import android.content.SharedPreferences;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;

import at.bitfire.gfxtablet.NetEvent.Type;


public class NetworkClient implements Runnable {
	static final int GFXTABLET_PORT = 40118;
	
	final LinkedBlockingQueue<NetEvent> motionQueue = new LinkedBlockingQueue<>();
	LinkedBlockingQueue<NetEvent> getQueue() { return motionQueue; }
	
	InetAddress destAddress;
	final SharedPreferences preferences;

	NetworkClient(SharedPreferences preferences) {
		this.preferences = preferences;
	}
	
	boolean reconfigureNetworking() {
		try {
			String hostName = preferences.getString(SettingsActivity.KEY_PREF_HOST, "unknown.invalid");
			destAddress = InetAddress.getByName(hostName);
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
				
				// graceful shutdown
				if (event.type == Type.TYPE_DISCONNECT)
					break;
				
				if (destAddress == null)		// no valid destination host
					continue;
			
				byte[] data = event.toByteArray();
				DatagramPacket pkt = new DatagramPacket(data, data.length, destAddress, GFXTABLET_PORT);
				socket.send(pkt);
			}
		} catch (Exception e) {
			Log.e("GfxTablet", "motionQueue failed: " + e.getMessage());
		}
	}
}
