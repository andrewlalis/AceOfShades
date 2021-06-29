package nl.andrewlalis.aos_server;

import nl.andrewlalis.aos_core.model.Player;
import nl.andrewlalis.aos_core.net.chat.ChatMessage;
import nl.andrewlalis.aos_core.net.chat.PlayerChatMessage;
import nl.andrewlalis.aos_server.command.GunsCommand;
import nl.andrewlalis.aos_server.command.ResetCommand;
import nl.andrewlalis.aos_server.command.chat.ChatCommand;
import nl.andrewlalis.aos_server.command.chat.GunCommand;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This chat manager is responsible for dealing with incoming player chats and
 * potentially executing commands, or simply relaying the chats on to the rest
 * of the players.
 */
public class ChatManager {
	private final Server server;
	private final Map<String, ChatCommand> chatCommands;

	public ChatManager(Server server) {
		this.server = server;
		this.chatCommands = new ConcurrentHashMap<>();
		this.chatCommands.put("gun", new GunCommand());
		this.chatCommands.put("reset", new ResetCommand(server));
		this.chatCommands.put("guns", new GunsCommand(server));
	}

	public void handlePlayerChat(ClientHandler handler, Player player, ChatMessage msg) {
		if (player == null) return;
		if (msg.getText().startsWith("/")) {
			String[] words = msg.getText().substring(1).split("\\s+");
			if (words.length == 0) return;
			String command = words[0];
			ChatCommand cmd = this.chatCommands.get(command);
			if (cmd != null) {
				cmd.execute(handler, player, Arrays.copyOfRange(words, 1, words.length));
			}
		} else {
			this.server.broadcastMessage(new PlayerChatMessage(player.getId(), msg.getText()));
		}
	}
}
