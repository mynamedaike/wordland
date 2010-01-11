package wordland.experiment.tasks;

import wordland.*;
import wordland.classifier.*;
import wordland.data.ParameterExt;
import wordland.data.ProblemExt;
import wordland.experiment.*;
import wordland.utils.ProblemUtils;

/**
 * Performs training and testing, the parameters must include a "classifier"
 * and "parameters" fields.
 */
public class SimpleTrainTestTask implements Task{
	private Classifier cls=null;
	private ParameterExt learnparam=null;
	private ProblemExt testprob=null;
	public SimpleTrainTestTask() {		
	}
	public void init() {
		cls=null;
	}
	public ProblemExt transform(ProblemExt prob,TaskParam param,Task.Dataset dataset,Logger log) {	
		ProblemExt ret=null;
		if (cls==null) {
			cls=(Classifier)param.getParam("classifier");
			if (cls==null) {
				return null;
			}	
		}
		if (learnparam==null) {
			learnparam=(ParameterExt)param.getParam("parameters");
			if (learnparam==null) {
				learnparam=new ParameterExt();
			}
		}
		switch (dataset) {
		case TRAIN :
			log.println("SimpleTrainTestTask: "+cls.getClass().toString()+" trained on "+prob.l+" documents with "+prob.n+" dimensions and "+prob.catnum+" categories");
			cls.train(prob, learnparam);
			ret=prob;
			break;
		case TEST :
			testprob=prob;
			ret=prob;
			break;
		case PREDICTED :
			int [] pred = cls.test(testprob);
			ret=ProblemUtils.createResultProblem(pred);
			break;
		default :
			ret=prob;
		}
		return ret;
	}
}
