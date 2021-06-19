package nl.andrewlalis.aos_server;

import nl.andrewlalis.aos_core.model.PlayerControlState;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class DatagramCommunicationThread extends Thread {
	private final Server server;
	private final DatagramSocket socket;

	public DatagramCommunicationThread(Server server, int port) throws SocketException {
		this.server = server;
		this.socket = new DatagramSocket(port);
	}

	public DatagramSocket getSocket() {
		return socket;
	}

	@Override
	public void run() {
		byte[] buffer = new byte[8192];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		while (true) {
			try {
				this.socket.receive(packet);
				Object obj = new ObjectInputStream(new ByteArrayInputStream(buffer)).readObject();
				if (obj instanceof PlayerControlState) {
					this.server.updatePlayerState((PlayerControlState) obj);
				}
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}
