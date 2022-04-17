package sensitisation;

import geneNormalization.GeneIDAndSymbol;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashSet;

public class ProccessSensitisationData {
	private String filePath,outPath;
	private GeneIDAndSymbol gias;
	public ProccessSensitisationData() throws ClassNotFoundException, SQLException{
		/*filePath = "../../Lord, C.J/PARPi_sensitisation.txt";
		outPath = "../../Lord, C.J/PARPi_sensitisation_ID.txt";*/
		filePath = "../../Turner, N.C/PARP_synthetic_lethal.txt";
		outPath = "../../Turner, N.C/PARP_synthetic_lethal_ID.txt";
		gias = new GeneIDAndSymbol();
	}
	public void transFile() throws IOException, SQLException{
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		PrintWriter pw = new PrintWriter(outPath);
		HashSet<String> idSet = new HashSet<>();
		while (br.ready()){
			String line = br.readLine();
			String gene = line.split("\t")[0];
			String id = gias.getIDbySymbol(gene);
			if (id!=null && !idSet.contains(id)){
				pw.println(id+"\t"+line);
				idSet.add(id);
			}
		}
		br.close();
		pw.close();
	}
	static public void main(String[] args) throws ClassNotFoundException, SQLException, IOException{
		ProccessSensitisationData psd = new ProccessSensitisationData();
		psd.transFile();
	}
}
