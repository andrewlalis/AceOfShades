package nl.andrewlalis.aos_client.view;

import nl.andrewlalis.aos_client.Client;
import nl.andrewlalis.aos_client.control.PlayerKeyListener;
import nl.andrewlalis.aos_client.control.PlayerMouseListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GameFrame extends JFrame {
	public GameFrame(String title, Client client, GamePanel gamePanel) throws HeadlessException {
		super(title);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		this.setResizable(false);
		gamePanel.setPreferredSize(new Dimension(800, 800));
		this.setContentPane(gamePanel);
		gamePanel.setFocusable(true);
		gamePanel.setRequestFocusEnabled(true);
		var mouseListener = new PlayerMouseListener(client, gamePanel);
		gamePanel.addKeyListener(new PlayerKeyListener(client));
		gamePanel.addMouseListener(mouseListener);
		gamePanel.addMouseMotionListener(mouseListener);
		gamePanel.addMouseWheelListener(mouseListener);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.out.println("Closing...");
				client.shutdown();
			}
		});
		this.pack();
		gamePanel.requestFocusInWindow();
		this.setLocationRelativeTo(null);
	}
}
