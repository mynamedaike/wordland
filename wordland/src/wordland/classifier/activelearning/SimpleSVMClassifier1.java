package wordland.classifier.activelearning;

import liblinear.Parameter;
import wordland.classifier.*;
import wordland.classifier.activelearning.*;
import wordland.competitions.activelearning10.Params;
import wordland.data.*;

public class SimpleSVMClassifier1 extends SimpleALClassifier{
	public SimpleSVMClassifier1() {
		super();
	}
	public double [] trainAndTest_DecValues(ProblemExt finaltrain, ProblemExt finaltest) {
		Parameter param = new Parameter(Params.LibLinearType,1,0.001);
		ParameterExt p = new ParameterExt();
		p.param=param;
		SVM classifier = new SVM();
		classifier.train(finaltrain, p);
		double [] pred = classifier.testDecValues(finaltest);
		return pred;
	}

}
