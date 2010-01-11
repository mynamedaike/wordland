package wordland.experiment.tasks;

import wordland.*;
import wordland.data.ProblemExt;
import wordland.dimred.DimensionalityReduction;
import wordland.experiment.*;
import wordland.utils.TFIDF;
import wordland.utils.Utils;

public class TFIDFTask implements Task{
	private TFIDF tfidf=new TFIDF();
	public TFIDFTask() {
		
	}
	public void init() {
		
	}	
	public ProblemExt transform(ProblemExt prob,TaskParam param,Task.Dataset dataset,Logger log) {
		if (prob==null) {
			return null;
		}		
		ProblemExt ret=null;
		switch (dataset) {
		case TRAIN :
			tfidf.count(prob);
			ret=tfidf.transform(prob);
			if (param.getParam("normalize")!=null) {
				Utils.norml2(ret);
			}
			log.println("TFIDF performed");
			break;
		default :
			ret=tfidf.transform(prob);
			if (param.getParam("normalize")!=null) {
				Utils.norml2(ret);
			}
		}
		return ret;
	}
}