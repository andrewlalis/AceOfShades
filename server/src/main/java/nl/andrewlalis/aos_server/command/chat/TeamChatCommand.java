package nl.andrewlalis.aos_server.command.chat;

import nl.andrewlalis.aos_core.model.Player;
import nl.andrewlalis.aos_core.net.chat.ChatType;
import nl.andrewlalis.aos_core.net.chat.PlayerChatMessage;
import nl.andrewlalis.aos_core.net.chat.SystemChatMessage;
import nl.andrewlalis.aos_server.ClientHandler;

/**
 * This command, when invoked, sends a message to all team members.
 */
public class TeamChatCommand implements ChatCommand {
	@Override
	public void execute(ClientHandler handler, Player player, String[] args) {
		if (player.getTeam() == null) {
			handler.send(new SystemChatMessage(SystemChatMessage.Level.WARNING, "You're not in a team, so you can't send team chat messages."));
			return;
		}
		var msg = new PlayerChatMessage(player.getId(), player.getName() + ": " + String.join(" ", args), ChatType.TEAM_PLAYER_CHAT);
		handler.getServer().sendTeamMessage(player.getTeam(), msg);
	}
}
