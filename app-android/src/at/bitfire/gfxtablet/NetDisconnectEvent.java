package at.bitfire.gfxtablet;

public class NetDisconnectEvent extends NetEvent {
	public NetDisconnectEvent() {
		super(0, 0, 0);
	}

	@Override
	public byte[] toByteArray() {
		return null;
	}
}
