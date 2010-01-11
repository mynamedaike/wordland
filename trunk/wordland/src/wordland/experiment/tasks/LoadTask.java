package wordland.experiment.tasks;

import wordland.*;
import wordland.data.Hierarchy;
import wordland.data.ProblemExt;
import wordland.data.input.LibLinearReader;
import wordland.experiment.*;

/**
 * loads data
 * supposes, that the TRAIN problem is always loaded before the TEST problem 
 */
public class LoadTask implements Task {
	private LibLinearReader cr=new LibLinearReader();
	private Hierarchy hier=null;
	private ProblemExt train=null;
	public LoadTask() {
	}
	public ProblemExt transform(ProblemExt prob,TaskParam param,Task.Dataset dataset,Logger log) {
		String fn;
		ProblemExt ret=null;
		if (hier==null && param.getParam("hierarchy")!=null) {
			hier=new Hierarchy();
			//hier.load(Params.hierarchyPath+"cat_hier.txt");
			hier.load((String)param.getParam("hierarchy"));
		}
		switch (dataset) {
		case TRAIN :
			fn=(String)param.getParam("train");
			if (fn!=null) {
				ret=readFile(fn);
				train=ret;
			}
			log.println("LoadTask: loaded training data");
			break;
		case TEST :
			fn=(String)param.getParam("test");
			if (fn!=null) {
				if (train==null) {
					ret=readFile(fn);
				}
				else {
					ret=readFileWithOldMaps(fn, train);
				}
			}
			log.println("LoadTask: loaded testing data");
			break;			
		case VALIDATION :
			fn=(String)param.getParam("validation");
			if (fn!=null) {
				ret=readFile(fn);
			}
			log.println("LoadTask: loaded validation data");
			break;
		default :
			ret=prob;
		}
		if (ret!=null && hier!=null) {
			ret.hierarchy=hier;
		}
		return ret;
	}
	private ProblemExt readFile(String fname) {
		return cr.readProblem(fname);
	}
	private ProblemExt readFileWithOldMaps(String fname,ProblemExt old) {
		return cr.readProblemWithOldMaps(fname, old);
	}
	public void init() {
		cr=new LibLinearReader();
	}		
}
