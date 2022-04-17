package pubmed;

import java.text.BreakIterator;
import java.util.List;
import java.util.Locale;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

public class AbstractToSentence {
	public static void main(String[] args) throws DocumentException, IOException, SAXException{
		String movePath,datasetPath;
		File dataSetDir = new File("DatasetXML(cell line AND cancer)");
		datasetPath = dataSetDir.getAbsolutePath()+File.separator;
		File outSetDir = new File("DatasetSentence(cell line AND cancer)");
		
		if (!dataSetDir.exists()){
			System.out.println("The DatasetXML directory is not exists!");
			return;
		}
		if (outSetDir.exists())
			movePath = outSetDir.getAbsolutePath() + File.separator;
		else {
			outSetDir.mkdir();
			movePath = outSetDir.getAbsolutePath() + File.separator;
		}
		
		SAXReader xmlReader=new SAXReader();
		xmlReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
		for (String eachFile: dataSetDir.list()){
			String PMID= eachFile.substring(0, eachFile.indexOf("."));
			int sentence =1;
			Document doc=xmlReader.read(new File(datasetPath + eachFile));
			BufferedWriter fileOUT = new BufferedWriter(new FileWriter(movePath + eachFile));
			Node titleNode = doc.selectSingleNode("//PubmedArticleSet/PubmedArticle/MedlineCitation/Article/ArticleTitle" );
			if (titleNode==null){
				System.out.println(eachFile + " skipped!!");
				continue;
			}
			String title = titleNode.getStringValue();
			fileOUT.write(PMID+"_0 "+title+"\n");
			List<? extends Node> abNode = doc.selectNodes("//PubmedArticleSet/PubmedArticle/MedlineCitation/Article/Abstract/AbstractText");
			for (Node eachNode:abNode){
				
				iterator.setText(eachNode.getStringValue());
				int start = iterator.first();
				for (int end = iterator.next();end != BreakIterator.DONE;start = end, end = iterator.next()) {
					fileOUT.write(PMID+"_"+(sentence++)+" "+eachNode.getStringValue().substring(start, end)+"\n");
				}
			}
			fileOUT.close();
			System.out.println(eachFile+" done!!");
		}
	}
}
