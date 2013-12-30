package es.deusto.ingenieria.ssdd.bitTorrent.Dao;

import java.util.ArrayList;

public class Piece {
	
	private boolean downloaded;
	private ArrayList<Block> blocks;
	
	
	public Piece(Integer pieceLenght) {
		super();
		this.downloaded = false;
		
		int blocks = pieceLenght/32;
		
		ArrayList<Block> blockList = new ArrayList<>();
		for(int i=0;i<blocks;i++){
			Block block = new Block();
			block.setDownloaded(false);
			block.setBytes(new byte [32]);
		}
		this.blocks = blockList;
	}
	
	
	public boolean getDownloaded() {
		return downloaded;
	}
	public void setDownloaded(boolean state) {
		this.downloaded = state;
	}
	public ArrayList<Block> getBlocks() {
		return blocks;
	}
	public void setBlocks(ArrayList<Block> blocks) {
		this.blocks = blocks;
	}

}
