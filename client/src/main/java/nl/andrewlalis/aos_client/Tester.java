package nl.andrewlalis.aos_client;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public class Tester {
	private static final String[] names = {
		"andrew", "john", "william", "farnsworth", "xXx_noSc0p3r_xXx"
	};

	public static void main(String[] args) {
		for (int i = 0; i < 2; i++) {
			Client client = new Client();
			try {
				client.connect("localhost", 8035, names[ThreadLocalRandom.current().nextInt(names.length)]);
			} catch (IOException | ClassNotFoundException e) {
				client.shutdown();
				e.printStackTrace();
			}
		}
	}
}
