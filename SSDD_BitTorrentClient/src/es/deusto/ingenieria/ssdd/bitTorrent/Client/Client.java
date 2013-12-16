package es.deusto.ingenieria.ssdd.bitTorrent.Client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

import es.deusto.ingenieria.ssdd.bitTorrent.bencoding.Bencoder;
import es.deusto.ingenieria.ssdd.bitTorrent.bencoding.Index;
import es.deusto.ingenieria.ssdd.bitTorrent.metainfo.MetainfoFile;
import es.deusto.ingenieria.ssdd.bitTorrent.metainfo.handler.MetainfoFileHandler;
import es.deusto.ingenieria.ssdd.bitTorrent.metainfo.handler.SingleFileHandler;
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


///////////// CREAR EL SOCKET QUE ESCUCHA LA PETICIONES ENTRANTES /////////////////////////////////
			
			ServerSocket serverSocket = new ServerSocket(PORT);
			
			
/////////////   PETICION AL TRACKER ////////////////////////////////			

			String peer_id = ToolKit.generatePeerId();
			
			System.out.println("hash not encoded"+metaInfo.getInfo());
			 

			InetAddress ip = InetAddress.getLocalHost();
			
//			String announce = handler.getMetainfo().getAnnounce()+"?info_hash="+metaInfo.getInfo().getUrlInfoHash()+"&peer_id="+peer_id+"&port="+PORT+"&uploaded=0&downloaded=0&left=0&ip="+ip.getHostAddress()+"&event=started";
			String announce = handler.getMetainfo().getAnnounce()+"?info_hash="+metaInfo.getInfo().getUrlInfoHash()+"&peer_id="+peer_id+"&port="+PORT+"&uploaded=0&downloaded=0&left=16384&event=started";

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
	 
			//print result
			System.out.println(response.toString());
			
//			Bencoder bencoder = new Bencoder();
//			HashMap<String, Object> unbencodedMap = bencoder.unbencodeDictionary(response);
//			Iterator<Entry<String, Object>> it = unbencodedMap.entrySet().iterator();
//			Entry<String, Object> entry = null;
//			
//			while (it.hasNext()) {
//				entry = it.next();
//				System.out.println("   * Key: '" + entry.getKey() + "' - Value: '" + entry.getValue().toString() + "'");
//			}
//		
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
		
	}

}
