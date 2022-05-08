package essentialGene;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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
import java.util.Set;

import triggerTermMining.TriggerTermRanking;

public class EssentialTriggerTermMultiCancer {
	List<String> diseases;
	List<TriggerTermRanking> ttrs;
	String OutFile;
	HashMap<String,Double> eTrigger;
	public EssentialTriggerTermMultiCancer(List<String> ids) throws IOException {
		diseases = ids;
		ttrs = new ArrayList<TriggerTermRanking>();
		for (int i=0;i<diseases.size();i++){
			ttrs.add(new TriggerTermRanking(diseases.get(i)));
			ttrs.get(i).readFiles();
			ttrs.get(i).ranking();
		}
		OutFile = "./allTriggerTermsRanked.txt";
		eTrigger = new HashMap<>();
	}
	public void extractCommonTrigger() throws IOException{
		
		
		for (int i=0;i<ttrs.size();i++){
			for (Entry<String,Double> entry: ttrs.get(i).getETrigger().entrySet()){
				if (eTrigger.containsKey(entry.getKey()))
					eTrigger.put(entry.getKey(), eTrigger.get(entry.getKey())+entry.getValue());
				else
					eTrigger.put(entry.getKey(), entry.getValue());
			}
		}
		eTrigger = sortMap(eTrigger);
		BufferedWriter triggerWriter = new BufferedWriter(new FileWriter(OutFile));
		int topN = 100,printI=0;
		for (Entry<String,Double> entry: eTrigger.entrySet()){
			if (printI++ < topN)
				System.out.println(entry.getKey()+" "+entry.getValue());
			triggerWriter.write(entry.getKey()+"\t"+entry.getValue()+"\n");
			
		}
		triggerWriter.close();
	}
	public void extractIntersectionTrigger() throws IOException{
		Set<String> intersection = null;
		for (int i=0;i<ttrs.size();i++){
			if (intersection == null)
				intersection = ttrs.get(i).getETrigger().keySet();
			else
				intersection.retainAll(ttrs.get(i).getETrigger().keySet());
			for (Entry<String,Double> entry: ttrs.get(i).getETrigger().entrySet()){
				if (eTrigger.containsKey(entry.getKey()))
					eTrigger.put(entry.getKey(), eTrigger.get(entry.getKey())+entry.getValue());
				else
					eTrigger.put(entry.getKey(), entry.getValue());
			}
			
		}
		eTrigger = sortMap(eTrigger);
		BufferedWriter triggerWriter = new BufferedWriter(new FileWriter(OutFile));
		int topN = 100,printI=0;
		for (Entry<String,Double> entry: eTrigger.entrySet()){
			if (intersection.contains(entry.getKey())){
				if (printI++ < topN)
					System.out.println(entry.getKey()+" "+entry.getValue());
				triggerWriter.write(entry.getKey()+"\t"+entry.getValue()+"\n");
			}
			
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
		ArrayList<String> ids = new ArrayList<String>(){{add("D003110");add("D001943");add("D010051");add("D010190");}};
		EssentialTriggerTermMultiCancer egmc = new EssentialTriggerTermMultiCancer(ids);
		egmc.extractCommonTrigger();
		//egmc.extractIntersectionTrigger();
		
	}
}
