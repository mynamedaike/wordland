package wordland.data;
import liblinear.FeatureNode;
import liblinear.Problem;
import java.util.*;


import org.jgrapht.util.ArrayUnenforcedSet;

import sun.misc.Sort;



public class ProblemExt extends Problem{
	public int catnum=0;
	//old=first, new=second
	private BidirectionalMap<Integer> categorymap=new BidirectionalMap<Integer> ();
	private BidirectionalMap<Integer> termmap=new BidirectionalMap<Integer> ();
	//public for a while, maybe we'll change it to private
	public Hierarchy hierarchy;
	//for active learning:
	public HashMap<Integer, Integer> seedData;
	public ProblemExt() {
		super();
	}
	/**
	 * not really a "by the book" copy constructor, but we can't replicate all the data all the time
	 */
	public ProblemExt(ProblemExt p) {
		this.l=p.l;
		this.n=p.n;
		this.catnum=p.catnum;
		this.bias=p.bias;
		this.y=p.y;
		this.x=p.x;
		this.hierarchy=p.hierarchy;
		copyHashes(p);
	}
	public void copyHashes(ProblemExt from) {
		copyCategoryHash(from);
		copyTermHash(from);
	}
	public void copyTermHash(ProblemExt from) {
		termmap=new BidirectionalMap<Integer>(from.termmap);
	}
	public void copyCategoryHash(ProblemExt from) {
		categorymap=new BidirectionalMap<Integer>(from.categorymap);
	}
	public void addTermHash(ProblemExt from) {
		termmap.addMap(from.termmap);
	}
	public void addCategoryMap(int old,int neww) {
		categorymap.addFirstSecond(old, neww);
	}
	public int getNewCategory(int old) {
		Integer i=categorymap.getSecond(old);
		if (i!=null) {
			return i.intValue();
		}
		else {
			return -1;
		}
	}
	public int getOldCategory(int neww) {
		Integer i=categorymap.getFirst(neww);
		if (i!=null) {
			return i.intValue();
		}
		else {
			return -1;
		}
	}
	public void addTerm(int old,int neww) {
		termmap.addFirstSecond(old, neww);
	}
	public int getNewTerm(int old) {
		Integer i=termmap.getSecond(old);
		if (i!=null) {
			return i.intValue();
		}
		else {
			return -1;
		}		
	}
	public int getOldTerm(int neww) {
		Integer i=termmap.getFirst(neww);
		if (i!=null) {
			return i.intValue();
		}
		else {
			return -1;
		}		
	}
	public BidirectionalMap<Integer> getCategoryMap() {
		return categorymap;
	}
	public void setCategoryMap(BidirectionalMap<Integer> c) {
		categorymap=c;
	}
	public void clearCategoryMap() {
		categorymap.clear();
	}
	public BidirectionalMap<Integer> getTermMap() {
		return termmap;
	}
	public void setTermMap(BidirectionalMap<Integer> c) {
		termmap=c;
	}
	public void clearTermMap() {
		termmap.clear();
	}
	public void createDummyTermHash(int n) {
		for (int i=1;i<n;i++) {
			addTerm(i,i);
		}
	}
	/**
	 * remaps a document from an other problem to this problems' termmap, adding new terms if necessary
	 * the linkage is done through the old 
	 */
	public FeatureNode [] remapDocument(FeatureNode [] terms,ProblemExt original) {
		ArrayList<FeatureNodeComp> ret=new ArrayList<FeatureNodeComp>();
		for (int i=0;i<terms.length;i++) {
			int oriold=original.getOldTerm(terms[i].index);
			int thisnew=this.getNewTerm(oriold);
			if (thisnew==-1) {
				thisnew=this.termmap.size()+1;
				this.addTerm(oriold, thisnew);
			}
			ret.add(new FeatureNodeComp(thisnew,terms[i].value));
		}
		Collections.sort(ret);
		FeatureNodeComp [] ret2=new FeatureNodeComp[ret.size()];
		ret.toArray(ret2);
		return ret2;
	}
	public void addPlusDimension() {
		for (int i=0; i<this.l; i++) {
			FeatureNode [] a = new FeatureNode [this.x[i].length+1];
			System.arraycopy(this.x[i], 0, a, 0, this.x[i].length);
			a[a.length-1] = new FeatureNode(this.n, 1);
			this.x[i] = a;
		}
		this.n++;
	}
	public int getHierarchyCategoryNum() {
		return hierarchy.getNodeNum();
	}
}
