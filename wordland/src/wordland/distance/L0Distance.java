package wordland.distance;

import liblinear.FeatureNode;

public class L0Distance implements DistanceMetric{
	public double distance(FeatureNode [] x,FeatureNode [] y) {
		double sum=0;
		int i,j;
		for (i=0,j=0;x!=null && y!=null && i<x.length && j<y.length;) {
			if (x[i].index<y[j].index) {
				sum+=1;
				i++;
			}
			else if (y[j].index<x[i].index) {
				sum+=1;
				j++;
			}
			else {
				if (x[i].value-y[j].value!=0) {
					sum+=1;
				}
				i++;
				j++;
			}
		}
		if (x!=null && i<x.length) {
			sum+=x.length-i;
		}
		if (y!=null && j<y.length) {
			sum+=y.length-j;
		}
		return sum;
	}	
}
