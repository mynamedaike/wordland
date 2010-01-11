package wordland.experiment;

import java.util.*;

public class TaskParam {
	private Hashtable<String,Object> params=new Hashtable<String,Object>();
	public TaskParam() {
		
	}
	public TaskParam(TaskParam o) {
		for (String k : o.params.keySet()) {
			addParam(k,o.getParam(k));
		}
	}
	public void addParam(String key,Object o) {
		params.put(key, o);
	}
	public Object getParam(String key) {
		return params.get(key);
	}
	public int size() {
		return params.size();
	}
	public static ArrayList<TaskParam> createNewParams(TaskParam original,String newp,int [] values) {
		ArrayList<TaskParam> ret=new ArrayList<TaskParam>();
		for (int v : values) {
			TaskParam tp=new TaskParam(original);
			tp.addParam(newp, v);
			ret.add(tp);
		}
		return ret;
	}
}
