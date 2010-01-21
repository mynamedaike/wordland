package wordland.classifier;

import java.util.ArrayList;
import java.util.Collections;

import wordland.*;
import wordland.competitions.lsthc09.Params;
import wordland.data.IndexValue;
import wordland.data.ParameterExt;
import wordland.data.ProblemExt;

import liblinear.*;
import libsvm.svm;
import libsvm.svm_problem;

/**
 * wrapper around liblinear
 * it's not truly probabilistic, but it returns the decision values
 */

public class SVM implements PClassifier{
	private Model model;
	private Linear linear=new Linear();
	public SVM () {		
	}
	public void train(ProblemExt prob,ParameterExt param) {
		model=linear.train(prob, param.param);
	}
	public int [] test(ProblemExt prob){
		int [] ret=new int [prob.l];
		for (int i=0;i<prob.l;i++) {
			ret[i]=linear.predict(model, prob.x[i]);
		}
		return ret;
	}
	public Classifier newInstance() {
		return new SVM();
	}
	public IndexValue [][] testp(ProblemExt prob) {
		int [] labels = model.getLabels();
		double [] pp = new double [model.getNrClass()];
		IndexValue [][] ret=new IndexValue[prob.l][model.getNrClass()];
		ArrayList<IndexValue> array=new ArrayList<IndexValue>();
		for (int i=0; i<prob.l; i++) {
			array.clear();
			linear.predictProbability(model, prob.x[i], pp);
			for (int j=0;j<pp.length;j++) {
				array.add(new IndexValue(labels[j],pp[j]));
			}
			Collections.sort(array);
			for (int j=0;j<array.size();j++) {
				ret[i][j]=array.get(j);
			}
		}
		return ret;			
	}
	public double [] testDecValues(ProblemExt prob) {
		double [] ret = new double [prob.l];
		for (int i=0; i<prob.l; i++)
			ret[i]=decisionValue(prob.x[i]);
		return ret;
	}
	private double decisionValue(FeatureNode [] x) {
		double [] dec = new double [model.getNrClass()];
		int [] labs = new int [model.getNrClass()];
		double decv;
		labs = model.getLabels();
		Linear.predictValues(model, x, dec);
		decv = (labs[0]*2-1)*dec[0]; //?????????????
		return decv;
	}
}
