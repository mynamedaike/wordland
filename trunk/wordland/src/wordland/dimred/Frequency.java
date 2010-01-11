package wordland.dimred;

import liblinear.*;
import java.util.*;

import wordland.*;
import wordland.data.BidirectionalMap;
import wordland.data.FeatureNodeComp2;
import wordland.data.ProblemExt;


/*
 * frequency based feature selection
 */
public class Frequency extends RankingMethod{
	private double [] freq;
	public Frequency () {		
	}
	public void collect(ProblemExt p) {
		freq=new double [p.n];
		for (int i=0;i<p.l;i++) {
			for (int j=0;j<p.x[i].length;j++) {
				freq[p.x[i][j].index]+=p.x[i][j].value;
			}
		}
		//for (int i=0; i<p.n; i++)
		//	if ((freq[i]>=2000) || (freq[i]<=10)) freq[i] = 0;
	}
	public void process() {
		ArrayList<FeatureNodeComp2> freqindexed=new ArrayList<FeatureNodeComp2>();
		for (int i=1;i<freq.length;i++) { //+1 because freq[0] is 0
			freqindexed.add(new FeatureNodeComp2(i,freq[i]));  
		}
		Collections.sort(freqindexed);
		Collections.reverse(freqindexed);
		termmap=new BidirectionalMap<Integer>();
		for (int i=0;i<nrf && i<freqindexed.size();i++) {
			termmap.addFirstSecond(freqindexed.get(i).index, i+1);
		}
	}
	public DimensionalityReduction newInstance() {
		return new Frequency();
	}
}
