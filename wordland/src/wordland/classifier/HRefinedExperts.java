package wordland.classifier;

import liblinear.*;
import libsvm.*;

import java.util.*;

import wordland.*;
import wordland.competitions.lsthc09.Params;
import wordland.data.BidirectionalMap;
import wordland.data.Hierarchy;
import wordland.data.ParameterExt;
import wordland.data.ProblemExt;
import wordland.utils.ProblemUtils;

/**
 * Hierarchical Refined Experts - from:
 * Refined Experts - Improving Classification in Large Taxonomies
 * Paul Bennett, Nam Nguyen
 * SIGIR 2009
 * uses LIBSVM as embedded classifier, so the libsvm_par should be set
 * uses the children's cousins as expert predictors - as in the paper
 * i hope i got this right :)
 */

public class HRefinedExperts extends HClassifier{
	private double bias=0;
	//n during training time
	private int n=-1;	
	private int [] first;
	private int [] correct;
	//the index that the other class gets
	private static int otheri=1000000;
	private PClassifier svm=null;
	private ParameterExt svmpar;	
	//this is needed for the leaf classifiers
	private PClassifier firstpredictor;
	//this is needed for the node classifiers for the "cousins", it will need to have PClassifiers as embedded so we use LIBSVM again
	private HClassifier hclassifier=new HSVM();
	private ParameterExt innerparam;
	public HRefinedExperts() {		
	}
	public void train(ProblemExt prob,ParameterExt param) {		
		if (param.embedded!=null && param.embedded instanceof PClassifier) {
			svm=(PClassifier)param.embedded;
			svmpar=param.embeddedparam;
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
			svmpar=new ParameterExt();
			svmpar.libsvm_par=libsvm_par;
			svmpar.embeddedparam=new ParameterExt();
			svmpar.embeddedparam.libsvm_par=libsvm_par;
			svm=new LIBSVM();
		}
		
		if (param.embeddedparam2!=null) {
			innerparam=param.embeddedparam2;
		}
		else {
			Parameter svmparam2=new Parameter(Params.LibLinearType,1,0.01);
			innerparam=new ParameterExt();
			innerparam.embeddedparam=new ParameterExt();
			innerparam.embeddedparam.param=svmparam2;
		}
		
		traincategories=new BidirectionalMap<Integer>(prob.getCategoryMap());
		trainparams=param;
		trainhierarchy=prob.hierarchy;
		bias=prob.bias;
		n=prob.n;
		firstpredictor=(PClassifier)svm.newInstance();
		firstpredictor.train(prob, svmpar);
		/*for (int i=0;i<prob.l;i++) {
			classifiers.put(prob.getOldCategory(prob.y[i]), firstpredictor);
		}*/
		hclassifier.train(prob,svmpar);
		train(prob,prob.hierarchy,innerparam,Hierarchy.root);
		first=null;
	}
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
			CombinedClassifier cls=new CombinedClassifier();
			PClassifier nodecls=(PClassifier)hclassifier.getNodeClassifier(node);
			classifiers.put(node, cls);
			for (int i=0;i<children.length;i++) {
				int [] cchildren=hier.getChildren(children[i]);
				if (cchildren==null) {
					cls.addExpertClassifier(prob.getNewCategory(children[i]), firstpredictor);
				}
				else {
					if (cchildren.length==1) {
						cls.addExpertClassifier(children[i], nodecls);
					}
					else {
						PClassifier ccls=(PClassifier)hclassifier.getNodeClassifier(children[i]);
						for (int j=0;j<cchildren.length;j++) {
							cls.addExpertClassifier(cchildren[j], ccls);
						}
					}
				}
			}
			//taken from HSVM
			ProblemExt [] probs=new ProblemExt [children.length];
			for (int i=0;i<children.length;i++) {
				probs[i]=ProblemUtils.getSubProblemWithOldMap(prob, hier.getSubHierarchy(children[i]), prob);
				ProblemUtils.setAllCategories(probs[i], children[i]);
			}
			ProblemExt newprob=ProblemUtils.mergeProblems(probs,false);
			cls.train(newprob, svmpar);
			for (int i=0;i<children.length;i++) {
				train(prob,hier,param,children[i]);
			}
		}
	}
	public int [] test(ProblemExt prob) {
		int [] ret=new int [prob.l];
		for (int i=0;i<prob.l;i++) {
			if (i%100==1) {
				System.out.println("["+i+"]");
			}
			int oldcat=test(prob.x[i],Hierarchy.root);
			ret[i]=prob.getNewCategory(oldcat);
		}
		//ret=ProblemUtils.transformOldLabelsToNew(ret, prob);
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
		if (pred[0]!=otheri) {
			boolean ok=false;
			for (int i : children) {
				if (i==pred[0]) {
					ok=true;
				}
			}
			if (!ok) {
				System.out.println("[HRefinedExperts.test] predicted node in hierarchy is inexistent");
				return pred[0];
			}
			return test(x,pred[0]);
		}
		else {
			//if the predicted category was the "other" category then backtracking should be performed, but
			//this is not handled right now, so we just select the first subcategory to continue going down on the
			//hierarchy
			System.out.println("[HRefinedExperts.test] predicted node is other, backtracking not performed");
			return test(x,children[0]);
		}
	}
	public Classifier newInstance() {
		return new HRefinedExperts();
	}
}
