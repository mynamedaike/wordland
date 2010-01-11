package wordland.utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import wordland.data.BidirectionalMap;
import wordland.data.IndexValue;
import wordland.data.ValueIndex;
import wordland.distance.*;

import liblinear.*;

public class Utils {
	/*public Utils() {
		
	}*/
	public static FeatureNode [] norml2(FeatureNode [] f) {
		if (f==null) {
			return null;
		}
		double sum=0;
		FeatureNode [] ret=new FeatureNode [f.length];
		for (int i=0;i<f.length;i++) {
			sum+=Math.pow(f[i].value, 2);			
		}
		sum=1.0/Math.sqrt(sum);
		for (int i=0;i<f.length;i++) {
			ret[i]=new FeatureNode(f[i].index,f[i].value*sum);
		}
		return ret;
	}
	public static void norml2(Problem p) {
		for (int i=0;i<p.l;i++) {
			p.x[i]=norml2(p.x[i]);
		}
	}
	public static FeatureNode [] normalize(FeatureNode [] f,DistanceMetric d) {
		if (f==null) {
			return null;
		}
		if (d==null) {
			return f;
		}
		double norm=d.distance(f, null);
		if (norm==0) {
			return f;
		}
		return mul(f,1.0/norm);
	}
	public static void normalize(Problem p,DistanceMetric d) {
		for (int i=0;i<p.l;i++) {
			p.x[i]=normalize(p.x[i],d);
		}
	}
	public static FeatureNode [] add(FeatureNode[]x, FeatureNode []y) {
		FeatureNode [] temp=new FeatureNode [((x!=null)?x.length:0)+((y!=null)?y.length:0)];
		int i,j,now=0;
		for (i=0,j=0;x!=null && y!=null && i<x.length && j<y.length;) {
			if (x[i].index<y[j].index) {
				temp[now++]=new FeatureNode(x[i].index,x[i].value);
				i++;
			}
			else if (y[j].index<x[i].index) {
				temp[now++]=new FeatureNode(y[j].index,y[j].value);
				j++;
			}
			else {
				temp[now++]=new FeatureNode(x[i].index,x[i].value+y[j].value);
				i++;
				j++;
			}
		}
		for (;x!=null && i<x.length;i++) {
			temp[now++]=new FeatureNode(x[i].index,x[i].value);
		}
		for (;y!=null && j<y.length;j++) {
			temp[now++]=new FeatureNode(y[j].index,y[j].value);
		}
		FeatureNode [] ret=new FeatureNode [now];
		System.arraycopy(temp, 0, ret, 0, now);  //rather ugly, but it may be faster than a for
		return ret;
	}
	public static FeatureNode [] mul(FeatureNode [] x,double s) {
		if (x==null) {
			return null;
		}
		FeatureNode [] ret=new FeatureNode [x.length];
		for (int i=0;i<x.length;i++) {
			ret[i]=new FeatureNode(x[i].index,x[i].value*s);
		}
		return ret;
	}
	/**
	 * returns the value of an index in a list of feature nodes with increasing index using binary search
	 * if there's no node at the index, then it returns 0, because all values have to be nonzero
	 */
	public static double getFeatureValue(FeatureNode [] x,int i) {
		int left=0,right=x.length-1;
		while (left!=right) {
			int middle;
			if (right-left>1) {
				middle=(left+right)/2;
			}
			else {
				if (x[left].index==i) {
					middle=left;
				}
				else if (x[right].index==i) {
					middle=right;
				}
				else {
					return 0;
				}
			}
			if (x[middle].index<i) {
				left=middle;
			}
			else if (x[middle].index>i) {
				right=middle;
			}
			else {
				return x[middle].value;
			}
		}
		return 0;
	}
	public static FeatureNode[] sparsify(double [] occ) {
		int nonzero=0;
		for (int j=0;j<occ.length;j++) {
			nonzero+=(occ[j]!=0)?1:0;
		}
		FeatureNode[] ret=new FeatureNode [nonzero];
		nonzero=0;
		//what feature index is occ[0]? is it 1? i think it is
		for (int j=0;j<occ.length;j++) {
			if (occ[j]!=0) {
				ret[nonzero++]=new FeatureNode(j+1,occ[j]);
			}
		}
		return ret;
	}
	public static int [] convertProbabilitiesToBestClass(IndexValue [][] p) {
		int [] ret=new int [p.length];
		for (int i=0;i<p.length;i++) {
			int maxi=-1;
			double max=0;
			for (int j=0;j<p[i].length;j++) {
				if (maxi==-1 || p[i][j].value>max) {
					maxi=j;
					max=p[i][j].value;
				}				
			}
			ret[i]=p[i][maxi].index;
		}
		return ret;
	}
	/**
	 * creates a mask of category assignment by setting the value of 1 for
	 * the specified category and 0 for all the other categories
	 */
	public static int [] createCategoryMask(int [] original,int cat) {
		int [] ret=new int [original.length];
		for (int i=0;i<original.length;i++) {
			if (original[i]==cat) {
				ret[i]=1;
			}
			else {
				ret[i]=0;
			}
		}
		return ret;
	}
	public static BidirectionalMap<Integer> mergeTermMaps(Hashtable<Integer,BidirectionalMap<Integer>> termmaps, int nrf) {
		BidirectionalMap<Integer> termmap;
		Hashtable<Integer,ValueIndex> occ=new Hashtable<Integer,ValueIndex>();
		for (int catid : termmaps.keySet()) {
			BidirectionalMap<Integer> tmap=termmaps.get(catid);
			for (int oldindex : tmap.getFirsts()) {
				ValueIndex olddata=occ.get(oldindex);
				if (olddata==null) {
					olddata=new ValueIndex(oldindex,1.0);
					occ.put(oldindex, olddata);
				}
				else {
					olddata.value+=1;
				}
			}
		}
		ArrayList<ValueIndex> values=new ArrayList<ValueIndex>();
		for (int oldindex : occ.keySet()) {
			values.add(occ.get(oldindex));
		}
		Collections.sort(values);
		Collections.reverse(values);
		termmap=new BidirectionalMap<Integer>();
		for (int i=0; i<nrf && i<values.size(); i++) {
			termmap.addFirstSecond(values.get(i).index, i+1);
			//System.out.println(values.get(i).value + "," + values.get(i).index);
		}
		return termmap;
	}
	public static BidirectionalMap<Integer> getTermMap(FeatureNode [] list) {
		BidirectionalMap<Integer> ret=new BidirectionalMap<Integer>();
		for (int i=0;i<list.length;i++) {
			ret.addFirstSecond(list[i].index, i);
		}
		return ret;
	}
}
