package wordland.competitions.activelearning10;

import java.util.*;

import wordland.classifier.activelearning.*;
import wordland.data.*;

public class CompetitionOracle implements Oracle{
	protected SubmitResults submit = SubmitResults.getInstance();
	protected String dataname;
	protected String expname;
	protected int expnum=0;
	public CompetitionOracle(String datanames,String expnames) {
		submit.login("", "");
		dataname=datanames;
		expname=expnames;
	}
	
	public boolean queryLabels(List<Integer> q,double [] allpredictions) {
		int [] qq=new int [q.size()];
		for (int i=0;i<q.size();i++) {
			qq[i]=q.get(i);
		}
		boolean ret=submit.submitResults(dataname, expname+expnum, qq, allpredictions);
		return ret;
	}
	
	public int getLabel(ProblemExt p,int i) {
		return 1;
	}
}
