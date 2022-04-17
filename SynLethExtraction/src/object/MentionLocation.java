package object;

/**
 * �摮ention雿蔭��� <br>
 * Mention: 甇�蝣箇����迂��������MID��������D(�虜Mention靘�Training��esting Set)
 * @author iir
 *
 */
public class MentionLocation{
	/**
	 * 甇孑ention��絲憪�蔭
	 */
	public int start;
	/**
	 * 甇孑ention�����蔭
	 */
	public int end;
	/**
	 * 甇孑ention���
	 */
	public String type;
	/**
	 * 甇孑ention����葡
	 */
	public String mentionStr;
	/**
	 * 甇孑ention 撠���D(��銝銝���)
	 */
	public String[] mentionID;
	/**
	 * 甇孑ention�����D(PMID)
	 */
	public String docID;
	
	public MentionLocation(){
		start=0;
		end=0;
		type=null;
		mentionStr=null;
		mentionID=null;
		docID=null;
	}
	
	public MentionLocation(int s,int e,String t,String mStr,String[] mID,String dID){
		start=s;
		end=e;
		type=t;
		mentionStr=mStr;
		mentionID=mID;
		docID=dID;
	}
	public MentionLocation(MentionLocation oMen) {
		start=oMen.start;
		end=oMen.end;
		type=oMen.type;
		mentionStr=oMen.mentionStr;
		mentionID=oMen.mentionID;
		docID=oMen.docID;
	}

	/**
	 * 撠entionLocation頛詨���葡嚗撘�:"mentionStr(start,end)[mentionID,docID]" <br>
	 * �銝剛����entionID,�������
	 */
	public String toString(){
		String mentionIDStr="";
		for (String e:mentionID)
			if (mentionIDStr.length()>0)
				mentionIDStr+=","+e;
			else
				mentionIDStr=e;
		return mentionStr+"("+start+","+end+")"+"["+mentionIDStr+"|"+docID+"]";
	}
}
