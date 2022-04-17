package pubmed;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.List;
import java.util.Locale;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import abner.Tagger;

class GeneNERThread implements Runnable{
	private int partNum;
	private int totalThread;
	private String[] docList;
	private String movePath,datasetPath;
	private Thread t;
	private SAXReader xmlReader;
	private Tagger tagger ;
	private BreakIterator iterator;
	
	public GeneNERThread(int part,int total,String[] docList,String movePath,String datasetPath) throws SAXException{
		partNum = part;
		totalThread = total;
		this.docList = docList;
		this.movePath = movePath;
		this.datasetPath = datasetPath;
		this.xmlReader=new SAXReader();
		xmlReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		this.tagger = new Tagger();
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
			String fileName = docList[i];
			try {
				writeFile(fileName);
			} catch (DocumentException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (((i-partNum)/totalThread) %1000 == 0)
				System.out.println(i + ": " + fileName + " DONE!! (at "+partNum+" Thread)");
		}
	}
	
	public String getTitleAbsFromXml(String fileName) throws DocumentException{
		Document doc=xmlReader.read(new File(datasetPath + fileName));
		Node titleNode = doc.selectSingleNode("//PubmedArticleSet/PubmedArticle/MedlineCitation/Article/ArticleTitle" );
		if (titleNode==null){
			System.out.println(fileName + " skipped!!");
			return null;
		}
		String str = titleNode.getStringValue();
		List<? extends Node> abNode = doc.selectNodes("//PubmedArticleSet/PubmedArticle/MedlineCitation/Article/Abstract/AbstractText");
		for (Node eachNode:abNode){
			str += "\n" + eachNode.getStringValue();
		}
		return str;
	}
	
	public boolean geneNERFilter(String titleAbs){
		iterator = BreakIterator.getSentenceInstance(Locale.US);
		iterator.setText(titleAbs);
		int start = iterator.first();
		for (int end = iterator.next();end != BreakIterator.DONE;start = end, end = iterator.next()) {
			String taggedStr = tagger.tagABNER(titleAbs.substring(start,end));
			if (taggedStr.contains("|B-GENE") || taggedStr.contains("|B-PROTEIN")){
				return true;
			}
		}
		return false;
	}
	public void writeFile(String fileName) throws DocumentException, IOException{
		String titleAbs = this.getTitleAbsFromXml(fileName);
		if (geneNERFilter(titleAbs)){
			System.out.println("There are gene(s) in "+fileName+"(at thread "+this.partNum+"): ");
			System.out.println(titleAbs);
			BufferedWriter fileOUT = new BufferedWriter(new FileWriter(movePath + fileName));
			fileOUT.write(titleAbs);
			fileOUT.close();
		}
	}
}

public class GeneNER {
	public String movePath,datasetPath;
	public String[] fileList;
	
	public GeneNER() throws SAXException{
		File dataSetDir = new File("../data/DatasetXML(cancer)");
		datasetPath = dataSetDir.getAbsolutePath()+File.separator;
		File outSetDir = new File("../data/DatasetXML(cancer)(with genes)");
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
	public static void main(String args[]) throws SAXException{
		/*Tagger tagger = new Tagger();
		
		String taggedStr = tagger.tagABNER("The repair capacity of lung cancer cell lines A549 and H1299 depends on HMGB1 expression level and the p53 status.\nElucidation of the cellular components responsive to chemotherapeutic agents as cisplatin rationalizes the strategy for anticancer chemotherapy. The removal of the cisplatin/DNA lesions gives the chance to the cancer cells to survive and compromises the chemotherapeutical treatment. Therefore the cell repair efficiency is substantial for the clinical outcome. High mobility group box 1 (HMGB1) protein is considered to be involved in the removal of the lesions as it binds with high affinity to cisplatin/DNA adducts. We demonstrated that overexpression of HMGB1 protein inhibited cis-platinated DNA repair in vivo and the effect strongly depended on its C-terminus. We registered increased levels of DNA repair after HMGB1 silencing only in p53 defective H1299 lung cancer cells. Next introduction of functional p53 resulted in DNA repair inhibition. H1299 cells overexpressing HMGB1 were significantly sensitized to treatment with cisplatin demonstrating the close relation between the role of HMGB1 in repair of cis-platinated DNA and the efficiency of the anti-cancer drug, the process being modulated by the C-terminus. In A549 cells with functional p53 the repair of cisplatin/DNA adducts is determined by а complex action of HMGB1 and p53 as an increase of DNA repair capacity was registered only after silencing of both proteins.");
		System.out.println(taggedStr);
		
		if (taggedStr.contains("|B-GENE") || taggedStr.contains("|B-PROTEIN"))
			System.out.println("YES");*/
		/* Multi-thread */
		int totalThread = 12;
		GeneNERThread [] runnableList = new GeneNERThread[totalThread];
		GeneNER gner = new GeneNER();
		for (int i = 0 ;i< totalThread ;i++){
			runnableList[i] = new GeneNERThread(i,totalThread,gner.fileList,gner.movePath,gner.datasetPath);
			runnableList[i].start();
		}
		
		/*==================================================================================*/
	}
	
}
