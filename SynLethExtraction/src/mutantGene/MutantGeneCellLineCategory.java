package mutantGene;

import geneNormalization.GeneIDAndSymbol;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

import object.CCLECategory;
import object.CosmicMutantGene;

public class MutantGeneCellLineCategory {
	private String categoryFile,mutantGeneFileCCLE,mutantGeneFileCosmic,outFile,cancerTypeFileCCLE,cancerTypeFileCOSMIC;
	public HashMap<String,CCLECategory> allCCLE;
	public HashMap<String,String> cancerType2ID;
	private GeneIDAndSymbol gias;
	public MutantGeneCellLineCategory() throws IOException, ClassNotFoundException, SQLException{
		categoryFile = "../../NERsuite/cell_line_dictionary_resources.txt";
		mutantGeneFileCCLE = "../../NERsuite/cell_line_dictionary_mutations.txt";
		mutantGeneFileCosmic = "../../Cosmic/CosmicMutantExport.tsv";
		cancerTypeFileCCLE = "../../NERsuite/CancerTypetoID.txt";
		cancerTypeFileCOSMIC = "../../Cosmic/DiseaseType2ID.txt";
		outFile = "../../NERsuite/cell_line_cancer_mutations.txt";
		allCCLE = new HashMap<>();
		cancerType2ID = new HashMap<>();
		gias = new GeneIDAndSymbol();
		loadCancerType2ID();
		readCCLE();
	}
	private void loadCancerType2ID() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(cancerTypeFileCCLE));
		while (br.ready()){
			String line = br.readLine();
			String[] eTab = line.split("\t");
			if (!eTab[1].equals("?"))
				cancerType2ID.put(eTab[0], eTab[1]);
		}
		br.close();
		br  = new BufferedReader(new FileReader(cancerTypeFileCOSMIC));
		while (br.ready()){
			String line = br.readLine();
			String[] eTab = line.split("\t");
			if (!eTab[1].equals("?"))
				cancerType2ID.put(eTab[0], eTab[1]);
		}
		br.close();
	}
	private void readCCLE() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(categoryFile));
		while (br.ready()){
			String line = br.readLine();
			String[] eTab = line.split("\t");
			if (eTab[1].equals("CCLE")){
				String cellLineName = eTab[2].substring(0, eTab[2].indexOf("_"));
				String cancer = eTab[2].substring(eTab[2].indexOf("_")+1);
				cancer = cancer.replace("_", " ");
				allCCLE.put(eTab[0],new CCLECategory(eTab[0],eTab[1],cellLineName,cancer,cancerType2ID.get(cancer)));
			}
		}
		br.close();
	}
	public void cellLineToCancer() throws IOException, SQLException{
		BufferedReader br = new BufferedReader(new FileReader(mutantGeneFileCCLE));
		PrintWriter pw  = new PrintWriter(outFile);
		int lineCount = 0;
		while (br.ready()){
			String line = br.readLine();
			String id = line.split("\t")[0];
			if (allCCLE.containsKey(id))
				pw.println(line+"\t"+allCCLE.get(id).cancer+"\t"+allCCLE.get(id).cancerid);
			if (lineCount++ % 1000 == 0)
				System.out.println("CCLE: "+lineCount+" line done!!");
		}
		br.close();
		lineCount = 0;
		br = new BufferedReader(new FileReader(mutantGeneFileCosmic));
		br.readLine();	//skip first line
		while (br.ready()){
			CosmicMutantGene cmg = new CosmicMutantGene(br.readLine(),"\t");
			String geneID = gias.getIDbySymbol(cmg.Gene_name);
			if (geneID != null && cancerType2ID.containsKey(cmg.Primary_site))
				pw.println(cmg.ID_tumour+"\t"+geneID+"\tCOSMIC\t"+cmg.Primary_site+"\t"+cancerType2ID.get(cmg.Primary_site));
			if (lineCount++ % 1000 == 0)
				System.out.println("COSMIC: "+lineCount+" line done!!");
		}
		br.close();
		pw.close();
	}
	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException{
		MutantGeneCellLineCategory mgclc = new MutantGeneCellLineCategory();
		mgclc.cellLineToCancer();
	}
}
