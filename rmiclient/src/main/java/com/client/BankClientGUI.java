package com.client;

import com.shared.BankService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class BankClientGUI extends JFrame {
	private final JTextField tfServerAddress;
	private final JTextField tfAccount;
	private final JPasswordField pfPassword;
	private final JTextField tfToAccount;
	private final JTextField tfAmount;
	private final JTextField tfMessage;
	private final JTextArea taLog;
	private BankService service;
	private ClientCallbackImpl callback;
	private boolean loggedIn = false;

	public BankClientGUI() {
		super("eBanking RMI");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(520, 460);

		tfAccount = new JTextField(12);
		pfPassword = new JPasswordField(12);
		tfToAccount = new JTextField(12);
		tfAmount = new JTextField(12);
		tfMessage = new JTextField(20);
		taLog = new JTextArea();
		taLog.setEditable(false);

		tfServerAddress = new JTextField("localhost", 12);

		JPanel form = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(4, 4, 4, 4);
		c.anchor = GridBagConstraints.WEST;
		int row = 0;

		c.gridx = 0; c.gridy = row; form.add(new JLabel("Địa chỉ Server:"), c);
		c.gridx = 1; c.gridy = row; form.add(tfServerAddress, c);
		row++;

		c.gridx = 0; c.gridy = row; form.add(new JLabel("Tài khoản gởi:"), c);
		c.gridx = 1; c.gridy = row; form.add(tfAccount, c);
		JButton btnLogin = new JButton("Đăng nhập");
		JButton btnRegister = new JButton("Đăng ký");
		c.gridx = 2; c.gridy = row; form.add(btnLogin, c);
		c.gridx = 3; c.gridy = row; form.add(btnRegister, c);
		row++;

		c.gridx = 0; c.gridy = row; form.add(new JLabel("Mật khẩu:"), c);
		c.gridx = 1; c.gridy = row; form.add(pfPassword, c);
		row++;

		c.gridx = 0; c.gridy = row; form.add(new JLabel("Tài khoản nhận:"), c);
		c.gridx = 1; c.gridy = row; form.add(tfToAccount, c);
		row++;

		c.gridx = 0; c.gridy = row; form.add(new JLabel("Số tiền:"), c);
		c.gridx = 1; c.gridy = row; form.add(tfAmount, c);
		row++;

		c.gridx = 0; c.gridy = row; form.add(new JLabel("Nội dung chuyển:"), c);
		c.gridx = 1; c.gridy = row; c.gridwidth = 2; form.add(tfMessage, c);
		c.gridwidth = 1;
		row++;

		JButton btnInquiry = new JButton("Vấn tin");
		JButton btnDeposit = new JButton("Nạp tiền");
		JButton btnWithdraw = new JButton("Rút tiền");
		JButton btnTransfer = new JButton("Chuyển khoản");
		c.gridx = 0; c.gridy = row; form.add(btnInquiry, c);
		c.gridx = 1; c.gridy = row; form.add(btnDeposit, c);
		c.gridx = 2; c.gridy = row; form.add(btnWithdraw, c);
		c.gridx = 3; c.gridy = row; form.add(btnTransfer, c);

		add(form, BorderLayout.NORTH);
		add(new JScrollPane(taLog), BorderLayout.CENTER);

		btnLogin.addActionListener(e -> handleLogin());
		btnRegister.addActionListener(e -> handleRegister());
		btnInquiry.addActionListener(e -> {
			try {
				String acc = requireAccount();
				long bal = service.getBalance(acc);
				taLog.append("Số dư hiện tại: " + bal + "\n");
			} catch (Exception ex) {
				showError(ex);
			}
		});
		btnDeposit.addActionListener(e -> {
			try {
				String acc = requireAccount();
				long amount = requireAmount();
				long bal = service.deposit(acc, amount);
				taLog.append("Đã nạp: " + amount + "  Số dư hiện tại: " + bal + "\n");
			} catch (Exception ex) {
				showError(ex);
			}
		});
		btnWithdraw.addActionListener(e -> {
			try {
				String acc = requireAccount();
				long amount = requireAmount();
				long bal = service.withdraw(acc, amount);
				taLog.append("Đã rút: " + amount + "  Số dư hiện tại: " + bal + "\n");
			} catch (Exception ex) {
				showError(ex);
			}
		});
		btnTransfer.addActionListener(e -> {
			try {
				String from = requireAccount();
				String to = tfToAccount.getText().trim();
				if (to.isEmpty()) throw new IllegalArgumentException("Nhập tài khoản nhận");
				long amount = requireAmount();
				String msg = tfMessage.getText();
				service.transfer(from, to, amount, msg);
				taLog.append("Đã chuyển " + amount + " đến " + to + "  Nội dung: " + msg + "\n");
			} catch (Exception ex) {
				showError(ex);
			}
		});
	}

	private void connectToServer() throws Exception {
		if (service == null) {
			String serverAddress = tfServerAddress.getText().trim();
			if (serverAddress.isEmpty()) {
				serverAddress = "localhost";
			}
			Registry registry = LocateRegistry.getRegistry(serverAddress, 1099);
			service = (BankService) registry.lookup("BankService");
			taLog.append("Đã kết nối đến server: " + serverAddress + "\n");
		}
		if (callback == null) {
			callback = new ClientCallbackImpl(taLog);
		}
	}

	private void handleLogin() {
		try {
			connectToServer();
			String acc = requireAccount();
			String pw = new String(pfPassword.getPassword());
			if (pw.isEmpty()) {
				throw new IllegalArgumentException("Nhập mật khẩu");
			}
			if (!service.login(acc, pw)) {
				throw new IllegalArgumentException("Sai tài khoản hoặc mật khẩu");
			}
			loggedIn = true;
			service.registerCallback(acc, callback);
			taLog.append("Đăng nhập thành công và đăng ký nhận thông báo cho tài khoản " + acc + "\n");
		} catch (Exception e) {
			showError(e);
		}
	}

	private void handleRegister() {
		try {
			connectToServer();
			String acc = requireAccount();
			String pw = new String(pfPassword.getPassword());
			if (pw.isEmpty()) {
				throw new IllegalArgumentException("Nhập mật khẩu");
			}
			if (!service.registerAccount(acc, pw)) {
				throw new IllegalArgumentException("Tài khoản đã tồn tại");
			}
			taLog.append("Đăng ký tài khoản " + acc + " thành công\n");
			// Tự động đăng nhập sau khi đăng ký
			loggedIn = true;
			service.registerCallback(acc, callback);
			taLog.append("Đã tự động đăng nhập và đăng ký nhận thông báo cho tài khoản " + acc + "\n");
		} catch (Exception e) {
			showError(e);
		}
	}

	private String requireAccount() {
		String acc = tfAccount.getText().trim();
		if (acc.isEmpty()) throw new IllegalArgumentException("Nhập tài khoản gởi");
		return acc;
	}

	private long requireAmount() {
		String s = tfAmount.getText().trim();
		if (s.isEmpty()) throw new IllegalArgumentException("Nhập số tiền");
		return Long.parseLong(s);
	}

	private void showError(Exception ex) {
		JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
	}
}


