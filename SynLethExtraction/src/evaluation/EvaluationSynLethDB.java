package evaluation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import object.SynLethPair;

public class EvaluationSynLethDB {
	private String synLethDBFile;
	private HashMap<String,SynLethPair> dataset;
	private HashSet<String> goldStandard,Daisy,textMine,EvalGene;
	private boolean isGenomeRNAiGene;
	public EvaluationSynLethDB(boolean isGenomeRNAiGene) throws IOException{
		synLethDBFile = "../../SynLethDB/sl_human";
		goldStandard = new HashSet<>();
		Daisy = new HashSet<>();
		textMine = new HashSet<>();
		dataset = new HashMap<>();
		this.isGenomeRNAiGene = isGenomeRNAiGene;
		EvalGene = new HashSet<>();
		EvalGene.add("3265");
		EvalGene.add("3845");
		EvalGene.add("4893");
		EvalGene.add("8883");
		/*
		 * GoldStandard
		 * 1.	Colon cancer (D003110)
		 * 		Cell line: DLD-1, HCT116
		 * 		Gene: 3265, 3845, 4893 & 8883
		 * 2.	Skin cancer (D012878)
		 * 		Cell line: A375
		 * 		Gene: 8883
		 * 3.	Pancreas cancer (D010190)
		 * 		Cell line: MIAPaCa-2
		 * 		Gene: 6240, 51727, 1633, 7298, 5243, 89845, 3177, 978, 2030 & 9154
		 */
		loadDataSet();
	}
	public void loadDataSet() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(synLethDBFile));
		br.readLine();	//skip first line
		while (br.ready()){
			SynLethPair slp = new SynLethPair(br.readLine(),"\t");
			if (slp.Type.equals("SDL"))
				continue;
			String key1 = slp.GeneAid+"|"+slp.GeneBid;
			String key2 = slp.GeneBid+"|"+slp.GeneAid;
			if (slp.Evidence.indexOf("Daisy") != -1){
				Daisy.add(key1);
				Daisy.add(key2);
			}
			if (slp.Evidence.indexOf("Text Mining") != -1){
				textMine.add(key1);
				textMine.add(key2);
			}
			if (slp.Evidence.indexOf("GenomeRNAi") != -1){
				if (isGenomeRNAiGene && (slp.Disease.equals("DLD-1") || slp.Disease.equals("HCT116")) && (EvalGene.contains(slp.GeneAid)||EvalGene.contains(slp.GeneBid))){
					goldStandard.add(key1);
					goldStandard.add(key2);
				}
				
			}
			if (!isGenomeRNAiGene && ! slp.Evidence.equals("Text Mining") && !slp.Evidence.equals("Daisy")){
				goldStandard.add(key1);
				goldStandard.add(key2);
			}
			if (dataset.containsKey(key1) || dataset.containsKey(key2)){
				System.err.println("Overlap: "+slp);
				SynLethPair old = dataset.containsKey(key1)?dataset.get(key1):dataset.get(key2);
				if (slp.Evidence.contains(old.Evidence)){
					dataset.put(key1,slp);
					dataset.put(key2, slp);
				}
				else if (!old.Evidence.contains(slp.Evidence)){
					dataset.get(key1).Score += slp.Score;
					dataset.get(key2).Score += slp.Score;
					dataset.get(key1).Evidence += ";"+slp.Evidence;
					dataset.get(key2).Evidence += ";"+slp.Evidence;
				}
					
			}
			else{
				dataset.put(key1,slp);
				dataset.put(key2,slp);
			}
			
		}
		br.close();
		
	}
	private HashMap<String, Integer> sortMap(HashMap<String, Integer> map) {
	       List<?> list = new LinkedList(map.entrySet());
	       // Defined Custom Comparator here
	       Collections.sort(list, new Comparator() {
	            public int compare(Object o1, Object o2) {
	               return ((Comparable) ((Map.Entry) (o2)).getValue())
	                  .compareTo(((Map.Entry) (o1)).getValue());
	            }
	       });

	       // Here I am copying the sorted list in HashMap
	       // using LinkedHashMap to preserve the insertion order
	       HashMap sortedHashMap = new LinkedHashMap();
	       for (Iterator it = list.iterator(); it.hasNext();) {
	              Map.Entry entry = (Map.Entry) it.next();
	              sortedHashMap.put(entry.getKey(), entry.getValue());
	       } 
	       return sortedHashMap;
	  }
	public HashSet<String> loadPotentialCo(String filePath,double[] threshold) throws IOException{
		if (threshold.length != 4){
			System.err.println("Wrong thresholds");
			return null;
		}
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		HashSet<String> poSL = new HashSet<String>();
		while (br.ready()){
			String line = br.readLine();
			String[] eTab = line.split("\t");
			String key1 = eTab[0]+"|"+eTab[1];
			String key2 = eTab[1]+"|"+eTab[0];
			if (checkCoThr(eTab,threshold)){
				if (!isGenomeRNAiGene){
					poSL.add(eTab[0]+"|"+eTab[1]);
					poSL.add(eTab[1]+"|"+eTab[0]);
				}
				else if (eTab[2].equals("D003110") &&( EvalGene.contains(eTab[0]) || EvalGene.contains(eTab[1]))){
					poSL.add(eTab[0]+"|"+eTab[1]);
					poSL.add(eTab[1]+"|"+eTab[0]);
				}
			}
		}
		br.close();
		return poSL;
	}
	public HashSet<String> loadPotentialCor(String filePath,double[] threshold) throws IOException{
		if (threshold.length != 4){
			System.err.println("Wrong thresholds");
			return null;
		}
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		HashSet<String> poSL = new HashSet<String>();
		while (br.ready()){
			String line = br.readLine();
			String[] eTab = line.split("\t");
			String spearman = eTab[4];
			String pearson = eTab[5];
			double spCoe = Double.parseDouble(spearman.substring(spearman.indexOf(":")+1, spearman.indexOf("|")));
			double spP = Double.parseDouble(spearman.substring(spearman.indexOf("|")+1));
			double peCoe = Double.parseDouble(pearson.substring(pearson.indexOf(":")+1, pearson.indexOf("|")));
			double peP = Double.parseDouble(pearson.substring(pearson.indexOf("|")+1));
			double value[] = {spCoe,spP,peCoe,peP};
			if (checkCorThr(value,threshold)){
				if (!isGenomeRNAiGene){
					poSL.add(eTab[0]+"|"+eTab[1]);
					poSL.add(eTab[1]+"|"+eTab[0]);
				}
				else if (eTab[2].equals("D003110") &&( EvalGene.contains(eTab[0]) || EvalGene.contains(eTab[1]))){
					poSL.add(eTab[0]+"|"+eTab[1]);
					poSL.add(eTab[1]+"|"+eTab[0]);
				}
			}
		}
		br.close();
		return poSL;
	}
	public HashSet<String> loadPotentialCoCor(String filePath,double[] thresholdCo,double[] thresholdCor) throws IOException{
		if (thresholdCo.length != 4 || thresholdCor.length != 4){
			System.err.println("Wrong thresholds");
			return null;
		}
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		HashSet<String> poSL = new HashSet<String>();
		while (br.ready()){
			String line = br.readLine();
			String[] eTab = line.split("\t");
			String spearman = eTab[8];
			String pearson = eTab[9];
			double spCoe = Double.parseDouble(spearman.substring(spearman.indexOf(":")+1, spearman.indexOf("|")));
			double spP = Double.parseDouble(spearman.substring(spearman.indexOf("|")+1));
			double peCoe = Double.parseDouble(pearson.substring(pearson.indexOf(":")+1, pearson.indexOf("|")));
			double peP = Double.parseDouble(pearson.substring(pearson.indexOf("|")+1));
			double value[] = {spCoe,spP,peCoe,peP};
			if (checkCoThr(eTab,thresholdCo) && checkCorThr(value,thresholdCor)){
				if (!isGenomeRNAiGene){
					poSL.add(eTab[0]+"|"+eTab[1]);
					poSL.add(eTab[1]+"|"+eTab[0]);
				}
				else if (eTab[2].equals("D003110") &&( EvalGene.contains(eTab[0]) || EvalGene.contains(eTab[1]))){
					poSL.add(eTab[0]+"|"+eTab[1]);
					poSL.add(eTab[1]+"|"+eTab[0]);
				}
			}
		}
		br.close();
		return poSL;
	}
	private boolean checkCoThr(String[] eTab, double[] threshold) {
		for (int i=0;i<threshold.length;i++){
			if (Double.parseDouble(eTab[i+4]) < threshold[i])
				return false;
		}
		return true;
	}
	private boolean checkCorThr(double[] input,double[] threshold){
		for (int i=0;i<threshold.length;i+=2){
			if (input[i]<threshold[i])
				return false;
			if (input[i+1] == -1)
				return false;
			if (input[i+1]>threshold[i+1])
				return false;
		}
		return true;
	}
	public void evaluate(HashSet<String> extractedSet,String describe) throws IOException{
		System.out.println("====="+describe+"=====");
		HashSet<String> tpSet = new HashSet<>(extractedSet);
		tpSet.retainAll(goldStandard);
		printSet(tpSet,"./tpSet_"+describe+".txt");
		HashSet<String> fpSet = new HashSet<>(extractedSet);
		fpSet.removeAll(goldStandard);
		printSet(fpSet,"./fpSet_"+describe+".txt");
		HashSet<String> fnSet = new HashSet<>(goldStandard);
		fnSet.removeAll(extractedSet);
		printSet(fnSet,"./fnSet_"+describe+".txt");
		int tp = tpSet.size()/2;
		int fp = fpSet.size()/2;
		int fn = fnSet.size()/2;
		double precision = (double)tp/(tp+fp);
		double recall = (double)tp/(tp+fn);
		System.out.println("tp: "+tp);
		System.out.println("fp: "+fp);
		System.out.println("fn: "+fn);
		System.out.println("Precision: "+precision);
		System.out.println("Recall: "+recall);
		System.out.println("F-score: "+(2*precision*recall/(precision+recall)));
		System.out.println("=================");
		
	}
	private void printSet(HashSet<String> set,String filePath) throws IOException{
		PrintWriter pw = new PrintWriter(filePath);
		for (String e:set){
			if (dataset.containsKey(e))
				pw.println(dataset.get(e));
			else
				pw.println(e);
		}
		pw.close();
	}
	static public void main(String[] args) throws IOException{
		EvaluationSynLethDB esldb = new EvaluationSynLethDB(true);
		double[] thresholdCo = {0,0,0,0}; 
		esldb.evaluate(esldb.loadPotentialCo("./potentialSLpairsCo.txt",thresholdCo),"Cooccurrence");
		esldb.evaluate(esldb.Daisy,"Daisy");
		esldb.evaluate(esldb.textMine,"Text Mining");
		double[] thresholdCor = new double[]{-1,0.05,-1,0.05};
		esldb.evaluate(esldb.loadPotentialCor("./potentialSLpairsSpCorD003110.txt", thresholdCor),"Correlation");
		esldb.evaluate(esldb.loadPotentialCoCor("./potentialSLpairsCoSpCorD003110.txt", thresholdCo,thresholdCor),"Cooccurrence & Correlation");
	}
}
