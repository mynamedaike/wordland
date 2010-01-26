package wordland.competitions.activelearning10;

import java.math.*;

import java.util.*;

import liblinear.Parameter;
import wordland.classifier.*;
import wordland.classifier.activelearning.*;

import wordland.data.input.*;
import wordland.data.*;
import wordland.distance.CosDistance;
import wordland.distance.CosSimilarity;
import wordland.distance.DistanceMetric;
import wordland.utils.EvalMeasures;
import wordland.utils.TFIDF;
import wordland.utils.Utils;
import wordland.classifier.*;
import java.util.*;

public class Main {
	public static void main(String [] args) {
		main_mzs(args);
	}
	public static void main_mzs(String [] args) {
		int i;
		SubmitResults submit=SubmitResults.getInstance();
		//submit.login(null, null);
		int [] tosample=new int [10];
		double [] pred=new double [10000];
		Random rand=new Random();
		for (i=0;i<10;i++) {
			tosample[i]=rand.nextInt();
		}
		for (i=0;i<10000;i++) {
			pred[i]=rand.nextDouble();
		}
		submit.submitResults("ibn_sina", "javaexp1", tosample, pred);
	}	
	public static void main_bz(String [] args) {
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
		SimpleSVMClassifier1 actl=new SimpleSVMClassifier1();
		actl.setOracle(new KnownLabelOracle());
		actl.train(train, null);
		actl.test(test);
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
