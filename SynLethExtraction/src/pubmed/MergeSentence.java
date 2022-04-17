package pubmed;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MergeSentence {
	public static void main(String args[]) throws IOException{
		File outSetDir = new File("DatasetSentence(cell line AND cancer)");
		File allSentencePart = new File("./allSentencesPart1.txt");
		BufferedWriter fileOUT = new BufferedWriter(new FileWriter(allSentencePart));
		int i = 0,part=1;
		for (String eachFile: outSetDir.list()){
			if (i>=200000){
				i=0;
				part++;
				fileOUT.close();
				fileOUT = new BufferedWriter(new FileWriter("./allSentencesPart"+part+".txt"));
			}
			BufferedReader br = new BufferedReader(new FileReader(outSetDir.getAbsolutePath()+File.separator+eachFile));
			while (br.ready()){
				String newLine = br.readLine();
				fileOUT.write(newLine+"\n");
				i++;
			}
			br.close();
			System.out.println(eachFile +" done!!");
		}
		fileOUT.close();
	}
}
