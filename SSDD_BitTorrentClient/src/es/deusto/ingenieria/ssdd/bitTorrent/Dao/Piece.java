package es.deusto.ingenieria.ssdd.bitTorrent.Dao;

import java.util.ArrayList;

public class Piece {
	
	private boolean downloaded;
	private ArrayList<Block> blocks;
	
	
	public Piece(Integer blocks) {
		super();
		this.downloaded = false;
		
		
		
		ArrayList<Block> blockList = new ArrayList<>();
		for(int i=0;i<blocks;i++){
			System.out.println("block added");
			Block block = new Block();
			block.setDownloaded(false);
			block.setBytes(new byte [32]);
			blockList.add(block);
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
