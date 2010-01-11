package wordland.dimred;

import liblinear.*;



import java.util.*;

import wordland.*;
import wordland.data.BidirectionalMap;
import wordland.data.Hierarchy;
import wordland.data.ProblemExt;
import wordland.utils.ProblemUtils;
import wordland.utils.Utils;


public class DRL1RegSVM extends RankingMethod {
	private boolean keepModels=false;
	private Hashtable<Integer,FeatureNode[]> models=new Hashtable<Integer,FeatureNode[]>();	
	public DRL1RegSVM() {
		super();
	}
	public void setKeepModels(boolean k) {
		keepModels=k;
	}
	public boolean getKeepModels() {
		return keepModels;
	}
	public void collect(ProblemExt p) {
		collect2(p);
	}

	public void collect1(ProblemExt p) {
		Parameter param=new Parameter(SolverType.L1R_L2LOSS_SVC,1,0.001);		
		HashSet<Integer> cats=ProblemUtils.getCategories(p);
		int [] origcats=p.y;
		int counter=0;
		ProblemExt p2=new ProblemExt(p);
		p2.x=ProblemUtils.copyDocuments(p.x);
		Utils.norml2(p2);
		for (int i : cats) {
			p2.y = Utils.createCategoryMask(origcats, i);
			Linear linear=new Linear();
			System.out.println("L1SVM 1-vs-rest nr:"+(counter++)+" out of "+cats.size());		
			Model model=linear.train(p2, param);
			double [] weights=model.getFeatureWeights();
			FeatureNode [] sparse=Utils.sparsify(weights);
			models.put(i, sparse);
		}
		p.y=origcats;
	}

	public void collect2(ProblemExt p) {
		Parameter param=new Parameter(SolverType.L1R_L2LOSS_SVC,1,0.001);		
		HashSet<Integer> cats=ProblemUtils.getCategories(p);
		BidirectionalMap<Integer> originalinv=p.getTermMap().reverse();
		int [] origcats=p.y;
		int counter=0;
		for (int i : cats) {
			int iold=p.getOldCategory(i);
			int parent=p.hierarchy.getParent(iold);
			int [] children=p.hierarchy.getChildren(parent);
			if (children.length>1) {
				int othercat=p.getNewCategory(children[0]);
				if (!p.hierarchy.isLeaf(children[0])) {
					int [] leaves=p.hierarchy.getOnlyLeafChildren(children[0]);
					othercat=p.getNewCategory(leaves[0]);
				}
				if (i==othercat) {
					othercat=p.getNewCategory(children[1]);
					if (!p.hierarchy.isLeaf(children[1])) {
						int [] leaves=p.hierarchy.getOnlyLeafChildren(children[1]);
						othercat=p.getNewCategory(leaves[0]);
					}
				}
				ProblemExt [] ps=new ProblemExt[children.length];
				for (int j=0;j<children.length;j++) {
					Hierarchy subhier=p.hierarchy.getSubHierarchy(children[j]);
					ps[j]=ProblemUtils.getSubProblemWithOldMap(p, subhier, p);
					if (children[j]==iold) {
						ProblemUtils.setAllCategories(ps[j], i);
					}
					else {
						ProblemUtils.setAllCategories(ps[j], othercat);
					}
				}
				System.out.print("L1SVM categories:");
				for (int cc=0;cc<children.length;cc++) {
					if (p.getNewCategory(children[cc])==i) {
						System.out.print("["+children[cc]+"] ");
					}
					else {
						System.out.print(""+children[cc]+" ");						
					}
				}
				System.out.println();
				System.out.println("L1SVM 1-vs-siblings nr:"+(counter++)+" out of "+cats.size());		
				ProblemExt p2=ProblemUtils.mergeProblems(ps, true);
				Utils.norml2(p2);
				Linear linear=new Linear();
				Model model=linear.train(p2, param);
				double [] weights=model.getFeatureWeights();
				FeatureNode [] sparse=Utils.sparsify(weights);
				FeatureNode [] sparse2=ProblemUtils.mapToOldTerms(sparse, p2.getTermMap());
				FeatureNode [] sparsefinal=ProblemUtils.mapToOldTerms(sparse2, originalinv);
				models.put(i, sparsefinal);
			}
			else {
				counter++;
			}
		}
		p.y=origcats;
	}

	public DimensionalityReduction newInstance() {
		return new DRL1RegSVM();
	}

	public void process() {
		Hashtable<Integer,BidirectionalMap<Integer>> termmaps=new Hashtable<Integer,BidirectionalMap<Integer>>();
		for (int cat : models.keySet()) {
			BidirectionalMap<Integer> tm=Utils.getTermMap(models.get(cat));
			termmaps.put(cat, tm);
		}
		termmap=Utils.mergeTermMaps(termmaps, nrf);
		if (!keepModels) {
			models=new Hashtable<Integer,FeatureNode[]>();
		}
	}

}
