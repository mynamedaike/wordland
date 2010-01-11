package wordland.classifier;

import liblinear.*;
import libsvm.*;

import java.util.*;

import wordland.*;
import wordland.data.BidirectionalMap;
import wordland.data.Hierarchy;
import wordland.data.IndexValue;
import wordland.data.ParameterExt;
import wordland.data.ProblemExt;
import wordland.utils.ProblemUtils;

/**
 * just an test of a simple method of hierarchical classification 
 * largely based on HSVM
 */
public class HierarchicalClassifiers1 extends HClassifier{
	private double bias=0;
	//n during training time
	private int n=-1;
	private PClassifier embedded;
	private ParameterExt embeddedparam;
	public HierarchicalClassifiers1() {
	}
	public void train(ProblemExt prob,ParameterExt param) {
		if (param.embedded!=null && param.embedded instanceof PClassifier) {
			embedded=(PClassifier)param.embedded;
			embeddedparam=param.embeddedparam;
		}
		else {
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
			embeddedparam=new ParameterExt();
			embeddedparam.libsvm_par=libsvm_par;
			embedded=new LIBSVM();			
		}
		traincategories=new BidirectionalMap<Integer>(prob.getCategoryMap());
		trainparams=param;
		trainhierarchy=prob.hierarchy;
		bias=prob.bias;
		n=prob.n;
		train(prob,prob.hierarchy,param,Hierarchy.root);
	}
	/*
	 * builds lots of classifiers
	 */
	private void train(ProblemExt prob,Hierarchy hier,ParameterExt param,int node) {
		int [] children = hier.getChildren(node);
		if (children==null) {
			return;
		}
		if (children.length==1) {
			train(prob,hier,param,children[0]);
		}
		else {
			//we store 	a classifier for each node
			Classifier cls=embedded.newInstance();
			classifiers.put(node, cls);
			//we store the leaves starting from all children into a problem
			ProblemExt newprob=ProblemUtils.mergeAllChildren(prob, hier, children,false,false);
			cls.train(newprob, embeddedparam);
			for (int i=0;i<children.length;i++) {
				train(prob,hier,param,children[i]);
			}
		}
	}
	public int [] test(ProblemExt prob) {
		int [] ret=new int [prob.l];
		for (int i=0;i<prob.l;i++) {
			Hashtable<Integer,Double> nodevalues=new Hashtable<Integer,Double>();
			nodevalues.put(Hierarchy.root, 0.0);
			test(prob.x[i],Hierarchy.root, 0.0, nodevalues);
			double max=0;
			int oldcat=0;
			for (int cat : nodevalues.keySet()) {
				double v=nodevalues.get(cat);
				if (v>max) {
					max=v;
					oldcat=cat;
				}
			}
			ret[i]=prob.getNewCategory(oldcat);
		}
		return ret;
	}
	/**
	 * test a single document
	 */
	private void test(FeatureNode [] x,int node,double nodevalue, Hashtable<Integer,Double> values) {
		int [] children=trainhierarchy.getChildren(node);
		if (children==null) {
			values.put(node, nodevalue);
			return;
		}
		if (children.length==1) {
			test(x,children[0],nodevalue,values);
		}
		else {
			PClassifier cls=(PClassifier)classifiers.get(node);
			IndexValue [][] pred=cls.testp(ProblemUtils.getProblem(x, bias, n));
			for (int child : children) {
				int i=0;
				for (;i<pred[0].length && pred[0][i].index!=child;i++);
				if (i<pred[0].length) {
					test(x,child,nodevalue+pred[0][i].value,values);
				}
				else {
					System.out.println("[HierarchicalClassifiers1.test] predicted node in hierarchy is inexistent");
				}
			}
		}
	}
	public Classifier newInstance() {
		return new HierarchicalClassifiers1();
	}

}
