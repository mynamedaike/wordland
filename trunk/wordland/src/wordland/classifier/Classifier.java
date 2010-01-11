package wordland.classifier;

import wordland.*;
import wordland.data.ParameterExt;
import wordland.data.ProblemExt;

/**
 * interface for classifiers, hopefully there will be many of these :)
 */
public interface Classifier {
	//must not change the parameter!
	public void train(ProblemExt prob,ParameterExt param);
	//test has to return categories with NEW ids!
	public int [] test(ProblemExt prob);
	public Classifier newInstance();
}
