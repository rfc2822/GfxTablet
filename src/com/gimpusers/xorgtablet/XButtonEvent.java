package com.gimpusers.xorgtablet;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class XButtonEvent extends XEvent {
	boolean down;
	
	public XButtonEvent(int x, int y, int pressure, boolean down) {
		super(x, y, pressure);
		this.down = down;
	}

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		
		try {
			dos.write(1);	/* EVENT_TYPE_BUTTON */
			dos.writeShort(x);
			dos.writeShort(y);
			dos.writeShort(pressure);
			dos.write(1);
			dos.write(down ? 1 : 0);
		} catch (IOException e) {
			return null;
		}
		
		return baos.toByteArray();
	}

}
