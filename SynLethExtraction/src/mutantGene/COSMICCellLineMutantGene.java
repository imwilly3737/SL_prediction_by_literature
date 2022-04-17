package mutantGene;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import object.CosmicMutantGene;

public class COSMICCellLineMutantGene {
	private String COSMICFile;
	public COSMICCellLineMutantGene(){
		COSMICFile = "../../Cosmic/CosmicMutantExport.tsv";
	}
	public void checkMutantbyGeneCellLine(String geneName,String cellLineName) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(COSMICFile));
		while(br.ready()){
			CosmicMutantGene cmg = new CosmicMutantGene(br.readLine(),"\t");
			if (cmg.Gene_name.equals(geneName) && cmg.Sample_name.equals(cellLineName)){
				System.out.println("YES");
			}
		}
		br.close();
	}
	public static void main(String args[]) throws IOException{
		COSMICCellLineMutantGene cclmg =  new COSMICCellLineMutantGene();
		cclmg.checkMutantbyGeneCellLine("NRAS", "CC20");
	}
}
