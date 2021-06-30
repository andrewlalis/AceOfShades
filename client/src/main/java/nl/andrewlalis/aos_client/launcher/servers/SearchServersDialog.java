package nl.andrewlalis.aos_client.launcher.servers;

import nl.andrewlalis.aos_client.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class SearchServersDialog extends JDialog {
	public SearchServersDialog(Frame frame, ServerInfoListModel serverInfoListModel) {
		super(frame, true);
		this.setTitle("Search for Servers");
		this.setContentPane(this.getContent(serverInfoListModel));
		this.pack();
		this.setLocationRelativeTo(frame);
	}

	private Container getContent(ServerInfoListModel serverInfoListModel) {
		JPanel panel = new JPanel(new BorderLayout());

		JButton prevButton = new JButton("<");
		JButton nextButton = new JButton(">");

		PublicServerListModel listModel = new PublicServerListModel(prevButton, nextButton);
		JList<PublicServerInfo> serversList = new JList<>(listModel);
		serversList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		serversList.setCellRenderer(PublicServerInfo.cellRenderer());
		JScrollPane scrollPane = new JScrollPane(serversList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(400, 600));
		panel.add(scrollPane, BorderLayout.CENTER);

		JPanel filtersPanel = new JPanel(new FlowLayout());
		JTextField searchField = new JTextField(20);
		searchField.setToolTipText("Search for a server by name.");
		filtersPanel.add(searchField);
		prevButton.addActionListener(e -> listModel.fetchPage(listModel.getCurrentPage() - 1));
		filtersPanel.add(prevButton);
		nextButton.addActionListener(e -> listModel.fetchPage(listModel.getCurrentPage() + 1));
		filtersPanel.add(nextButton);

		panel.add(filtersPanel, BorderLayout.NORTH);

		searchField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				listModel.fetchPage(0, searchField.getText().trim());
			}
		});

		serversList.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
					serversList.setSelectedIndex(serversList.locationToIndex(e.getPoint()));
					PublicServerInfo info = serversList.getSelectedValue();
					if (info == null) return;
					connect(info);
				} else if (SwingUtilities.isRightMouseButton(e)) {
					serversList.setSelectedIndex(serversList.locationToIndex(e.getPoint()));
					PublicServerInfo info = serversList.getSelectedValue();
					if (info == null) return;
					JPopupMenu menu = new JPopupMenu();
					JMenuItem addToListItem = new JMenuItem("Add to My Servers");
					addToListItem.addActionListener(e1 -> {
						serverInfoListModel.add(new ServerInfo(
							info.name(),
							info.address(),
							null
						));
						dispose();
					});
					menu.add(addToListItem);
					menu.show(serversList, e.getX(), e.getY());
				}
			}
		});

		return panel;
	}

	private void connect(PublicServerInfo serverInfo) {
		String username = JOptionPane.showInputDialog(this, "Enter a username.", "Username", JOptionPane.PLAIN_MESSAGE);
		if (username == null || username.isBlank()) return;
		String[] parts = serverInfo.address().split(":");
		String host = parts[0];
		int port = Integer.parseInt(parts[1]);
		this.dispose();
		try {
			new Client(host, port, username);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Could not connect:\n" + e.getMessage(), "Connection Error", JOptionPane.WARNING_MESSAGE);
		}
	}
}
