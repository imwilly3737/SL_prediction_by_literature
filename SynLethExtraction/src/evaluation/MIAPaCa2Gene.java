package evaluation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

import object.SynLethPair;

public class MIAPaCa2Gene {
	private String sldbPath;
	public MIAPaCa2Gene(){
		sldbPath = "../../SynLethDB/sl_human";
	}
	public void getCommonGene() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(sldbPath));
		br.readLine(); //skip first line
		HashSet<String> commonGene = new HashSet<>();
		while (br.ready()){
			SynLethPair slp = new SynLethPair(br.readLine(),"\t");
			if (slp.Evidence.equals("GenomeRNAi") && slp.Disease.equals("MIAPaCa-2")){
				if (slp.GeneBid.equals("203"))
					commonGene.add(slp.GeneAid);
				if (slp.GeneAid.equals("203"))
					commonGene.add(slp.GeneBid);
			}
		}
		br.close();
		for (String e:commonGene){
			System.out.println(e);
		}
	}
	static public void main(String[] args) throws IOException{
		MIAPaCa2Gene mpcg = new MIAPaCa2Gene();
		mpcg.getCommonGene();
	}
}
