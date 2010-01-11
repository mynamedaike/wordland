package wordland.distance;

import liblinear.FeatureNode;

/**
 * it's cheaper to copy the code from cossimilarity than to create a new object of it each time, too bad static doesn't work here
 */
public class CosDistance implements DistanceMetric{
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
		return (sum!=0)?1.0/sum:1000000;
	}
}
