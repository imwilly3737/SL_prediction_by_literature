package syntheticLethal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashSet;

import correlation.CorrelationBetweenGenes;

public class SLGeneExpressionCorrelation {
	private String potentialSLFile,outFile,specificDisease;
	private CorrelationBetweenGenes cb;
	private HashSet<String> EvalGene;
	public SLGeneExpressionCorrelation(String specificDisease,boolean isCo) throws ClassNotFoundException, IOException, SQLException{
		this.specificDisease = specificDisease;
		EvalGene = new HashSet<>();
		EvalGene.add("142");
		if (isCo){
			potentialSLFile = "./potentialSLpairsCo.txt";
			if (specificDisease!=null)
				outFile = "./potentialSLpairsCoSpCor"+specificDisease+".txt";
			else
				outFile = "./potentialSLpairsCoSpCor.txt";
		}
		else{
			potentialSLFile = "./potentialSLpairs.txt";
			if (specificDisease!=null)
				outFile = "./potentialSLpairsSpCor"+specificDisease+".txt";
			else
				outFile = "./potentialSLpairsSpCor.txt";
		}
		cb = new CorrelationBetweenGenes();
	}
	public void calculateCorrelationEachSL() throws IOException, SQLException{
		BufferedReader br = new BufferedReader(new FileReader(potentialSLFile));
		PrintWriter writer = new PrintWriter(outFile);
		int pairCount = 0;
		while (br.ready()){
			String line = br.readLine();
			String[] eTab = line.split("\t");
			String gene1 = eTab[0], gene2 = eTab[1],disease = eTab[2];
			if (disease.equals(specificDisease) && (EvalGene.contains(gene1) || EvalGene.contains(gene2))){
				cb.setEntities(gene1, gene2, disease);
				cb.spearman();
				if (cb.getGeneXOK()){
					line = line+"\tSpearman:"+cb.getCoe()+"|"+cb.getP();
				}
				cb.pearson();
				if (cb.getGeneXOK()){
					line = line+"\tPearson:"+cb.getCoe()+"|"+cb.getP();
					writer.println(line);
				}
			}
			if (pairCount++ %1000==0)
				System.out.println(pairCount+" pairs done!!");
		}
		br.close();
		writer.close();
	}
	public static void main(String args[]) throws ClassNotFoundException, IOException, SQLException{
		SLGeneExpressionCorrelation slgec = new SLGeneExpressionCorrelation("D001943",false);
		slgec.calculateCorrelationEachSL();
	}
}