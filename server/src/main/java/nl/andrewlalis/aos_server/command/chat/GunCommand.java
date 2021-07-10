package nl.andrewlalis.aos_server.command.chat;

import nl.andrewlalis.aos_core.model.Player;
import nl.andrewlalis.aos_core.model.tools.Gun;
import nl.andrewlalis.aos_core.model.tools.GunType;
import nl.andrewlalis.aos_core.net.chat.SystemChatMessage;
import nl.andrewlalis.aos_server.ClientHandler;

import java.util.Locale;

public class GunCommand implements ChatCommand {
	@Override
	public void execute(ClientHandler handler, Player player, String[] args) {
		if (args.length < 1) {
			handler.send(new SystemChatMessage(SystemChatMessage.Level.WARNING, "No gun name specified. Use /guns to see available guns."));
			return;
		}
		String gunName = String.join(" ", args);
		GunType gunType = null;
		for (GunType type : handler.getServer().getWorld().getGunTypes().values()) {
			if (type.name().equalsIgnoreCase(gunName)) {
				gunType = type;
				break;
			}
		}
		if (gunType == null) {
			handler.send(new SystemChatMessage(SystemChatMessage.Level.WARNING, "Unknown gun name. Use /guns to see available guns."));
			return;
		}
		player.setGun(new Gun(gunType));
		handler.send(new SystemChatMessage(SystemChatMessage.Level.INFO, "Changed gun to " + player.getGun().getType().name() + "."));
	}
}
