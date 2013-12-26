package es.deusto.ingenieria.ssdd.bitTorrent.Client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
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
import es.deusto.ingenieria.ssdd.bitTorrent.peer.protocol.messages.BitfieldMsg;
import es.deusto.ingenieria.ssdd.bitTorrent.peer.protocol.messages.Handsake;
import es.deusto.ingenieria.ssdd.bitTorrent.peer.protocol.messages.PeerProtocolMessage;
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




			/////////////   REQUEST TO TRACKER ////////////////////////////////			

			String localPeerId = ToolKit.generatePeerId();

			InetAddress ip = InetAddress.getLocalHost();

			String response = notifyTracker(handler.getMetainfo(),localPeerId,Integer.toString(PORT),"0","0","16384","started");

			System.out.println(response.toString());

			Bencoder bencoder = new Bencoder();
			HashMap<String, Object> unbencodedMap = bencoder.unbencodeDictionary(String.valueOf(response).getBytes());
			Iterator<Entry<String, Object>> it = unbencodedMap.entrySet().iterator();
			Entry<String, Object> entry = null;

			while (it.hasNext()) {
				entry = it.next();
				System.out.println("   * Key: '" + entry.getKey() + "' - Value: '" + entry.getValue().toString() + "'");
			}

			
			//GET PEER LIST
			ArrayList<Object> peerListUnbencoded = (ArrayList<Object>)unbencodedMap.get("peers");

			ArrayList<Peer> peerList = new ArrayList<Peer>();
			for (int i=0; i<peerListUnbencoded.size();i++){
					
				//Get current peer info
				HashMap<String, Object> currentHash= (HashMap<String, Object>) peerListUnbencoded.get(i);
				
				//Get ip
				InetAddress currentIp = InetAddress.getByName((String)currentHash.get("ip"));
				//Get port
				Integer port =(Integer) currentHash.get("port");
				

				//Check if peer is localhost
				if(!port.equals(PORT) || !port.equals(PORT)){
					Peer currentPeer = new Peer();
					currentPeer.setIp(currentIp);			
					currentPeer.setPort(port);

					//Get peer id
					String peerId =(String) currentHash.get("peer id");
					currentPeer.setId(peerId);

					peerList.add(currentPeer);
				}
				
			}

			
			//SEND HANDSAKE
			//Para probar se manda la peticion al primero de la lista
			System.out.println("Connecting to "+peerList.get(0).getIp()+":"+peerList.get(0).getPort());
			Socket socket = new Socket(peerList.get(0).getIp(),peerList.get(0).getPort() );

			DataOutputStream socketOutputStream = new DataOutputStream(socket.getOutputStream());   

			Handsake handsake = new Handsake();

			byte[] hash = metaInfo.getInfo().getInfoHash();
			String peerId = peerList.get(0).getId();


			handsake.setInfoHash(new String(hash));
			handsake.setPeerId(peerId);


			byte[] handsakeBytes=handsake.getBytes();
			int handshakeLenght = handsakeBytes.length;

			if(handshakeLenght != 68){
				System.out.println("Handshake lenght incorrect");
			}
			socketOutputStream.write(handsakeBytes);

			System.out.println("Handsake sended:"+handsake.toString());
			
			
			
			//Se recive el mensaje


			DataInputStream in = new DataInputStream(socket.getInputStream());

			byte [] responseHandsake = new byte[68];
			in.read(responseHandsake);
			System.out.println(new String(responseHandsake));

			int availableBytes = in.available();
			byte [] responseBytes = new byte[availableBytes];
			in.read(responseBytes);

			System.out.println(new String(responseBytes));
			
//			SE PARSEA EL MENSAJE
			System.out.println("Name length"+new Integer(responseHandsake[0]));
			
			byte [] hashBytes = Arrays.copyOfRange(responseHandsake, 28, 48);
			System.out.println("Info hash recivido"+new String(hashBytes));
			
//			Se comprueba que tenemos el mismo fichero comparando el hash de respuesta con nuestro hash
			if(compareHash(new String(hashBytes),new String(hash))){
				System.out.println("Hash checked");
			}
			
			
			
//			Se separan los mensajes recividos
			int from =0;
			int to = 0;
			int responseLength = responseBytes.length;
			ArrayList<byte[]> messageByteList = new ArrayList<>();
			
			while(from < responseLength){
				byte [] lenghtBytes = Arrays.copyOfRange(responseBytes, from, from+4);
				to = ToolKit.bigEndianBytesToInt(lenghtBytes, 0)+4+from;
				messageByteList.add(Arrays.copyOfRange(responseBytes, from, to));
				from = to;
			}
			

			socketOutputStream.close();
			socket.close();
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
		
		public static boolean compareHash(String hash1, String hash2){
			
			return hash1.equals(hash2);
		}

	}
