package triggerTermMining;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
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

import negex.CallKit;
import negex.GenNegEx;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import object.ChunkIndex;
import object.EntityIndex;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

public class EssentialGeneTriggerTerm {
	private String originalPath,stopWordPath,modelPath,taggerPath,specificD,dirPath,logPath,triggerPath;
	private String[] fileList;
	private HashSet<String> stopWord;
	private Tokenizer tokenizer;
	private POSTaggerME tagger;
	private TokenizerModel tModel;
	private POSModel pModel;
	private BufferedWriter logWriter;
	private BufferedWriter triggerWriter;
	private ArrayList<Character> extractPOS;
	//private ArrayList<HashMap<String,Integer>> triggerMap;
	private MaxentTagger Mtagger;
	private DependencyParser Dparser;
	private HashMap<String,Integer> triggerMap;
	@SuppressWarnings("serial")
	private boolean isEssential;
	public EssentialGeneTriggerTerm(boolean essential,String disease) throws IOException{
		loadNLPmodels();
		isEssential = essential;
		specificD = disease;
		loadFilePath();
		tokenizer = new TokenizerME(tModel);	
		tagger = new POSTaggerME(pModel);
		File dataSetDir = new File(dirPath);
		originalPath = dataSetDir.getAbsolutePath() + File.separator;
		fileList = dataSetDir.list();
		
		stopWordPath = "../data/stopWords.txt";
		loadStopWord();
		
		logWriter = new BufferedWriter(new FileWriter(logPath));
		triggerWriter = new BufferedWriter(new FileWriter(triggerPath));
		
		extractPOS = new ArrayList<Character>(){{add('J');add('N');add('V');}};
		/*
		 *  J: Adjective
		 *  N: Noun
		 *  V: Verb
		 */
		/*triggerMap = new ArrayList<>();
		for (int i=0;i<extractPOS.size();i++)
			triggerMap.add(new HashMap<>());*/
		triggerMap = new HashMap<>();
		
		modelPath = DependencyParser.DEFAULT_MODEL;
	    taggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";
	    Mtagger = new MaxentTagger(taggerPath);
	    Dparser = DependencyParser.loadFromModelFile(modelPath);
		
	}
	private void loadFilePath() {
		dirPath = "../data/DatasetXML(pattern sentences)";
		logPath = "./triggerTermLog.txt";
		triggerPath = "./allTriggerTerms.txt";
		if (!isEssential){
			//dirPath += "(not essential)(only 4 cancers)";
			dirPath += "(not essential)";
			logPath = logPath.replace(".txt", "_not.txt");
			triggerPath = triggerPath.replace(".txt", "_not.txt");
		}
		if (specificD!=null){
			logPath = logPath.replace(".txt", "_"+specificD+".txt");
			triggerPath = triggerPath.replace(".txt", "_"+specificD+".txt");
		}
		System.out.println("Log file path: "+logPath);
		System.out.println("Trigger file path: "+triggerPath);
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
	public void extractTrigger() throws IOException{
		
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
						extractTriggerTermDependency(senLine,eGSet,eDSet);
						
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
		
		/*for (int i=0;i< this.extractPOS.size();i++)
			triggerMap.set(i,sortPatternMap(triggerMap.get(i)));*/
		triggerMap = sortMap(triggerMap);
		int topN = 100, printI = 0;
		System.out.println(triggerMap.size());
		/*for (int i=0;i< this.triggerMap.size();i++){
			printI = 0;
			for (Entry<String,Integer> entry: triggerMap.get(i).entrySet()){
				if (printI++ < topN)
					System.out.println(extractPOS.get(i)+" "+entry.getKey()+" "+entry.getValue());
				triggerWriter.write(extractPOS.get(i)+"\t"+entry.getKey()+"\t#"+entry.getValue()+"\n");
			
			}
		}*/
		for (Entry<String,Integer> entry: triggerMap.entrySet()){
			if (printI++ < topN)
				System.out.println(entry.getKey()+" "+entry.getValue());
			triggerWriter.write(entry.getKey()+"\t#"+entry.getValue()+"\n");
		}
		triggerWriter.close();
		logWriter.close();
	}
	private HashMap<String, Integer> sortMap(HashMap<String, Integer> map) {
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
	public HashSet<Integer> entityStringToIndexSet(HashSet<String> entitySet){
		HashSet<Integer> eIDSet = new HashSet<Integer>();
		for (String str:entitySet){
			int start = Integer.parseInt(str.split("\t")[1]);
			int end = Integer.parseInt(str.split("\t")[2]);
			for (int i=start;i<end;i++)
				eIDSet.add(i);
		}
		return eIDSet;
	}
	public void extractTriggerTerm(String senLine,HashSet<Integer> eIDSet){
		String[] tokens = tokenizer.tokenize(senLine);
		Span[] tokenSpans = tokenizer.tokenizePos(senLine);
		String[] tags = tagger.tag(tokens);
		for (int i=0;i<tags.length;i++){
			char firstChar = tags[i].charAt(0);
			if (extractPOS.contains(firstChar) && !eIDSet.contains(tokenSpans[i].getStart()) && !stopWord.contains(tokens[i])){
				//HashMap<String,Integer> temp = this.triggerMap.get(extractPOS.indexOf(firstChar));
				if (triggerMap.containsKey(tokens[i]))
					triggerMap.put(tokens[i], triggerMap.get(tokens[i])+1);
				else
					triggerMap.put(tokens[i], 1);
			}
		}
		
	}
	public void extractTriggerTermDependency(String senLine,HashSet<String> eGSet,HashSet<String> eDSet){
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
	      HashSet<Integer> eGISet = getEISet(eGSet,tagged);
	      HashSet<Integer> eDISet = getEISet(eDSet,tagged);
	      HashSet<Integer> eAISet = getAISet(eGSet,tagged);
	      eAISet.addAll(getAISet(eDSet,tagged));
	      // get the shared parent
	      for (int eG: eGISet){
	    	  for (int eD: eDISet){
	    		  String trigger = getSharedMinParent(parentMap,depthMap,eG,eD,eAISet);
	    		  if (trigger!=null)
	    			  if (triggerMap.containsKey(trigger))
	    				  triggerMap.put(trigger,triggerMap.get(trigger)+1);
	    			  else
	    				  triggerMap.put(trigger, 1);
	    	  
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
			if (specificD !=null && eachEtab[4].equals("Disease")){
				if (specificD.equals(eachEtab[5]))
					temp.add(getIndexFromStartEnd(tagged,Integer.parseInt(eachEtab[1]),Integer.parseInt(eachEtab[2])));
			}else
				temp.add(getIndexFromStartEnd(tagged,Integer.parseInt(eachEtab[1]),Integer.parseInt(eachEtab[2])));
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
	private boolean extractNegation(String senText){	// true: affirmed, false: negative
        GenNegEx g = new GenNegEx(false);
        String sentence = CallKit.cleans(senText);
       String scope = g.negScope(sentence);       
       if (scope.equals("-1")) 
            return false;
       else
           return true;
    }
	static public void main(String args[]) throws IOException{
		//breast cancer = "D001943";
		//pancreatic cancer = "D010190";
		//ovarian cancer="D010051";
		//colon cancer="D003110" or "D015179";
		EssentialGeneTriggerTerm egtt = new EssentialGeneTriggerTerm(false,null);
		/*egtt.extractTrigger();
		egtt = new EssentialGeneTriggerTerm(true,"D003110");
		egtt.extractTrigger();
		egtt = new EssentialGeneTriggerTerm(false,"D003110");
		egtt.extractTrigger();
		egtt = new EssentialGeneTriggerTerm(true,"D015179");
		egtt.extractTrigger();
		egtt = new EssentialGeneTriggerTerm(false,"D015179");
		egtt.extractTrigger();
		egtt = new EssentialGeneTriggerTerm(true,"D001943");
		egtt.extractTrigger();
		egtt = new EssentialGeneTriggerTerm(false,"D001943");
		egtt.extractTrigger();
		egtt = new EssentialGeneTriggerTerm(true,"D010190");
		egtt.extractTrigger();
		egtt = new EssentialGeneTriggerTerm(false,"D010190");
		egtt.extractTrigger();
		egtt = new EssentialGeneTriggerTerm(true,"D010051");
		egtt.extractTrigger();
		egtt = new EssentialGeneTriggerTerm(false,"D010051");
		egtt.extractTrigger();*/
		
		/*HashSet<String> tempSE = new HashSet<>();
		tempSE.add("10027321_2\t180\t204\tcarcinoembryonic antigen\tGene\t1048");
		tempSE.add("10027321_2\t206\t209\tCEA\tGene\t1048");
		tempSE.add("10027321_2\t91\t108\tcolorectal cancer\tDisease\tD015179");
		egtt.extractTriggerTerm("A pilot, escalating-dose study of oral marimastat was performed in patients with recurrent colorectal cancer, in whom evaluation of serological response was made by measurement of carcinoembryonic antigen (CEA) levels.",egtt.entityStringToIndexSet(tempSE));
		*/
		// 10568187_0	12838318_3
		/*HashSet<String> tempEG = new HashSet<>();
		HashSet<String> tempED = new HashSet<>();
		tempED.add("12838318_4	15	27	colon cancer	Disease	D003110");
		tempEG.add("12838318_4	109	113	KAI1	Gene	3732");
		tempEG.add("12838318_4	114	118	CD82	Gene	3732");
		tempEG.add("12838318_4	104	107	CD9	Gene	928");
		tempEG.add("12838318_4	98	103	MRP-1	Gene	928");
		tempEG.add("12838318_4	123	128	CD151	Gene	977");
		egtt.extractTriggerTermDependency("We studied 146 colon cancer patients who underwent curative surgery and studied the expression of MRP-1/CD9, KAI1/CD82 and CD151 using reverse transcriptase - polymerase chain reaction and immunohistochemistry.", tempEG, tempED);
		for (Entry<String,Integer> entry: egtt.triggerMap.entrySet()){
			System.out.println(entry.getKey()+" "+entry.getValue());
		}*/
		String modelPath = DependencyParser.DEFAULT_MODEL;
	    String taggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";
		String text = "The aim of this study is to clarify the clinical significance of the member of TM4SF (MRP-1/CD9, KAI1/CD82 and CD151) in human colon cancer.";

	    MaxentTagger tagger = new MaxentTagger(taggerPath);
	    DependencyParser parser = DependencyParser.loadFromModelFile(modelPath);

	    DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));
	    for (List<HasWord> sentence : tokenizer) {
	      List<TaggedWord> tagged = tagger.tagSentence(sentence);
	      GrammaticalStructure gs = parser.predict(tagged);

	      // Print typed dependencies
	      System.out.println(gs);
	      for (TypedDependency e: gs.typedDependencies())
	    	  System.out.println(e.gov());
	    }
	}
}
