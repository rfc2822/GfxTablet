package at.bitfire.gfxtablet;

import android.content.SharedPreferences;
import android.util.Log;
import android.util.SparseArray;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.FileOutputStream;

public class NetworkServer implements Runnable {
	static final int GFXTABLET_PORT = 40118;
	NetworkClient netClient;
	final SharedPreferences preferences;

	NetworkServer(SharedPreferences preferences) {
		this.preferences = preferences;
	}

	@Override
	public void run() {
		try {
			DatagramSocket socket = new DatagramSocket(GFXTABLET_PORT);
			int packets = 0;

			SparseArray<byte[]> buffer = new SparseArray<>();
			// Init has to be done twice because the first call will be set on the server with 0.0.0.0
			// but we need the ip of the client.
			CanvasActivity.get().sendMotionStopSignal();
			CanvasActivity.get().sendMotionStopSignal();
			CanvasActivity.get().refreshBackground();
			while (true) {
				byte[] buf = new byte[60031];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				int n = buf[60029];
				Log.i("receive:", String.valueOf(n));
				if (n != 0){
					packets = buf[60030];
					buffer.put(n, buf);
				} else if (buffer.size() == packets) {
					try {
						String path = CanvasActivity.SCREEN_PATH;
						boolean parts = buffer.size() == (int) buf[0];
						for (int i=0; i < buf[0]; i++) {
							if (parts) {
								parts = buffer.keyAt(i) == i+1;
							}
						}
						if (!parts) {
							buffer.clear();
							CanvasActivity.get().refreshBackground();
							Log.i("Image Problem", "tying to refetch the screenshot");
							continue;
						}
						Log.i("receive", "completed with " + buffer.size());

						FileOutputStream fos = new FileOutputStream(path);
                        for (int i=1; i <= buffer.size(); i++) {
                            fos.write(buffer.get(i), 0, 60000);
                        }
						fos.flush();
						fos.close();
						CanvasActivity.get().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								CanvasActivity.get().showTemplateImage();
							}
						});
					} catch (IOException e) {
						e.printStackTrace();
					}
					buffer.clear();
				} else {
					buffer.clear();
					//compile image and set it
					CanvasActivity.get().refreshBackground();
					Log.i("PacketProblem", "Did not receive all packages - refreshing - " + String.valueOf(buffer.size()) + " and " + String.valueOf(packets));
				}
			}
		} catch (Exception e) {
			Log.i("GfxTablet", "Screenshot server failed: " + e.getMessage());
		}
	}
}
