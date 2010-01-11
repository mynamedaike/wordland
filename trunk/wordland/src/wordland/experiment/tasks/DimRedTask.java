package wordland.experiment.tasks;


import java.util.*;

import wordland.*;
import wordland.classifier.Classifier;
import wordland.data.ProblemExt;
import wordland.dimred.*;
import wordland.experiment.*;

public class DimRedTask implements Task{
	private DimensionalityReduction dr=null;
	public DimRedTask() {		
	}
	public void init() {
		dr=null;
	}
	public ProblemExt transform(ProblemExt prob,TaskParam param,Task.Dataset dataset,Logger log) {
		if (prob==null) {
			return null;
		}
		ProblemExt ret=null;
		if (dr==null) {
			dr=(DimensionalityReduction)param.getParam("dimred");
			if (dr==null) {
				return null;
			}
			if (param.getParam("dim")!=null) {
				dr.setNrFeatures(((Integer)param.getParam("dim")).intValue());
			}
			log.println("DimRedTask: "+dr.getClass().toString()+" dim="+param.getParam("dim"));
		}
		switch (dataset) {
		case TRAIN :
			dr.collect(prob);
			dr.process();
			ret=dr.remap(prob);
			break;
		default :
			ret=dr.remap(prob);
		}
		return ret;
	}
}
