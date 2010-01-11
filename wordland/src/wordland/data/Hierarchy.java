package wordland.data;

import java.io.*;
import java.util.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;

/*
 * a class to store the category hierarchy and do some things with it 
 */
public class Hierarchy {
	public static int root=1000000;
	public int depth;
	private DirectedGraph<Integer,DefaultWeightedEdge> graph;
	public Hierarchy() {
		graph=new DefaultDirectedWeightedGraph<Integer,DefaultWeightedEdge>(DefaultWeightedEdge.class);
		depth = 0;
	}
	public Hierarchy(Hierarchy o) {
		graph=new DefaultDirectedWeightedGraph<Integer,DefaultWeightedEdge>(DefaultWeightedEdge.class);
		depth=addAllChildren(o,Hierarchy.root,0);
	}
	public void load(String fname) {
		try {
			BufferedReader in=new BufferedReader(new FileReader(fname));
			String line;
			while ((line=in.readLine())!=null) {
				String [] w=line.split(" ");
				if (w.length > depth) depth = w.length;
				int [] classes=new int [w.length+1];
				classes[0]=root;
				for (int i=0;i<w.length;i++) {
					classes[i+1]=Integer.parseInt(w[i]);
				}
				for (int i=0;i<classes.length-1;i++) {
					int c1=classes[i];
					int c2=classes[i+1];
					addEdge(c1,c2,1.0);
				}
			}
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}
	public boolean contains(int head) {
		return graph.containsVertex(head);
	}
	public int [] getChildren(int head) {
		if (!graph.containsVertex(head)) {
			return null;
		}
		int n=graph.outDegreeOf(head);
		if (n==0) {
			return null;
		}
		int [] ret=new int [n];
		int i=0;
		for (DefaultWeightedEdge e: graph.outgoingEdgesOf(head)) {
			ret[i++]=graph.getEdgeTarget(e).intValue();
		}
		return ret;
	}
	public int [] getAllChildren(int head) {
		if (!graph.containsVertex(head)) {
			return null;
		}
		int [] children=getChildren(head);
		if (children==null) {
			return null;
		}
		int n=children.length;
		int [][]list=new int [n][];
		int all=n;
		for (int i=0;i<n;i++) {
			list[i]=getAllChildren(children[i]);
			if (list[i]!=null) {
				all+=list[i].length;
			}
		}
		int [] ret=new int [all];
		int retc=0;
		for (int i=0;i<n;i++) {
			ret[retc++]=children[i];
			if (list[i]!=null) {
				for (int j=0;j<list[i].length;j++) {
					ret[retc++]=list[i][j];
				}
			}
		}
		return ret;
	}
	public HashSet<Integer> getChildrenToLevel(int head,int levels) {
		if (levels==0) {
			return null;
		}
		if (!graph.containsVertex(head)) {
			return null;
		}
		int [] children=getChildren(head);
		if (children==null) {
			return null;
		}
		HashSet<Integer> ret=new HashSet<Integer>();
		for (int i : children) {
			if (levels>1) {
				HashSet<Integer> sub=getChildrenToLevel(i,levels-1);
				if (sub!=null) {
					ret.addAll(sub);
				}
			}
			ret.add(i);
		}
		return ret;
	}
	/**
	 * Returns only the inner vertices (non-leaves) 
	 */
	public int [] getInnerChildren(int head) {
		return null;
	}
	/**
	 * Reduce the hierarchy; 
	 * paths (i -> j -> k) are transformed to (i -> k) 
	 */
	public void reduceHierarchy(int head) {
		if (isLeaf(head)) 
			return;
		int [] children = getChildren(head);
		if (children.length == 1) {
			int p = getParent(head);
			if (p < 0) {
				reduceHierarchy(children[0]);
				return;
			}
			graph.addEdge(p, children[0]);
			graph.removeEdge(head, children[0]);
			graph.removeVertex(head);
			reduceHierarchy(children[0]);
		} else {
			for (int i=0; i<children.length; i++)
				reduceHierarchy(children[i]);
		}
	}
	public int addAllChildren(Hierarchy o,int head,int level) {
		int [] children=o.getChildren(head);
		if (children==null) {
			return level;
		}
		int max=0;
		for (int i=0;i<children.length;i++) {
			addEdge(head,children[i],1.0);
			int newdepth=addAllChildren(o,children[i],level+1);
			if (newdepth>max) {
				max=newdepth;
			}
		}
		return max;
	}
	public int [] getOnlyLeafChildren(int head) {
		if (!graph.containsVertex(head)) {
			return null;
		}
		int [] children=getChildren(head);
		if (children==null) {
			return null;
		}
		int n=children.length;
		int [][]list=new int [n][];
		int all=0;
		for (int i=0;i<n;i++) {
			list[i]=getOnlyLeafChildren(children[i]);
			if (list[i]!=null) {
				all+=list[i].length;
			}
			else {
				all+=1;
			}
		}
		int [] ret=new int [all];
		int retc=0;
		for (int i=0;i<n;i++) {
			if (list[i]!=null) {
				for (int j=0;j<list[i].length;j++) {
					ret[retc++]=list[i][j];
				}
			}
			else {
				ret[retc++]=children[i];
			}
		}
		return ret;		
	}
	public int getParent(int head) {
		if (!graph.containsVertex(head)) {
			return -1;
		}
		Set<DefaultWeightedEdge> ee=graph.incomingEdgesOf(head);
		if (ee.size()!=1) {
			return -1;
		}
		Iterator<DefaultWeightedEdge> ei=ee.iterator();
		DefaultWeightedEdge e=ei.next();
		return graph.getEdgeSource(e).intValue();
	}
	public int [] getPathToRoot(int head) {
		ArrayList<Integer> path = new ArrayList<Integer>();
		int vertex = head;
		path.add(vertex);
		while (vertex != root) {
			vertex = getParent(vertex);
			path.add(vertex);
		}
		int [] p = new int [path.size()];
		for (int i=0; i<path.size(); i++)
			p[i] = path.get(i);
		return p;
	}
	/**
	 * Get the Lowest Common Ancestor of two nodes
	 */
	public int getLCA(int v1, int v2) {
		int pv1 [] = getPathToRoot(v1);
		int pv2 [] = getPathToRoot(v2);
		int r = root;
		for (int i=0; i<pv1.length; i++) {
			for (int j=0; j<pv2.length; j++)
				if (pv1[i] == pv2[j]) {
					r = pv1[i];
					break;
				}
			if (r == pv1[i]) break;
		}
		return r;
	}
	public int getDistance(int v1, int v2) {
		int pv1 [] = getPathToRoot(v1);
		int pv2 [] = getPathToRoot(v2);
		int d = 0;
		for (int i=0; i<pv1.length; i++) {
			for (int j=0; j<pv2.length; j++)
				if (pv1[i] == pv2[j]) {
					d = i+j;
					break;
				}
			if (d != 0) break;
		}
		return d;
	}
	public void addEdge(int c1,int c2,double w) {
		if (!graph.containsVertex(c1)) {
			graph.addVertex(c1);
		}
		if (!graph.containsVertex(c2)) {
			graph.addVertex(c2);
		}
		try {
			DefaultWeightedEdge e=graph.addEdge(c1, c2);
			((WeightedGraph)graph).setEdgeWeight(e, w);
		}
		catch (Exception e) {  //if this edge already exists, an Exception is thrown						
		}
	}
	public Hierarchy getSubHierarchy(int head) {
		Hierarchy ret=new Hierarchy();
		if (!graph.containsVertex(head)) {
			return null;
		}
		ret.addEdge(root, head, 1.0);
		ret.depth=ret.addAllChildren(this,head,0);
		return ret;
	}
	public double getEdgeWeight(int c1,int c2) {
		DefaultWeightedEdge e=graph.getEdge(c1, c2);
		if (e==null) {
			return -1;
		}
		else return ((WeightedGraph)graph).getEdgeWeight(e);
	}
	/**
	 * adds only the children of a node to another hierarchy
	 */
	public void addChildren(int head,Hierarchy where) {
		int [] children=getChildren(head);
		if (children==null) {
			return;
		}
		for (int i=0;i<children.length;i++) {
			where.addEdge(head, children[i], getEdgeWeight(head,children[i]));
		}
	}
	public String printHierarchy() {
		return printNode(root);
	}
	private String printNode(int node) {
		int [] children=getChildren(node);
		if (children==null) {
			return ""+node;
		}
		else {
			StringBuffer ret=new StringBuffer();
			ret.append("(["+node+"] ");
			for (int i=0;i<children.length;i++) {
				ret.append(printNode(children[i]));
				if (i<children.length-1) {
					ret.append(" ");
				}
			}
			ret.append(")");
			return ret.toString();
		}
	}
	public boolean isLeaf(int v) {
		if (graph.outDegreeOf(v) == 0)
			return true;
		return false;
	}
	public int getNodeNum() {
		return graph.vertexSet().size();
	}
}
