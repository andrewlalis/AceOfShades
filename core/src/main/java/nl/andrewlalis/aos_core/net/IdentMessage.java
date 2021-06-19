package nl.andrewlalis.aos_core.net;

public class IdentMessage extends Message {
	private final String name;
	private final int datagramPort;

	public IdentMessage(String name, int datagramPort) {
		super(Type.IDENT);
		this.name = name;
		this.datagramPort = datagramPort;
	}

	public String getName() {
		return name;
	}

	public int getDatagramPort() {
		return datagramPort;
	}
}
