package extractAbstractFromPubmed;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class ExtractAbstractsThread implements Runnable{
	private int partNum;
	private int totalThread;
	private String[] idList;
	private String movePath;
	private String baseFetch;
	private Thread t;
	public ExtractAbstractsThread(int part,int total,String[] idList,String movePath){
		partNum = part;
		totalThread = total;
		this.idList = idList;
		this.movePath = movePath;
		baseFetch = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&retmode=xml&id=";
	}
	public void start(){
		if (t == null)
	      {
	         t = new Thread(this,Integer.toString(partNum));
	         t.start();
	      }
	}
	public void run(){
		String fetch,temp;
		int idx = 0;
		
		for (int i = partNum; i < idList.length; i+=totalThread) {
			String PMID = idList[i];
			fetch = baseFetch + PMID;

			try {
				File fileWrite = new File(movePath + PMID + ".txt");
				if (fileWrite.exists())
					continue;
				
				URL urlFetch = new URL(fetch);
				URLConnection connectFetch = urlFetch.openConnection();

				BufferedReader fileIN = new BufferedReader(new InputStreamReader(connectFetch.getInputStream(), "UTF-8"));
				BufferedWriter fileOUT = new BufferedWriter(new FileWriter(movePath + PMID + ".txt"));

				while ((temp = fileIN.readLine()) != null) {
					fileOUT.write(temp);
					fileOUT.newLine();
				}

				fileIN.close();
				fileOUT.close();
			} catch (Exception e) {
				e.printStackTrace();
				i-=totalThread;
				continue;
			}

			idx += 1;
			if (idx%10 == 0)
				System.out.println(idx + ": " + PMID + ".txt DONE!! (at "+partNum+" Thread)");
		}
		System.out.println("thread "+partNum+" ends!");
	}
	
}
