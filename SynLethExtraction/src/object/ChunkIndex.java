package object;

public class ChunkIndex {
	public int start;
	public int end;
	public String chunkType;
	public ChunkIndex(){
		
	}
	public ChunkIndex(int s,int e,String t){
		start = s;
		end = e;
		chunkType = t;
	}
}
