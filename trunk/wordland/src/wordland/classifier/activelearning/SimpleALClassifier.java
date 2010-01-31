package wordland.classifier.activelearning;

import java.util.*;

import liblinear.Parameter;

import wordland.classifier.*;
import wordland.competitions.activelearning10.Params;
import wordland.data.*;
import wordland.distance.*;
import wordland.utils.*;


public abstract class SimpleALClassifier implements ALClassifier{
	protected Oracle oracle;
	protected ProblemExt train1;
	protected ProblemExt test1;
	protected ProblemExt train;
	protected HashSet<Integer> already = new HashSet<Integer>();
	
	public SimpleALClassifier() {		
	}
	public void setOracle(Oracle o) {
		oracle=o;
	}
	public void train(ProblemExt trainn,ParameterExt param) {
		ArrayList<Integer> trainind = new ArrayList<Integer>();
		for (Integer seed : trainn.seedData.keySet())
			trainind.add(seed);
		trainind.add(mostDifferent(trainn));
		train1 = ProblemUtils.createProblemFrom(trainn, trainind);
		///
		if (train1.y[1] != 0) {
			System.out.println("Shit!");
			train1.y[1] = 0;
		}
		///
		train=trainn;
		already.addAll(trainind);
	}
	public int [] test(ProblemExt test) {
		test1 = ProblemUtils.union(train, test);
		int [] predlabels=null;
		int querysize = 10;
		double [] neededpred;
		
		for (int i=0; i<10; i++) {
			int j;
			System.out.println("train size="+train1.l);
			double [] pred;
			pred = trainAndTest_DecValues(train1, test1);
			neededpred=pred;
			predlabels=toLabels(pred);
			EvalMeasures e = new EvalMeasures(test1, null, predlabels, train1.catnum);
			e.printMeasures();
			double [] trainpred=new double [train.l];
			System.arraycopy(pred, 0, trainpred, 0, trainpred.length);
			ArrayList<Integer> query = wantedInstances1(trainpred, already, querysize);
			if (!oracle.queryLabels(query, neededpred)) {
				System.out.println("The oracle is not functioning properly!");
				return null;
			}
			for (int q : query) {
				int label = oracle.getLabel(train, q);
				train.y[q] = label;
			}
			already.addAll(query);
			train1 = ProblemUtils.createProblemFrom(train, already);
		}
		return predlabels;
	}
	public Classifier newInstance() {
		return new SimpleSVMClassifier1();
	}
	public static ArrayList<Integer> wantedInstances1(double [] pred, Set<Integer> notthese,int size) {
		int i,j;
		ArrayList<Integer> q = new ArrayList<Integer>();
		ArrayList<ValueIndex> ind = new ArrayList<ValueIndex>();
		
		for (i=0; i<pred.length; i++) {
			ValueIndex vi = new ValueIndex(i,Math.abs(pred[i]));
			ind.add(vi);
		}
		Collections.sort(ind);
		
		i=0;j=0;
		while (i<size && j<ind.size()) {
			if (!notthese.contains(ind.get(j).index)) {
				q.add(ind.get(j).index);
				System.out.print(ind.get(j).value+" ");
				i++;
			}
			j++;
		}
		System.out.println();
		return q;
	}
	public abstract double [] trainAndTest_DecValues(ProblemExt finaltrain, ProblemExt finaltest);
	/*
	public static int [] trainAndTest_LibLinear(ProblemExt finaltrain, ProblemExt finaltest) {
		Parameter param = new Parameter(Params.LibLinearType,1,0.001);
		ParameterExt p = new ParameterExt();
		p.param=param;
		SVM classifier = new SVM();
		classifier.train(finaltrain, p);
		int [] pred = classifier.test(finaltest);
		return pred;
	}
	*/
	public static int [] toLabels(double [] decValues) {
		int [] ret = new int[decValues.length];
		for (int i=0; i<decValues.length; i++)
			ret[i] = (decValues[i]>=0)?1:0;
		return ret;
	}	
	/**
	 * Returns the index of the most different learning instance
	 * from prob according to the distance/similarity measure
	 * @param prob
	 * @return
	 */
	public static int mostDifferent(ProblemExt prob) {
		DistanceMetric dm = new CosSimilarity();
		ArrayList<Integer> seedind = new ArrayList<Integer>();
		for (Integer seed : prob.seedData.keySet())
			seedind.add(prob.seedData.get(seed));
		double sim = 0;
		int index = 0;
		sim = Math.abs(dm.distance(prob.x[seedind.get(0)], prob.x[0]));
		double newsim;
		for (int i=1; i<prob.x.length; i++)
			if ((newsim = Math.abs(dm.distance(prob.x[seedind.get(0)], prob.x[i]))) < sim) {
				sim = newsim;
				index = i;
			}
		return index;
	}
}