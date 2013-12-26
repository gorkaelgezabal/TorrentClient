package es.deusto.ingenieria.ssdd.bitTorrent.Client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

import es.deusto.ingenieria.ssdd.bitTorrent.Dao.Peer;
import es.deusto.ingenieria.ssdd.bitTorrent.bencoding.Bencoder;
import es.deusto.ingenieria.ssdd.bitTorrent.bencoding.Index;
import es.deusto.ingenieria.ssdd.bitTorrent.metainfo.MetainfoFile;
import es.deusto.ingenieria.ssdd.bitTorrent.metainfo.handler.MetainfoFileHandler;
import es.deusto.ingenieria.ssdd.bitTorrent.metainfo.handler.SingleFileHandler;
import es.deusto.ingenieria.ssdd.bitTorrent.peer.protocol.messages.Handsake;
import es.deusto.ingenieria.ssdd.bitTorrent.util.StringUtils;
import es.deusto.ingenieria.ssdd.bitTorrent.util.ToolKit;

public class Client {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int PORT = 3333;
		try {
			MetainfoFileHandler<?> handler;
			
			String torrentDir ="C:/Users/Nirie/Desktop/ar.docx.torrent";
			File torrent = new File(torrentDir);
			handler = new SingleFileHandler();
			System.out.println(torrent.getPath());
			handler.parseTorrenFile(torrent.getPath());
			
			MetainfoFile<?> metaInfo = handler.getMetainfo();
			
			
			
			
/////////////   PETICION AL TRACKER ////////////////////////////////			

			String peer_id = ToolKit.generatePeerId();
			
			
			
			
			System.out.println("hash not encoded"+metaInfo.getInfo());
			 
			
			InetAddress ip = InetAddress.getLocalHost();
			
			String response = notifyTracker(handler.getMetainfo(),peer_id,Integer.toString(PORT),"0","0","16384","started");
	 			
			System.out.println(response.toString());
			
			Bencoder bencoder = new Bencoder();
			HashMap<String, Object> unbencodedMap = bencoder.unbencodeDictionary(String.valueOf(response).getBytes());
			Iterator<Entry<String, Object>> it = unbencodedMap.entrySet().iterator();
			Entry<String, Object> entry = null;
			
			while (it.hasNext()) {
				entry = it.next();
				System.out.println("   * Key: '" + entry.getKey() + "' - Value: '" + entry.getValue().toString() + "'");
			}
			
			ArrayList<Object> peerListUnbencoded = (ArrayList<Object>)unbencodedMap.get("peers");
			
			ArrayList<Peer> peerList = new ArrayList<Peer>();
			for (int i=0; i<peerListUnbencoded.size();i++){
				Peer currentPeer = new Peer();
				
				//Get current peer info
				HashMap<String, Object> currentHash= (HashMap<String, Object>) peerListUnbencoded.get(i);
				
				//Get ip
				InetAddress currentIp = InetAddress.getByName((String)currentHash.get("ip"));
				System.out.println("vla"+currentIp);
				currentPeer.setIp(currentIp);
				
				//Get port
				Integer port =(Integer) currentHash.get("port");
				currentPeer.setPort(port);
				
				//Get peer id
				String peerId =(String) currentHash.get("peer id");
				currentPeer.setId(peerId);
				
				peerList.add(currentPeer);
			}
			
			
			//PRUEBA:  Se manda un handsake al primer peer que no sea yo
			
//			Peer currentPruebaPeer = null;
//			Peer pruebaPeer = null;
//			for(int i=0; i<peerList.size();i++){
//				currentPruebaPeer = peerList.get(0);
//				boolean t = currentPruebaPeer.getIp().isAnyLocalAddress();
//				boolean r = currentPruebaPeer.getIp().isLoopbackAddress();
////				if (!currentPruebaPeer.getIp().isAnyLocalAddress()&&!currentPruebaPeer.getIp().isLoopbackAddress()){
//				System.out.println(currentPruebaPeer.getPort()+" is equal 39402");
//				if(currentPruebaPeer.getPort() == 39402){	
//					pruebaPeer=currentPruebaPeer;
//				}
//				
//			}
			
			
			System.out.println("Connecting to "+InetAddress.getLocalHost()+":"+39402);
			Socket socket = new Socket(InetAddress.getLocalHost(),39402 );
			OutputStream socketOutputStream = socket.getOutputStream();
			
			Handsake handsake = new Handsake();
			
			byte[] hash = metaInfo.getInfo().getInfoHash();
			
			

			handsake.setInfoHash(new String(hash));
			handsake.setPeerId(peer_id);
			String prueba = handsake.getInfoHash();
			System.out.println("lenght"+prueba.getBytes().length);
			
			byte[] handsakeBytes=handsake.getBytes();
			int handshakeLenght = handsakeBytes.length;
			
			if(handshakeLenght != 68){
				System.out.println("Handshake lenght incorrect");
			}
			socketOutputStream.write(handsakeBytes, 0,handshakeLenght);
			socketOutputStream.close();
			socket.close();
			System.out.println("Handsake sended:"+handsake.toString());
			//Se recive el mensaje
			
			int BUFFER_SIZE = 65536;
			ServerSocket serverSocket = new ServerSocket(PORT);
			Socket clientSocket = serverSocket.accept();
		
			 long startTime = System.currentTimeMillis();
	         byte[] buffer = new byte[BUFFER_SIZE];
	         int read;
	         int totalRead = 0;
	       
	         
	         BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	         StringBuffer response2 = new StringBuffer();
	         int inputLine;
	         
	         String payload = "";
	         while ((inputLine = in.read()) != -1) {
				
	        	 System.out.println("reading");
	        	 payload = payload + inputLine;
				}
				in.close();

			 
			 System.out.println(payload);
			 
			 byte[] bytes = payload.getBytes();
			 int value = ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16)
				        | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
			 System.out.println("Message lenght"+value);
//		
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
	}
	
	
	public static String notifyTracker (MetainfoFile<?> metaInfo,String peer_id, String port, String uploaded, String downloaded, String left, String event){
		
		
		System.out.println("hash not encoded"+metaInfo.getInfo());
		 

		try {
			InetAddress ip = InetAddress.getLocalHost();

			String announce = metaInfo.getAnnounce()+"?info_hash="+metaInfo.getInfo().getUrlInfoHash()+"&peer_id="+peer_id+"&port="+port+"&uploaded="+uploaded+"&downloaded="+downloaded+"&left="+left+"&event="+event;

			System.out.println("ANNOUNCE"+announce);
			URL url = new URL(announce);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			System.out.println("Request sent");
			String USER_AGENT = "Mozilla/5.0"; 
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", USER_AGENT);
			
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	 
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			return String.valueOf(response);
			
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}

}
