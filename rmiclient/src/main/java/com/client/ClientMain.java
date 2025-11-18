package com.client;

import javax.swing.SwingUtilities;

public class ClientMain {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			BankClientGUI gui = new BankClientGUI();
			gui.setLocationRelativeTo(null);
			gui.setVisible(true);
		});
	}
}


