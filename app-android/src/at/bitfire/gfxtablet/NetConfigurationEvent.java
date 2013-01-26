package at.bitfire.gfxtablet;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NetConfigurationEvent extends NetEvent {
	public NetConfigurationEvent(int x, int y, int pressure) {
		super(x, y, pressure);
	}

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		
		try {
			dos.write(2);	/* EVENT_TYPE_SET_RESOLUTION */
			dos.writeShort(x);
			dos.writeShort(y);
			dos.writeShort(pressure);
		} catch (IOException e) {
			return null;
		}
		
		return baos.toByteArray();
	}
}