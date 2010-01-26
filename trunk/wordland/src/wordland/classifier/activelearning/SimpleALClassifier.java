package wordland.classifier.activelearning;

import java.util.ArrayList;
import java.util.Arrays;

import liblinear.Parameter;

import wordland.classifier.*;
import wordland.competitions.activelearning10.Params;
import wordland.data.*;
import wordland.distance.CosSimilarity;
import wordland.distance.DistanceMetric;
import wordland.utils.EvalMeasures;

public abstract class SimpleALClassifier implements ALClassifier{
	protected Oracle oracle;
	protected ProblemExt train1;
	protected ProblemExt test1;
	protected ProblemExt train;
	
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
		train1 = ProblemExt.createProblemFrom(trainn, trainind);
		///
		if (train1.y[1] != 0) {
			System.out.println("Shit!");
			train1.y[1] = 0;
		}
		///
		train=trainn;
	}
	public int [] test(ProblemExt test) {
		test1 = ProblemExt.union(train, test);
		int [] predlabels=null;
		int querysize = 10;
		double [] neededpred = new double [train.l+test.l];
		
		for (int i=0; i<10; i++) {
			int j,howfar;
			System.out.println("train size="+train1.l);
			double [] pred = trainAndTest_DecValues(train1, train1);
			for (j=0;j<pred.length;j++) {
				neededpred[j]=pred[j];
			}
			howfar=j;
			pred = trainAndTest_DecValues(train1, test1);
			for (j=0;j<pred.length;j++) {
				neededpred[j+howfar]=pred[j];
			}
			predlabels=toLabels(pred);
			EvalMeasures e = new EvalMeasures(test1, null, predlabels, train1.catnum);
			e.printMeasures();
			ArrayList<Integer> query = wantedInstances1(Arrays.copyOfRange(pred, 0, train.l-2-i*querysize-1), querysize);
			if (!oracle.queryLabels(query, neededpred)) {
				System.out.println("The oracle is not functioning properly!");
			}
			for (int q : query) {
				int label = oracle.getLabel(train, q);
				train.y[q] = label;
			}
			ProblemExt [] ret = ProblemExt.addremoveNodes(train1, test1, query);
			train1 = ret[0];
			test1 = ret[1];
		}
		return predlabels;
	}
	public Classifier newInstance() {
		return new SimpleSVMClassifier1();
	}
	public static ArrayList<Integer> wantedInstances1(double [] pred, int size) {
		ArrayList<Integer> q = new ArrayList<Integer>();
		int [] ind = new int[pred.length];
		for (int i=0; i<ind.length; i++)
			ind[i] = i;
		for (int i=0; i<pred.length-1; i++)
			for (int j=i+1; j<pred.length; j++)
				if (Math.abs(pred[i]) > Math.abs(pred[j])) {
					double a1 = pred[i];
					pred[i] = pred[j];
					pred[j] = a1;
					int a2 = ind[i];
					ind[i] = ind[j];
					ind[j] = a2;
				}
		for (int i=0; i<size; i++) {
			q.add(ind[i]);
			System.out.print(pred[i]+" ");
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