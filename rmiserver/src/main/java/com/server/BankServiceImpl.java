package com.server;

import com.shared.BankService;
import com.shared.ClientCallback;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BankServiceImpl extends UnicastRemoteObject implements BankService {
	private final AccountStore accountStore;
	private final Map<String, ClientCallback> accountIdToCallback;

	public BankServiceImpl() throws IOException {
		super();
		File data = new File("data/accounts.txt");
		this.accountStore = new AccountStore(data);
		this.accountIdToCallback = new ConcurrentHashMap<>();
	}

	@Override
	public synchronized boolean login(String accountId, String password) throws RemoteException {
		return accountStore.validateCredentials(accountId, password);
	}

	@Override
	public synchronized long getBalance(String accountId) throws RemoteException {
		if (!accountStore.hasAccount(accountId)) throw new RemoteException("Account not found");
		return accountStore.getBalance(accountId);
	}

	@Override
	public synchronized long deposit(String accountId, long amount) throws RemoteException {
		if (amount <= 0) throw new RemoteException("Amount must be positive");
		try {
			if (!accountStore.hasAccount(accountId)) throw new RemoteException("Account not found");
			accountStore.addBalance(accountId, amount);
			return accountStore.getBalance(accountId);
		} catch (IOException e) {
			throw new RemoteException("I/O error", e);
		}
	}

	@Override
	public synchronized long withdraw(String accountId, long amount) throws RemoteException {
		if (amount <= 0) throw new RemoteException("Amount must be positive");
		if (!accountStore.hasAccount(accountId)) throw new RemoteException("Account not found");
		long balance = accountStore.getBalance(accountId);
		if (balance < amount) throw new RemoteException("Insufficient funds");
		try {
			accountStore.addBalance(accountId, -amount);
			return accountStore.getBalance(accountId);
		} catch (IOException e) {
			throw new RemoteException("I/O error", e);
		}
	}

	@Override
	public synchronized void transfer(String fromAccountId, String toAccountId, long amount, String message) throws RemoteException {
		if (amount <= 0) throw new RemoteException("Amount must be positive");
		if (fromAccountId.equals(toAccountId)) throw new RemoteException("Cannot transfer to same account");
		if (!accountStore.hasAccount(fromAccountId)) throw new RemoteException("Source account not found");
		if (!accountStore.hasAccount(toAccountId)) throw new RemoteException("Destination account not found");
		long fromBal = accountStore.getBalance(fromAccountId);
		if (fromBal < amount) throw new RemoteException("Insufficient funds");
		try {
			accountStore.addBalance(fromAccountId, -amount);
			accountStore.addBalance(toAccountId, amount);
		} catch (IOException e) {
			throw new RemoteException("I/O error", e);
		}
		ClientCallback cb = accountIdToCallback.get(toAccountId);
		if (cb != null) {
			try {
				cb.onTransferReceived(fromAccountId, amount, message);
			} catch (Exception ignored) {
				// callback may fail if client went offline
			}
		}
	}

	@Override
	public synchronized void registerCallback(String accountId, ClientCallback callback) throws RemoteException {
		accountIdToCallback.put(accountId, callback);
	}

	@Override
	public synchronized void unregisterCallback(String accountId) throws RemoteException {
		accountIdToCallback.remove(accountId);
	}
}


