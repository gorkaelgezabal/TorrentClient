package es.deusto.ingenieria.ssdd.bitTorrent.Client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class PWPListener implements Runnable{

	private int PORT;
	
	
	
	public PWPListener(int port) {
		super();
		PORT = port;
	}



	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		try {
			ServerSocket serverSocket = new ServerSocket(PORT);
			
			while (true) {
                Socket socket = serverSocket.accept();
                
                PWPSender receiverThread = new PWPSender(socket);
                receiverThread.run();
            }

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
