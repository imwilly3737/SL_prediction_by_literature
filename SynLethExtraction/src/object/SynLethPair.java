package object;

public class SynLethPair { 
	public String GeneASymbol,GeneAid,GeneBSymbol,GeneBid,PubmedID,Evidence,Type,Species,Disease;
	public double Score;
	public SynLethPair() {
	}
	public SynLethPair(String line,String sep){
		String[] eTab=line.split(sep);
		GeneASymbol = eTab[0];
		GeneAid = eTab[1];
		GeneBSymbol = eTab[2];
		GeneBid = eTab[3];
		PubmedID = eTab[4];
		Evidence = eTab[5];
		Type = eTab[6];
		Species = "Human";
		Disease = eTab[7];
		Score = Double.parseDouble(eTab[8]);
	}
	@Override
	public String toString() {
		return GeneASymbol+"|"+GeneAid+"|"+GeneBSymbol+"|"+GeneBid+"|"+PubmedID+"|"+Evidence+"|"+Type+"|"+Species+"|"+Disease+"|"+Score;
	}
	
}
