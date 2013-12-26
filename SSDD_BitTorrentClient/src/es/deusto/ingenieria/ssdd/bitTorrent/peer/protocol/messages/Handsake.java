package es.deusto.ingenieria.ssdd.bitTorrent.peer.protocol.messages;

import java.io.ByteArrayOutputStream;

public class Handsake {	
	private static final String DEFAULT_PROTOCOL = "BitTorrent protocol";
	private static final String RESERVED = "00000000";
	private static final byte[] RESERVED2 = new byte[8];
	
	public static byte[] getReserved2() {
		return RESERVED2;
	}


	private int nameLength;
	private String protocolName;
	private byte[] reserved;
	private String infoHash;
	private String peerId;
	
	public Handsake() {		
		this.protocolName = Handsake.DEFAULT_PROTOCOL;
		this.nameLength = this.protocolName.length();
		this.reserved = Handsake.RESERVED2;
	}
	
	public int getNameLength() {
		return nameLength;
	}

	public String getProtocolName() {
		return protocolName;
	}

	public void setProtocolName(String protocolName) {
		this.protocolName = protocolName;
		this.nameLength = protocolName.length();
	}

	public byte[] getReserved() {
		return reserved;
	}

	public String getInfoHash() {
		return infoHash;
	}

	public void setInfoHash(String infoHash) {
		this.infoHash = infoHash;
	}

	public String getPeerId() {
		return peerId;
	}

	public void setPeerId(String peerId) {
		this.peerId = peerId;
	}
	
	public byte[] getBytes() {
		try {
			ByteArrayOutputStream result = new ByteArrayOutputStream();

			result.write(this.nameLength);
			result.write(this.protocolName.getBytes());
			result.write(this.reserved);
			result.write(this.infoHash.getBytes());
			result.write(this.peerId.getBytes());
			
			return result.toByteArray();
		} catch (Exception ex) {
			return null;
		}
	}		
		
		
	public String toString() {
		return new String(this.getBytes());
	}
}