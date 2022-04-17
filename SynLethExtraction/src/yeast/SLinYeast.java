package yeast;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class SLinYeast {
	public String inPath = "./yeast_interaction_data.tab";
	public String outPath ="./yeast_SL_data.tab";
	public String outFAPath = "./yeast_SL_simple.tab";
	public static void main(String args[]) throws IOException{
		SLinYeast sy = new SLinYeast();
		sy.extractFewAttributes();
		
	}
	
	public void extract() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(inPath));
		PrintWriter writer = new PrintWriter(outPath, "UTF-8");
		while (br.ready()){
			String line = br.readLine();
			if (line.split("\t")[4].equals("Synthetic Lethality")){
				writer.println(line);
			}
		}
		writer.close();
		br.close();
	}
	
	public void extractFewAttributes() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(outPath));
		PrintWriter writer = new PrintWriter(outFAPath, "UTF-8");
		while (br.ready()){
			String line = br.readLine();
			String[] lineTab = line.split("\t");
			String nline = lineTab[0]+"\t"+lineTab[1]+"\t"+lineTab[2]+"\t"+lineTab[3]+"\t";
			nline += lineTab[10].substring(lineTab[10].indexOf("PMID:")+5);	//PMID
			writer.println(nline);
			
		}
		writer.close();
		br.close();
	}
}
