package wordland.classifier;
import liblinear.*;

import java.util.*;

import wordland.*;
import wordland.competitions.lsthc09.Params;
import wordland.data.BidirectionalMap;
import wordland.data.Hierarchy;
import wordland.data.ParameterExt;
import wordland.data.ProblemExt;
import wordland.utils.ProblemUtils;

/**
 * a class to do hierarchical svm classification
 * actually, it can use any kind of multiclass classifier in a hierarchical manner
 * but its name is not yet changed to reflect this *  
 */
public class HSVM extends HClassifier{
	private double bias=0;
	//n during training time
	private int n=-1;
	private Classifier embedded;
	private ParameterExt embeddedparam;
	public HSVM() {
	}
	public void train(ProblemExt prob,ParameterExt param) {
		if (param.embedded==null) {
			embedded=new SVM();
			embeddedparam=new ParameterExt();
			embeddedparam.param=new Parameter(Params.LibLinearType,1,0.0001);
		}
		else {
			embedded=param.embedded;
			embeddedparam=param.embeddedparam;
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
			int oldcat=test(prob.x[i],Hierarchy.root);
			ret[i]=prob.getNewCategory(oldcat);
		}
		return ret;
	}
	/**
	 * test a single document
	 */
	private int test(FeatureNode [] x,int node) {
		int ret;
		int [] children=trainhierarchy.getChildren(node);
		if (children==null) {
			return node;
		}
		if (children.length==1) {
			return test(x,children[0]);
		}
		Classifier cls=classifiers.get(node);
		int [] pred=cls.test(ProblemUtils.getProblem(x, bias, n));
		boolean ok=false;
		for (int i : children) {
			if (i==pred[0]) {
				ok=true;
			}
		}
		if (!ok) {
			System.out.println("[HSVM.test] predicted node in hierarchy is inexistent");
			return pred[0];
		}
		return test(x,pred[0]);
	}
	public Classifier newInstance() {
		return new HSVM();
	}
}
