package cosmic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

import object.CosmicMutantGene;

public class CheckCosmicTumor {
	private String cosmicFile;
	public CheckCosmicTumor(){
		cosmicFile = "../../Cosmic/CosmicMutantExport.tsv";
	}
	public void tumorSet() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(cosmicFile));
		br.readLine();
		HashSet<String> tumors = new HashSet<>();
		while (br.ready()){
			CosmicMutantGene cmg = new CosmicMutantGene(br.readLine(),"\t");
			tumors.add(cmg.Primary_site);
		}
		br.close();
		for (String e:tumors)
			System.out.println(e);
	}
	static public void main(String[] args) throws IOException{
		CheckCosmicTumor cct = new CheckCosmicTumor();
		cct.tumorSet();
	}
}
