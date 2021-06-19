package nl.andrewlalis.aos_client;

import nl.andrewlalis.aos_core.model.World;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class DatagramReceiver extends Thread {
	private final DatagramSocket datagramSocket;
	private final Client client;

	private volatile boolean running;

	public DatagramReceiver(Client client, int port) throws SocketException {
		this.datagramSocket = new DatagramSocket(port);
		this.client = client;
	}

	public DatagramSocket getDatagramSocket() {
		return datagramSocket;
	}

	public void shutdown() {
		this.running = false;
		this.datagramSocket.close();
	}

	@Override
	public void run() {
		this.running = true;
		byte[] buffer = new byte[8192];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		while (this.running) {
			try {
				this.datagramSocket.receive(packet);
				ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(packet.getData()));
				Object obj = ois.readObject();
				if (obj instanceof World) {
					this.client.setWorld((World) obj);
				}
			} catch (IOException | ClassNotFoundException e) {
				// Ignore any receive exception.d
			}
		}
	}
}
