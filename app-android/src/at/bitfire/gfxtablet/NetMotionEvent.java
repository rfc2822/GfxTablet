package at.bitfire.gfxtablet;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NetMotionEvent extends NetEvent {
	public NetMotionEvent(int x, int y, int pressure) {
		super(x, y, pressure);
	}

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		
		try {
			dos.write(0);	/* EVENT_TYPE_MOTION */
			dos.writeShort(x);
			dos.writeShort(y);
			dos.writeShort(pressure);
		} catch (IOException e) {
			return null;
		}
		
		return baos.toByteArray();
	}
}
