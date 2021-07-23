package nl.andrewlalis.aos_server.command.chat;

import nl.andrewlalis.aos_core.model.Player;
import nl.andrewlalis.aos_core.net.chat.SystemChatMessage;
import nl.andrewlalis.aos_server.ClientHandler;

public class KillDeathRatioCommand implements ChatCommand {
	@Override
	public void execute(ClientHandler handler, Player player, String[] args) {
//		float ratio = player.getKillCount() / ((float) player.getDeathCount());
//		handler.send(new SystemChatMessage(SystemChatMessage.Level.INFO, String.format("Your Kill/Death ratio is %.2f.", ratio)));
		handler.send(new SystemChatMessage(SystemChatMessage.Level.WARNING, "K/D command not yet implemented."));
	}
}
