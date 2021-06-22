package nl.andrewlalis.aos_server.command.chat;

import nl.andrewlalis.aos_core.model.Player;
import nl.andrewlalis.aos_server.ClientHandler;

public interface ChatCommand {
	void execute(ClientHandler handler, Player player, String[] args);
}
