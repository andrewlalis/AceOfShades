package nl.andrewlalis.aos_server.command;

import nl.andrewlalis.aos_server.Server;

public class ResetCommand implements Command {
	private final Server server;

	public ResetCommand(Server server) {
		this.server = server;
	}

	@Override
	public void execute(String[] args) {
		this.server.resetGame();
		System.out.println("Reset the game.");
	}
}
