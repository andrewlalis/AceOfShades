package nl.andrewlalis.aos_server;

import nl.andrewlalis.aos_core.model.PlayerControlState;
import nl.andrewlalis.aos_core.net.data.DataTypes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class DataTransceiver extends Thread {
	private final DatagramSocket socket;
	private final Server server;

	private volatile boolean running;

	public DataTransceiver(Server server, int port) throws SocketException {
		this.socket = new DatagramSocket(port);
		this.server = server;
	}

	public void send(byte[] bytes, InetAddress address, int port) {
		DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, port);
		try {
			this.socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		this.running = false;
		if (!this.socket.isClosed()) {
			this.socket.close();
		}
	}

	@Override
	public void run() {
		this.running = true;
		byte[] buffer = new byte[1400];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		while (this.running) {
			try {
				this.socket.receive(packet);
				ByteBuffer b = ByteBuffer.wrap(packet.getData(), 0, packet.getLength());
				byte type = b.get();
				if (type == DataTypes.INIT) { // New client is trying to initiate an outbound connection so simply echo packet.
					this.send(new byte[]{DataTypes.INIT}, packet.getAddress(), packet.getPort());
				} else if (type == DataTypes.PLAYER_CONTROL_STATE) {
					int playerId = b.getInt();
					if (playerId < 1) continue;
					byte[] stateBuffer = new byte[b.remaining()];
					b.get(stateBuffer);
					this.server.updatePlayerState(playerId, PlayerControlState.fromBytes(stateBuffer));
				}
			} catch (SocketException e) {
				if (!e.getMessage().equals("Socket closed")) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
