package wordland.classifier;

import liblinear.*;
import java.util.*;

import wordland.*;
import wordland.data.ParameterExt;
import wordland.data.ProblemExt;
import wordland.data.ValueIndex;
import wordland.distance.*;

/**
 * k Nearest Neighbor classification
 * only stores a reference to the training data, not the whole data again (as it needs to)
 */
public class KNN implements Classifier {
	private int k=1;
	private ProblemExt traindata;
	private DistanceMetric dist=new EuclideanDistance();
	public KNN() {		
	}
	public Classifier newInstance() {
		return new KNN();
	}

	public void train(ProblemExt prob, ParameterExt param) {
		if (param.knn_k>0) {
			k=param.knn_k;
		}
		if (param.distance!=null) {
			dist=param.distance;
		}
		traindata=prob;
	}

	public int[] test(ProblemExt prob) {
		System.out.println("kNN testing...");
		int [] ret=new int[prob.l];
		for (int i=0;i<prob.l;i++) {
			if (i%100==0) {
				System.out.println("["+i+"]");
			}
			ret[i]=test(prob.x[i]);
		}
		return ret;
	}
	
	private int test(FeatureNode [] x) {
		ValueIndex [] best = new ValueIndex[k];
		int newk=0;
		for (int i=0;i<traindata.l;i++) {
			double d=dist.distance(x, traindata.x[i]);
			boolean skip=false;
			for (int j=0;j<k && !skip;j++) {
				if (best[j]==null) {
					best[j]=new ValueIndex(traindata.y[i],d);
					skip=true;
					newk++;
				}
				else if (best[j].value>d) {
					for (int jj=((newk<k-1) ? newk:k-1);jj>j;jj--) {
						best[jj]=best[jj-1];
					}
					best[j]=new ValueIndex(traindata.y[i],d);
					skip=true;
					if (newk<k) {
						newk++;
					}
				}
			}
		}
		Hashtable<Integer,Integer> occ=new Hashtable<Integer,Integer>();
		int max=0,most=0;
		for (int i=0;i<k;i++) {
			if (best[i]!=null) {
				Integer frq=occ.get(best[i].index);
				int val;
				if (frq==null) {
					val=1;
				}
				else {
					val=1+frq.intValue();
				}
				occ.put(best[i].index, val);
				if (val>max) {
					max=val;
					most=best[i].index;
				}				
			}
		}
		return most;
	}
}
