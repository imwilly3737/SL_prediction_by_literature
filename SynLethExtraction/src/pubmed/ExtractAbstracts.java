package pubmed;

import javax.xml.parsers.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

import java.net.*;
import java.util.stream.Collectors;
import java.io.*;

class ExtractAbstractsThread implements Runnable{
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

			if (i%100 == 0)
				System.out.println("Checking the " + i + "th PMID (at "+partNum+" Thread)");

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
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				i-=totalThread;
				continue;
			}

			idx += 1;
			if (idx%10 == 0)
				System.out.println(idx + ": " + PMID + ".txt DONE!! (at "+partNum+" Thread)");
		}
	}
}

class ExtractAbstractsBatch implements Runnable{
	private int count;
	private String[] idList;
	private String movePath;
	private String baseFetch;
	private Thread t;
	public ExtractAbstractsBatch(String queryKey, String webEnv, int count,String movePath){
		this.movePath = movePath;
		this.count = count;
		baseFetch = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&retmode=xml";
		baseFetch += "&WebEnv=" + webEnv + "&query_key=" + queryKey;
	}
	public void start(){

		if (t == null)
		{
			t = new Thread(this);
			t.start();
		}
	}
	public void run(){
		String fetch,temp;
		int max = 100;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();

		factory.setIgnoringComments(true);
		factory.setCoalescing(true);
		factory.setNamespaceAware(false);
		factory.setValidating(false);
		DocumentBuilder parser = null;
		Transformer transformer = null;
		try {
			parser = factory.newDocumentBuilder();
	        transformer = transformerFactory.newTransformer();
		} catch (ParserConfigurationException | TransformerConfigurationException e1) {
			e1.printStackTrace();
		}
		for (int start = 0; start < count; start += max) {
			fetch = baseFetch + "&retstart=" + start + "&retmax=" + max;
			try {
				System.out.println("partition" + start / max + " START!!");
				
				URL urlFetch = new URL(fetch);
				URLConnection connectFetch = urlFetch.openConnection();
				Document document = parser.parse(connectFetch.getInputStream());
				NodeList childList = document.getChildNodes();
				NodeList nodeList = null;
				for (int i = 0; i < childList.getLength(); i++)
					if (childList.item(i).getNodeName() == "PubmedArticleSet" && childList.item(i).getNodeType() == Node.ELEMENT_NODE)
						{
							nodeList = childList.item(i).getChildNodes();
							break;
						}

				/* ======= Print the query result to a file for debug ========*/
                //DOMSource querySource = new DOMSource(document);
                //StreamResult queryResult =  new StreamResult(new File(movePath + "partition" + start / max + ".xml"));
                //transformer.transform(querySource, queryResult);
				/* ===========================================================*/
				
				for (int i = 0; i < nodeList.getLength(); i++) {
					Element pubmedArticle = (Element) nodeList.item(i);
					String PMID = pubmedArticle.getElementsByTagName("PMID").item(0).getFirstChild().getNodeValue();

					File fileWrite = new File(movePath + PMID + ".txt");
					if (fileWrite.exists())
						continue;
					Document articleXml = parser.newDocument();
					Element root = articleXml.createElement("PubmedArticleSet"); 
					articleXml.appendChild(root);
					Node clonedNode = pubmedArticle.cloneNode(true);
					articleXml.adoptNode(clonedNode);
	                root.appendChild(clonedNode);
	                
	                //At the end, we save the file XML on disk
	                DOMSource articleSource = new DOMSource(articleXml);
	                StreamResult result =  new StreamResult(fileWrite);
	                transformer.transform(articleSource, result);
	                
	                if (i % 10 == 0)
	                	System.out.println("Done for " + PMID);
				}
					


				System.out.println("partition" + start / max + " DONE!!");
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
		
	}
}
public class ExtractAbstracts {
	public static void main(String args[]) {
		String temp, movePath, fetch;
		
		int idx = 0;
		int retstart = 0;

		File dataSetDir = new File("../data/DatasetXML(colon cancer)");

		if (dataSetDir.exists())
			movePath = dataSetDir.getAbsolutePath() + File.separator;
		else {
			dataSetDir.mkdir();
			movePath = dataSetDir.getAbsolutePath() + File.separator;
		}

		/*============================ Search PubMed Abstract==============================*/
		try{
			// URL url = new URL("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=ggh+AND+human[Organism]&retmax=1000000");
			// URL url = new URL("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=cancer+AND+human[Organism]&retmax=5000000&retstart="+ retstart);
			// URL url = new URL("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=cell+line+AND+human[Organism]&retmax=1000000");
			String term = "%28colon+cancer+OR+colorectal+cancer+OR+rectal+cancer+OR+bowel+cancer%29+AND+1800%2F01%2F01%3A2015%2F12%2F31%5Bdp%5D+AND+human[Organism]";
			//URL url = new URL("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=" + term + "&retmax=5000000&retstart="+ retstart);
			URL url = new URL("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=" + term + "&retmax=5000000&retstart="+ retstart + "&usehistory=y");
			
			URLConnection connect = url.openConnection();

			BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream(), "UTF-8"));
			BufferedWriter out = new BufferedWriter(new FileWriter("pubmedDataSet.txt"));

			while ((temp = in.readLine()) != null) {
				out.write(temp);
				out.newLine();
			}

			in.close();
			out.close();
		}catch(Exception e){
			e.printStackTrace();
			return;
		}
		/*==================================================================================*/
		
		/*============================= Fetch PubMed Abstract==============================*/
		/*DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		factory.setIgnoringComments(true);
		factory.setCoalescing(true);
		factory.setNamespaceAware(false);
		factory.setValidating(false);

		NodeList idList = null;
		try{
			DocumentBuilder parser = factory.newDocumentBuilder();
			Document document = parser.parse(new File("pubmedDataSet.txt"));
			idList = document.getElementsByTagName("Id");
		}catch(Exception e){
			e.printStackTrace();
			return;
		}
		String[] pmidList = new String[idList.getLength()];
		for (int i = 0; i < idList.getLength(); i++) {
			Element sectionIDList = (Element) idList.item(i);
			Node PMID = sectionIDList.getFirstChild();
			String pmid = PMID.getNodeValue();
			pmidList[i] = new String(pmid);
		}
		// Multi-thread 
		int totalThread = 1;
		ExtractAbstractsThread [] runnableList = new ExtractAbstractsThread[totalThread];
		for (int i = 0 ;i< totalThread ;i++){
			runnableList[i] = new ExtractAbstractsThread(i,totalThread,pmidList,movePath);
			runnableList[i].start();
			System.out.println(i);
		}*/
		
		/*==================================================================================*/
		/*======================== Fetch PubMed Abstract Batch==============================*/
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		factory.setIgnoringComments(true);
		factory.setCoalescing(true);
		factory.setNamespaceAware(false);
		factory.setValidating(false);

		NodeList idList = null;
		String webEnv, key;
		int count;
		try{
			DocumentBuilder parser = factory.newDocumentBuilder();
			Document document = parser.parse(new File("pubmedDataSet.txt"));
			webEnv = document.getElementsByTagName("WebEnv").item(0).getFirstChild().getNodeValue();
			key = document.getElementsByTagName("QueryKey").item(0).getFirstChild().getNodeValue();
			count = Integer.parseInt(document.getElementsByTagName("Count").item(0).getFirstChild().getNodeValue());

		}catch(Exception e){
			e.printStackTrace();
			return;
		}
		// Multi-thread 
		int totalThread = 1;
		ExtractAbstractsBatch [] runnableList = new ExtractAbstractsBatch[totalThread];
		for (int i = 0 ;i< totalThread ;i++){
			runnableList[i] = new ExtractAbstractsBatch(key, webEnv, count, movePath);
			runnableList[i].start();
			System.out.println(i);
		}
		/*==================================================================================*/
	}
}
