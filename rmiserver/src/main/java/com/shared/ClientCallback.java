package com.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientCallback extends Remote {
	void onTransferReceived(String fromAccountId, long amount, String message) throws RemoteException;
}


