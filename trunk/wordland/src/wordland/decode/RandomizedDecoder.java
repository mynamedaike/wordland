package wordland.decode;

import java.util.*;
import wordland.data.*;

public class RandomizedDecoder implements Decoder{
	private Hashtable<HashSet<Integer>,ValueIndex> [] tab;
	public RandomizedDecoder() {
	}
	public static double[][] extendMatrix(double [][] matrix) {
		double [][] ret=new double [matrix.length+2][matrix.length+2];
		for (int i=0;i<matrix.length;i++) {
			for (int j=0;j<matrix.length;j++) {
				ret[i+1][j+1]=matrix[i][j];
			}
		}
		return ret;
	}
	public int [] decode(double [][] matrix,int k) {
		int [] ret=null;
		double retval=k*1000;
		int colors=getColors(k);
		int [] coloring=new int [matrix.length];
		Random rnd=new Random();
		int i;
		int iter=1000;
		while (iter>0) {
			for (i=0;i<coloring.length;i++) {
				coloring[i]=rnd.nextInt(colors);
			}
			int [] sol=dynProg(matrix,coloring,k);
			if (sol!=null) {
				double solval=getPathLength(matrix,sol);
				if (solval<retval) {
					retval=solval;
					ret=sol;
				}
			}
			iter--;
		}
		return ret; 
	}
	private int [] dynProg(double [][] matrix, int [] coloring, int k) {
		int nr=matrix.length;
		tab=new Hashtable [matrix.length];
		double [] maxtab=new double [matrix.length];
		HashSet<Integer> [] maxsets=new HashSet [matrix.length]; 
		int i;
		HashSet<Integer> empty=new HashSet<Integer>();
		for (i=0;i<tab.length;i++) {
			tab[i]=new Hashtable<HashSet<Integer>,ValueIndex>();
			tab[i].put(empty, new ValueIndex(0,matrix[0][i]));
			maxtab[i]=matrix[0][i];
			maxsets[i]=empty;
		}
		
		for (i=0;i<k+1;i++) {
			for (int dst=1;dst<nr-1;dst++) {
				if (i==k) {
					dst=nr-1;
				}
				Set<HashSet<Integer>> dstkeys=tab[dst].keySet();
				for (HashSet<Integer> sp : dstkeys) {
					if (sp.size()==i) {
						//double d=tab[dst].get(sp).value;
						double max=0;
						int maxsrc=0;
						for (int src=1;src<nr-1;src++) {
							if (max<matrix[src][dst] && !sp.contains(coloring[src])) {
								max=matrix[src][dst];
								maxsrc=src;								
							}
						}
						if (maxsrc!=0) {
							HashSet<Integer> newsp=new HashSet<Integer>();
							newsp.addAll(sp);
							newsp.add(coloring[maxsrc]);
							double d=0;
							Enumeration<ValueIndex> values=tab[maxsrc].elements();
							while (values.hasMoreElements()) {
								ValueIndex value=values.nextElement();
								if (value.value>max) {
									d=value.value;
								}
							}
							tab[dst].put(newsp,new ValueIndex(maxsrc,max+d));
						}
					}
				}
			}
		}
		return null;
	}
	private double getPathLength(double [][] matrix,int [] p) {
		int [] pp=new int [p.length+2];
		for (int i=0;i<p.length;i++) {
			pp[i+1]=p[i];
		}
		pp[0]=0;
		pp[p.length]=matrix.length;
		return getPathLengthProper(matrix, pp);
	}
	private double getPathLengthProper(double [][] matrix,int [] p) {
		double ret=0;
		for (int i=0;i<p.length-1;i++) {
			double add=0;
			add=matrix[p[i]][p[i+1]];
			ret+=add;
		}
		return ret;		
	}
	private int getColors(int k) {
		int ret=0;
		double min=1;
		for (double r=k;r<=2*k;r++) {
			double v=2*Math.pow(1+(1/r), k)*(1-k/(r+1));
			if (Math.abs(v-1)<min) {
				min=Math.abs(v-1);
				ret=(int)r;
			}
		}
		return ret;
	}
}
