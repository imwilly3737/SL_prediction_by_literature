package syntheticLethal;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

public class ExtractPotentialSLPairs {
	private String mutantGeneFile,potentialEGFile,potentialSLFile,geneSenFile,geneAbFile,disSenFile,disAbFile;
	private HashMap<String,HashSet<String>> egMap,mgMap;
	private HashMap<String,Double> egScore;
	private HashMap<String,Integer> geneSen,geneAb;
	private HashMap<String,HashMap<String,Integer>> disSen,disAb;
	private double threshold;
	private int geneSenMax,geneAbMax;
	private HashMap<String,Integer> disSenMax,disAbMax;
	private boolean checkCo;
	public ExtractPotentialSLPairs(boolean checkCo) throws IOException, ClassNotFoundException{
		mutantGeneFile = "../../NERsuite/cell_line_cancer_mutations.txt";
		potentialEGFile = "./allPotentialEssentialGene(new trigger)(0).txt";
		potentialSLFile = "./potentialSLpairs.txt";
		geneSenFile = "../data/GeneCooccurrence/TotalGeneSen.ser";
		geneAbFile = "../data/GeneCooccurrence/TotalGeneAb.ser";
		disSenFile = "../data/DisGeneCooccurrence/TotalDisSen.ser";
		disAbFile = "../data/DisGeneCooccurrence/TotalDisAb.ser";
		egMap = new HashMap<>();
		mgMap = new HashMap<>();
		egScore = new HashMap<>();
		threshold = 10.0;
		this.checkCo = checkCo;
		if (checkCo)
			potentialSLFile = "./potentialSLpairsCo.txt";
		loadMGMap();
		loadEGMap();
		if (checkCo)
			loadCoMap();
	}
	private void loadEGMap() throws IOException{
		if (egMap.size()>0)
			return;
		BufferedReader br = new BufferedReader(new FileReader(potentialEGFile));
		while (br.ready()){
			String[] eTab = br.readLine().split("\t");
			String geneID= eTab[0].split("\\|")[0];
			String diseaseID = eTab[0].split("\\|")[1];
			if (diseaseID.equals("null") || geneID.equals("null"))
				continue;
			double score = Double.parseDouble(eTab[1]);
			if (mgMap.containsKey(diseaseID) && mgMap.get(diseaseID).contains(geneID))	// if this gene is a mutant gene, skip it
				continue;
			if (!egMap.containsKey(diseaseID))
				egMap.put(diseaseID, new HashSet<String>());
			egMap.get(diseaseID).add(geneID);
			String key = geneID+"|"+diseaseID;
			if (!egScore.containsKey(key))
				egScore.put(key, 0.0);
			egScore.put(key, egScore.get(key)+score);
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
	private void loadCoMap() throws ClassNotFoundException, IOException{
		geneAb = (HashMap<String, Integer>) readObject(geneAbFile);
		geneSen = (HashMap<String, Integer>) readObject(geneSenFile);
		disAb = (HashMap<String, HashMap<String, Integer>>) readObject(disAbFile);
		disSen = (HashMap<String, HashMap<String, Integer>>) readObject(disSenFile);
		
		geneAbMax = 0;
		for (Entry<String,Integer>entry:geneAb.entrySet())
			if (geneAbMax < entry.getValue())
				geneAbMax = entry.getValue();
		geneSenMax = 0;
		for (Entry<String,Integer>entry:geneSen.entrySet())
			if (geneSenMax < entry.getValue())
				geneSenMax = entry.getValue();
		
		disAbMax = new HashMap<>();
		for (Entry<String,HashMap<String,Integer>>entryD:disAb.entrySet()){
			String disease = entryD.getKey();
			disAbMax.put(disease, 0);
			for (Entry<String,Integer>entry:entryD.getValue().entrySet())
				if (disAbMax.get(disease) < entry.getValue())
					disAbMax.put(disease, entry.getValue());
		}
		disSenMax = new HashMap<>();
		for (Entry<String,HashMap<String,Integer>>entryD:disSen.entrySet()){
			String disease = entryD.getKey();
			disSenMax.put(disease, 0);
			for (Entry<String,Integer>entry:entryD.getValue().entrySet())
				if (disSenMax.get(disease) < entry.getValue())
					disSenMax.put(disease, entry.getValue());
		}
	}
	private Object readObject(String filePath) throws IOException, ClassNotFoundException{
		FileInputStream fileIn = new FileInputStream(filePath);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        Object e = in.readObject();
        in.close();
        fileIn.close();
        return e;
	}
	public void extractpotentialSLPairs() throws IOException{
		PrintWriter pw = new PrintWriter(potentialSLFile);
		for (String eDisease:egMap.keySet()){
			if (!mgMap.containsKey(eDisease))
				continue;
			for (String eEG: egMap.get(eDisease)){
				if (egScore.get(eEG+"|"+eDisease)>=threshold){
					for (String eMG: mgMap.get(eDisease)){
						if (checkCo){
							String geneKey = eMG+"|"+eEG;
							double slGeneAb = 0;
							if (geneAb.containsKey(geneKey))
								slGeneAb = (double)geneAb.get(geneKey)/geneAbMax;
							double slGeneSen = 0;
							if (geneSen.containsKey(geneKey))
								slGeneSen = (double)geneSen.get(geneKey)/geneSenMax;
							double slDisAb = 0;
							if (disAb.containsKey(eDisease) && disAb.get(eDisease).containsKey(geneKey))
								slDisAb = (double)disAb.get(eDisease).get(geneKey)/disAbMax.get(eDisease);
							double slDisSen = 0;
							if (disSen.containsKey(eDisease) && disSen.get(eDisease).containsKey(geneKey))
								slDisSen = (double)disSen.get(eDisease).get(geneKey)/disSenMax.get(eDisease);
							if (slGeneAb != 0)
								pw.println(eMG+"\t"+eEG+"\t"+eDisease+"\t"+egScore.get(eEG+"|"+eDisease)+"\t"+slGeneAb+"\t"+slGeneSen+"\t"+slDisAb+"\t"+slDisSen);
						}else{
							pw.println(eMG+"\t"+eEG+"\t"+eDisease+"\t"+egScore.get(eEG+"|"+eDisease));
						}
					}
				}
			}
		}
		pw.close();
	}
	public static void main (String args[]) throws IOException, ClassNotFoundException{
		ExtractPotentialSLPairs eslp = new ExtractPotentialSLPairs(true);
		eslp.extractpotentialSLPairs();
	}
}
