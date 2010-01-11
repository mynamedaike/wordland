package wordland.distance;

import liblinear.FeatureNode;

public class GaussSimilarity implements DistanceMetric {
	public double sigma = 1;
	public void setSigma(double s) {
		sigma = s;
	}
	public double distance(FeatureNode [] x,FeatureNode [] y) {
		double sum=0;
		int i,j;
		for (i=0,j=0;x!=null && y!=null && i<x.length && j<y.length;) {
			if (x[i].index<y[j].index) {
				sum+=x[i].value*x[i].value;
				i++;
			}
			else if (y[j].index<x[i].index) {
				sum+=y[j].value*y[j].value;
				j++;
			}
			else {
				sum+=(x[i].value-y[j].value)*(x[i].value-y[j].value);
				i++;
				j++;
			}
		}
		for (;x!=null && i<x.length;i++) {
			sum+=x[i].value*x[i].value;
		}
		for (;y!=null && j<y.length;j++) {
			sum+=y[j].value*y[j].value;
		}
		return Math.exp(-sum/(2*Math.pow(sigma, 2)));
	}
}
