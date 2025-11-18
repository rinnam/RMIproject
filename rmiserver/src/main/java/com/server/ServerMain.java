package com.server;

import com.shared.BankService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
	public static void main(String[] args) throws Exception {
		// Set system property để RMI server có thể nhận kết nối từ xa
		String hostname = System.getProperty("java.rmi.server.hostname");
		if (hostname == null || hostname.isEmpty()) {
			// Nếu không set, sẽ dùng IP của máy hiện tại
			// Có thể set bằng: -Djava.rmi.server.hostname=<IP_CUA_MAY_SERVER>
			System.out.println("Lưu ý: Nếu chạy trên mạng, hãy set: -Djava.rmi.server.hostname=<IP_SERVER>");
		}
		
		Registry registry;
		try {
			registry = LocateRegistry.createRegistry(1099);
			System.out.println("RMI Registry đã được tạo trên port 1099");
		} catch (Exception e) {
			registry = LocateRegistry.getRegistry(1099);
			System.out.println("Đã kết nối với RMI Registry hiện có trên port 1099");
		}
		BankService service = new BankServiceImpl();
		registry.rebind("BankService", service);
		System.out.println("RMI Bank server đã khởi động trên port 1099");
		System.out.println("Server sẵn sàng nhận kết nối từ client...");
	}
}


