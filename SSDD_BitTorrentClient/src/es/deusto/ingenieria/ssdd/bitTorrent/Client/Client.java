package es.deusto.ingenieria.ssdd.bitTorrent.Client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import es.deusto.ingenieria.ssdd.bitTorrent.Dao.Peer;
import es.deusto.ingenieria.ssdd.bitTorrent.bencoding.Bencoder;
import es.deusto.ingenieria.ssdd.bitTorrent.metainfo.MetainfoFile;
import es.deusto.ingenieria.ssdd.bitTorrent.metainfo.handler.MetainfoFileHandler;
import es.deusto.ingenieria.ssdd.bitTorrent.metainfo.handler.SingleFileHandler;
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

			String localPeerId = ToolKit.generatePeerId();
			int numberOfPieces = metaInfo.getInfo().getByteSHA1().size();
			System.out.println("Number of pi"+numberOfPieces);

			/////////////   REQUEST TO TRACKER ////////////////////////////////			

			

			String response = notifyTracker(handler.getMetainfo(),localPeerId,Integer.toString(PORT),"0","0","16384","started");

			//			Lanzar el hilo para escuchar las peticiones

			PWPListener listenerThread = new PWPListener(PORT);
			Thread thread = new Thread(listenerThread);
			thread.start();


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
					
					ArrayList<Boolean> pieces = new ArrayList<Boolean>();
					for(int j=0; j<numberOfPieces;j++){
						pieces.add(false);
					}
					
					currentPeer.setPieces(pieces);
					
				}

			}


			//			Se lanza un hilo por cada peer para ejecutar el protocolo


			for(int i=0; i<peerList.size();i++){

				Peer peer = peerList.get(i);
				PWPReceiver pwp = new PWPReceiver(peer,metaInfo);
				pwp.run();
			}

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
