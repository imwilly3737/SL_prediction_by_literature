package patternMining;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PatternRanking {
	private String EPFile,NEPFile,OutFile;
	private HashMap<String,Double> EPattern,NEPattern;
	private double maxEP,maxNEP;
	public PatternRanking(){
		EPFile = "./allPatterns.txt";
		NEPFile = "./allPatterns_not.txt";
		OutFile = "./allPatternRanked.txt";
		EPattern = new HashMap<>();
		NEPattern = new HashMap<>();
		maxEP = maxNEP = -1;
	}
	public void readFiles() throws IOException{
		BufferedReader ebr = new BufferedReader(new FileReader(EPFile));
		while (ebr.ready()){
			String line = ebr.readLine();
			String pattern = line.substring(0,line.indexOf("\t"));
			double number = Double.parseDouble(line.substring(line.indexOf("\t")+2));
			EPattern.put(pattern, number);
			if (maxEP <0 )
				maxEP = number;
		}
		ebr.close();
		BufferedReader nebr = new BufferedReader(new FileReader(NEPFile));
		while (nebr.ready()){
			String line = nebr.readLine();
			String pattern = line.substring(0,line.indexOf("\t"));
			double number = Double.parseDouble(line.substring(line.indexOf("\t")+2));
			NEPattern.put(pattern, number);
			if (maxNEP <0 )
				maxNEP = number;
		}
		nebr.close();
		for (Entry<String,Double> entry: EPattern.entrySet()){
			EPattern.put(entry.getKey(), entry.getValue()/maxEP);
		}
		for (Entry<String,Double> entry:NEPattern.entrySet()){
			NEPattern.put(entry.getKey(), entry.getValue()/maxNEP);
		}
	}
	public void ranking() throws IOException{
		for (Entry<String,Double> entry: EPattern.entrySet()){
			if (NEPattern.containsKey(entry.getKey()))
				EPattern.put(entry.getKey(),entry.getValue()*(1-NEPattern.get(entry.getKey())));
		}
		EPattern = sortPatternMap(EPattern);
		
		BufferedWriter patternWriter = new BufferedWriter(new FileWriter(OutFile));
		int topN = 100, i=0;
		for (Entry<String,Double> entry: EPattern.entrySet()){
			if (i++ < topN)
				System.out.println(entry.getKey()+" "+entry.getValue());
			patternWriter.write(entry.getKey()+"\t"+entry.getValue()+"\n");
			
		}
		patternWriter.close();
	}
	public void rankingDifference() throws IOException{
		for (Iterator<Map.Entry<String, Double>> it = EPattern.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, Double> entry = it.next();
			if (NEPattern.containsKey(entry.getKey())) {
				it.remove();
			}
		}
		EPattern = sortPatternMap(EPattern);
		
		BufferedWriter patternWriter = new BufferedWriter(new FileWriter(OutFile));
		int topN = 100, i=0;
		for (Entry<String,Double> entry: EPattern.entrySet()){
			if (i++ < topN)
				System.out.println(entry.getKey()+" "+entry.getValue());
			patternWriter.write(entry.getKey()+"\t"+entry.getValue()+"\n");
			
		}
		patternWriter.close();
	}
	private HashMap<String, Double> sortPatternMap(HashMap<String, Double> map) { 
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
	static public void main(String[] args) throws IOException{
		PatternRanking pr = new PatternRanking();
		pr.readFiles();
		pr.ranking();
		
	}
}
