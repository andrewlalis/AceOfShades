package nl.andrewlalis.aos_core.net;

import java.io.Serializable;

public class Message implements Serializable {
	private final Type type;

	public Message(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return "Message{" +
			"type=" + type +
			'}';
	}
}
