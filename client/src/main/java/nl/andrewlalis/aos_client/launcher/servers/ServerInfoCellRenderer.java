package nl.andrewlalis.aos_client.launcher.servers;

import javax.swing.*;
import java.awt.*;

public class ServerInfoCellRenderer implements ListCellRenderer<ServerInfo> {
	@Override
	public Component getListCellRendererComponent(JList<? extends ServerInfo> list, ServerInfo value, int index, boolean isSelected, boolean cellHasFocus) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(value.getName()));
		JTextField hostField = new JTextField(value.getHost());
		hostField.setEditable(false);
		panel.add(hostField, BorderLayout.CENTER);
		if (value.getUsername() != null) {
			panel.add(new JLabel("Username: " + value.getUsername()), BorderLayout.SOUTH);
		}

		if (isSelected) {
			panel.setBackground(list.getSelectionBackground());
			panel.setForeground(list.getSelectionForeground());
		} else {
			panel.setBackground(list.getBackground());
			panel.setForeground(list.getForeground());
		}

		return panel;
	}
}
