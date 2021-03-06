package essentialGene;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;

class CollectSentencesThread implements Runnable{
	private int part,total;
	private String[] fileList;
	private String originalPath,movePath,essentialGenePath;
	private Thread t;
	private HashSet<String> EGSet;
	private BufferedWriter writer;
	private boolean isEssential;	// are these sentences essential or not?
	// if you want extract all diseases, delete this variable
	private HashMap<String,String> diseaseMap; //extract disease
	public CollectSentencesThread(int i, int totalThread, String[] fileList,
			String originalPath, String movePath, boolean isEssential) {
		this.part = i;
		this.total = totalThread;
		this.fileList = fileList;
		this.originalPath = originalPath;
		this.essentialGenePath = "../../COLT-Cancer/EssentialGeneWithCancer.txt";
		this.movePath = movePath;
		this.EGSet = new HashSet<>();
		this.isEssential = isEssential;
		
		diseaseMap = new HashMap<>();
		
		diseaseMap.put("Colon cancer", "D015179");
		/*diseaseMap.put("Breast cancer", "D001943");
		diseaseMap.put("Ovarian cancer", "D010051");
		diseaseMap.put("Pancreatic cancer", "D010190");*/
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
		try {
			loadEssentialGene();
			String outPath = movePath+"Thread"+part+".txt";
			writer = new BufferedWriter(new FileWriter(outPath));
		} catch (IOException e1) {
			e1.printStackTrace();
			System.err.println("ERROR: Read essential genes file!!");
		}
		for (int i = part; i < fileList.length; i+=total) {
			String PMID = fileList[i];

			try {
				collectSentence(PMID);
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
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void loadEssentialGene() throws IOException{
		if (this.EGSet.size()>0)
			return;
		BufferedReader br = new BufferedReader(new FileReader(essentialGenePath));
		while (br.ready()){
			String line = br.readLine();
			String geneID = line.split("\t")[4];
			String diseaseID = line.split("\t")[5];
			if(!geneID.equals("null") && !diseaseID.equals("null")) 
				EGSet.add(geneID+"|"+diseaseID);
		}
		br.close();
	}
	public void collectSentence(String PMIDtxt) throws IOException{
		String filePath = originalPath+ PMIDtxt;
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		
		HashSet<String> GeneIDSet = new HashSet<>();
		HashSet<String> DisIDSet = new HashSet<>();
		HashSet<String> lineSet = new HashSet<>();
		while (br.ready()) {
			String senLine = br.readLine();
			if (!senLine.contains("|"))
				break;
			while (br.ready()){
				String entityLine = br.readLine();
				if (!entityLine.contains("\t")){	//entity line end, check essential gene
					if (GeneIDSet.size()>0 && DisIDSet.size()>0){
						HashSet<String> idToPrint = new HashSet<String>();
						for (String gID:GeneIDSet)
							for (String dID:DisIDSet){
								if (isEssential && EGSet.contains(gID+"|"+dID)){
									idToPrint.add(gID);
									idToPrint.add(dID);
								}
								//else if (!isEssential && !EGSet.contains(gID+"|"+dID) && diseaseMap.containsValue(dID)){
								else if (!isEssential && !EGSet.contains(gID+"|"+dID)){
									idToPrint.add(gID);
									idToPrint.add(dID);
								}
							}
						if (idToPrint.size()>0){
							writer.write(senLine+"\n");
							for (String eLine:lineSet)
								if (idToPrint.contains(eLine.split("\t")[5])){
									writer.write(eLine+"\n");
								}
							writer.newLine();
						}
					}
					GeneIDSet.clear();
					DisIDSet.clear();
					lineSet.clear();
					break;
				}
				String entityID = entityLine.split("\t")[5];
				if (entityLine.split("\t")[4].equals("Gene")){
					GeneIDSet.add(entityID);
					lineSet.add(entityLine);
				}
				else if (entityLine.split("\t")[4].equals("Disease")){
					DisIDSet.add(entityID);
					lineSet.add(entityLine);
				}
			}
		}
		
		br.close();
	}
	
}
public class EssentialGenePatternSentence {
	private String movePath,originalPath;
	private String[] fileList;
	public EssentialGenePatternSentence(){
		File dataSetDir = new File("../data/DatasetXML(colon cancer)(with genes and diseases)(in sentences)");
		originalPath = dataSetDir.getAbsolutePath() + File.separator;
		// File outSetDir = new File("../data/DatasetXML(colon cancer)(pattern sentences)(essential)");
		File outSetDir = new File("../data/DatasetXML(colon cancer)(pattern sentences)(not essential)");
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

		EssentialGenePatternSentence atswe = new EssentialGenePatternSentence();
		
		/* Multi-thread */
		int totalThread = 4;
		CollectSentencesThread [] runnableList = new CollectSentencesThread[totalThread];
		for (int i = 0 ;i< totalThread ;i++){
			// runnableList[i] = new CollectSentencesThread(i,totalThread,atswe.fileList,atswe.originalPath,atswe.movePath,true);
			runnableList[i] = new CollectSentencesThread(i,totalThread,atswe.fileList,atswe.originalPath,atswe.movePath,false);
			runnableList[i].start();
		}
		
		/*==================================================================================*/
	}
}
