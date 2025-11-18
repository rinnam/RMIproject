package com.server;

import com.shared.BankService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
	public static void main(String[] args) throws Exception {
		Registry registry;
		try {
			registry = LocateRegistry.createRegistry(1099);
		} catch (Exception e) {
			registry = LocateRegistry.getRegistry(1099);
		}
		BankService service = new BankServiceImpl();
		registry.rebind("BankService", service);
		System.out.println("RMI Bank server started on port 1099");
	}
}


