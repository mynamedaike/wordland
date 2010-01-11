package wordland.cluster;

import liblinear.*;
import java.util.*;

import wordland.*;
import wordland.data.ParameterExt;
import wordland.data.ProblemExt;
import wordland.data.ValueIndex;
import wordland.distance.*;

/**
 * this wraps around any clustering algorithm by reducing the number of points to be clustered, clusters it
 * with the embedded algorithm and then assigns the rest of the points to the nearest clusters
 * it passes it's own parameters down to the embedded clusterer
 * to choose which points to select, it uses the select_distance parameter, if it's null, then it uses
 * random selection
 */
public class ClusterWrapper implements Clusterer{
	protected Clusterer wrapped;
	public ClusterWrapper(Clusterer c) {
		wrapped=c;
	}
	public int [] cluster(ProblemExt p,DistanceMetric d,ParameterExt par) {
		ProblemExt p2=new ProblemExt();  //temporary problem to pass to the embedded algorithm
		ArrayList<ValueIndex> list=new ArrayList<ValueIndex>();  //list of points that will be clustered by the embedded algorithm, eventually
		p2.l=par.maxpoints;
		p2.n=p.n;
		p2.x=new FeatureNode [p2.l][];	
		if (p2.l>p.l) {
			//if there are less points in the dataset than the maximum that can be clustered with the algorithm, then we're lucky
			p2.l=p.l;
			p2.x=p.x;
		}
		else {			
			if (par.select_distance==null) {
				//if there is no method given to measure the points then we assign them randomly
				HashSet<Integer> already=new HashSet<Integer>();
				for (int i=0;i<p2.l;i++) {
					int ind=(int)(Math.random()*p2.l);
					while (!already.contains(ind)) {
						ind=(int)(Math.random()*p2.l);
					}
					already.add(ind);
					p2.x[i]=p.x[ind];
				}
			}
			else {
				for (int i=0;i<p.l;i++) {
					//we measure each point by comparing it to the null vector
					double val=par.select_distance.distance(null, p.x[i]);
					list.add(new ValueIndex(i,val));
				}
				Collections.sort(list);
				Collections.reverse(list);  //we need the list in descending order
				for (int i=0;i<p2.l;i++) {
					p2.x[i]=p.x[list.get(i).index];
				}
			}
		}
		int [] first=wrapped.cluster(p2, d, par);  //first rough clustering
		if (p2.l<par.maxpoints) {
			return first;
		}
		//first assign the already assigned points 
		int [] ret=new int [p.l];
		for (int i=0;i<p.l;i++) {
			ret[i]=-1;  //unassigned
		}		
		for (int i=0;i<p2.l;i++) {
			int ind=list.get(i).index;  //the first entries in list contain the indices of those points that will be clustered
			ret[ind]=first[i];
		}
		//next assign the rest of the points to the original clusters without recalculating their centers
		ArrayList<HashSet<Integer>> firsthash=ClusterUtils.intListToHash(first, par.clusters);
		FeatureNode [][] centers=ClusterUtils.calcCenters(p2, firsthash);
		for (int i=0;i<p.l;i++) {
			if (ret[i]==-1) {
				int mink=ClusterUtils.getClosestCenter(p.x[i], centers, d);
				ret[i]=mink;
			}
		}
		return ret;
	}
}
