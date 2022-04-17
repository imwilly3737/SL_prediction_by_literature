package pubmed;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

class ExtractPubtatorThread implements Runnable{
	private int partNum;
	private int totalThread;
	private String[] docList;
	private String movePath;
	private Thread t;
	private String baseFetch;
	private BufferedWriter logOUT;
	
	public ExtractPubtatorThread(int part,int total,String[] docList,String movePath,BufferedWriter log) throws IOException{
		partNum = part;
		totalThread = total;
		this.docList = docList;
		this.movePath = movePath;
		baseFetch = "https://www.ncbi.nlm.nih.gov/CBBresearch/Lu/Demo/RESTful/tmTool.cgi/Gene/";
		logOUT = log;
	}
	public void start(){
		if (t == null)
	      {
	         t = new Thread(this,Integer.toString(partNum));
	         t.start();
	      }
	}
	public void run(){
		for (int i = partNum; i < docList.length; i+=totalThread) {
			String PMID = docList[i].replace(".txt", "");
			String pubtatorStr = "";
			if (((i-partNum)/totalThread)%100 == 0)
				System.out.println("Checking the " + i + "th PMID " + PMID + " (at "+partNum+" Thread)");
			File fileWrite = new File(movePath + PMID + ".txt");
			if (fileWrite.exists())
				continue;
			try {
				pubtatorStr = extractPMID(PMID);
			
				if (checkGeneNER(pubtatorStr,PMID)){
					BufferedWriter fileOUT = new BufferedWriter(new FileWriter(fileWrite));
					fileOUT.write(pubtatorStr);
					fileOUT.close();
				}
			} catch (IOException e) {
				if (!e.toString().contains("FileNotFoundException")){
					e.printStackTrace();
					System.err.println(PMID+" retry!! (at "+partNum+" Thread)");
					i-=totalThread;
					try {
						Thread.sleep(30000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					continue;
				}
			}
			if (((i-partNum)/totalThread) %100 == 0){
				System.out.println(i + ": " + PMID + ".txt DONE!! (at "+partNum+" Thread)");
				writeLog(i + ": " + PMID + ".txt DONE!! (at "+partNum+" Thread)");
			}
		}
	}
	
	public String extractPMID(String id) throws IOException{
		String fetch = baseFetch+id+"/PubTator/", temp, allStr = "";
		URL urlFetch = new URL(fetch);
		URLConnection connectFetch = urlFetch.openConnection();

		BufferedReader fileIN = new BufferedReader(new InputStreamReader(connectFetch.getInputStream(), "UTF-8"));
		while ((temp = fileIN.readLine()) != null) {
			allStr += temp+"\n";
		}
		fileIN.close();
		return allStr.trim();
	}
	public boolean checkGeneNER(String pubtator,String id){
		if (pubtator.contains(id+"\t"))
			return true;
		else
			return false;
	}
	
	public void writeLog(String message){
		try {
			logOUT.append(message+"\n");
			logOUT.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

public class ExtractAbstractsfromPubtatorGene {
	private String movePath;
	private String[] fileList;
	private String logFile;
	private BufferedWriter logOUT;
	public ExtractAbstractsfromPubtatorGene() throws IOException{
		File dataSetDir = new File("../data/DatasetXML(colon cancer)");
		File outSetDir = new File("../data/DatasetXML(colon cancer)(with genes)");
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
		
		logFile = "./extractPubtatorLog.txt";
		logOUT = new BufferedWriter(new FileWriter(logFile));
	}
	
	static public void main(String[] args) throws IOException{
		ExtractAbstractsfromPubtatorGene eafp = new ExtractAbstractsfromPubtatorGene();
		
		/* Multi-thread */
		int totalThread = 4;
		ExtractPubtatorThread [] runnableList = new ExtractPubtatorThread[totalThread];
		for (int i = 0 ;i< totalThread ;i++){
			runnableList[i] = new ExtractPubtatorThread(i,totalThread,eafp.fileList,eafp.movePath,eafp.logOUT);
			runnableList[i].start();
		}
		
		/*==================================================================================*/
	}
}
