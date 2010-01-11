package wordland.distance;

import liblinear.FeatureNode;

public class CosSimilarity implements DistanceMetric{
	public double distance(FeatureNode [] x,FeatureNode [] y) {
		double sum=0;
		int i,j;
		for (i=0,j=0;x!=null && y!=null && i<x.length && j<y.length;) {
			if (x[i].index<y[j].index) {
				i++;
			}
			else if (y[j].index<x[i].index) {
				j++;
			}
			else {
				sum+=x[i].value*y[j].value;
				i++;
				j++;
			}
		}
		return sum;
	}
}
