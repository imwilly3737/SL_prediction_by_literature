package essentialGene;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerModel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;

public class ExtractGeneFromTrigger {
	private String triggerFile;
	private String stopWordPath;
	private String potentialGeneFile;
	private double triggerTh;
	private HashMap<String,Double> eTrigger,potentialGene;
	private Tokenizer tokenizer;
	private POSTaggerME tagger;
	private TokenizerModel tModel;
	private POSModel pModel;
	private MaxentTagger Mtagger;
	private DependencyParser Dparser;
	private String specificD;
	private HashMap<Integer,String> indexToGene,indexToDisease;
	private HashSet<String> stopWord;
	public ExtractGeneFromTrigger(String disease,double threshold) throws IOException{
		triggerFile = "./allTriggerTermsRanked.txt";
		stopWordPath = "../data/stopWords.txt";
		potentialGeneFile = "./allPotentialEssentialGene(D003110)(new trigger)(1).txt";
		loadNLPmodels();
		this.triggerTh = threshold;
		eTrigger = new HashMap<>();
		potentialGene = new HashMap<>();
		specificD = disease;
		stopWordPath = "../data/stopWords.txt";
		loadStopWord();
		loadTrigger();
		
		String modelPath = DependencyParser.DEFAULT_MODEL;
	    String taggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";
		Mtagger = new MaxentTagger(taggerPath);
	    Dparser = DependencyParser.loadFromModelFile(modelPath);
	}
	private void loadNLPmodels() {
		InputStream modelIn = null;
		try {
			modelIn = new FileInputStream("./nlp_model/en-token.bin");
			tModel = new TokenizerModel(modelIn);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				} catch (IOException e) {
				}
			}
		}
		
		modelIn = null;
		try {
			modelIn = new FileInputStream("./nlp_model/en-pos-maxent.bin");
			pModel = new POSModel(modelIn);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				} catch (IOException e) {
				}
			}
		}
		
	}
	public void loadStopWord() throws IOException{
		if (stopWord == null)
			stopWord = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(stopWordPath));
		while (br.ready()){
			String sw = br.readLine();
			stopWord.add(sw);
		}
		br.close();
	}
	private void loadTrigger() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(triggerFile));
		while (br.ready()){
			String[] eTab= br.readLine().split("\t");
			if (eTab.length <2 || Double.parseDouble(eTab[1])<triggerTh)
				break;
			eTrigger.put(eTab[0], Double.parseDouble(eTab[1]));
		}
		br.close();
	}
	public void extractGene() throws IOException{
		//extractGeneInDir("../data/DatasetXML(pattern sentences)");
		//extractGeneInDir("../data/DatasetXML(pattern sentences)(not essential)(only 4 cancers)");
		extractGeneInDir("../data/DatasetXML(pattern sentences)(not essential)");
		potentialGene = sortMap(potentialGene);
		printEssentialGene(potentialGeneFile);
	}
	private void printEssentialGene(String outFile) throws IOException {
		BufferedWriter geneWriter = new BufferedWriter(new FileWriter(outFile));
		int topN = 100, printI=0;
		for (Entry<String,Double> entry: potentialGene.entrySet()){
			if (printI++ < topN)
				System.out.println(entry.getKey()+" "+entry.getValue());
			geneWriter.write(entry.getKey()+"\t"+entry.getValue()+"\n");
			
		}
		geneWriter.close();
		
	}
	private void extractGeneInDir(String dirPath) throws IOException{
		File dataSetDir = new File(dirPath);
		String[] fileList = dataSetDir.list();
		String originalPath = dataSetDir.getAbsolutePath() + File.separator;
		for (String eFile: fileList){
			BufferedReader br = new BufferedReader(new FileReader(originalPath+eFile));
			HashSet<String> eGSet = new HashSet<>();
			HashSet<String> eDSet = new HashSet<>();
			while (br.ready()){
				String senLine = br.readLine();
				if (senLine.split("\\|").length == 1)
					break;
				String id = senLine.split("\\|")[0];
				senLine = senLine.split("\\|")[2];
				while (br.ready()){
					String entityLine = br.readLine();
					if (!entityLine.contains("\t")){	//entity line end, check essential gene
						extractEssentialGeneDependency(senLine,eGSet,eDSet);
						
						eGSet.clear();
						eDSet.clear();
						break;
					}
					if (entityLine.split("\t")[4].equals("Gene")){
						eGSet.add(entityLine);
					}
					else{
						eDSet.add(entityLine);
					}
				}
			}
			br.close();
			System.out.println(eFile+" DONE!");
		}
	}
	public void extractEssentialGeneDependency(String senLine,HashSet<String> eGSet,HashSet<String> eDSet){
		boolean firstSen = true;
		DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(senLine));
		for (List<HasWord> sentence : tokenizer) {
	    	if (firstSen == false){	// not only one sentence
	    		System.err.println("Skip sentence:\n"+senLine);
	    		return;
	    	}
	    	firstSen = false;
		}
		tokenizer = new DocumentPreprocessor(new StringReader(senLine));
	    for (List<HasWord> sentence : tokenizer) {
	    	
	      List<TaggedWord> tagged = Mtagger.tagSentence(sentence);
	      GrammaticalStructure gs = Dparser.predict(tagged);
	
	      // Add to parent set
	      HashMap<Integer,IndexedWord> parentMap = new HashMap<>();
	      for (TypedDependency e: gs.typedDependencies()){
	    	  if (e.reln().getShortName().equals("neg"))
	    		  return;
	    	  // add this indexedword to set
	    	  if (parentMap.containsKey(e.dep().index())){
	    		  parentMap.put(e.dep().index(), e.gov());
	    	  }
	    	  else{
	    		  parentMap.put(e.dep().index(), e.gov());
	    	  }
	      }
	      HashMap<Integer,Integer> depthMap = getDepthMap(parentMap);
	      indexToGene = new HashMap<>();
	      indexToDisease = new HashMap<>();
	      HashSet<Integer> eGISet = getEISet(eGSet,tagged);
	      HashSet<Integer> eDISet = getEISet(eDSet,tagged);
	      HashSet<Integer> eAISet = getAISet(eGSet,tagged);
	      eAISet.addAll(getAISet(eDSet,tagged));
	      // get the shared parent
	      for (int eG: eGISet){
	    	  for (int eD: eDISet){
	    		  String trigger = getSharedMinParent(parentMap,depthMap,eG,eD,eAISet);
	    		  if (trigger!=null){
	    			  if (eTrigger.containsKey(trigger)){
	    				  String gene = indexToGene.get(eG);
	    				  String disease = indexToDisease.get(eD);
	    				  String key = null;
	    				  if (specificD != null)
	    					  key = gene;
	    				  else
	    					  key = gene+"|"+disease;
	    				  if (potentialGene.containsKey(key))
	    					  potentialGene.put(key, potentialGene.get(key)+eTrigger.get(trigger));
	    				  else
	    					  potentialGene.put(key, eTrigger.get(trigger));
	    			  }
	    		  }
	    	  }
	      }
	      
	    }
	}
	private HashSet<Integer> getAISet(HashSet<String> eGSet, List<TaggedWord> tagged) {
		HashSet<Integer> temp = new HashSet<>();
		for (String eG:eGSet){
			String[] eachEtab = eG.split("\t");
			int start = Integer.parseInt(eachEtab[1]);
			int end = Integer.parseInt(eachEtab[2]);
			for (int i=0;i<tagged.size();i++){
				if (tagged.get(i).endPosition() <= end && tagged.get(i).beginPosition() >= start){
					temp.add(i+1);
					break;
				}
				if (tagged.get(i).endPosition() >= end && tagged.get(i).beginPosition() <= start){
					temp.add(i+1);
					break;
				}
			}
		}
		return temp;
	}
	private HashSet<Integer> getEISet(HashSet<String> eGSet,List<TaggedWord> tagged) {
		HashSet<Integer> temp = new HashSet<>();
		for (String eG: eGSet){
			String[] eachEtab = eG.split("\t");
			if (eachEtab[4].equals("Disease")){
				if (specificD ==null || specificD.equals(eachEtab[5])){
					int index = getIndexFromStartEnd(tagged,Integer.parseInt(eachEtab[1]),Integer.parseInt(eachEtab[2]));
					temp.add(index);
					indexToDisease.put(index,eachEtab[5]);
				}
			}else{
				int index = getIndexFromStartEnd(tagged,Integer.parseInt(eachEtab[1]),Integer.parseInt(eachEtab[2]));
				temp.add(index);
				indexToGene.put(index,eachEtab[5]);
			}
		}
		return temp;
	}
	private HashMap<Integer, Integer> getDepthMap(HashMap<Integer, IndexedWord> parentMap) {
		HashMap<Integer, Integer> depthMap = new HashMap<>(); 
		for (Entry<Integer, IndexedWord> entry:parentMap.entrySet()){
			int depth = 0, i = entry.getKey();
			while (parentMap.containsKey(i)){
				depth++;
				i = parentMap.get(i).index();
			}
			depthMap.put(entry.getKey(), depth);
		}
		return depthMap;
	}
	private String getSharedMinParent(HashMap<Integer, IndexedWord> parentMap, HashMap<Integer, Integer> depthMap, int gI, int dI,HashSet<Integer> eA) {
		if (gI == -1 || dI == -1){
			return null;
		}
		HashSet<IndexedWord> intersection = getParentSet(gI,parentMap,eA);
		intersection.retainAll(getParentSet(dI,parentMap,eA));
		int max = -1;
		String minP = null;
		for (IndexedWord e: intersection){
			if (!parentMap.containsKey(e.index()))	// no parents == ROOT (skip this)
				continue;
			
			if (max < depthMap.get(e.index())){
				max = depthMap.get(e.index());
				minP = e.value();
			}
		}
		if (minP != null)
			minP = minP.toLowerCase();
		return minP;
	}
	private HashSet<IndexedWord> getParentSet(int index,HashMap<Integer, IndexedWord> parentMap, HashSet<Integer> eA) {
		HashSet<IndexedWord> pSet = new HashSet<IndexedWord>();
		IndexedWord iw=parentMap.get(index);
		for (int i = iw.index(),depth = 0;parentMap.containsKey(i) && depth < 2;iw=parentMap.get(i),i = iw.index()){
			if (stopWord.contains(iw.value()))
				continue;
			if (eA.contains(i))
				continue;
			pSet.add(iw);
			depth++;
		}
		return pSet;
	}
	private int getIndexFromStartEnd(List<TaggedWord> tagged, int start,int end) {
		for (int i=0;i<tagged.size();i++){
			if (tagged.get(i).endPosition() <= end && tagged.get(i).beginPosition() >= start)
				return i+1;
			if (tagged.get(i).endPosition() >= end && tagged.get(i).beginPosition() <= start)
				return i+1;
		}
		return -1;
	}
	private HashMap<String, Double> sortMap(HashMap<String, Double> map) {
	       List<?> list = new LinkedList(map.entrySet());
	       // Defined Custom Comparator here
	       Collections.sort(list, new Comparator() {
	            public int compare(Object o1, Object o2) {
	               return ((Comparable) ((Map.Entry) (o2)).getValue())
	                  .compareTo(((Map.Entry) (o1)).getValue());
	            }
	       });

	       // Here I am copying the sorted list in HashMap
	       // using LinkedHashMap to preserve the insertion order
	       HashMap sortedHashMap = new LinkedHashMap();
	       for (Iterator it = list.iterator(); it.hasNext();) {
	              Map.Entry entry = (Map.Entry) it.next();
	              sortedHashMap.put(entry.getKey(), entry.getValue());
	       } 
	       return sortedHashMap;
	  }
	static public void main(String args[]) throws IOException{
		//breast cancer = "D001943";
		//pancreatic cancer = "D010190";
		//ovarian cancer="D010051";
		//colon cancer="D003110"; or "D015179";
		ExtractGeneFromTrigger egft = new ExtractGeneFromTrigger("D003110",1.0);
		egft.extractGene();
	}
}
