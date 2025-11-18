package com.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class AccountStore {
	private final File dataFile;
	private final Map<String, Long> accountIdToBalance;
	private final Map<String, String> accountIdToPassword;

	AccountStore(File dataFile) throws IOException {
		this.dataFile = dataFile;
		this.accountIdToBalance = new HashMap<>();
		this.accountIdToPassword = new HashMap<>();
		load();
	}

	synchronized long getBalance(String accountId) {
		return accountIdToBalance.getOrDefault(accountId, 0L);
	}

	synchronized boolean hasAccount(String accountId) {
		return accountIdToBalance.containsKey(accountId);
	}

	synchronized void addBalance(String accountId, long delta) throws IOException {
		long current = accountIdToBalance.getOrDefault(accountId, 0L);
		accountIdToBalance.put(accountId, current + delta);
		save();
	}

	synchronized Map<String, Long> snapshot() {
		return Collections.unmodifiableMap(new HashMap<>(accountIdToBalance));
	}

	synchronized boolean validateCredentials(String accountId, String password) {
		String pw = accountIdToPassword.get(accountId);
		return pw != null && pw.equals(password);
	}

	synchronized boolean createAccount(String accountId, String password) throws IOException {
		if (accountIdToBalance.containsKey(accountId)) {
			return false; // Account already exists
		}
		accountIdToPassword.put(accountId, password);
		accountIdToBalance.put(accountId, 0L); // Start with balance 0
		save();
		return true;
	}

	private void load() throws IOException {
		if (!dataFile.exists()) {
			dataFile.getParentFile().mkdirs();
			save();
			return;
		}
		try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#")) continue;
				// Supported formats:
				// legacy: id=balance
				// new:    id:password:balance
				if (line.contains(":")) {
					String[] parts = line.split(":");
					if (parts.length != 3) continue;
					accountIdToPassword.put(parts[0], parts[1]);
					accountIdToBalance.put(parts[0], Long.parseLong(parts[2]));
				} else {
					String[] parts = line.split("=");
					if (parts.length != 2) continue;
					accountIdToPassword.put(parts[0], "");
					accountIdToBalance.put(parts[0], Long.parseLong(parts[1]));
				}
			}
		}
	}

	private void save() throws IOException {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(dataFile))) {
			for (Map.Entry<String, Long> e : accountIdToBalance.entrySet()) {
				String pw = accountIdToPassword.getOrDefault(e.getKey(), "");
				bw.write(e.getKey() + ":" + pw + ":" + e.getValue());
				bw.newLine();
			}
		}
	}
}


