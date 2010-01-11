package wordland.utils;
import java.util.*;

import wordland.data.BidirectionalMap;
import wordland.data.FeatureNodeComp;
import wordland.data.Hierarchy;
import wordland.data.ProblemExt;

import liblinear.*;

public class ProblemUtils {
	/**
	 * takes a problem and keeps only the categories that are present in the hierarchy and gives them the category numbers
	 * from the old problem (if it's not null, if it is, then it will set new ids to each category and set the maps accordingly)
	 */
	public static ProblemExt getSubProblemWithOldMap(ProblemExt p,Hierarchy h,ProblemExt old) {
		int [] cats=h.getOnlyLeafChildren(Hierarchy.root);
		ArrayList<Integer> [] docindices = new ArrayList [cats.length];
		int sum=0;
		for (int i=0;i<cats.length;i++) {
			int newcat=p.getNewCategory(cats[i]);
			docindices[i]=ProblemUtils.getCategoryMemberIndices(p, newcat);
			sum+=docindices[i].size();
		}
		FeatureNode [][] xx=new FeatureNode[sum][];
		int [] yy=new int [sum];
		ProblemExt ret=new ProblemExt();
		if (old!=null) {
			ret.copyCategoryHash(old);
		}
		ret.copyTermHash(p);
		int xxi=0;
		int newcat=0,numcat=0;
		for (int i=0;i<cats.length;i++) {
			int pcat=p.y[docindices[i].get(0).intValue()];
			int oldcat=p.getOldCategory(pcat);
			if (ret.getNewCategory(oldcat)==-1) {
				ret.addCategoryMap(oldcat, newcat);
			}
			else {
				newcat=ret.getNewCategory(oldcat);
			}
			for (Integer ind : docindices[i]) {
				xx[xxi]=p.x[ind.intValue()];
				yy[xxi++]=newcat;
			}
			newcat++;
			numcat++;
		}
		ret.bias=p.bias;
		ret.catnum=numcat;
		ret.l=sum;
		ret.n=p.n;
		ret.x=xx;
		ret.y=yy;
		ret.hierarchy=h;
		return ret;
	}
	public static ArrayList<Integer> getCategoryMemberIndices(ProblemExt p,int c) {
		ArrayList<Integer> ret=new ArrayList<Integer> ();
		for (int i=0;i<p.l;i++) {
			if (p.y[i]==c) {
				ret.add(i);
			}
		}
		return ret;
	}
	/**
	 * it will search for all the appearances of leaves originating from node in the labels corresponding to a set of documents, passed as "y"
	 */
	public static ArrayList<Integer> getHierarchyNodeIndices(int [] y,Hierarchy h,int node) {
		ArrayList<Integer> ret=new ArrayList<Integer> ();
		int [] leaves1=h.getOnlyLeafChildren(node);
		if (leaves1!=null) {
			HashSet<Integer> leaves=new HashSet<Integer>();
			for (int i : leaves1) {
				leaves.add(i);
			}
			for (int i=0;i<y.length;i++) {
				if (leaves.contains(y[i])) {
					ret.add(i);
				}
			}
		}
		return ret;
	}
	/**
	 * regroups a dataset into two categories as indicated by the classifier code.
	 * as liblinear does not need to receive the classes in contiguous form, we have
	 * to make a new target vector only, the instance data can be copied simply from
	 * the original dataset.
	 */
	public static ProblemExt regroupData(ProblemExt old, int [] classmap) {
		ProblemExt ret=new ProblemExt(old);
		ret.y=new int [ret.l];
		for (int i=0;i<ret.l;i++) {
			ret.y[i]=classmap[old.y[i]];
		}
		return ret;
	}
	/**
	 * The same as above, but with 3 values: 
	 * 0 - exclude class, +1 - positive class, -1 - negative class
	 */
	public static ProblemExt regroupData3(ProblemExt old, int [] classmap) {
		ProblemExt ret = new ProblemExt();
		int c = 0;
		for (int i=0; i<old.l; i++) {
			if (classmap[old.y[i]] != 0)
				c++;
		}
		ret.x = new FeatureNode [c][];
		ret.y = new int [c];
		ret.l = c;
		ret.n = old.n;
		ret.bias = old.bias;
		ret.catnum = old.catnum;
		ret.hierarchy = old.hierarchy;
		ret.copyHashes(old);
		c = 0;
		for (int i=0; i<old.l; i++) {
			if (classmap[old.y[i]] != 0) {
				ret.y[c] = classmap[old.y[i]]; //+1 or -1
				ret.x[c] = new FeatureNode[old.x[i].length];
				for (int j=0; j<old.x[i].length; j++) {
					ret.x[c][j] = new FeatureNode(old.x[i][j].index, old.x[i][j].value);
				}	
				c++;
			}
		}
		return ret;
	}
	/**
	 * sets all the categories in a problem to the same id
	 */
	public static void setAllCategories(ProblemExt p, int c) {
		for (int i=0;i<p.l;i++) {
			p.y[i]=c;
		}
		p.clearCategoryMap();
		p.addCategoryMap(c, c);
		p.hierarchy=new Hierarchy();
		p.hierarchy.addEdge(Hierarchy.root, c, 1.0);
		p.catnum=1;
	}
	/**
	 * merges a lot of problems into one
	 * combines the categories in the subproblems, merges the category ids too (if they are not inconsistent)
	 * remapping the documents is a serious issue, should be commented on more extensively
	 * to save memory, sometimes documents are just copied into merged problems, but this is also
	 * a necessity, when the termmaps are supposed to be the same over the original problems and also
	 * in the resulting problem (for example, for feature selection). when adding the validation data
	 * to the training set, it is however wise to remap all the data
	 */
	public static ProblemExt mergeProblems(ProblemExt [] p,boolean remap) {
		ProblemExt ret=new ProblemExt();
		ret.hierarchy=new Hierarchy();
		int i,s=0;
		//local catmap that will include also a map between non leaf nodes of the
		//hierarchy too, because that need to be remapped too, but not necessarily
		//stored in the resulting problem
		BidirectionalMap<Integer> tempcatmap=new BidirectionalMap<Integer>();
		tempcatmap.addFirstSecond(Hierarchy.root, Hierarchy.root);
		int pathmin=0;  //this will store what is the max id that is used in the leaf categories so
		//that we can use numbers larger than this to encode nodes in the resulting hierarchy
		for (i=0;i<p.length;i++) {
			s+=p[i].l;
			HashSet<Integer> catset=ProblemUtils.getCategories(p[i]);
			int maxtemp=0;
			for (int cat : catset) {
				int m=p[i].getOldCategory(cat);
				if (m>maxtemp) {
					maxtemp=m;
				}
			}
			if (maxtemp+1>pathmin) {
				pathmin=maxtemp+1;
			}
		}
		FeatureNode [][] xx=new FeatureNode[s][];
		int [] yy=new int [s];
		int offs=0;
		int newcats=0;
		for (i=0;i<p.length;i++) {
			for (int j=0;j<p[i].l;j++) {
				if (remap) {
					xx[offs]=ret.remapDocument(p[i].x[j], p[i]);
				}
				else {
					xx[offs]=p[i].x[j];
				}
				int oldc=p[i].getOldCategory(p[i].y[j]);  //the old category of the copied document, taken from it's original problem
				int newc=p[i].y[j];  //the new category that it will belong to
				if (oldc==-1) {
					//if the constituent problem p[i] has no was old-to-new category mapping
					//then we map it's existing category to the new mapping in oldc
					//though this should never happen :))
					oldc=p[i].y[j];
					System.out.println("[ProblemUtils.mergeProblems] something's fishy with the old category");
				}
				int newcc;  //let's check whether we stored this id before?
				if ((newcc=ret.getNewCategory(oldc))==-1) {
					ret.addCategoryMap(oldc, newc);
					tempcatmap.addFirstSecond(oldc, newc);
					//ret.hierarchy.addEdge(Hierarchy.root, newc, 1.0);
					newcats++;
				}
				else if (newcc!=newc){
					System.out.println("[ProblemUtils.mergeProblems] some inconsistency is found in the categories to be merged");					
				}
				yy[offs++]=newc;
				//now we need to construct the whole path to the root of this category
				int [] path = p[i].hierarchy.getPathToRoot(oldc);
				boolean done=false;
				for (int op=1;op<path.length && !done;op++) {
					int orip=path[op];  //original parent
					int oric=path[op-1];  //original category
					double edgew=p[i].hierarchy.getEdgeWeight(orip, oric);
					int retco=ret.getOldCategory(tempcatmap.getSecond(oric));  //returned problem old category
					if (retco==-1) {
						retco=tempcatmap.getSecond(oric); //it is a non-leaf category, so it has no old/new mapping, whatever it's id is, is fine
					}
					int retpo=ret.hierarchy.getParent(retco);  //returned problem old parent
					if (retpo==-1) {
						int retponew=-1;
						if (tempcatmap.containsFirst(orip)) {
							retponew=tempcatmap.getSecond(orip);							
						}
						if (retponew==-1) {
							retponew=pathmin++;
							tempcatmap.addFirstSecond(orip, retponew);
						}
						ret.hierarchy.addEdge(retponew, retco, edgew);
					}
					else {
						done=true;
					}
				}
			}
		}
		if (!remap) {
			ret.setTermMap(p[0].getTermMap());
		}
		ret.catnum=newcats;
		ret.bias=p[0].bias;  //where to get this information from?
		ret.l=s;
		ret.n=ret.getTermMap().size()+1;
		ret.x=xx;
		ret.y=yy;
		return ret;
	}
	/**
	 * it merges all the leaf categories belonging to a list of categories
	 * it can set increasing categories or categories with node ids
	 * for remap, see above
	 */
	public static ProblemExt mergeAllChildren(ProblemExt prob,Hierarchy hier,int [] nodes,boolean increasingcats,boolean remap) {
		ProblemExt [] probs=new ProblemExt [nodes.length];
		for (int i=0;i<nodes.length;i++) {
			probs[i]=ProblemUtils.getSubProblemWithOldMap(prob, hier.getSubHierarchy(nodes[i]), prob);
			if (increasingcats) {
				ProblemUtils.setAllCategories(probs[i], i);
			}
			else {
				ProblemUtils.setAllCategories(probs[i], nodes[i]);
			}
		}
		ProblemExt newprob=ProblemUtils.mergeProblems(probs,remap);
		return newprob;
	}
	public static int getNumberofCategories(ProblemExt p) {
		HashSet<Integer> hash=new HashSet<Integer>();
		for (int i=0;i<p.l;i++) {
			hash.add(p.y[i]);
		}
		return hash.size();
	}
	public static HashSet<Integer> getCategories(ProblemExt p) {
		HashSet<Integer> hash=new HashSet<Integer>();
		for (int i=0;i<p.l;i++) {
			hash.add(p.y[i]);
		}
		return hash;		
	}
	public static ProblemExt getProblem(FeatureNode [] x,double bias,int max) {
		ProblemExt ret=new ProblemExt();
		ret.l=1;
		ret.x=new FeatureNode [1][];
		ret.y=new int [1];
		ret.y[0]=0;
		ret.bias=bias;
		ret.x[0]=x;
		ret.catnum=1;
		if (max==-1) {
			max=x[x.length-1].index;
		}
		ret.n=max;
		return ret;
	}
	/**
	 * transforms a set of labels to the original labels found in the corpus
	 * (so that it can be easily looked up in the hierarchy)
	 */
	public static int [] transformNewLabelsToOld(int [] lab,ProblemExt p) {
		int [] ret=new int [lab.length];
		for (int i=0;i<lab.length;i++) {
			int ii=p.getOldCategory(lab[i]);
			if (ii!=-1) {
				ret[i]=ii;
			}
			else {
				System.out.println("[ProblemUtils.transformNewLabelsToOld] unknown category found");
				ret[i]=lab[i];
			}
		}
		return ret;
	}
	public static int [] transformOldLabelsToNew(int [] lab,ProblemExt p) {
		int [] ret=new int [lab.length];
		for (int i=0;i<lab.length;i++) {
			int ii=p.getNewCategory(lab[i]);
			if (ii!=-1) {
				ret[i]=ii;
			}
			else {
				System.out.println("[ProblemUtils.transformOldLabelsToNew] unknown category found");
				ret[i]=lab[i];
			}
		}
		return ret;
	}
	public static ProblemExt createResultProblem(int [] pred) {
		ProblemExt ret=new ProblemExt();
		ret.l=pred.length;
		ret.n=0;
		ret.y=pred;
		return ret;
	}
	/**
	 * collapses a problem to certain number of upper levels, so that the
	 * nodes on that level will become the leaves of the hiearchy
	 */
	public static ProblemExt collapseProblemToLevels(ProblemExt p,int startnode,int levels,BidirectionalMap<Integer> catremap) {
		ProblemExt ret=new ProblemExt(p);
		ret.y=new int [p.l];
		HashSet<Integer> cats=p.hierarchy.getChildrenToLevel(startnode, levels);
		Hashtable<Integer,Integer> catmap=new Hashtable<Integer,Integer>();
		BidirectionalMap<Integer> mycatremap;
		if (catremap==null) {
			mycatremap = new BidirectionalMap<Integer>();			
		}
		else {
			mycatremap = catremap;
		}
		ret.setCategoryMap(mycatremap);
		//System.out.println("[ProblemUtils.collapseProblemToLevels] started remapping documents");
		for (int i=0;i<p.l;i++) {
			ret.x[i]=p.x[i];
			Integer replacecat=catmap.get(p.y[i]);
			if (replacecat==null) {
				int [] allcats=p.hierarchy.getPathToRoot(p.getOldCategory(p.y[i]));
				int j=-1;
				//find the lowest category that is a super-category of the original category
				for (j=0;j<allcats.length && !cats.contains(allcats[j]);j++);
				replacecat=allcats[j];
				catmap.put(p.y[j], replacecat);
			}
			Integer newcat=mycatremap.getSecond(replacecat);
			if (newcat==null) {
				if (catremap==null) {
					newcat=mycatremap.size();
					mycatremap.addFirstSecond(replacecat,newcat);
				}
				else {
					System.out.println("[ProblemUtils.collapseProblemToLevels] found a category in test that was not included in train");
					newcat=0;
				}
			}
			ret.y[i]=newcat;
			//System.out.println("remapped document "+i+" from category "+p.y[i]+" to category "+newcat);
		}
		ret.catnum=mycatremap.size();
		ret.hierarchy=HierarchyUtils.collapseHierarchyToLevels(p.hierarchy, startnode, levels);
		return ret;
	}
	public static FeatureNode [] copyDocument(FeatureNode [] doc) {
		FeatureNode [] ret=new FeatureNode[doc.length];
		for (int i=0;i<doc.length;i++) {
			ret[i]=new FeatureNode(doc[i].index,doc[i].value);
		}
		return ret;
	}
	public static FeatureNode [][] copyDocuments(FeatureNode [][] docs) {
		FeatureNode [][] ret=new FeatureNode[docs.length][];
		for (int i=0;i<docs.length;i++) {
			ret[i]=copyDocument(docs[i]);
		}
		return ret;
	}
	/**
	 * remaps a document to old terms (the one hopefully consistent through all problem representations
	 * because it's loaded from the input files)
	 * termmap has to be first=old, second=new
	 */
	public static FeatureNode [] mapToOldTerms(FeatureNode [] doc,BidirectionalMap<Integer> termmap) {
		ArrayList<FeatureNodeComp> ret=new ArrayList<FeatureNodeComp>();
		for (FeatureNode n : doc) {
			ret.add(new FeatureNodeComp(termmap.getFirst(n.index),n.value));			
		}
		Collections.sort(ret);
		FeatureNodeComp [] ret2=new FeatureNodeComp[ret.size()];
		ret.toArray(ret2);
		return ret2;
	}
	
}
