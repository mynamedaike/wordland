package wordland.classifier;

import libsvm.*;
import java.util.*;

import wordland.*;
import wordland.data.IndexValue;
import wordland.data.ParameterExt;
import wordland.data.ProblemExt;
import wordland.utils.ProblemUtils;

public class LIBSVM implements PClassifier{
	private svm_model model;
	private int probability=0;
	private int traincatnum;
	public LIBSVM() {
	}
	public static svm_problem convert(ProblemExt prob) {
		svm_problem pr = new svm_problem();
		pr.l = prob.l;
		pr.y = new double [prob.y.length];
		pr.x = new svm_node [prob.x.length][];
		for (int i=0; i<prob.x.length; i++) {
			pr.y[i] = prob.y[i];
			pr.x[i] = new svm_node [prob.x[i].length];
			for (int j=0; j<prob.x[i].length; j++) {
				pr.x[i][j] = new svm_node();
				pr.x[i][j].index = prob.x[i][j].index;
				pr.x[i][j].value = prob.x[i][j].value;
			}
		}
		return pr;
	}
	public void train(ProblemExt prob, ParameterExt param) {
		svm_problem pr = convert(prob);
		traincatnum=prob.catnum;
		probability=param.libsvm_par.probability;
		model = svm.svm_train(pr, param.libsvm_par);
	}
	public int [] test(ProblemExt prob) {
		svm_problem pr = convert(prob);
		int [] pred = new int [prob.l];
		for (int i=0; i<prob.l; i++) {
			pred[i] = (int)svm.svm_predict(model, pr.x[i]);
		}
		return pred;
	}
	public Classifier newInstance() {
		return new LIBSVM();
	}
	public IndexValue [][] testp(ProblemExt prob) {
		if (probability!=1) {
			int [] retcls=test(prob);
			return convertToProbability(retcls,ProblemUtils.getCategories(prob));
		}
		else {
			svm_problem pr = convert(prob);
			int [] labels = new int [traincatnum];
			svm.svm_get_labels(model, labels);
			double [] pp = new double [traincatnum];
			IndexValue [][] ret=new IndexValue[prob.l][traincatnum];
			ArrayList<IndexValue> array=new ArrayList<IndexValue>();
			for (int i=0; i<prob.l; i++) {
				array.clear();
				svm.svm_predict_probability(model, pr.x[i],pp);
				for (int j=0;j<pp.length;j++) {
					array.add(new IndexValue(labels[j],pp[j]));
				}
				Collections.sort(array);
				for (int j=0;j<traincatnum;j++) {
					ret[i][j]=array.get(j);
				}
			}
			return ret;			
		}
	}
	public static IndexValue [][] convertToProbability(int [] cls,HashSet<Integer> cats) {
		int nr=cats.size();
		IndexValue [][] ret=new IndexValue [cls.length][nr];
		ArrayList<IndexValue> array=new ArrayList<IndexValue>();
		for (int i=0;i<cls.length;i++) {
			array.clear();
			for (int j : cats) {
				array.add(new IndexValue(j,cls[i]==j?1.0:0));
			}
			Collections.sort(array);
			for (int j=0;j<nr;j++) {
				ret[i][j]=array.get(j);
			}
		}
		return ret;
	}
}
