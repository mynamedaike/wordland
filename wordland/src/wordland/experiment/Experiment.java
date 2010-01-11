package wordland.experiment;

import java.util.*;

import wordland.*;
import wordland.data.ProblemExt;

/**
 * performs automatic experiments with many different setups and logs the results
 */
public class Experiment {
	private ArrayList<ArrayList<Task>> stages=new ArrayList<ArrayList<Task>>();
	private Hashtable<Task,ArrayList<TaskParam>> taskparams=new Hashtable<Task,ArrayList<TaskParam>>();
	private Logger output;
	private ArrayList<Hashtable<Task.Dataset,ProblemExt>> stagedata=new ArrayList<Hashtable<Task.Dataset,ProblemExt>>(); 
	public Experiment() {
		//output=new Logger("results.txt");
		this("results");
	}
	public Experiment(String out) {
		output=new Logger(out);
	}
	public void setStages(int n) {
		stages.clear();
		stagedata.clear();
		for (int i=0;i<n;i++) {
			stages.add(new ArrayList<Task>());
			Hashtable<Task.Dataset,ProblemExt> zerostage = new Hashtable<Task.Dataset,ProblemExt>();
			//it is not needed to initialize the stage datas with null values
			/*for (Task.Dataset d : Task.Dataset.values()) {
				zerostage.put(d, null);
			}*/
			stagedata.add(zerostage);
		}
	}
	public void addTask(int stage,Task task) {
		ArrayList<Task> thestage=stages.get(stage);
		if (thestage==null) {
			thestage=new ArrayList<Task>();
			stages.add(stage,thestage);
		}
		thestage.add(task);
	}
	public void addNextStageTask(Task task) {
		ArrayList<Task> thestage=new ArrayList<Task>();
		thestage.add(task);
		stages.add(thestage);
	}
	public void addTaskParam(Task task,TaskParam param) {
		ArrayList<TaskParam> tps=taskparams.get(task);
		if (tps==null) {
			tps=new ArrayList<TaskParam>();
			taskparams.put(task, tps);
		}
		tps.add(param);
	}
	public void addTaskParam(Task task,ArrayList<TaskParam> params) {
		for (TaskParam t : params) {
			addTaskParam(task,t);
		}
	}
	public void run() {
		output.begin();
		runStage(0);
		output.close();
	}
	private void runStage(int s) {
		ArrayList<Task> tasks=stages.get(s);
		Hashtable<Task.Dataset,ProblemExt> data=stagedata.get(s);
		Hashtable<Task.Dataset,ProblemExt> nextdata=null;
		if (s+1<stagedata.size()) {
			nextdata=stagedata.get(s+1);
		}
		ProblemExt res;
		if (tasks.size()!=0) {
			for (Task task : tasks) {
				ArrayList<TaskParam> params=taskparams.get(task);
				if (params==null) {
					addTaskParam(task,new TaskParam());
					params=taskparams.get(task);
				}
				for (TaskParam taskparam : params) {
					task.init();
					for (Task.Dataset d : Task.Dataset.values()) {
						res=task.transform(data.get(d), taskparam, d, output);
						if (nextdata!=null && res!=null) {
							nextdata.put(d, res);
						}
					}
					if (s+1<stages.size()) {
						runStage(s+1);
					}
				}
			}
		}
		else if (s<stages.size()) {
			copyResults(s+1,s);
			runStage(s+1);
		}
	}
	//s1=to, s2=from
	private void copyResults(int s1, int s2) {
		Hashtable<Task.Dataset,ProblemExt> hs1=stagedata.get(s1);
		Hashtable<Task.Dataset,ProblemExt> hs2=stagedata.get(s2);
		for (Task.Dataset key : hs2.keySet()) {
			hs1.put(key, hs2.get(key));
		}		
	}
}
