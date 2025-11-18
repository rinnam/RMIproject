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
	public synchronized boolean registerAccount(String accountId, String password) throws RemoteException {
		if (accountId == null || accountId.trim().isEmpty()) {
			throw new RemoteException("Tên tài khoảng không được trống");
		}
		if (password == null || password.trim().isEmpty()) {
			throw new RemoteException("Mật khẩu trống");
		}
		try {
			return accountStore.createAccount(accountId.trim(), password);
		} catch (IOException e) {
			throw new RemoteException("I/O error", e);
		}
	}

	@Override
	public synchronized long getBalance(String accountId) throws RemoteException {
		if (!accountStore.hasAccount(accountId)) throw new RemoteException("Không tìm thấy tài khoản");
		return accountStore.getBalance(accountId);
	}

	@Override
	public synchronized long deposit(String accountId, long amount) throws RemoteException {
		if (amount <= 0) throw new RemoteException("Phải nhập số dương");
		try {
			if (!accountStore.hasAccount(accountId)) throw new RemoteException("Không tìm thấy tài khoản");
			accountStore.addBalance(accountId, amount);
			return accountStore.getBalance(accountId);
		} catch (IOException e) {
			throw new RemoteException("I/O error", e);
		}
	}

	@Override
	public synchronized long withdraw(String accountId, long amount) throws RemoteException {
		if (amount <= 0) throw new RemoteException("Phải nhập số dương");
		if (!accountStore.hasAccount(accountId)) throw new RemoteException("Không tìm thấy tài khoản");
		long balance = accountStore.getBalance(accountId);
		if (balance < amount) throw new RemoteException("Số dư không đủ");
		try {
			accountStore.addBalance(accountId, -amount);
			return accountStore.getBalance(accountId);
		} catch (IOException e) {
			throw new RemoteException("I/O error", e);
		}
	}

	@Override
	public synchronized void transfer(String fromAccountId, String toAccountId, long amount, String message) throws RemoteException {
		if (amount <= 0) throw new RemoteException("Phải nhập số dương");
		if (fromAccountId.equals(toAccountId)) throw new RemoteException("Không thể chuyển tiền cho chính mình");
		if (!accountStore.hasAccount(fromAccountId)) throw new RemoteException("Không tìm thấy tài khoản gởi");
		if (!accountStore.hasAccount(toAccountId)) throw new RemoteException("Không tìm thấy tài khoản nhận");
		long fromBal = accountStore.getBalance(fromAccountId);
		if (fromBal < amount) throw new RemoteException("Số dư không đủ");
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


