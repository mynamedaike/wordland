package wordland.cluster;
import java.util.*;

import wordland.*;
import wordland.data.ProblemExt;
import wordland.distance.*;
import wordland.utils.Utils;

import liblinear.*;

public class ClusterUtils {
	public static FeatureNode [][] calcCenters(ProblemExt p,ArrayList<HashSet<Integer>> oldclu) {
		int k=oldclu.size();
		FeatureNode [][] centers=new FeatureNode [k][];
		int i;
		for (i=0;i<k;i++) {
			centers[i]=null;
			HashSet<Integer> cluster=oldclu.get(i);
			for (Integer ind : cluster) {
				centers[i]=Utils.add(centers[i], p.x[ind.intValue()]);
			}
			centers[i]=Utils.mul(centers[i], 1.0/cluster.size());
		}
		return centers;
	}
	public static int numberOfChanges(ArrayList<HashSet<Integer>> c1,ArrayList<HashSet<Integer>> c2) {
		int ret=0;
		for (int i=0;i<c1.size();i++) {
			HashSet<Integer> s1=c1.get(i);
			HashSet<Integer> s2=c2.get(i);
			for (Integer ii : s1) {
				if (!s2.contains(ii)) {
					ret++;
				}
			}
		}
		return ret;
	}
	public static int getClosestCenter(FeatureNode [] x,FeatureNode [][] centers,DistanceMetric d) {
		double min=0;
		int mink=-1;
		int i,k=centers.length;
		for (i=0;i<k;i++) {
			if (centers[i]!=null) {
				double dd=d.distance(x, centers[i]);
				if (dd<min || mink==-1) {
					min=dd;
					mink=i;
				}					
			}
		}
		return mink;
	}
	public static int [] hashToIntList(ArrayList<HashSet<Integer>> h,int length) {
		int [] ret=new int [length];
		for (int cluster=0;cluster<h.size();cluster++) {
			for (Integer ii : h.get(cluster)) {
				ret[ii.intValue()]=cluster;
			}
		}
		return ret;
	}
	public static ArrayList<HashSet<Integer>> intListToHash(int [] l,int k) {
		ArrayList<HashSet<Integer>> ret=new ArrayList<HashSet<Integer>>();
		for (int i=0;i<k;i++) {
			ret.add(new HashSet<Integer>());			
		}
		for (int i=0;i<l.length;i++) {
			ret.get(l[i]).add(i);
		}
		return ret;
	}
}
