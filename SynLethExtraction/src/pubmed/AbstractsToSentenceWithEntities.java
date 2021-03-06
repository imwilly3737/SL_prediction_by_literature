/* Write by Ming-Yu Chien
 * 撠誑Abstract��雿�ubtator鞈��隞兄entence��雿�
 * 蝭����: main�撘�
 */
package pubmed;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import object.MentionLocation;

class AbstractsToSentenceWithEntitiesThread implements Runnable{
	private int part,total;
	private Thread t;
	private String[] docList;
	private String movePath,originalPath;
	public AbstractsToSentenceWithEntitiesThread(int part,int total,String[] docList,String originalPath,String movePath){
		this.part = part;
		this.total = total;
		this.docList = docList;
		this.originalPath = originalPath;
		this.movePath = movePath;
	}
	public void start(){
		if (t == null)
	      {
	         t = new Thread(this,Integer.toString(part));
	         t.start();
	      }
	}
	public void run(){
		int idx = 0;
		
		for (int i = part; i < docList.length; i+=total) {
			String PMID = docList[i];

			try {
				transFile(PMID);
			} catch (Exception e) {
				e.printStackTrace();
				i-=total;
				System.err.println("Retry "+PMID+"!(in thread "+part+")");
				continue;
			}

			idx += 1;
			if (idx%1000 == 0)
				System.out.println(idx + ": " + PMID + ".txt DONE!! (at "+part+" Thread)");
		}
	}
	public void transFile(String PMIDtxt) throws IOException{

		String title,ab,id;
		String filePath = originalPath+ PMIDtxt;
		FileReader fr = new FileReader(filePath);
		BufferedReader br = new BufferedReader(fr);
		
		HashSet<String> totalFile = new HashSet<String>();

		String outPath = movePath+PMIDtxt;
		PrintWriter writer = new PrintWriter(outPath, "UTF-8");
		while (br.ready()) {
			ArrayList<MentionLocation> diseases=new ArrayList<MentionLocation>();
			ArrayList<MentionLocation> genes=new ArrayList<MentionLocation>();
			title = br.readLine();
			ab = br.readLine();
			if (title == null && ab == null)
				break;
			id = title.split("\\|")[0];
			
			title = title.split("\\|")[2];
			if (ab.split("\\|").length == 3)
				ab = ab.split("\\|")[2];
			else
				ab ="";
			while (br.ready()) {
				String text=br.readLine();
				if (!text.contains("\t"))
					break;
				String[] textp=text.split("\t");
				 if (textp[4].equals("Disease")){
					 int start = Integer.parseInt(textp[1]);
					 int end = Integer.parseInt(textp[2]);
					 String menText = textp[3];
					 String[] menID;
					 if (textp.length > 5)
						 menID = textp[5].split("\\|");
					 else
						 menID = new String[]{"null"};
					 MentionLocation men = new MentionLocation(start,end,"Disease",menText,menID,id);
					 diseases.add(men);
				 }
				 else{	// Gene
					 int start = Integer.parseInt(textp[1]);
					 int end = Integer.parseInt(textp[2]);
					 String menText = textp[3];
					 String[] menID;
					 if (textp.length > 5)
						 menID = textp[5].split("\\|");
					 else
						 menID = new String[]{"null"};
					 MentionLocation men = new MentionLocation(start,end,"Gene",menText,menID,id);
					 genes.add(men);
				 }
			}
			
			if (totalFile.contains(id))
				continue;
			else
				totalFile.add(id);
			matchEntities(title+"\n"+ab,diseases,genes,id,"s",writer);
			
		}
		br.close();
		writer.close();
	}
	
	private void matchEntities(String text, ArrayList<MentionLocation> diseases,
			ArrayList<MentionLocation> genes, String id,String prefix,PrintWriter writer) {
		BreakIterator border = BreakIterator.getSentenceInstance(Locale.US);
		border.setText(text);
		int start = border.first();
		int senCount = 0;
		for (int end = border.next(); end != BreakIterator.DONE; start = end, end = border.next()) {
			String outid = id+"_"+senCount++;		// add senCount suffix
			
			String sentenceStr=text.substring(start,end);
			if (sentenceStr.contains("\n"))
				sentenceStr = sentenceStr.replace("\n", "");
			ArrayList<MentionLocation> mDiseases = new ArrayList<MentionLocation>();
			ArrayList<MentionLocation> mGenes = new ArrayList<MentionLocation>();
			for (int i=0;i<diseases.size();i++){
				if (diseases.get(i).start >= start && diseases.get(i).end <= end)
					mDiseases.add(diseases.get(i));
			}
			for (int i=0;i<genes.size();i++){
				if (genes.get(i).start >= start && genes.get(i).end <= end)
					mGenes.add(genes.get(i));
			}
						
			if (mDiseases.size()>0 || mGenes.size()>0){
				writer.println(outid+"|"+prefix+"|"+sentenceStr);
				for (MentionLocation eMDis:mDiseases){
					String dID="";
					for (String e:eMDis.mentionID){
						if (dID.length() == 0)
							dID += e;
						else
							dID += "|"+e;
					}
					writer.println(outid+"\t"+(eMDis.start-start)+"\t"+(eMDis.end-start)+"\t"+eMDis.mentionStr+"\tDisease\t"+dID);
				}
				for (MentionLocation eMG:mGenes){
					String cID="";
					for (String e:eMG.mentionID){
						if (cID.length() == 0)
							cID += e;
						else
							cID += "|"+e;
					}
					writer.println(outid+"\t"+(eMG.start-start)+"\t"+(eMG.end-start)+"\t"+eMG.mentionStr+"\tGene\t"+cID);
				}
				writer.println();
			}
		}
		
	}
}
public class AbstractsToSentenceWithEntities {
	private String movePath,originalPath;
	private String[] fileList;
	
	public AbstractsToSentenceWithEntities() throws IOException{
		
		File dataSetDir = new File("../data/DatasetXML(colon cancer)(with genes and diseases)");	// input files (all pubtator files)
		originalPath = dataSetDir.getAbsolutePath() + File.separator;
		File outSetDir = new File("../data/DatasetXML(colon cancer)(with genes and diseases)(in sentences)");	// output file directory
		if (!dataSetDir.exists()){
			System.err.println("The "+dataSetDir+" directory is not exists!");
			System.exit(0);
		}
		if (outSetDir.exists())
			movePath = outSetDir.getAbsolutePath() + File.separator;
		else {
			outSetDir.mkdir();
			movePath = outSetDir.getAbsolutePath() + File.separator;
		}
		fileList = dataSetDir.list();
		
	}
	
	
	static public void main(String args[]) throws IOException{

		AbstractsToSentenceWithEntities atswe = new AbstractsToSentenceWithEntities();
		
		/* Multi-thread */
		int totalThread = 4;
		AbstractsToSentenceWithEntitiesThread [] runnableList = new AbstractsToSentenceWithEntitiesThread[totalThread];
		for (int i = 0 ;i< totalThread ;i++){
			runnableList[i] = new AbstractsToSentenceWithEntitiesThread(i,totalThread,atswe.fileList,atswe.originalPath,atswe.movePath);
			runnableList[i].start();
		}
		
		/*==================================================================================*/
	}
}
