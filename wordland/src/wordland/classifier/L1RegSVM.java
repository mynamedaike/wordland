package wordland.classifier;

import wordland.*;
import wordland.data.ParameterExt;
import wordland.data.ProblemExt;
import liblinear.*;
//import org.gnu.glpk.*;

/**
 * this will implement l1 regularized svms using linear programming solved by glpk
 */
public class L1RegSVM implements Classifier {
	private Model [] model;
	private int classnum;
	private int [] labels;
	public L1RegSVM() {		
	}
	public void train(ProblemExt prob,ParameterExt param) {
		
	}
	public int [] test(ProblemExt prob) {
		return null;
	}
	public Classifier newInstance() {
		return new L1RegSVM();
	}
}
