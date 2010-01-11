package wordland.data;

import liblinear.FeatureNode;

public class FeatureNodeComp2 extends FeatureNode implements Comparable<FeatureNode>{
	public FeatureNodeComp2(int index,double value) {
		super(index,value);
	}
	public int compareTo(FeatureNode o) {
		if (!(o instanceof FeatureNode)) {
			return -1;
		}
		else {			
			return (int)Math.signum(value-((FeatureNode)o).value);
		}
	}
	public String toString() {
		return "("+index+":"+value+")";
	}
}
