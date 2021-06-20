package nl.andrewlalis.aos_server.command;

import nl.andrewlalis.aos_server.Server;

public class StopCommand implements Command {
	private final Server server;

	public StopCommand(Server server) {
		this.server = server;
	}

	@Override
	public void execute(String[] args) {
		this.server.shutdown();
	}
}
