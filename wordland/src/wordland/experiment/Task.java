package wordland.experiment;

import wordland.*;
import wordland.data.ProblemExt;

public interface Task {
	public enum Dataset {
		TRAIN, TEST, VALIDATION, PREDICTED, OTHER;
	}
	public void init();
	//returns null if there's some problem, then the whole experiment is aborted
	public ProblemExt transform(ProblemExt prob,TaskParam param,Task.Dataset dataset,Logger log);
}
