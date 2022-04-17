package essentialGene;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class ExtractSentencesFromGeneAndDisease {
	private String allSentencesFile;
	private String specificD, sepcificG;
	private HashMap<Integer,String> indexToGene,indexToDisease;
	public ExtractSentencesFromGeneAndDisease(String disease,String gene) throws IOException{
		allSentencesFile = "./allSentences(" + disease + ")(" + gene + ").txt";
		specificD = disease;
		sepcificG = gene;
	}
	
	public void extractGene() throws IOException{
		//extractGeneInDir("../data/DatasetXML(pattern sentences)");
		//extractGeneInDir("../data/DatasetXML(pattern sentences)(not essential)(only 4 cancers)");
		extractSentenceInDir("../data/DatasetXML(pattern sentences)(not essential)");
	}
	private void extractSentenceInDir(String dirPath) throws IOException{
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
				ArrayList<String> senLines = new ArrayList<String>();
				senLines.add(senLine);
				while (br.ready()){
					String entityLine = br.readLine();
					senLines.add(entityLine);
					if (!entityLine.contains("\t")){	//entity line end, check essential gene
						if (eGSet.contains(sepcificG) && eDSet.contains(specificD))
							printSentences(allSentencesFile, senLines);
						eGSet.clear();
						eDSet.clear();
						break;
					}
					if (entityLine.split("\t")[4].equals("Gene")){
						eGSet.add(entityLine.split("\t")[5]);
					}
					else if (entityLine.split("\t")[4].equals("Disease")){
						eDSet.add(entityLine.split("\t")[5]);
					}
				}
			}
			br.close();
			System.out.println(eFile+" DONE!");
		}
	}
	private void printSentences(String outFile, ArrayList<String> lines) throws IOException {
		BufferedWriter senWriter = new BufferedWriter(new FileWriter(outFile, true));
		for (String line: lines){
			System.out.println(line);
			senWriter.write(line + "\n");
		}
		senWriter.close();
		
	}
	static public void main(String args[]) throws IOException{
		//breast cancer = "D001943";
		//pancreatic cancer = "D010190";
		//ovarian cancer="D010051";
		//colon cancer="D003110"; or "D015179";
		ExtractSentencesFromGeneAndDisease esfgd = new ExtractSentencesFromGeneAndDisease("D003110","3732");
		esfgd.extractGene();
	}
}
