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
 * a hierarchic classifier based on classifying the top levels of the hierarchy
 * separately and the bottom separately again
 */
public class HierarchicalClassifiers2 extends HClassifier{
	private double bias=0;
	//n during training time
	private int n=-1;
	private Classifier embedded;
	private ParameterExt embeddedparam;	
	private Classifier topcls;
	private BidirectionalMap<Integer> toptraincats;
	private Hashtable<Integer,Hierarchy> hierarchies=new Hashtable<Integer,Hierarchy>();
	private Hashtable<Integer,BidirectionalMap<Integer>> catmaps=new Hashtable<Integer,BidirectionalMap<Integer>>();
	private Hierarchy tophierarchy;
	public HierarchicalClassifiers2() {
	}
	public void train(ProblemExt prob,ParameterExt param) {
		//prob.addPlusDimension();
		if (param.embedded!=null) {
			embedded=param.embedded;
			embeddedparam=param.embeddedparam;
		}
		else {
			embedded=new ECOC2();			
			Parameter paramm=new Parameter(Params.LibLinearType,1,0.01);
			embeddedparam=new ParameterExt();
			embeddedparam.param=paramm;
			embeddedparam.ecoc_nr=Params.ecocCodeLength;
		}
		trainparams=param;
		trainhierarchy=prob.hierarchy;
		bias=prob.bias;
		n=prob.n;
		
		Classifier othercls;
		ParameterExt otherparam;
		if (param.embedded2!=null) {
			othercls=param.embedded2;
			otherparam=param.embeddedparam2;
		}
		else {
			othercls=new RemapClassifier(new OnlineHieron());
			//othercls=new SVM();
			//othercls=new HSVM();
			Parameter paramm=new Parameter(Params.LibLinearType,1,0.0001);
			otherparam=new ParameterExt();
			otherparam.param=paramm;			
		}
		
		ProblemExt toptrain;
		toptrain=ProblemUtils.collapseProblemToLevels(prob, Hierarchy.root, 2, null);
		tophierarchy=toptrain.hierarchy;
		toptraincats=toptrain.getCategoryMap();
		traincategories=prob.getCategoryMap();
		topcls=embedded.newInstance();
		topcls.train(toptrain, embeddedparam);
		int [] leaves=toptrain.hierarchy.getOnlyLeafChildren(Hierarchy.root);
		for (int leaf : leaves) {
			Hierarchy leafh=prob.hierarchy.getSubHierarchy(leaf);
			hierarchies.put(leaf, leafh);
			ProblemExt leafp=ProblemUtils.getSubProblemWithOldMap(prob, leafh, prob);
			Classifier leafcls=othercls.newInstance();
			classifiers.put(leaf, leafcls);
			catmaps.put(leaf, leafp.getCategoryMap());
			leafcls.train(leafp, otherparam);			
		}
	}
	public int [] test(ProblemExt prob) {
		//prob.addPlusDimension();
		int [] ret=new int [prob.l];
		for (int i=0;i<prob.l;i++) {
			Hashtable<Integer,Double> nodevalues=new Hashtable<Integer,Double>();
			nodevalues.put(Hierarchy.root, 0.0);
			ret[i]=test(prob.x[i]);
		}
		return ret;
	}
	private int test(FeatureNode [] x) {
		int ret;
		ProblemExt xp=ProblemUtils.getProblem(x, bias, n);
		xp.hierarchy=tophierarchy;
		xp.setCategoryMap(toptraincats);
		int [] first=topcls.test(xp);
		int node=toptraincats.getFirst(first[0]);
		Classifier cls=classifiers.get(node);
		xp.hierarchy=hierarchies.get(node);
		xp.setCategoryMap(catmaps.get(node));
		int [] pred=cls.test(xp);
		return pred[0];
	}
	public Classifier newInstance() {
		return new HierarchicalClassifiers1();
	}

}
