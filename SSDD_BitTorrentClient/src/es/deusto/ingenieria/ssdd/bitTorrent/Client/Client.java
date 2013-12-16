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
import java.util.List;
import java.util.Random;

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
			
			String announce = handler.getMetainfo().getAnnounce()+"?info_hash="+metaInfo.getInfo().getUrlInfoHash()+"&peer_id="+peer_id+"&port="+PORT+"&uploaded=0&downloaded=0&left=0&ip="+ip.getHostAddress()+"&event=started";
			
			System.out.println("ANNOUNCE"+announce);
			URL url = new URL(announce);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			System.out.println("Request sent");
			con.setRequestMethod("GET");
			
			InputStream is = con.getInputStream(); 
			
			int i;
			char c;
			while((i=is.read())!=-1)
	         {
	            // converts integer to character
	            c=(char)i;
	            
	            // prints character
	            System.out.print(c);
	         }
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
		
	}

}
