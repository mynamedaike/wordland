package wordland.experiment.tasks;


import java.util.*;

import wordland.*;
import wordland.classifier.Classifier;
import wordland.cluster.*;
import wordland.data.ParameterExt;
import wordland.data.ProblemExt;
import wordland.dimred.*;
import wordland.experiment.*;

public class DimRedClustTask implements Task{
	private ClusteringMethod dr=null;
	private ParameterExt drparam=null;
	public DimRedClustTask() {		
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
			dr=(ClusteringMethod)param.getParam("dimred");
			if (dr==null) {
				return null;
			}
			drparam=(ParameterExt)param.getParam("parameters");
			if (drparam==null) {
				return null;
			}
			if (param.getParam("dim")!=null) {
				drparam.clusters=((Integer)param.getParam("dim")).intValue();
			}
			log.println("DimRedClustTask: "+dr.getClass().toString()+" distance metric "+drparam.select_distance.getClass()+" normalization :"+drparam.normalization.getClass()+" dim="+drparam.clusters);
			dr.setParameters(drparam);
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
