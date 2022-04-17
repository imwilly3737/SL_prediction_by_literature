package errorAnalysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

import correlation.CorrelationBetweenGenes;
import object.SynLethPair;

public class CheckGenomeRNAiGeneExpression {
	private String synLethDBFile;
	private CorrelationBetweenGenes cbg;
	public CheckGenomeRNAiGeneExpression() throws ClassNotFoundException, IOException, SQLException{
		synLethDBFile = "../../SynLethDB/sl_human";
		cbg = new CorrelationBetweenGenes();
	}
	public void checkCorrelation() throws IOException, SQLException{
		BufferedReader br = new BufferedReader(new FileReader(synLethDBFile));
		br.readLine();
		while (br.ready()){
			SynLethPair slp = new SynLethPair(br.readLine(),"\t");
			if (slp.Evidence.indexOf("GenomeRNAi") != -1 && (slp.GeneAid.equals("8883") || slp.GeneBid.equals("8883"))){
				cbg.setEntities(slp.GeneAid, slp.GeneBid, "ALL");
				if (cbg.getGeneXOK()){
					cbg.spearman();
					System.out.println(slp.GeneAid+","+slp.GeneBid+","+cbg.getCoe()+","+cbg.getP());
				}
			}
		}
		br.close();
	}
	static public void main(String[] args) throws ClassNotFoundException, IOException, SQLException{
		CheckGenomeRNAiGeneExpression cgrge = new CheckGenomeRNAiGeneExpression();
		cgrge.checkCorrelation();
	}
}
