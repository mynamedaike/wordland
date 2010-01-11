package wordland.distance;
import liblinear.*;

/**
 * Kullback–Leibler divergence
 * when there's no corresponding y[i] to x[i] then infinity should be added to the divergence, this is simulated with the "big" value
 */
public class KLDivergence implements DistanceMetric{
	public double distance(FeatureNode [] x,FeatureNode [] y) {
		double sum=0;
		double big=100000;
		int i,j;
		for (i=0,j=0;x!=null && y!=null && i<x.length && j<y.length;) {
			if (x[i].index<y[j].index) {
				sum+=big;
				i++;
			}
			else if (y[j].index<x[i].index) {
				j++;
			}
			else {
				sum+=x[i].value*Math.log(x[i].value/y[j].value);
				i++;
				j++;
			}
		}
		for (;x!=null && i<x.length;i++) {
			sum+=big;
		}
		return sum;
	}
}