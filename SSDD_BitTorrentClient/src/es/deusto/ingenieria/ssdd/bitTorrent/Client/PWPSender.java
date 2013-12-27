package es.deusto.ingenieria.ssdd.bitTorrent.Client;

import java.net.Socket;

public class PWPSender implements Runnable{

	private Socket socket;
	
	public PWPSender(Socket socket) {
		super();
		this.socket = socket;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
