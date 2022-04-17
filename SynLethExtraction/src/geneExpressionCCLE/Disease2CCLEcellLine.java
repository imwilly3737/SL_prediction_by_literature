package geneExpressionCCLE;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;

public class Disease2CCLEcellLine {
	private String ccle,outFile;
	private HashMap<String,HashSet<String>> typeSet;
	public Disease2CCLEcellLine(){
		ccle = "../../CCLE/CCLE_Expression_2012-09-29.res";
		outFile = "../../CCLE/ccleCellLine2Disease.txt";
		typeSet = new HashMap<>();
	}
	public void getDiseaseCellLineName() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(ccle));
		String[] eTab = br.readLine().split("\t");
		br.close();
		for (String e:eTab){
			if (e.indexOf("_") != -1){
				String type = e.split("_")[1];
				if (!typeSet.containsKey(type))
					typeSet.put(type,new HashSet<String>());
				typeSet.get(type).add(e);
			}
		}
	}
	public void printMap() throws IOException{
		PrintWriter pw = new PrintWriter(outFile);
		for (String type: typeSet.keySet()){
			pw.print(type);
			for (String e:typeSet.get(type))
				pw.print("\t"+e);
			pw.println();
		}
		pw.close();
	}
	public static void main(String[] args) throws IOException{
		Disease2CCLEcellLine d2ccl = new Disease2CCLEcellLine();
		d2ccl.getDiseaseCellLineName();
		//d2ccl.printMap();	// if you print again, you will rewrite the correct file
		// After print map, manually change the cell line type to its cancer MeSH term
	}
}
