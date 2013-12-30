package es.deusto.ingenieria.ssdd.bitTorrent.Dao;

import java.net.InetAddress;
import java.util.ArrayList;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

public class Peer {
	
	private InetAddress ip;
	private int port;
	private String state;
	private String id;
	private ArrayList<Boolean> pieces = new ArrayList<Boolean>() ;
	
	
	public InetAddress getIp() {
		return ip;
	}
	public void setIp(InetAddress ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public ArrayList<Boolean> getPieces() {
		return pieces;
	}
	public void setPieces(ArrayList<Boolean> pieces) {
		this.pieces = pieces;
	}
	
	
	
	

}
