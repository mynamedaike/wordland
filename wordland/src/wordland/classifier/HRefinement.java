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
 * Hierarchical Refinement - from:
 * Refined Experts - Improving Classification in Large Taxonomies
 * Paul Bennett, Nam Nguyen
 * SIGIR 2009
 * this is also a compound classifier, it needs an embedded classifier, if it finds none, it will use simple svm
 * this same classifier is used at first for a raw prediction, it can be either hierarchical or flat, though it will
 * be used as a flat classifier at the nodes. Parameters given in param should be enough for it.
 */

public class HRefinement extends HClassifier{
	private double bias=0;
	//n during training time
	private int n=-1;
	private int [] first;
	private int [] correct;
	//the index that the other class gets
	private static int otheri=1000000;
	private Classifier embedded;
	private ParameterExt embeddedparam;
	public HRefinement() {		
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
		Classifier firstpredictor=embedded.newInstance();
		firstpredictor.train(prob, embeddedparam);
		first=firstpredictor.test(prob);
		first=ProblemUtils.transformNewLabelsToOld(first, prob);
		//the thing to learn from this: it's not good to have the hierarchy in one labeling (original) but the labels of the docs in a new labeling
		correct=ProblemUtils.transformNewLabelsToOld(prob.y, prob);
		firstpredictor=null;  //let's hope GC will erase it soon
		train(prob,prob.hierarchy,param,Hierarchy.root);
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
			Classifier cls=embedded.newInstance();
			classifiers.put(node, cls);
			//we store all the categories whose members will be added to the final classifier
			HashSet<Integer> addedcats=new HashSet<Integer>();
			//first we store the leaves starting from a child in separate problems
			//+1 problem is needed for the "other" class for all the misclassified documents
			ProblemExt [] probs=new ProblemExt [children.length+1];			
			for (int i=0;i<children.length;i++) {
				probs[i]=ProblemUtils.getSubProblemWithOldMap(prob, hier.getSubHierarchy(children[i]), prob);
				ProblemUtils.setAllCategories(probs[i], children[i]);
				int [] leaves1=hier.getOnlyLeafChildren(children[i]);
				if (leaves1!=null) {
					for (int ii : leaves1) {
						addedcats.add(ii);
					}
				}
			}
			//as part of the refinement we will add all the documents that have been categorized by the first classifier
			//as belonging to this node
			ArrayList<Integer> misc=new ArrayList<Integer>();  //stores all the misclassifications
			for (int i=0;i<children.length;i++) {
				//get the indices of document that were predicted to belong to children[i]
				ArrayList<Integer> predind=ProblemUtils.getHierarchyNodeIndices(first, hier, children[i]);
				//get the leaf categories that really belong to children[i] in a HashSet to simplify testing containment
				int [] leaves1=hier.getOnlyLeafChildren(children[i]);
				if (leaves1!=null) {
					HashSet<Integer> leaves=new HashSet<Integer>();
					for (int ii : leaves1) {
						leaves.add(ii);
					}
					for (int ii : predind) {
						//see if the ground truth really is that document with index ii belongs to children[i] and it won't be already added
						if (!leaves.contains(correct[ii]) && !addedcats.contains(correct[ii])) {
							misc.add(ii);
						}
					}
				}
			}
			ProblemExt other=new ProblemExt();  //to store misclassifications
			other.bias=prob.bias;
			other.catnum=1;
			other.n=prob.n;
			other.l=misc.size();
			other.y=new int [other.l];
			other.x=new FeatureNode [other.l][];
			other.addCategoryMap(otheri, otheri);
			//actually assign the misclassifications to the other category
			for (int i=0;i<other.l;i++) {
				Integer ind=misc.get(i);
				other.x[i]=prob.x[ind.intValue()];
				other.y[i]=otheri;
			}
			probs[children.length]=other;
			ProblemExt newprob=ProblemUtils.mergeProblems(probs,false);
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
				System.out.println("[HRefinement.test] predicted node in hierarchy is inexistent");
				return pred[0];
			}
			return test(x,pred[0]);
		}
		else {
			//if the predicted category was the "other" category then backtracking should be performed, but
			//this is not handled right now, so we just select the first subcategory to continue going down on the
			//hierarchy
			System.out.println("[HRefinement.test] predicted node is other, backtracking not performed");
			return test(x,children[0]);
		}
	}
	public Classifier newInstance() {
		return new HRefinement();
	}
}
