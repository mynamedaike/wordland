package wordland.classifier;

import java.util.ArrayList;
import java.util.Collections;

import wordland.*;
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
}
