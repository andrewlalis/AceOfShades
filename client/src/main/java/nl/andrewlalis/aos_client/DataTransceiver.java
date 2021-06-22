package nl.andrewlalis.aos_client;

import nl.andrewlalis.aos_core.net.data.DataTypes;
import nl.andrewlalis.aos_core.net.data.PlayerDetailUpdate;
import nl.andrewlalis.aos_core.net.data.WorldUpdate;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class DataTransceiver extends Thread {
	private final Client client;
	private final DatagramSocket socket;

	private volatile boolean running;

	public DataTransceiver(Client client) throws SocketException {
		this.client = client;
		this.socket = new DatagramSocket();
	}

	public int getLocalPort() {
		return this.socket.getLocalPort();
	}

	public void send(byte[] bytes, InetAddress address, int port) throws IOException {
		if (this.socket.isClosed()) return;
		var packet = new DatagramPacket(bytes, bytes.length, address, port);
		this.socket.send(packet);
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
		System.out.println("Datagram socket opened on " + this.socket.getLocalAddress() + ":" + this.socket.getLocalPort());
		byte[] buffer = new byte[1400];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		while (this.running) {
			try {
				this.socket.receive(packet);
				ByteBuffer b = ByteBuffer.wrap(packet.getData(), 0, packet.getLength());
				byte type = b.get();
				if (type == DataTypes.WORLD_DATA) {
					byte[] worldData = new byte[b.remaining()];
					b.get(worldData);
					WorldUpdate update = WorldUpdate.fromBytes(worldData);
					this.client.updateWorld(update);
				} else if (type == DataTypes.PLAYER_DETAIL) {
					byte[] detailData = new byte[b.remaining()];
					b.get(detailData);
					PlayerDetailUpdate update = PlayerDetailUpdate.fromBytes(detailData);
					this.client.updatePlayer(update);
				}
			} catch (SocketException e) {
				if (!e.getMessage().equals("Socket closed")) {
					e.printStackTrace();
				}
				this.shutdown();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
