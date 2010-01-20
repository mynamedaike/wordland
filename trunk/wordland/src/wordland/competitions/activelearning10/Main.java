package wordland.competitions.activelearning10;

import liblinear.Parameter;

import wordland.data.input.*;
import wordland.data.*;
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
		int [] pred = trainAndTest_LibLinear(train, test);
		EvalMeasures e = new EvalMeasures(test, null, pred, train.catnum);
		e.printMeasures();
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
	public static ProblemExt [] transformData_nofs(ProblemExt train, ProblemExt test) {
		ProblemExt [] ret = new ProblemExt[2]; 
		ret[0] = train;
		ret[1] = test;
		Utils.norml2(ret[0]);
		Utils.norml2(ret[1]);
		return ret;
	}
}
