package pairRanking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map.Entry;


public class CooccurrenceSum {
	private String geneDir,disDir;
	private HashMap<String,Integer> geneAb,geneSen;
	private HashMap<String,HashMap<String,Integer>> disAb,disSen;
	public CooccurrenceSum(){
		geneDir = "../data/GeneCooccurrence/";
		disDir = "../data/DisGeneCooccurrence/";
	}
	public void geneCooccurrenceSum() throws IOException, ClassNotFoundException{
		File dir = new File(geneDir);
		for (String eachFile: dir.list()){
			FileInputStream fileIn = new FileInputStream(geneDir+eachFile);
	        ObjectInputStream in = new ObjectInputStream(fileIn);
	        HashMap<String,Integer> temp = (HashMap<String, Integer>) in.readObject();
	        in.close();
	        fileIn.close();
	        if (eachFile.contains("Sen")){
	        	if (geneSen == null)
	        		geneSen=temp;
	        	else{
	        		for (Entry<String,Integer> entry:temp.entrySet()){
	        			String key = entry.getKey();
	        			int value = entry.getValue();
	        			if (geneSen.containsKey(key))
	        				geneSen.put(key, geneSen.get(key)+value);
	        			else
	        				geneSen.put(key, value);
	        		}
	        	}
	        }
	        else{
	        	if (geneAb == null)
	        		geneAb=temp;
	        	else{
	        		for (Entry<String,Integer> entry:temp.entrySet()){
	        			String key = entry.getKey();
	        			int value = entry.getValue();
	        			if (geneAb.containsKey(key))
	        				geneAb.put(key, geneAb.get(key)+value);
	        			else
	        				geneAb.put(key, value);
	        		}
	        	}
	        }
		}
	}
	public void disCooccurrenceSum() throws IOException, ClassNotFoundException{
		File dir = new File(disDir);
		for (String eachFile: dir.list()){
			FileInputStream fileIn = new FileInputStream(disDir+eachFile);
	        ObjectInputStream in = new ObjectInputStream(fileIn);
	        HashMap<String,HashMap<String,Integer>> temp = (HashMap<String, HashMap<String, Integer>>) in.readObject();
	        in.close();
	        fileIn.close();
	        if (eachFile.contains("Sen")){
	        	if (disSen == null)
	        		disSen=temp;
	        	else{
	        		for (Entry<String,HashMap<String,Integer>> entryD:temp.entrySet()){
	        			String disease = entryD.getKey();
	        			if (disSen.containsKey(disease))
		        			for (Entry<String,Integer> entry:entryD.getValue().entrySet()){
		        				String key = entry.getKey();
		        				int value = entry.getValue();
		        				if (disSen.get(disease).containsKey(key))
		        					disSen.get(disease).put(key, disSen.get(disease).get(key)+value);
		        				else
		        					disSen.get(disease).put(key, value);
		        			}
	        			else
	        				disSen.put(disease, entryD.getValue());
	        		}
	        	}
	        }
	        else{
	        	if (disAb == null)
	        		disAb=temp;
	        	else{
	        		for (Entry<String,HashMap<String,Integer>> entryD:temp.entrySet()){
	        			String disease = entryD.getKey();
	        			if (disAb.containsKey(disease))
		        			for (Entry<String,Integer> entry:entryD.getValue().entrySet()){
		        				String key = entry.getKey();
		        				int value = entry.getValue();
		        				if (disAb.get(disease).containsKey(key))
		        					disAb.get(disease).put(key, disAb.get(disease).get(key)+value);
		        				else
		        					disAb.get(disease).put(key, value);
		        			}
	        			else
	        				disAb.put(disease, entryD.getValue());
	        		}
	        	}
	        }
		}
	}
	public void printMap() throws IOException{
		FileOutputStream fileOut = new FileOutputStream(geneDir+"TotalGeneSen.ser");
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(geneSen);
        out.close();
        fileOut.close();
        
        fileOut = new FileOutputStream(geneDir+"TotalGeneAb.ser");
        out = new ObjectOutputStream(fileOut);
        out.writeObject(geneAb);
        out.close();
        fileOut.close();
        
        fileOut = new FileOutputStream(disDir+"TotalDisSen.ser");
        out = new ObjectOutputStream(fileOut);
        out.writeObject(disSen);
        out.close();
        fileOut.close();
        
        fileOut = new FileOutputStream(disDir+"TotalDisAb.ser");
        out = new ObjectOutputStream(fileOut);
        out.writeObject(disAb);
        out.close();
        fileOut.close();
	}
	public static void main(String[] args) throws ClassNotFoundException, IOException{
		CooccurrenceSum ccs = new CooccurrenceSum();
		
		ccs.geneCooccurrenceSum();
		ccs.disCooccurrenceSum();
		ccs.printMap();
	}
}
