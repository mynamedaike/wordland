package wordland.cluster;
import java.util.*;

import wordland.*;
import wordland.data.ParameterExt;
import wordland.data.ProblemExt;
import wordland.distance.*;
import liblinear.*;

/**
 * classic k-means clustering of points given in the problem object
 */
public class KMeans implements Clusterer{
	public int [] cluster(ProblemExt p,DistanceMetric d,ParameterExt par){
		int k=par.clusters;
		ArrayList<HashSet<Integer>> h = cluster_hash(p,d,par);
		int [] ret=ClusterUtils.hashToIntList(h, p.l);
		return ret;
	}
	private ArrayList<HashSet<Integer>> cluster_hash(ProblemExt p,DistanceMetric d,ParameterExt par){
		int i,j;
		int k=par.clusters;
		ArrayList<HashSet<Integer>> oldclu=new ArrayList<HashSet<Integer>>();
		ArrayList<HashSet<Integer>> newclu=new ArrayList<HashSet<Integer>>();
		for (i=0;i<k;i++) {
			oldclu.add(new HashSet<Integer>());
			newclu.add(new HashSet<Integer>());
		}
		//first randomly assign the points to the clusters, this should be later improved to the k-means+ method
		//to make sure that each cluster has at least one element, we assign the first k points to the k clusters
		for (i=0;i<k;i++) {
			oldclu.get(i).add(i);
		}
		for (i=k;i<p.l;i++) {
			int r=(int)(Math.random()*k);
			oldclu.get(r).add(i);
		}
		//now comes the clustering
		int oldchanges=100000000;
		int newchanges=0;
		FeatureNode [][] centers=new FeatureNode [k][];
		while (Math.abs(newchanges-oldchanges)>(p.l/100)) {
			//first compute the cluster centers
			oldchanges=newchanges;
			centers=ClusterUtils.calcCenters(p, oldclu);
			for (i=0;i<k;i++) {
				newclu.get(i).clear();
			}
			//then compute the distance to each center
			for (i=0;i<p.l;i++) {
				int mink=ClusterUtils.getClosestCenter(p.x[i], centers, d);
				newclu.get(mink).add(i);
			}
			//if there are empty clusters then we assign some a point to them randomly from the other clusters
			for (i=0;i<k;i++) {
				HashSet<Integer> cluster=newclu.get(i);
				if (cluster.size()==0) {
					HashSet<Integer> borrow=null;
					while (borrow==null) {
						borrow=newclu.get((int)(Math.random()*k));
						if (borrow.size()<2) {
							borrow=null;
						}
					}					
					Integer val=borrow.iterator().next();					
					cluster.add(val);
					borrow.remove(val);
				}
			}
			//finnaly, count the number of changes, this could be done more efficiently at the same time
			//of assigning the points to clusters, but I hope this routine will be reusable sometime for
			//perplexity calculation
			newchanges=ClusterUtils.numberOfChanges(oldclu,newclu);
			System.out.println("clustering: changes: "+newchanges);
			for (i=0;i<oldclu.size();i++) {
				HashSet<Integer> cold=oldclu.get(i);
				HashSet<Integer> cnew=newclu.get(i);
				cold.clear();
				cold.addAll(cnew);
			}			
		}
		return oldclu;	
	}	
	
}
