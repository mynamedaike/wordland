package wordland.dimred;

import java.util.*;

import wordland.*;
import wordland.competitions.lsthc09.Params;
import wordland.data.BidirectionalMap;
import wordland.data.FeatureNodeComp;
import wordland.data.ProblemExt;

import liblinear.*;

/**
 * the simplest f.s. methods are ranking methods. they share a lot of stuff, basically only a different measure
 * for the terms are implemented in each one, now it has only one subclass, Frequency which shared its remapping methods
 */
public abstract class RankingMethod implements DimensionalityReduction{
	protected int nrf = Params.numFeatures;
	//changed first=old, second=new
	protected BidirectionalMap<Integer> termmap;
	protected FeatureNode [] remap(FeatureNode [] old) {
		ArrayList<FeatureNodeComp> ret=new ArrayList<FeatureNodeComp>();
		for (int i=0;i<old.length;i++) {
			Integer term=termmap.getSecond(old[i].index);
			if (term!=null) {
				ret.add(new FeatureNodeComp(term.intValue(),old[i].value));
			}			
		}
		if (ret.size()==0) {
			return null;
		}
		Collections.sort(ret);
		FeatureNodeComp [] retarray=new FeatureNodeComp [ret.size()];
		ret.toArray(retarray);
		return retarray;
	}
	public ProblemExt remap(ProblemExt p) {
		ProblemExt ret=new ProblemExt(p);
		ret.l=p.l;
		ret.n=nrf+1; //+1 is necessary because we do not have a feature with index 0
		ret.bias=p.bias;
		ret.catnum=p.catnum;
		ret.y=new int [ret.l];
		ret.x=new FeatureNode [ret.l][];
		System.arraycopy(p.y, 0, ret.y, 0, ret.l);
		for (int i=0;i<ret.l;i++) {
			ret.x[i]=remap(p.x[i]);
			if (ret.x[i]==null) {  //if the instance was remapped to an empty instance, set a common one
				ret.x[i]=new FeatureNode[1];
				ret.x[i][0]=new FeatureNode(1,1);
			}
		}
		ret.copyCategoryHash(p);
		for (Integer oldindex : termmap.getFirsts()) {
			int newindex=termmap.getSecond(oldindex);
			int olderindex=p.getOldTerm(oldindex);
			ret.addTerm(olderindex, newindex);
		}
		return ret;
	}
	public void setNrFeatures(int i) {
		nrf=i;
	}
	public int getNrFeatures() {
		return termmap.size();
	}
	public BidirectionalMap<Integer> getTermmap() {
		return termmap;
	}
}
