package mutantGene;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class GetCellLineNameByGeneAndCancer {
	private String cellLineFile, mutantFile;
	private HashMap<String,String> cellLineMap;
	public GetCellLineNameByGeneAndCancer() throws IOException{
		cellLineFile = "../../NERsuite/cell_line_dictionary_synonyms.txt";
		mutantFile = "../../NERsuite/cell_line_cancer_mutations.txt";
		cellLineMap = new HashMap<>();
		loadCellLineMap();
	}
	private void loadCellLineMap() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(cellLineFile));
		br.readLine();	// skip first line
		while (br.ready()){
			String[] eTab = br.readLine().split("\t");
			String id =eTab[0];
			String name = eTab[1];
			cellLineMap.put(id, name);
		}
		br.close();
		cellLineMap.get("5337");
	}
	public void printCellLinebyGeneCancer(String inputG,String inputC) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(mutantFile));
		br.readLine();	// skip first line
		int count = 0;
		while (br.ready()){
			String[] eTab = br.readLine().split("\t");
			String cellLine = eTab[0];
			String gene = eTab[1];
			String resource = eTab[2];
			String cancer = eTab[4];
			if (gene.equals(inputG) && cancer.equals(inputC)){
				if (resource.equals("CCLE"))
					System.out.println(cellLineMap.get(cellLine));
				//else
					//System.out.println(cellLine);
				count++;
			}
		}
		br.close();
		System.out.println(count);
	}
	static public void main(String[] args) throws IOException{
		GetCellLineNameByGeneAndCancer gclnbgac = new GetCellLineNameByGeneAndCancer();
		gclnbgac.printCellLinebyGeneCancer("3845", "D003110");
	}
}
