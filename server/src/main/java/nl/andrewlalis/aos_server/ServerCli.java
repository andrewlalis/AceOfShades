package nl.andrewlalis.aos_server;

import nl.andrewlalis.aos_server.command.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Command-line interface for issuing commands to the AOS server at runtime.
 */
public class ServerCli extends Thread {
	private final Map<String, Command> commands = new HashMap<>();

	private final BufferedReader reader;

	private volatile boolean running;

	public ServerCli(Server server) {
		this.reader = new BufferedReader(new InputStreamReader(System.in));
		this.commands.put("reset", new ResetCommand(server));
		this.commands.put("help", new HelpCommand());
		this.commands.put("stop", new StopCommand(server));

		this.commands.put("list", new ListPlayersCommand(server));
		this.commands.put("kick", new KickCommand(server));
	}

	public void shutdown() {
		this.running = false;
		try {
			this.reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		this.running = true;
		String input;
		System.out.println("Server command-line-interface initialized. Type \"help\" for more information.");
		while (this.running) {
			try {
				input = reader.readLine();
				String[] words = input.split("\\s+");
				if (words.length == 0) continue;
				String command = words[0].toLowerCase();
				String[] args = Arrays.copyOfRange(words, 1, words.length);
				Command cmd = this.commands.get(command);
				if (cmd == null) {
					System.out.println("Unknown command.");
				} else {
					cmd.execute(args);
					if (command.equals("stop")) this.running = false; // Needed to exit and avoid a blocking read.
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
