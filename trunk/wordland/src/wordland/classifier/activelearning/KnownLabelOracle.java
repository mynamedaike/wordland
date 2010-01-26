package wordland.classifier.activelearning;

import wordland.data.*;
import java.util.*;

public class KnownLabelOracle implements Oracle {
	public KnownLabelOracle() {		
	}
	
	public boolean queryLabels(List<Integer> q,double [] allpredictions) {
		return true;
	}
	public int getLabel(ProblemExt p,int i) {
		return p.y[i];
	}
}
