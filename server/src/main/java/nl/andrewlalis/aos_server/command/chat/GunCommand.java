package nl.andrewlalis.aos_server.command.chat;

import nl.andrewlalis.aos_core.model.Player;
import nl.andrewlalis.aos_core.model.tools.Gun;
import nl.andrewlalis.aos_core.net.chat.SystemChatMessage;
import nl.andrewlalis.aos_server.ClientHandler;

public class GunCommand implements ChatCommand {
	@Override
	public void execute(ClientHandler handler, Player player, String[] args) {
		if (args.length < 1) {
			return;
		}
		String gunName = args[0];
		if (gunName.equalsIgnoreCase("smg")) {
			player.setGun(Gun.ak47());
		} else if (gunName.equalsIgnoreCase("rifle")) {
			player.setGun(Gun.m1Garand());
		} else if (gunName.equalsIgnoreCase("shotgun")) {
			player.setGun(Gun.winchester());
		}
		handler.send(new SystemChatMessage(SystemChatMessage.Level.INFO, "Changed gun to " + player.getGun().getType().name() + "."));
	}
}
