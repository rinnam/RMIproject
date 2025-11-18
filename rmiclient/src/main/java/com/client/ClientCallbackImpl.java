package com.client;

import com.shared.ClientCallback;

import javax.swing.JTextArea;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClientCallbackImpl extends UnicastRemoteObject implements ClientCallback {
	private final JTextArea logArea;

	public ClientCallbackImpl(JTextArea logArea) throws RemoteException {
		super();
		this.logArea = logArea;
	}

	@Override
	public void onTransferReceived(String fromAccountId, long amount, String message) throws RemoteException {
		logArea.append("Đã nhận " + amount + "  Người chuyển: " + fromAccountId + "  Nội dung: " + (message == null ? "" : message) + "\n");
	}
}


