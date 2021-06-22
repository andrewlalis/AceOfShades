package nl.andrewlalis.aos_server.command;

import nl.andrewlalis.aos_core.model.Player;
import nl.andrewlalis.aos_server.ClientHandler;
import nl.andrewlalis.aos_server.Server;
import nl.andrewlalis.aos_server.command.chat.ChatCommand;

public class ResetCommand implements Command, ChatCommand {
	private final Server server;

	public ResetCommand(Server server) {
		this.server = server;
	}

	@Override
	public void execute(String[] args) {
		this.server.resetGame();
		System.out.println("Reset the game.");
	}

	@Override
	public void execute(ClientHandler handler, Player player, String[] args) {
		this.server.resetGame();
	}
}
