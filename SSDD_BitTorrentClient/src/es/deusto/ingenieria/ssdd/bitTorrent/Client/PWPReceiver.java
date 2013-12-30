package es.deusto.ingenieria.ssdd.bitTorrent.Client;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.deusto.ingenieria.ssdd.bitTorrent.Dao.Block;
import es.deusto.ingenieria.ssdd.bitTorrent.Dao.Peer;
import es.deusto.ingenieria.ssdd.bitTorrent.metainfo.MetainfoFile;
import es.deusto.ingenieria.ssdd.bitTorrent.peer.protocol.messages.BitfieldMsg;
import es.deusto.ingenieria.ssdd.bitTorrent.peer.protocol.messages.CancelMsg;
import es.deusto.ingenieria.ssdd.bitTorrent.peer.protocol.messages.ChokeMsg;
import es.deusto.ingenieria.ssdd.bitTorrent.peer.protocol.messages.Handsake;
import es.deusto.ingenieria.ssdd.bitTorrent.peer.protocol.messages.HaveMsg;
import es.deusto.ingenieria.ssdd.bitTorrent.peer.protocol.messages.InterestedMsg;
import es.deusto.ingenieria.ssdd.bitTorrent.peer.protocol.messages.KeepAliveMsg;
import es.deusto.ingenieria.ssdd.bitTorrent.peer.protocol.messages.NotInterestedMsg;
import es.deusto.ingenieria.ssdd.bitTorrent.peer.protocol.messages.PeerProtocolMessage;
import es.deusto.ingenieria.ssdd.bitTorrent.peer.protocol.messages.PeerProtocolMessage.Type;
import es.deusto.ingenieria.ssdd.bitTorrent.peer.protocol.messages.PieceMsg;
import es.deusto.ingenieria.ssdd.bitTorrent.peer.protocol.messages.PortMsg;
import es.deusto.ingenieria.ssdd.bitTorrent.peer.protocol.messages.RequestMsg;
import es.deusto.ingenieria.ssdd.bitTorrent.peer.protocol.messages.UnChokeMsg;
import es.deusto.ingenieria.ssdd.bitTorrent.util.ToolKit;
import es.deusto.ingenieria.ssdd.bitTorrent.Dao.Piece;

public class PWPReceiver implements Runnable{

	private Peer peer;
	private MetainfoFile<?> metaInfo;

	public PWPReceiver(Peer peer, MetainfoFile<?> metaInfo) {
		super();
		this.peer = peer;
		this.metaInfo = metaInfo;
	}

	@Override
	public void run() {
		
		//SEND HANDSAKE
		//Para probar se manda la peticion al primero de la lista
		try{
			
		boolean firstCon = true;	
		boolean conected = true;	
			
		System.out.println("Connecting to "+peer.getIp()+":"+peer.getPort());
		Socket socket = new Socket(peer.getIp(),peer.getPort() );
		
		DataOutputStream socketOutputStream = new DataOutputStream(socket.getOutputStream());   

		Handsake handsake = new Handsake();

		byte[] hash = metaInfo.getInfo().getInfoHash();
		String peerId = peer.getId();


		handsake.setInfoHash(new String(hash));
		handsake.setPeerId(peerId);


		byte[] handsakeBytes=handsake.getBytes();
		int handshakeLenght = handsakeBytes.length;

		if(handshakeLenght != 68){
			System.out.println("Handshake lenght incorrect");
		}
		socketOutputStream.write(handsakeBytes);

		System.out.println("Handsake sended:"+handsake.toString());
		
		
		
		//Se recive el mensaje, provamos con 2
		int k=0;
		while(k<3){
			System.out.println("ciclo"+k);
			k++;
			DataInputStream in = new DataInputStream(socket.getInputStream());
			
			if(firstCon){
				byte [] responseHandsake = new byte[68];
				in.read(responseHandsake);
				System.out.println(new String(responseHandsake));


//				SE PARSEA EL HANDSAKE (SOLO LA PRIMERA VEZ) 
				System.out.println("Name length"+new Integer(responseHandsake[0]));
				
				byte [] hashBytes = Arrays.copyOfRange(responseHandsake, 28, 48);
				System.out.println("Info hash recivido"+new String(hashBytes));
				
//				Se comprueba que tenemos el mismo fichero comparando el hash de respuesta con nuestro hash
				if(compareHash(new String(hashBytes),new String(hash))){
					System.out.println("Hash checked");
				}
				
				firstCon = false;
			}

			
			
			int availableBytes = in.available();
			byte [] responseBytes = new byte[availableBytes];
			in.read(responseBytes);
			
			System.out.println(new String(responseBytes));
			
//			Se separan los mensajes recibidos
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
			
//			Se parsean los mensajes recibidos
			ArrayList<PeerProtocolMessage> parsedMessages = new ArrayList<>();
			for(int i=0; i<messageByteList.size();i++){
				
				byte[] currentMessageBytes = messageByteList.get(i);				
				PeerProtocolMessage message = parseMessage(currentMessageBytes);
				parsedMessages.add(message);
			}
			
			
			for(int i=0;i<parsedMessages.size();i++){
				
				PeerProtocolMessage message = parsedMessages.get(i);
				
				Type type = message.getType();
				
				
				
				if(type.equals(Type.BITFIELD)){
					BitfieldMsg bitfield = (BitfieldMsg) message;
					byte[] payload = bitfield.getPayload();
					
					
				}
				else if(type.equals(Type.CANCEL)){
					System.out.println("CANCEL");
				}
				else if(type.equals(Type.CHOKE)){
					System.out.println("CHOKE");
				}
				else if(type.equals(Type.HAVE)){
					
					HaveMsg have = (HaveMsg) message;
					byte[] payload = have.getPayload();
					int pieceIndex = ToolKit.bigEndianBytesToInt(payload, 0);
					
					ArrayList<Boolean> pieces = peer.getPieces();
					pieces.set(pieceIndex, true);
					peer.setPieces(pieces);
					System.out.println("Have index :"+pieceIndex);
					
					if(i==parsedMessages.size()-1){
						
						InterestedMsg interested = new InterestedMsg();
						byte[] lenghtBytes = new byte[4];
						
						
						byte[] lenght = ToolKit.intToBigEndianBytes(1, lenghtBytes, 0);
						interested.setLength(lenght);
						byte[] interestBytes = interested.getBytes();
						
						socketOutputStream.write(interestBytes);
						Thread.sleep(500);
						System.out.println("interested writed");
					}
				}
				else if(type.equals(Type.INTERESTED)){
					System.out.println("INTERESTED");
				}
				else if(type.equals(Type.KEEP_ALIVE)){
					System.out.println("KEEP_ALIVE");
				}
				else if(type.equals(Type.NOT_INTERESTED)){
					System.out.println("NOT_INTERESTED");
				}
				else if(type.equals(Type.PIECE)){
					
					PieceMsg piece = (PieceMsg) message;
					byte[] payload = piece.getPayload();
					piece.ge
					writePiece(payload);
					
				}
				else if(type.equals(Type.PORT)){
					System.out.println("PORT");
				}
				else if(type.equals(Type.REQUEST)){
					System.out.println("REQUEST");
				}
				else if(type.equals(Type.UNCHOKE)){
					System.out.println("UNCHOKE");
					
					List<Piece> torrentFile = Client.getTorrentFile();
					int index =0;
					int begin = 0;
					boolean found = false;
					for(int j=0; j<torrentFile.size()&&!found;j++){
						
						Piece currentPiece = torrentFile.get(j);
						boolean downloaded = currentPiece.getDownloaded();
						
						if(!downloaded){
							index = j;
							ArrayList<Block> currentBlocks = currentPiece.getBlocks();
							
							boolean found2 = false;
							for(int l=0;l<currentBlocks.size()&&!found2;l++){
								
								Block block = currentBlocks.get(l);
								boolean downloadedBlock = block.getDownloaded();
								
								if(downloadedBlock){
									begin = l;
									found2=true;
								}
							}
							
							found = true;
						}
					}
					RequestMsg request = new RequestMsg(index, begin, 32);
					
					byte[] requestBytes = request.getBytes();
					
					socketOutputStream.write(requestBytes);
					Thread.sleep(500);
				}
				
			}

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
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	


	}

	public static boolean compareHash(String hash1, String hash2){
		
		return hash1.equals(hash2);
	}
	

	public synchronized void writePiece(byte[] piece, int index){
		
        try {
        	
        	String fileName = metaInfo.getInfo().getName();
        	File file = new File(fileName+".part");
        	if(!file.exists()){
        		file.createNewFile();
        	}
        	
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			
			raf.seek();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static PeerProtocolMessage parseMessage(byte[] msgBytes) {
		PeerProtocolMessage message = null;
		
		if (msgBytes != null && msgBytes.length != 0) {
			
			int length = ToolKit.bigEndianBytesToInt(msgBytes, 0);
			
			//
			if (length == 0) {
				return new KeepAliveMsg();
			}
			
			int id = msgBytes[4];
					
			switch (id) {			
				case 0:	//choke
					message = new ChokeMsg();
					break;
				case 1:	//unchoke
					message = new UnChokeMsg();
					break;
				case 2:	//interested
					message = new InterestedMsg();
					break;
				case 3: //not_interested
					message = new NotInterestedMsg();
					break;
				case 4: //have
					message = new HaveMsg(ToolKit.bigEndianBytesToInt(msgBytes, 5));	//Piece index
					break;
				case 5:	//bitfield
					message = new BitfieldMsg(Arrays.copyOfRange(msgBytes, 5, msgBytes.length));	//Bitfield
					break;
				case 6:	//request
					message = new RequestMsg(ToolKit.bigEndianBytesToInt(msgBytes, 5), 		//Piece index 
							                 ToolKit.bigEndianBytesToInt(msgBytes, 9), 		//Block offset
							                 ToolKit.bigEndianBytesToInt(msgBytes, 13));	//Block length	
					break;
				case 7:	//piece
					message = new PieceMsg(ToolKit.bigEndianBytesToInt(msgBytes, 5),			//Piece index
              			   				   ToolKit.bigEndianBytesToInt(msgBytes, 9),			//Block offset
              			   				   Arrays.copyOfRange(msgBytes, 13, msgBytes.length));	//Data					
					break;
				case 8:	//cancel
					message = new CancelMsg(ToolKit.bigEndianBytesToInt(msgBytes, 5), 	//Piece index
			                 				ToolKit.bigEndianBytesToInt(msgBytes, 9), 	//Block offset
			                 				ToolKit.bigEndianBytesToInt(msgBytes, 13));	//Block length						
					break;
				case 9:	//port
					message = new PortMsg(ToolKit.bigEndianBytesToInt(msgBytes, 5));	//Port number
					break;					
			}
		}
		
		return message;		
	}
}
