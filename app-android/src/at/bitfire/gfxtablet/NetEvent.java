package at.bitfire.gfxtablet;

public abstract class NetEvent {
	int x, y, pressure;
	
	public int getX() { return x; }
	public int getY() { return y; }
	public int getPressure() { return pressure; }

	public NetEvent(int x, int y, int pressure) {
		this.x = Math.max(x, 0);
		this.y = Math.max(y, 0);
		this.pressure = pressure;
	}
		
	public abstract byte[] toByteArray();
}
