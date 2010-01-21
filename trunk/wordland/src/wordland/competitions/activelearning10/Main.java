package wordland.competitions.activelearning10;

import java.util.*;

import liblinear.Parameter;

import wordland.data.input.*;
import wordland.data.*;
import wordland.distance.CosDistance;
import wordland.distance.CosSimilarity;
import wordland.distance.DistanceMetric;
import wordland.utils.EvalMeasures;
import wordland.utils.TFIDF;
import wordland.utils.Utils;
import wordland.classifier.*;


public class Main {
	public static void main(String [] args) {
		Dense2SparseReader reader = new Dense2SparseReader(); 
		ProblemExt problem = reader.readProblem(Params.rootPath+Params.dataSet+".data", Params.rootPath+Params.dataSet+".label", Params.rootPath+Params.dataSet+".seed");
		ProblemExt train = new ProblemExt();
		ProblemExt test = new ProblemExt();
		Dense2SparseReader.splitTrainTest(problem, train, test);

//		Dense2DenseReader reader = new Dense2DenseReader(); 
//		ProblemDense problem = reader.readProblem(Params.rootPath+Params.dataSet+".data", Params.rootPath+Params.dataSet+".label", Params.rootPath+Params.dataSet+".seed");
		
		transformData_nofs(train, test);
//		int [] pred = trainAndTest_LibLinear(train, test);
//		EvalMeasures e = new EvalMeasures(test, null, pred, train.catnum);
//		e.printMeasures();

		actLearner(train, test, null);
	}
	public static int [] actLearner(ProblemExt train, ProblemExt test, ParameterExt param) {
		ArrayList<Integer> trainind = new ArrayList<Integer>();
		for (Integer seed : train.seedData.keySet())
			trainind.add(seed);
		trainind.add(mostDifferent(train));
		ProblemExt train1 = ProblemExt.createProblemFrom(train, trainind);
		///
		if (train1.y[1] != 0) {
			System.out.println("Shit!");
			train1.y[1] = 0;
		}
		///
		ProblemExt test1 = ProblemExt.union(train, test);
		int querysize = 10;
		for (int i=0; i<10; i++) {
			System.out.println("train size="+train1.l);
			double [] pred = trainAndTest_LibLinear_DecValues(train1, test1);
			EvalMeasures e = new EvalMeasures(test1, null, toLabels(pred), train1.catnum);
			e.printMeasures();
			ArrayList<Integer> query = wantedInstances1(Arrays.copyOfRange(pred, 0, train.l-2-i*querysize-1), querysize);
			ProblemExt [] ret = ProblemExt.addremoveNodes(train1, test1, query);
			train1 = ret[0];
			test1 = ret[1];
		}
		return null;
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
	public static int [] trainAndTest_LibLinear(ProblemExt finaltrain, ProblemExt finaltest) {
		Parameter param = new Parameter(Params.LibLinearType,1,0.001);
		ParameterExt p = new ParameterExt();
		p.param=param;
		SVM classifier = new SVM();
		classifier.train(finaltrain, p);
		int [] pred = classifier.test(finaltest);
		return pred;
	}
	public static double [] trainAndTest_LibLinear_DecValues(ProblemExt finaltrain, ProblemExt finaltest) {
		Parameter param = new Parameter(Params.LibLinearType,1,0.001);
		ParameterExt p = new ParameterExt();
		p.param=param;
		SVM classifier = new SVM();
		classifier.train(finaltrain, p);
		double [] pred = classifier.testDecValues(finaltest);
		return pred;
	}
	public static ProblemExt [] transformData_nofs(ProblemExt train, ProblemExt test) {
		ProblemExt [] ret = new ProblemExt[2]; 
		ret[0] = train;
		ret[1] = test;
		Utils.norml2(ret[0]);
		Utils.norml2(ret[1]);
		return ret;
	}
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
