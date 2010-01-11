package wordland.dimred;


import java.util.*;

import wordland.*;
import wordland.competitions.lsthc09.Params;
import wordland.data.BidirectionalMap;
import wordland.data.Hierarchy;
import wordland.data.ProblemExt;
import wordland.utils.ProblemUtils;
import wordland.utils.Utils;

/**
 * Does a hierarchical feature selection using a ranking method at each node to select features
 */
public class HierarchicalMethod extends RankingMethod{
	private RankingMethod embedded;
	private int nrf=Params.numFeatures;
	private Hashtable<Integer,BidirectionalMap<Integer>> termmaps=new Hashtable<Integer,BidirectionalMap<Integer>>();
	public HierarchicalMethod(RankingMethod emb) {
		embedded=emb;
	}
	public void setNrFeatures(int i) {
		nrf=i;
	}
	public int getNrFeatures() {
		return nrf;
	}
	public void collect(ProblemExt p) {
		collect(p,Hierarchy.root);
	}
	private void collect(ProblemExt p,int node) {
		int [] children=p.hierarchy.getChildren(node);
		if (children==null) {
			return;
		}
		if (children.length==1) {
			collect(p,children[0]);
		}
		else {
			ProblemExt newprob=ProblemUtils.mergeAllChildren(p, p.hierarchy, children,true,false);
			RankingMethod dr=(RankingMethod)(embedded.newInstance());
			dr.setNrFeatures(embedded.getNrFeatures());
			dr.collect(newprob);
			dr.process();
			BidirectionalMap<Integer> tmap=dr.getTermmap();
			termmaps.put(node, tmap);
			for (int i=0;i<children.length;i++) {
				collect(p,children[i]);
			}
		}
	}
	public void process() {
		termmap=Utils.mergeTermMaps(termmaps, nrf);
		termmaps=null;
	}
	public DimensionalityReduction newInstance() {
		return new HierarchicalMethod(embedded);
	}
}
