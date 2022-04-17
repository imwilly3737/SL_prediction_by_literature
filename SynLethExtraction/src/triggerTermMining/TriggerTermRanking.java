package triggerTermMining;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import patternMining.PatternRanking;

public class TriggerTermRanking {
	private String ETFile,NETFile,OutFile,specificD;
	private HashMap<String,Double> ETrigger,NETrigger;
	private double maxET,maxNET;
	private boolean isPrintScreen;
	public TriggerTermRanking(String disease){
		this(disease,false);
	}
	public TriggerTermRanking(String disease, boolean isPrint) {
		specificD = disease;
		if (specificD == null){
			ETFile = "./allTriggerTerms.txt";
			NETFile = "./allTriggerTerms_not.txt";
			OutFile = "./allTriggerTermsRanked.txt";
		}else{
			ETFile = "./allTriggerTerms_"+specificD+".txt";
			NETFile = "./allTriggerTerms_not_"+specificD+".txt";
			OutFile = "./allTriggerTermsRanked_"+specificD+".txt";
		}
		setETrigger(new HashMap<>());
		NETrigger = new HashMap<>();
		maxET = maxNET = -1;
		isPrintScreen=isPrint;
	}
	public void readFiles() throws IOException{
		BufferedReader ebr = new BufferedReader(new FileReader(ETFile));
		while (ebr.ready()){
			String line = ebr.readLine();
			String trigger = line.substring(0,line.indexOf("\t"));
			double number = Double.parseDouble(line.substring(line.indexOf("\t")+2));
			if (trigger.length() > 1)
				getETrigger().put(trigger, number);
			if (maxET <0 )
				maxET = number;
			if (number < (maxET/100) )
				break;
		}
		ebr.close();
		BufferedReader nebr = new BufferedReader(new FileReader(NETFile));
		while (nebr.ready()){
			String line = nebr.readLine();
			String trigger = line.substring(0,line.indexOf("\t"));
			double number = Double.parseDouble(line.substring(line.indexOf("\t")+2));
			if (trigger.length() > 1)
				NETrigger.put(trigger, number);
			if (maxNET <0 )
				maxNET = number;
			if (number < (maxNET/100) )
				break;
		}
		nebr.close();
		for (Entry<String,Double> entry: getETrigger().entrySet()){
			getETrigger().put(entry.getKey(), entry.getValue()/maxET);
		}
		for (Entry<String,Double> entry:NETrigger.entrySet()){
			NETrigger.put(entry.getKey(), (entry.getValue()+1)/(maxNET+1));
		}
	}
	public void ranking() throws IOException{
		for (Entry<String,Double> entry: getETrigger().entrySet()){
			if (NETrigger.containsKey(entry.getKey()))
				//ETrigger.put(entry.getKey(),entry.getValue()*(1-NETrigger.get(entry.getKey()))); //first ranking method
				getETrigger().put(entry.getKey(), entry.getValue()/NETrigger.get(entry.getKey()));
			else
				getETrigger().put(entry.getKey(),entry.getValue()*(maxNET+1));
		}
		setETrigger(sortMap(getETrigger()));
		
		BufferedWriter triggerWriter = new BufferedWriter(new FileWriter(OutFile));
		int topN = 100, i=0;
		for (Entry<String,Double> entry: getETrigger().entrySet()){
			if (i++ < topN && isPrintScreen)
				System.out.println(entry.getKey()+" "+entry.getValue());
			triggerWriter.write(entry.getKey()+"\t"+entry.getValue()+"\n");
			
		}
		triggerWriter.close();
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
	static public void main(String[] args) throws IOException{
		//breast cancer = "D001943";
		//pancreatic cancer = "D010190";
		//ovarian cancer="D010051";
		//colon cancer="D003110";
		ArrayList<String> ids = new ArrayList<String>(){{add("D001943");add("D010051");}};
		TriggerTermRanking ttr = new TriggerTermRanking(null,true);
		ttr.readFiles();
		ttr.ranking();
		
	}
	public HashMap<String,Double> getETrigger() {
		return ETrigger;
	}
	public void setETrigger(HashMap<String,Double> eTrigger) {
		ETrigger = eTrigger;
	}
}
