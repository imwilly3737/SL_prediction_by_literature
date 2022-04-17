package essentialGene;

import geneNormalization.GeneIDAndSymbol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

public class EssentialGeneAndCellLineNormalization {
	private String mapFile;
	private String egFile;
	private String outFile; 
	private HashMap<String,String> cellLineMap;
	private HashMap<String,String> diseaseMap;
	private GeneIDAndSymbol gias;
	public EssentialGeneAndCellLineNormalization() throws ClassNotFoundException, SQLException{
		mapFile = "../../COLT-Cancer/CancerOfCellLine.tsv";
		egFile = "../../COLT-Cancer/EssentialGene.txt";
		outFile = "../../COLT-Cancer/EssentialGeneWithCancer.txt";
		cellLineMap = new HashMap<>();
		diseaseMap = new HashMap<>();
		gias = new GeneIDAndSymbol();
	}
	public void loadMap() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(mapFile));
		while (br.ready()){
			String line = br.readLine();
			String cellLine = line.split("\t")[0];
			cellLine = cellLine.replaceAll(" ", ".");
			cellLine = cellLine.replaceAll("-", ".");
			String cancer = line.split("\t")[1]+" cancer";
			cellLineMap.put(cellLine, cancer);
		}
		br.close();
		diseaseMap.put("Breast cancer", "D001943");
		diseaseMap.put("Colon cancer", "D015179");
		diseaseMap.put("Ovarian cancer", "D010051");
		diseaseMap.put("Pancreatic cancer", "D010190");
	}
	public void curateCancerType() throws IOException, SQLException{
		BufferedReader br = new BufferedReader(new FileReader(egFile));
		BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
		int lineNum = 0;
		HashSet<String> bSet = new HashSet<>(),OSet=new HashSet<>(),PSet=new HashSet<>(),CSet=new HashSet<>();
		while(br.ready()){
			String line = br.readLine();
			String cellLine = line.split("\t")[1];
			String geneSymbol = line.split("\t")[0];
			String geneID = gias.getIDbySymbol(geneSymbol);
			String cancer = cellLineMap.get(cellLine);
			String cancerID = diseaseMap.get(cancer);
			bw.write(line+"\t"+cancer+"\t"+geneID+"\t"+cancerID+"\n");
			if ((lineNum++)%1000 == 0)
				System.out.println(lineNum+":"+line+"\t"+cancer+"\t"+geneID+"\t"+cancerID+"\n");
			if (cancerID != null){
				if (cancerID.equals("D001943"))
					bSet.add(geneID);
				else if (cancerID.equals("D010051"))
					OSet.add(geneID);
				else if (cancerID.equals("D010190"))
					PSet.add(geneID);
				else if (cancerID.equals("D015179"))
					CSet.add(geneID);
			}
		}
		System.out.println(bSet.size());
		System.out.println(OSet.size());
		System.out.println(PSet.size());
		System.out.println(CSet.size());
		br.close();
		bw.close();
	}
	static public void main(String[] args) throws ClassNotFoundException, SQLException, IOException{
		EssentialGeneAndCellLineNormalization egacln = new EssentialGeneAndCellLineNormalization();
		egacln.loadMap();
		egacln.curateCancerType();
	}
}
