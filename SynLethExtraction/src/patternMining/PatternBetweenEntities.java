package patternMining;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import object.ChunkIndex;
import object.EntityIndex;

public class PatternBetweenEntities {
	private String originalPath,stopWordPath;
	private String[] fileList;
	private HashSet<String> stopWord;
	private String tokens[];
	private String tags[];
	private String chunkTags[];
	private Span tokenSpans[];
	private Tokenizer tokenizer;
	private POSTaggerME tagger;
	private ChunkerME chunker;
	private TokenizerModel tModel;
	private POSModel pModel;
	private ChunkerModel cModel;
	private BufferedWriter logWriter;
	private BufferedWriter patternWriter;
	public PatternBetweenEntities() throws IOException{
		loadNLPmodels();
		tokenizer = new TokenizerME(tModel);	
		tagger = new POSTaggerME(pModel);
		chunker = new ChunkerME(cModel);
		File dataSetDir = new File("../data/DatasetXML(pattern sentences)");
		originalPath = dataSetDir.getAbsolutePath() + File.separator;
		fileList = dataSetDir.list();
		
		stopWordPath = "../data/stopWords.txt";
		loadStopWord();
		
		logWriter = new BufferedWriter(new FileWriter("./patternLog.txt"));
		patternWriter = new BufferedWriter(new FileWriter("./allPatterns.txt"));
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
		
		modelIn = null;
		try {
			modelIn = new FileInputStream("./nlp_model/en-chunker.bin");
			cModel = new ChunkerModel(modelIn);
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
	public void extractPattern() throws IOException{
		HashMap<String,Integer> patternMap = new HashMap<>();
		for (String eFile: fileList){
			BufferedReader br = new BufferedReader(new FileReader(originalPath+eFile));
			HashSet<String> entitySet = new HashSet<>();
			HashSet<String> GeneIDSet = new HashSet<>();
			HashSet<String> DisIDSet = new HashSet<>();
			while (br.ready()){
				String senLine = br.readLine();
				if (senLine.split("\\|").length == 1)
					break;
				String id = senLine.split("\\|")[0];
				senLine = senLine.split("\\|")[2];
				while (br.ready()){
					String entityLine = br.readLine();
					if (!entityLine.contains("\t")){	//entity line end, check essential gene
						for (String gID:GeneIDSet)
							for (String dID:DisIDSet){
									ArrayList<String> patterns = extractStringGivenID(gID,dID,senLine,entitySet);
									patterns = removeStopWords(patterns);
									patterns = simplePatternFilter(patterns);
									if (patterns.size()>0){
										for (String str: patterns)
											if (patternMap.containsKey(str))
												patternMap.put(str, patternMap.get(str)+1);
											else
												patternMap.put(str, new Integer(1));
									}
								}
						
						GeneIDSet.clear();
						DisIDSet.clear();
						entitySet.clear();
						break;
					}
					String entityID = entityLine.split("\t")[5];
					if (entityLine.split("\t")[4].equals("Gene")){
						GeneIDSet.add(entityID);
						entitySet.add(entityLine);
					}
					else{
						DisIDSet.add(entityID);
						entitySet.add(entityLine);
					}
				}
			}
			br.close();
			System.out.println(eFile+" DONE!");
		}
		patternMap = sortPatternMap(patternMap);
		int topN = 100, i=0;
		for (Entry<String,Integer> entry: patternMap.entrySet()){
			if (i++ < topN)
				System.out.println(entry.getKey()+" "+entry.getValue());
			patternWriter.write(entry.getKey()+"\t#"+entry.getValue()+"\n");
			
		}
		patternWriter.close();
		logWriter.close();
	}
	private HashMap<String, Integer> sortPatternMap(HashMap<String, Integer> map) { 
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
	private ArrayList<String> simplePatternFilter(ArrayList<String> patterns) {
		ArrayList<String> nPatterns = new ArrayList<String>();
		// remove more than one space
		for (String eStr:patterns)
			nPatterns.add(eStr.replaceAll("  ", " "));
		
		// remove too long pattern and too short pattern
		for (int i=0;i<nPatterns.size();i++)
			if (nPatterns.get(i).length() > 50 || nPatterns.get(i).length() < 2)
				nPatterns.remove(i--);
		return nPatterns;
	}
	private ArrayList<String> extractStringGivenID(String gID, String dID, String senLine, HashSet<String> entitySet) {
		HashSet<EntityIndex> gIndex = new HashSet<>();
		HashSet<EntityIndex> dIndex = new HashSet<>();
		ArrayList<String> allString = new ArrayList<>();
		for (String eachE: entitySet){
			EntityIndex temp = new EntityIndex();
			String[] eachEtab = eachE.split("\t");
			if (eachEtab[5].equals(gID) || eachEtab[5].equals(dID)){
				temp.start = Integer.parseInt(eachEtab[1]);
				temp.end = Integer.parseInt(eachEtab[2]);
				if (eachEtab[4].equals("Disease")){
					dIndex.add(temp);
				}
				else{
					gIndex.add(temp);
				}
			}
		}
		for (EntityIndex gEI: gIndex)
			for (EntityIndex dEI: dIndex){
				if ((gEI.start >= dEI.start && gEI.end <= dEI.end) ||(gEI.start <= dEI.start && gEI.end >= dEI.end))	//check if the entity is contained by another
					continue;
				String tempStr = keepVerbNounPhrase(senLine,gEI,dEI);
				if (tempStr != null)
				allString.add(tempStr);
			}
		return allString;
	}
	public ArrayList<String> removeStopWords(ArrayList<String> oPatterns){
		ArrayList<String> nPatterns = new ArrayList<>();
		for (String eStr: oPatterns){
			for (String sw: stopWord){
				eStr = eStr.replaceAll("\\b"+sw+"\\b", "");
			}
			nPatterns.add(eStr);
		}
		return nPatterns;
	}
	public String keepVerbNounPhrase(String str,EntityIndex gEI,EntityIndex dEI){
		int firstS = -1, secondE = -1;
		str = str.toLowerCase();
		// there sometimes is a bug from pubtator: end index is larger than length of string
		if (dEI.end > str.length() || gEI.end > str.length())
			return null;
		
		if (gEI.end <= dEI.start){
			str = str.substring(0, gEI.start)+"GENE"+str.substring(gEI.end, dEI.start)+"DISEASE"+str.substring(dEI.end);
			firstS = str.indexOf("GENE");
			secondE = str.indexOf("DISEASE")+7;
		}
		else if (dEI.start <= gEI.end){
			str = str.substring(0, dEI.start)+"DISEASE"+str.substring(dEI.end, gEI.start)+"GENE"+str.substring(gEI.end);
			firstS = str.indexOf("DISEASE");
			secondE = str.indexOf("GENE")+4;
		}
		else{
			System.err.println("Wrong entity index!("+str+")");
			return null;
		}
		
		tokens = tokenizer.tokenize(str);
		tokenSpans = tokenizer.tokenizePos(str);
		tags = tagger.tag(tokens);
		chunkTags = chunker.chunk(tokens, tags);
		if (chunkTags[0].contains("I-"))	// if the first one is "Inside" tag
			chunkTags[0] = chunkTags[0].replace("I-", "B-");
			
		ArrayList<ChunkIndex> chunkRanges = new ArrayList<>();
		int lastE=-1,lastS = -1;
		String lastType = null;
		//initialize
		if (tokenSpans[0].getStart() <= firstS && tokenSpans[0].getEnd() >= firstS && chunkTags[0].equals("O"))
			return null;
		if (tokenSpans[0].getStart() <= secondE && tokenSpans[0].getEnd() >= secondE && chunkTags[0].equals("O"))
			return null;
		if (chunkTags[0].startsWith("B-")){
			lastS = tokenSpans[0].getStart();
			lastE = tokenSpans[0].getEnd();
			lastType = chunkTags[0].substring(2);
		}
		for (int i=1;i<chunkTags.length;i++){
			// check if entity is in phrase
			if (tokenSpans[i].getStart() <= firstS && tokenSpans[0].getEnd() >= firstS && chunkTags[i].equals("O"))
				return null;
			if (tokenSpans[i].getStart() <= secondE && tokenSpans[0].getEnd() >= secondE && chunkTags[i].equals("O"))
				return null;
			// create chunkRanges
			if (chunkTags[i].startsWith("B-")){
				if (lastS >=0 && lastE >=0)
					chunkRanges.add(new ChunkIndex(lastS,lastE,lastType));
				lastS = tokenSpans[i].getStart();
				lastE = tokenSpans[i].getEnd();
				lastType = chunkTags[i].substring(2);
			}
			else if (chunkTags[i].startsWith("I-")){
				lastE = tokenSpans[i].getEnd();
			}
			else{	//startWith("O-")
				if (lastS >=0 && lastE>=0)
					chunkRanges.add(new ChunkIndex(lastS,lastE,lastType));
				lastS=lastE=-1;
				lastType = null;
			}
		}
		if (lastS >=0 && lastE >=0)
			chunkRanges.add(new ChunkIndex(lastS,lastE,lastType));
		int chunkS =-1,chunkE =-1;
		for (ChunkIndex e:chunkRanges){
			if (e.start <= firstS){
				chunkS = e.start;
			}
			if (e.start <= secondE){
				chunkE = e.end;
			}
		}
		try {
			logWriter.write(firstS+" "+secondE+" "+chunkS+" "+chunkE+" "+str+"\n");
			//System.out.println(firstS+" "+secondE+" "+chunkS+" "+chunkE+" "+str);	// for debug
			logWriter.write("Pattern :"+str.substring(chunkS,chunkE)+"\n\n");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return str.substring(chunkS,chunkE);
	}
	public static void main(String[] args) throws IOException{
		PatternBetweenEntities pbe = new PatternBetweenEntities();
		pbe.extractPattern();
		//pbe.keepVerbNounPhrase("Overexpression of SIX1 is an independent prognostic marker in stage I-III colorectal cancer.",new EntityIndex(18,22) , new EntityIndex(76,93));
	}
}
