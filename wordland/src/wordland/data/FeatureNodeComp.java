package wordland.data;

import liblinear.FeatureNode;

/**
 * a FeatureNode that is comparable based on index
 */

public class FeatureNodeComp extends FeatureNode implements Comparable<FeatureNode>{
	public FeatureNodeComp(int index,double value) {
		super(index,value);
	}
	public int compareTo(FeatureNode o) {
		if (!(o instanceof FeatureNode)) {
			return -1;
		}
		else {
			return index-((FeatureNode)o).index;
		}
	}
	public String toString() {
		return "("+index+":"+value+")";
	}
}
