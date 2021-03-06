package essentialGene;

import geneNormalization.GeneIDAndSymbol;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

public class EvaluationEssentialGene {
	private String potentialFile,essentialFile,specificD,mutantFile;
	private HashSet<String> EGSet,MGSet;
	private HashMap<String,String> diseaseToCCLE;
	private double threshold;
	private GeneIDAndSymbol gias;
	public EvaluationEssentialGene(String disease,double threshold) throws IOException, ClassNotFoundException, SQLException{
		potentialFile = "potentialEssentialGene(new trigger)(0).txt";
		essentialFile = "../../COLT-Cancer/EssentialGeneWithCancer.txt";
		mutantFile = "../../NERsuite/cell_line_cancer_mutations.txt";
		EGSet = new HashSet<String>();
		MGSet = new HashSet<String>();
		diseaseToCCLE = new HashMap<>();
		diseaseToCCLE.put("D010190","PANCREAS");
		diseaseToCCLE.put("D001943","BREAST");
		diseaseToCCLE.put("D010051","OVARY");
		specificD = disease;
		this.threshold = threshold;
		loadEssentialGene();
		gias = new GeneIDAndSymbol();
		loadMutantGene();
	}
	public void loadEssentialGene() throws IOException{
		if (this.EGSet.size()>0)
			return;
		BufferedReader br = new BufferedReader(new FileReader(essentialFile));
		while (br.ready()){
			String line = br.readLine();
			String geneID = line.split("\t")[4];
			String diseaseID = line.split("\t")[5];
			if(!geneID.equals("null") && !diseaseID.equals("null") && diseaseID.equals(specificD))
					EGSet.add(geneID);
		}
		br.close();
	}
	public void loadMutantGene() throws IOException{
		if (this.MGSet.size()>0)
			return;
		BufferedReader br = new BufferedReader(new FileReader(mutantFile));
		while (br.ready()){
			String line = br.readLine();
			String geneID = line.split("\t")[1];
			String disease = line.split("\t")[3];
			if(!geneID.equals("null") && disease.equals(diseaseToCCLE.get(specificD)))
					MGSet.add(geneID);
		}
		br.close();
	}
	public void evaluation() throws IOException, SQLException {
		if (this.EGSet.size()==0){
			System.out.println("No Essential Gene Loaded: use \"loadEssentialGene()\" function");
			return;
		}
		BufferedReader br = new BufferedReader(new FileReader(potentialFile));
		int tp=0,fn=0,fp=0;
		HashSet<String> tpGene = new HashSet<String>();
		HashSet<String> fpGene = new HashSet<String>();
		while(br.ready()){
			String line = br.readLine();
			String geneID = line.split("\t")[0];
			double score = Double.parseDouble(line.split("\t")[1]);
			if (score >= threshold){
				if (EGSet.contains(geneID) && !tpGene.contains(geneID)){
					tp++;
					tpGene.add(geneID);
				}else if (!EGSet.contains(geneID) && !fpGene.contains(geneID) && gias.getSymbolbyID(geneID)!=null && !MGSet.contains(geneID)){
					fp++;
					fpGene.add(geneID);
					System.out.println(geneID);
				}
			}
				
		}
		br.close();
		EGSet.retainAll(getAllGeneInAbstract());
		fn = EGSet.size() - tpGene.size();
		System.out.println("Disease: "+specificD);
		System.out.println("True positive: "+tp);
		System.out.println("False positive: "+fp);
		System.out.println("False negative: "+fn);
		double precision = (double)tp/(tp+fp);
		double recall = (double)tp/(tp+fn);
		double fscore = 2*precision*recall/(precision+recall);
		System.out.println("Precision: "+precision);
		System.out.println("Recall: "+ recall);
		System.out.println("Fscore: "+fscore);
	}
	public HashSet<String> getAllGeneInAbstract() throws IOException{
		HashSet<String> geneIDSet;
		geneIDSet = getGeneInDir("../data/DatasetXML(pattern sentences)");
		geneIDSet.addAll(getGeneInDir("../data/DatasetXML(pattern sentences)(not essential)"));
		return geneIDSet;
	}
	private HashSet<String> getGeneInDir(String dirPath) throws IOException {
		HashSet<String> temp = new HashSet<>();
		File dataSetDir = new File(dirPath);
		String[] fileList = dataSetDir.list();
		String originalPath = dataSetDir.getAbsolutePath() + File.separator;
		for (String eFile: fileList){
			BufferedReader br = new BufferedReader(new FileReader(originalPath+eFile));
			HashSet<String> eGSet = new HashSet<>();
			HashSet<String> eDSet = new HashSet<>();
			while (br.ready()){
				String senLine = br.readLine();
				if (senLine.split("\\|").length == 1)
					break;
				String id = senLine.split("\\|")[0];
				senLine = senLine.split("\\|")[2];
				while (br.ready()){
					String entityLine = br.readLine();
					if (!entityLine.contains("\t")){	//entity line end, check essential gene
						temp.addAll(getGeneInSen(eGSet,eDSet));
						
						eGSet.clear();
						eDSet.clear();
						break;
					}
					if (entityLine.split("\t")[4].equals("Gene")){
						eGSet.add(entityLine);
					}
					else{
						eDSet.add(entityLine);
					}
				}
			}
			br.close();
			System.out.println(eFile+" DONE!");
		}
		return temp;
	}
	private HashSet<String> getGeneInSen(HashSet<String> eGSet,HashSet<String> eDSet) {
		HashSet<String> temp = new HashSet<>();
		for (String eD:eDSet){
			String diseaseID = eD.split("\t")[5];
			if (diseaseID.equals(specificD)){
				for (String eG:eGSet){
					String geneID = eG.split("\t")[5];
					temp.add(geneID);
				}
				break;
			}
		}
		return temp;
	}
	public static void main(String args[]) throws IOException, ClassNotFoundException, SQLException{
		//breast cancer = "D001943";
		//pancreatic cancer = "D010190";
		//ovarian cancer="D010051";
		EvaluationEssentialGene eeg = new EvaluationEssentialGene("D001943",1);
		eeg.evaluation();
	}
}
