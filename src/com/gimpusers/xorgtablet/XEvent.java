package com.gimpusers.xorgtablet;

public abstract class XEvent {
	int x, y, pressure;
	
	public int getX() { return x; }
	public int getY() { return y; }
	public int getPressure() { return pressure; }

	public XEvent(int x, int y, int pressure) {
		this.x = Math.max(x, 0);
		this.y = Math.max(y, 0);
		this.pressure = pressure;
	}
		
	public abstract byte[] toByteArray();
}
