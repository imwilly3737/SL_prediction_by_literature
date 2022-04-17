package tDistribution;

import org.apache.commons.math3.distribution.TDistribution;

public class TestTDistribution {
	public static void main(String[] args){
		TDistribution td = new TDistribution(8);
		System.out.println(td.cumulativeProbability(-0.5049782));
	}
}
