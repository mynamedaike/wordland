package wordland.classifier;

import java.util.*;

import wordland.*;
import wordland.data.Hierarchy;
import wordland.data.IndexValue;
import wordland.data.ParameterExt;
import wordland.data.ProblemExt;
import wordland.utils.ProblemUtils;
import wordland.utils.Utils;
import liblinear.*;
import libsvm.*;

/**
 * this is also an abstract classifier that needs a classifier that is capable of returning class probabilities
 * it extends the data points it receives
 * The Combination of Text Classifiers Using Reliability Indicators
 * Paul N. Bennett, Susan T. Dumais, Eric Horvitz 
 */
public class CombinedClassifier implements PClassifier{
	private PClassifier embedded;  //final classifier
	private ParameterExt embparam;  //final parameters
	private Hashtable<Integer,PClassifier> experts=new Hashtable<Integer,PClassifier>();
	private int oldn;
	private double oldbias;
	public CombinedClassifier() {		
	}
	public void addExpertClassifier(int catnum,PClassifier cls) {
		experts.put(catnum, cls);
	}
	public void train(ProblemExt prob,ParameterExt param) {
		int expn=experts.size();
		if (param.embedded==null || !(param.embedded instanceof PClassifier)) {
			svm_parameter libsvm_par;
			libsvm_par = new svm_parameter();
			libsvm_par.svm_type = svm_parameter.C_SVC;
			libsvm_par.kernel_type = svm_parameter.LINEAR;
			libsvm_par.degree = 3;
			libsvm_par.gamma = 100;
			libsvm_par.coef0 = 1;
			libsvm_par.nu = 0.5;
			libsvm_par.cache_size = 40;
			libsvm_par.C = 1;
			libsvm_par.eps = 1e-3;
			libsvm_par.p = 0.1;
			libsvm_par.shrinking = 1;
			libsvm_par.probability = 1;
			libsvm_par.nr_weight = 0;
			libsvm_par.weight_label = new int[0];
			libsvm_par.weight = new double[0];
			embparam=new ParameterExt();
			embparam.libsvm_par=libsvm_par;
			embedded=new LIBSVM();
		}
		else {
			embparam=param.embeddedparam;
			embedded=(PClassifier)param.embedded.newInstance();
		}
		
		oldn=prob.n;
		oldbias=prob.bias;
		
		ProblemExt newprob=new ProblemExt();
		newprob.l=prob.l;
		newprob.catnum=prob.catnum;
		newprob.bias=prob.bias;
		newprob.hierarchy=new Hierarchy(prob.hierarchy);
		newprob.n=prob.n+expn;
		newprob.copyHashes(prob);
		newprob.y=prob.y;
		newprob.x=new FeatureNode [newprob.l][];
		for (int i=0;i<newprob.l;i++) {
			newprob.x[i]=new FeatureNode[prob.x[i].length+expn];
			for (int j=0;j<prob.x[i].length;j++) {
				newprob.x[i][j]=new FeatureNode(prob.x[i][j].index,prob.x[i][j].value);
			}
		}
		for (int i=0;i<prob.l;i++) {
			FeatureNode [] exp=expertPredictions(prob.x[i],prob.bias,oldn);
			for (int j=0;j<exp.length;j++) {
				newprob.x[i][prob.x[i].length+j]=exp[j];
			}
		}
		embedded.train(newprob, embparam);
	}
	//test has to return categories with NEW ids
	public int [] test(ProblemExt prob) {
		int [] ret=Utils.convertProbabilitiesToBestClass(testp(prob));
		return ret;
	}
	private FeatureNode [] expertPredictions(FeatureNode [] x,double bias,int max) {
		int expn=experts.size();
		int expindex=0;  //the index of the expert, relative to the end of the old document
		FeatureNode [] ret=new FeatureNode [expn];
		ProblemExt tempprob=ProblemUtils.getProblem(x, bias, max);
		for (int cid : experts.keySet()) {  //cid will contain the categories identified by the experts
			PClassifier cls=experts.get(cid);
			IndexValue [][] pred=cls.testp(tempprob);
			int j;
			//select the prediction for the actual category id
			for (j=0;j<pred[0].length && pred[0][j].index!=cid;j++);
			double val=0;
			if (j<pred[0].length) {
				val=pred[0][j].value;
			}
			ret[expindex++]=new FeatureNode(max+expindex,val);
		}
		return ret;
	}
	public IndexValue [][] testp(ProblemExt p) {
		IndexValue [][] ret=new IndexValue[p.l][];
		for (int i=0;i<p.l;i++) {
			ret[i]=testp(p.x[i],oldbias,oldn);
		}
		return ret;
	}
	private IndexValue [] testp(FeatureNode [] x,double bias,int max) {
		ProblemExt testp=ProblemUtils.getProblem(x, bias, max);
		FeatureNode [] exp=expertPredictions(x,bias,max);
		FeatureNode [] newx=new FeatureNode[testp.x[0].length+exp.length];
		for (int i=0;i<x.length;i++) {
			newx[i]=x[i];
		}
		for (int i=0;i<exp.length;i++) {
			newx[i+x.length]=exp[i];
		}
		testp=ProblemUtils.getProblem(newx, bias, max+exp.length);
		IndexValue [][] ret=embedded.testp(testp);
		return ret[0];
	}
	public Classifier newInstance() {
		return (new CombinedClassifier());
	}
}
