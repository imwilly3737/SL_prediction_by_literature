package pairRanking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map.Entry;

import object.MentionLocation;

class GeneCooccurrenceThread implements Runnable{
	private int part,total;
	private Thread t;
	private String[] docList;
	private String movePath,originalPath;
	private HashMap<String,Integer> coSen,coAb;
	private BreakIterator border;
	public GeneCooccurrenceThread(int part,int total,String[] docList,String originalPath,String movePath){
		this.part = part;
		this.total = total;
		this.docList = docList;
		this.originalPath = originalPath;
		this.movePath = movePath;
		coSen = new HashMap<>();
		coAb = new HashMap<>();
		border = BreakIterator.getSentenceInstance(Locale.US);
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
		try {
			printMap();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void transFile(String PMIDtxt) throws IOException{
		String title,ab,id;
		String filePath = originalPath+ PMIDtxt;
		FileReader fr = new FileReader(filePath);
		BufferedReader br = new BufferedReader(fr);
		
		HashSet<String> totalFile = new HashSet<String>();

		while (br.ready()) {
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
				 if (!textp[4].equals("Disease")){		// chemical
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
			countCooccurrence(title+"\n"+ab,genes,id);
			
		}
		br.close();
	}
	private void countCooccurrence(String text,ArrayList<MentionLocation> genes, String id) {
		// add co-occurrence in abstract-level
		for (int i=0;i<genes.size();i++)
			for (int j=i+1;j<genes.size();j++)
				for (String ei:genes.get(i).mentionID)
					for (String ej:genes.get(j).mentionID)
						if (!ei.equals(ej)){
							String key = ei+"|"+ej;
							if (coAb.containsKey(key))
								coAb.put(key, coAb.get(key)+1);
							else
								coAb.put(key, 1);
							key = ej+"|"+ei;
							if (coAb.containsKey(key))
								coAb.put(key, coAb.get(key)+1);
							else
								coAb.put(key, 1);
						}
		
		// add co-ooccurrence in sentence-level
		
		border.setText(text);
		int start = border.first();
		int boundaryS = 0, boundaryE = 0;
		for (int end = border.next(); end != BreakIterator.DONE; start = end, end = border.next()) {
			boundaryS = boundaryE;
			for (int i=boundaryS;i<genes.size();i++)
				if (genes.get(i).start> end){
					boundaryE = i;
					break;
				}
			for (int i=boundaryS;i<boundaryE;i++)
				for (int j=i+1;j<boundaryE;j++)
					for (String ei:genes.get(i).mentionID)
						for (String ej:genes.get(j).mentionID)
							if (!ei.equals(ej)){
								String key = ei+"|"+ej;
								if (coSen.containsKey(key))
									coSen.put(key, coSen.get(key)+1);
								else
									coSen.put(key, 1);
								key = ej+"|"+ei;
								if (coSen.containsKey(key))
									coSen.put(key, coSen.get(key)+1);
								else
									coSen.put(key, 1);
							}
		}
	}
	private void printMap() throws IOException {
		FileOutputStream fileOut = new FileOutputStream(movePath+"Thread"+part+"Sen.ser");
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(coSen);
        out.close();
        fileOut.close();
        
        fileOut = new FileOutputStream(movePath+"Thread"+part+"Ab.ser");
        out = new ObjectOutputStream(fileOut);
        out.writeObject(coAb);
        out.close();
        fileOut.close();
	}
}

class DisCooccurrenceThread implements Runnable{
	private int part,total;
	private Thread t;
	private String[] docList;
	private String movePath,originalPath;
	private HashMap<String,HashMap<String,Integer>> coSen,coAb;
	private BreakIterator border;
	public DisCooccurrenceThread(int part,int total,String[] docList,String originalPath,String movePath){
		this.part = part;
		this.total = total;
		this.docList = docList;
		this.originalPath = originalPath;
		this.movePath = movePath;
		coSen = new HashMap<>();
		coAb = new HashMap<>();
		border = BreakIterator.getSentenceInstance(Locale.US);
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
		try {
			printMap();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void transFile(String PMIDtxt) throws IOException{

		String title,ab,id;
		String filePath = originalPath+ PMIDtxt;
		FileReader fr = new FileReader(filePath);
		BufferedReader br = new BufferedReader(fr);
		
		HashSet<String> totalFile = new HashSet<String>();

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
				 else{
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
			countCooccurrence(title+"\n"+ab,genes,diseases,id);
			
		}
		br.close();
	}
	private void countCooccurrence(String text,ArrayList<MentionLocation> genes,ArrayList<MentionLocation> diseases, String id) {
		// add co-occurrence in abstract-level
		for (int d=0;d<diseases.size();d++)
			for (String ed:diseases.get(d).mentionID){
				HashMap<String,Integer> temp;
				if (!coAb.containsKey(ed))
					coAb.put(ed, new HashMap<String,Integer>());
				temp = coAb.get(ed);
				for (int i=0;i<genes.size();i++)
					for (int j=i+1;j<genes.size();j++)
						for (String ei:genes.get(i).mentionID)
							for (String ej:genes.get(j).mentionID)
								if (!ei.equals(ej)){
									String key = ei+"|"+ej;
									if (temp.containsKey(key))
										temp.put(key, temp.get(key)+1);
									else
										temp.put(key, 1);
									key = ej+"|"+ei;
									if (temp.containsKey(key))
										temp.put(key, temp.get(key)+1);
									else
										temp.put(key, 1);
								}
			}
		
		// add co-ooccurrence in sentence-level
		
		border.setText(text);
		int start = border.first();
		int boundaryS = 0, boundaryE = 0,boundaryDS = 0 , boundaryDE = 0;
		for (int end = border.next(); end != BreakIterator.DONE; start = end, end = border.next()) {
			boundaryS = boundaryE;
			boundaryDS = boundaryDE;
			for (int i=boundaryS;i<genes.size();i++)
				if (genes.get(i).start> end){
					boundaryE = i;
					break;
				}
			for (int i=boundaryDS;i<diseases.size();i++)
				if (diseases.get(i).start>end){
					boundaryDE = i;
					break;
				}
			for (int d=boundaryDS;d<boundaryDE;d++)
				for (String ed:diseases.get(d).mentionID){
					HashMap<String,Integer> temp;
					if (!coSen.containsKey(ed))
						coSen.put(ed, new HashMap<String,Integer>());
					temp = coSen.get(ed);
					for (int i=boundaryS;i<boundaryE;i++)
						for (int j=i+1;j<boundaryE;j++)
							for (String ei:genes.get(i).mentionID)
								for (String ej:genes.get(j).mentionID)
									if (!ei.equals(ej)){
										String key = ei+"|"+ej;
										if (temp.containsKey(key))
											temp.put(key, temp.get(key)+1);
										else
											temp.put(key, 1);
										key = ej+"|"+ei;
										if (temp.containsKey(key))
											temp.put(key, temp.get(key)+1);
										else
											temp.put(key, 1);
									}
				}
		}
	}
	private void printMap() throws IOException {
		FileOutputStream fileOut = new FileOutputStream(movePath+"Thread"+part+"Sen.ser");
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(coSen);
        out.close();
        fileOut.close();
        
        fileOut = new FileOutputStream(movePath+"Thread"+part+"Ab.ser");
        out = new ObjectOutputStream(fileOut);
        out.writeObject(coAb);
        out.close();
        fileOut.close();
	}
}

public class Cooccurrence {
	private String genePath,disPath,geneOutPath,disOutPath;
	private String[] geneFileList,disFileList;
	public Cooccurrence(){
		File dataSetDir = new File("../data/DatasetXML(cancer)(with genes)");
		genePath = dataSetDir.getAbsolutePath() + File.separator;
		if (!dataSetDir.exists()){
			System.err.println("The "+dataSetDir+" directory is not exists!");
			System.exit(0);
		}
		geneFileList = dataSetDir.list();
		dataSetDir = new File("../data/DatasetXML(cancer)(with genes and diseases)");
		disPath = dataSetDir.getAbsolutePath() + File.separator;
		if (!dataSetDir.exists()){
			System.err.println("The "+dataSetDir+" directory is not exists!");
			System.exit(0);
		}
		disFileList = dataSetDir.list();
		
		File outSetDir = new File("../data/GeneCooccurrence");
		if (!outSetDir.exists())
			outSetDir.mkdir();
		geneOutPath = outSetDir.getAbsolutePath() + File.separator;
		
		outSetDir = new File("../data/DisGeneCooccurrence");
		if (!outSetDir.exists())
			outSetDir.mkdir();
		disOutPath = outSetDir.getAbsolutePath() + File.separator;
	}
	public void test(String filePath) throws ClassNotFoundException, IOException{
		FileInputStream fileIn = new FileInputStream(filePath);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        HashMap<String,Integer> e = (HashMap<String, Integer>) in.readObject();
        in.close();
        fileIn.close();
        
        for (Entry<String,Integer> entry: e.entrySet()){
        	System.out.println(entry.getKey()+":"+entry.getValue());
        }
	}
	static public void main(String args[]) throws IOException, ClassNotFoundException{

		Cooccurrence cc = new Cooccurrence();
		//cc.test("../data/GeneCooccurrence/Thread1Ab.ser");
		/* Multi-thread */
		int totalThread = 12;
		GeneCooccurrenceThread [] geneRunnableList = new GeneCooccurrenceThread[totalThread];
		DisCooccurrenceThread [] disRunnableList = new DisCooccurrenceThread[totalThread];
		for (int i = 0 ;i< totalThread ;i++){
			geneRunnableList[i] = new GeneCooccurrenceThread(i,totalThread,cc.geneFileList,cc.genePath,cc.geneOutPath);
			geneRunnableList[i].start();
		}
		for (int i = 0 ;i< totalThread ;i++){
			disRunnableList[i] = new DisCooccurrenceThread(i,totalThread,cc.disFileList,cc.disPath,cc.disOutPath);
			disRunnableList[i].start();
		}
		/*==================================================================================*/
	}
}
