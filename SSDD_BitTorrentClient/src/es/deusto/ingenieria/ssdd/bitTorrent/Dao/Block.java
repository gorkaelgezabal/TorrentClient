package es.deusto.ingenieria.ssdd.bitTorrent.Dao;

public class Block {
	
	private boolean downloaded;
	private byte[] bytes;
	
	
	public boolean getDownloaded() {
		return downloaded;
	}
	public void setDownloaded(boolean downloaded) {
		this.downloaded = downloaded;
	}
	public byte[] getBytes() {
		return bytes;
	}
	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}
}
