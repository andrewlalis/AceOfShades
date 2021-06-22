package nl.andrewlalis.aos_core.net;

public class IdentMessage extends Message {
	private final String name;

	/**
	 * The port that the client will use to send and receive UDP packets.
	 */
	private final int udpPort;

	public IdentMessage(String name, int udpPort) {
		super(Type.IDENT);
		this.name = name;
		this.udpPort = udpPort;
	}

	public String getName() {
		return name;
	}

	public int getUdpPort() {
		return this.udpPort;
	}
}
