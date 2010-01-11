package wordland.utils;
import wordland.data.ProblemExt;
import liblinear.*;

/*
 * a class to implement tf-idf term weighting
 */
public class TFIDF {
	private double [] tf;  //won't be used, maybe in another version of tf-idf
	private int [] df;
	private double [] idf;
	public TFIDF() {
	}
	public void count(ProblemExt p) {
		tf=new double [p.n];
		df=new int [p.n];
		for (int i=0;i<p.l;i++) {
			for (int j=0;j<p.x[i].length;j++) {
				int index=p.x[i][j].index;
				df[index]++;
				tf[index]+=p.x[i][j].value;
			}
		}
		idf=new double [p.n];
		for (int i=0;i<p.n;i++) {
			if (df[i]>0) {
				idf[i]=Math.log(((double)p.l)/df[i]);
			}
			else {
				idf[i]=0;
			}
		}
	}
	private FeatureNode [] transform(ProblemExt p,int inst) {
		FeatureNode [] ret=new FeatureNode [p.x[inst].length];
		double docsize=0;
		for (int i=0;i<p.x[inst].length;i++) {
			docsize+=p.x[inst][i].value;
		}
		for (int i=0;i<p.x[inst].length;i++) {
			int index=p.x[inst][i].index;
			double val=p.x[inst][i].value/docsize*idf[index];
			ret[i]=new FeatureNode(index,val);
		}
		return ret;
	}
	public ProblemExt transform(ProblemExt p) {
		ProblemExt ret=new ProblemExt(p);
		for (int i=0;i<p.l;i++) {
			ret.x[i]=transform(p,i);
		}
		return ret;
	}
}
