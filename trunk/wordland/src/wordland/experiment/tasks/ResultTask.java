package wordland.experiment.tasks;

import wordland.*;
import wordland.data.Hierarchy;
import wordland.data.ProblemExt;
import wordland.experiment.*;
import wordland.utils.EvalMeasures;

/**
 * will print the results
 */
public class ResultTask implements Task{
	private ProblemExt test;
	private int catnum=0;
	private Hierarchy hier=null;
	public ResultTask() {
		
	}
	public void init() {		
	}
	public ProblemExt transform(ProblemExt prob,TaskParam param,Task.Dataset dataset,Logger log) {
		ProblemExt ret=null;
		switch (dataset) {
		case TRAIN :
			catnum=prob.catnum;
			hier=prob.hierarchy;
			break;
		case TEST :
			test=prob;
			break;
		case PREDICTED :
			EvalMeasures e = new EvalMeasures(test, hier, prob.y, catnum);
			e.printMeasures();
			log.println(e.printMeasuresToString());
		}
		return null;
	}
}
