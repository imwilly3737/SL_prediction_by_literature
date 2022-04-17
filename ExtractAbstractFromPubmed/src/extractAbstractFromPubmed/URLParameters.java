package extractAbstractFromPubmed;

public class URLParameters {
	public int retMax;
	public int retStart;
	public String queryWord;
	public String organism;
	public URLParameters(){
		retMax = 500000;		// the max number of abstracts you want to extract
		retStart = 0;			// index of first record returned
		queryWord = "cancer";	// query word you want to search in pubmed
		organism = "human";		// organism you want to search in pubmed
	}
	public URLParameters(int retMax,int retStart,String queryWord,String organism){
		this.retMax = retMax;
		this.retStart = retStart;
		this.queryWord = queryWord;
		this.organism = organism;
	}
}
