package correlation;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

public class TestCorelation {
	public static void main (String args[]) throws ClassNotFoundException, IOException, SQLException{
		/*SpearmansCorrelation spc = new SpearmansCorrelation();
		double[] xArray = {0.011,4.282,4.136,4.998,1.493},yArray = {-7.488,-5.104,-7.301,-7.37,-7.329};
		System.out.println(spc.correlation(xArray, yArray));*/
		CorrelationBetweenGenes cbg = new CorrelationBetweenGenes("CCLE");
		cbg.setEntities("6240", "1351", "D003110");
		cbg.spearman();
		System.out.println(cbg.getCoe()+" "+cbg.getP());
	}
}
