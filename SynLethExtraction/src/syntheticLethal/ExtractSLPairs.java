package syntheticLethal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;

public class ExtractSLPairs {
	private String mutantGeneFile,essentialGeneFile,SLFile;
	private HashMap<String,HashSet<String>> egMap,mgMap;
	private HashMap<String,Double> egScore;
	private double threshold;
	public ExtractSLPairs() throws IOException{
		mutantGeneFile = "../../NERsuite/cell_line_cancer_mutations.txt";
		essentialGeneFile = "../../COLT-Cancer/EssentialGeneWithCancer.txt";
		SLFile = "./SLpairs.txt";
		egMap = new HashMap<>();
		mgMap = new HashMap<>();
		egScore = new HashMap<>();
		threshold = 0.05;
		loadEGMap();
		loadMGMap();
	}
	private void loadEGMap() throws IOException{
		if (egMap.size()>0)
			return;
		BufferedReader br = new BufferedReader(new FileReader(essentialGeneFile));
		while (br.ready()){
			String[] eTab = br.readLine().split("\t");
			String geneID= eTab[4];
			String diseaseID = eTab[5];
			if (diseaseID.equals("null") || geneID.equals("null"))
				continue;
			double score = Double.parseDouble(eTab[2]);
			if (!egMap.containsKey(diseaseID))
				egMap.put(diseaseID, new HashSet<String>());
			egMap.get(diseaseID).add(geneID);
			String key = geneID+"|"+diseaseID;
			if (!egScore.containsKey(key))
				egScore.put(key, 0.0);
			egScore.put(key, egScore.get(key)+0.05-score);
		}
		br.close();
	}
	private void loadMGMap() throws IOException{
		if (mgMap.size()>0)
			return;
		BufferedReader br = new BufferedReader(new FileReader(mutantGeneFile));
		while (br.ready()){
			String[] eTab = br.readLine().split("\t");
			String geneID = eTab[1];
			String diseaseID = eTab[4];
			if (diseaseID.equals("null") || geneID.equals("null"))
				continue;
			if (!mgMap.containsKey(diseaseID))
				mgMap.put(diseaseID, new HashSet<String>());
			mgMap.get(diseaseID).add(geneID);
		}
		br.close();
	}
	public void extractSLPairs() throws IOException{
		PrintWriter pw = new PrintWriter(SLFile);
		for (String eDisease:egMap.keySet()){
			if (!mgMap.containsKey(eDisease))
				continue;
			for (String eEG: egMap.get(eDisease)){
				if (egScore.get(eEG+"|"+eDisease)>=threshold){
					for (String eMG: mgMap.get(eDisease)){
						if (!egMap.get(eDisease).contains(eMG) && !mgMap.get(eDisease).contains(eEG))
							pw.println(eMG+"\t"+eEG+"\t"+eDisease+"\t"+egScore.get(eEG+"|"+eDisease));
					}
				}
			}
		}
		pw.close();
	}
	public static void main (String args[]) throws IOException{
		ExtractSLPairs eslp = new ExtractSLPairs();
		eslp.extractSLPairs();
	}
}
