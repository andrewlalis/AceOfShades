package nl.andrewlalis.aos_core.net;

public class IdentMessage extends Message {
	private final String name;

	public IdentMessage(String name) {
		super(Type.IDENT);
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
