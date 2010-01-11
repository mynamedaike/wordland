package wordland.dimred;

import wordland.*;
import wordland.cluster.*;
import wordland.data.ParameterExt;
import wordland.data.ProblemExt;
import wordland.distance.*;
import wordland.utils.Utils;
import liblinear.*;

/**
 * clustering based feature selection
 * needs the input data to use only the first catnum-1 values as category ids
 */
public class ClusteringMethod implements DimensionalityReduction {
	protected Clusterer clusterer;
	protected DistanceMetric dist;
	protected ParameterExt param;
	protected ProblemExt stat;
	protected int [] map;
	public ClusteringMethod(Clusterer c,DistanceMetric d,ParameterExt p) {
		clusterer=c;
		dist=d;
		param=p;
	}
	public void collect(ProblemExt p) {
		System.out.println("started data collection for clustering fs...");
		stat=new ProblemExt();
		stat.l=p.n;  //we'll have a point for each word in the corpus
		stat.n=p.catnum+1;  //and each point will have as many dimensions as many categories there are
		//we'll scan through the whole corpus to count the occurence of words in the categories
		stat.x=new FeatureNode[stat.l][];
		for (int i=1;i<stat.l;i++) {
			if (i%100==1) {
				System.out.println("["+i+"/"+stat.l+"]");
			}
			double [] occ=new double [stat.n];
			for (int j=0;j<p.l;j++) {
				double v=Utils.getFeatureValue(p.x[j], i);
				occ[p.y[j]+1]+=v;  //+1 same
			}			
			FeatureNode [] temp=Utils.sparsify(occ);
			if (param.normalization==null) {
				stat.x[i]=temp;
			}
			else {
				stat.x[i]=Utils.normalize(temp, param.normalization);
			}
		}
	}
	public void process() {
		map=clusterer.cluster(stat, dist, param);
	}
	public ProblemExt remap(ProblemExt p) {
		ProblemExt ret=new ProblemExt(p);
		ret.l=p.l;
		ret.catnum=p.catnum;
		ret.n=param.clusters+1;  //+1 the usual
		ret.y=new int [ret.l];
		ret.x=new FeatureNode[ret.l][];
		ret.bias=0;
		for (int i=0;i<ret.l;i++) {
			ret.x[i]=remap(p.x[i],map,param.clusters);
			ret.y[i]=p.y[i];
		}
		ret.copyCategoryHash(p);
		ret.createDummyTermHash(ret.n);
		return ret;
	}
	private FeatureNode[] remap(FeatureNode[] x,int [] map,int k) {
		double [] temp=new double[k+1];
		for (FeatureNode f : x) {
			int m=map[f.index]+1;  //+1 the usual
			temp[m]+=f.value;
		}
		return Utils.sparsify(temp);
	}
	public void setNrFeatures(int i) {
		param.clusters=i;
	}
	public int getNrFeatures() {
		return param.clusters;
	}
	public DimensionalityReduction newInstance() {
		return new ClusteringMethod(clusterer,dist,param);
	}
	public void setParameters(ParameterExt p) {
		param=p;
	}
}
