package com.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BankService extends Remote {
	boolean login(String accountId, String password) throws RemoteException;
	long getBalance(String accountId) throws RemoteException;
	long deposit(String accountId, long amount) throws RemoteException;
	long withdraw(String accountId, long amount) throws RemoteException;
	void transfer(String fromAccountId, String toAccountId, long amount, String message) throws RemoteException;
	void registerCallback(String accountId, ClientCallback callback) throws RemoteException;
	void unregisterCallback(String accountId) throws RemoteException;
}


