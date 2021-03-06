package evaluation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;

public class EvaluationPARPsyntheticLethal {
	private String goldStandardFile;
	private HashSet<String> EvalGene,goldStandard;
	public EvaluationPARPsyntheticLethal() throws IOException{
		goldStandardFile = "../../Turner, N.C/PARP_synthetic_lethal_ID.txt";
		EvalGene = new HashSet<>();
		goldStandard = new HashSet<>();
		EvalGene.add("142");
		loadGoldStandard();
	}
	public void loadGoldStandard() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(goldStandardFile));
		while (br.ready()){
			String line = br.readLine();
			String gene = line.split("\t")[0];
			goldStandard.add(gene+"|142");
			goldStandard.add("142|"+gene);
		}
		br.close();
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
			if (eTab[2].equals("D001943") &&( EvalGene.contains(eTab[0]) || EvalGene.contains(eTab[1]))){
				if (checkCoThr(eTab,thresholdCo) && checkCorThr(value,thresholdCor)){
					poSL.add(eTab[0]+"|"+eTab[1]);
					poSL.add(eTab[1]+"|"+eTab[0]);
				}
			}
		}
		br.close();
		return poSL;
	}
	public HashSet<String> loadCor(String filePath,double[] threshold) throws IOException{
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
			if (eTab[2].equals("D001943") &&( EvalGene.contains(eTab[0]) || EvalGene.contains(eTab[1]))){
				if (checkCorThr(value,threshold)){
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
		HashSet<String> fpSet = new HashSet<>(extractedSet);
		fpSet.removeAll(goldStandard);
		HashSet<String> fnSet = new HashSet<>(goldStandard);
		fnSet.removeAll(extractedSet);
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
	public static void main(String[] args) throws IOException{
		EvaluationPARPsyntheticLethal epsl = new EvaluationPARPsyntheticLethal();
		double[] thresholdCo = {0,0,0,0};
		double[] thresholdCor = new double[]{-1,0.05,-1,1};
		epsl.evaluate(epsl.loadPotentialCoCor("./potentialSLpairsCoSpCorD001943.txt", thresholdCo,thresholdCor),"Cooccurrence & Correlation");
		epsl.evaluate(epsl.loadCor("./potentialSLpairsSpCorD001943.txt",thresholdCor),"Correlation");
	}
}
