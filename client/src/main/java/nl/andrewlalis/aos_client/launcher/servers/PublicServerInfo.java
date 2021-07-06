package nl.andrewlalis.aos_client.launcher.servers;

import javax.swing.*;
import java.awt.*;

public record PublicServerInfo(
	String name,
	String address,
	String description,
	String location,
	Image icon,
	int maxPlayers,
	int currentPlayers
) {
	public static ListCellRenderer<PublicServerInfo> cellRenderer() {
		return (list, value, index, isSelected, cellHasFocus) -> {
			JPanel panel = new JPanel(new BorderLayout());
			panel.setBorder(BorderFactory.createTitledBorder(value.name()));

			panel.add(new JLabel("Address: " + value.address()), BorderLayout.NORTH);

			JPanel content = new JPanel();
			if (value.icon() != null) {
				JLabel iconLabel = new JLabel(new ImageIcon(value.icon()));
				content.add(iconLabel);
			}

			JTextArea descriptionArea = new JTextArea(value.description());
			descriptionArea.setEditable(false);
			descriptionArea.setWrapStyleWord(true);
			descriptionArea.setLineWrap(true);
			content.add(descriptionArea);
			panel.add(content, BorderLayout.CENTER);

			JPanel bottomPanel = new JPanel();
			bottomPanel.add(new JLabel(String.format("Current players: %d / %d", value.currentPlayers(), value.maxPlayers())));

			panel.add(bottomPanel, BorderLayout.SOUTH);

			if (isSelected) {
				panel.setBackground(list.getSelectionBackground());
				panel.setForeground(list.getSelectionForeground());
			} else {
				panel.setBackground(list.getBackground());
				panel.setForeground(list.getForeground());
			}

			return panel;
		};
	}
}
