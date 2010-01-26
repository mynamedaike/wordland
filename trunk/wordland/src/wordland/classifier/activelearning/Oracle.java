package wordland.classifier.activelearning;

import wordland.data.*;
import java.util.*;

/**
 * An oracle that will tell the correct label for a data point from a problem (usually by looking it up from the same data source).
 */
public interface Oracle {
	public boolean queryLabels(List<Integer> q,double [] allpredictions); 
	public int getLabel(ProblemExt p,int i);
}
