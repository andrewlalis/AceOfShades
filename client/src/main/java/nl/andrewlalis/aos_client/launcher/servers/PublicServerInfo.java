package nl.andrewlalis.aos_client.launcher.servers;

import javax.swing.*;
import java.awt.*;

public record PublicServerInfo(
	String name,
	String address,
	String version,
	String description,
	String location,
	Image icon,
	int maxPlayers,
	int currentPlayers
) {
	public static ListCellRenderer<PublicServerInfo> cellRenderer() {
		return (list, value, index, isSelected, cellHasFocus) -> {
			JPanel panel = new JPanel(new GridBagLayout());
			var c = new GridBagConstraints();

			c.anchor = GridBagConstraints.CENTER;
			c.insets = new Insets(1, 1, 1, 1);
			c.gridx = 0;
			c.gridy = 0;
			c.gridheight = 4;
			c.weightx = 0.25;
			c.fill = GridBagConstraints.BOTH;
			if (value.icon() != null) {
				JLabel iconLabel = new JLabel(new ImageIcon(value.icon()));
				panel.add(iconLabel, c);
			}

			c.anchor = GridBagConstraints.LINE_START;
			c.gridx = 1;
			c.gridy = 0;
			c.gridheight = 1;
			c.weightx = 0.75;
			c.fill = GridBagConstraints.HORIZONTAL;
			var nameLabel = new JLabel(value.name() + " [" + value.version() + "]");
			nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
			panel.add(nameLabel, c);
			c.gridy++;
			var addressLabel = new JLabel(value.address());
			addressLabel.setFont(new Font("monospaced", Font.PLAIN, 12));
			panel.add(addressLabel, c);
			c.gridy++;
			JTextArea descriptionArea = new JTextArea(value.description());
			descriptionArea.setFont(new Font("monospaced", Font.PLAIN, 12));
			descriptionArea.setEditable(false);
			descriptionArea.setWrapStyleWord(true);
			descriptionArea.setLineWrap(true);
			panel.add(descriptionArea, c);
			c.gridy++;
			panel.add(new JLabel(String.format("%d / %d Players", value.currentPlayers(), value.maxPlayers())), c);

			if (isSelected) {
				panel.setBackground(list.getSelectionBackground());
				panel.setForeground(list.getSelectionForeground());
				descriptionArea.setForeground(list.getSelectionForeground());
				descriptionArea.setBackground(list.getSelectionBackground());
			} else {
				panel.setBackground(list.getBackground());
				panel.setForeground(list.getForeground());
				descriptionArea.setForeground(list.getForeground());
				descriptionArea.setBackground(list.getBackground());
			}

			panel.revalidate();

			return panel;
		};
	}
}
