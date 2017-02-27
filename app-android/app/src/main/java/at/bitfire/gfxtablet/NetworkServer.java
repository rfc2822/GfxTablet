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

			SparseArray<byte[]> buffer = new SparseArray<>();
			// Init has to be done twice because the first call will be set on the server with 0.0.0.0
			// but we nee the ip of the client.
			CanvasActivity.get().sendMotionStopSignal();
			CanvasActivity.get().sendMotionStopSignal();
			while (true) {
				byte[] buf = new byte[60030];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				int n = buf[60029];
				Log.i("receive:", String.valueOf(n));
				if (n != 0){
					buffer.put(n, buf);
				} else if (buffer.size() > 0 ) {
					try {
						String path = CanvasActivity.get().getFilesDir().getPath() + "/desktop.png";
						path = "/storage/emulated/0/test.png"; //TODO set via options
						Log.i("buffer:", String.valueOf(buf[0]));
						boolean parts = buffer.size() == (int) buf[0];
						for (int i=0; i < buf[0]; i++) {
							if (parts) {
								Log.i("keyAt " + i, String.valueOf(buffer.keyAt(i)));
								parts = buffer.keyAt(i) == i+1;
							}
						}
						if (!parts) {
							buffer.clear();
							CanvasActivity.get().sendMotionStopSignal();
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
						File file = new File(path);
						long size = file.length();
						Log.i("file-path", path);
						Log.i("file-size", String.valueOf(size));
						Log.i("file-path-current", preferences.getString(SettingsActivity.KEY_TEMPLATE_IMAGE, null));
						preferences.edit().putString(SettingsActivity.KEY_TEMPLATE_IMAGE, path).apply();
						CanvasActivity.get().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								CanvasActivity.get().showTemplateImage();
							}
						});
					} catch (IOException e) {
						e.printStackTrace();
					}
					//compile image and set it
					buffer.clear();
				}
			}
		} catch (Exception e) {
			Log.i("GfxTablet", "Screenshot server failed: " + e.getMessage());
		}
	}
}
