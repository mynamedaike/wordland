package wordland.utils;

import wordland.data.Hierarchy;


/**
 * some of the more often used functions for hierarchies
 * oops, what i wanted to put here belongs somewhere else, but having this class is not a very bad idea
 */

public class HierarchyUtils {
	public static Hierarchy collapseHierarchyToLevels(Hierarchy hier,int startnode,int levels) {
		Hierarchy ret=HierarchyUtils.collapseHierarchyToLevels_private(hier, startnode, levels);
		if (startnode!=Hierarchy.root) {
			ret.addEdge(Hierarchy.root, startnode, 1.0);
		}
		return ret;
	}	
	private static Hierarchy collapseHierarchyToLevels_private(Hierarchy hier,int startnode,int levels) {
		if (!hier.contains(startnode)) {
			return null;
		}
		if (levels<=0) {
			return null;
		}
		int [] children=hier.getChildren(startnode);
		if (children==null) {
			return null;
		}
		Hierarchy ret=new Hierarchy();
		for (int i : children) {
			ret.addEdge(startnode, i, hier.getEdgeWeight(startnode, i));
			Hierarchy sub=null;
			if (levels>1) {
				sub=collapseHierarchyToLevels(hier, i, levels-1);
			}
			if (sub!=null) {
				ret.addAllChildren(sub, startnode, 1);
			}
		}
		return ret;
	}
	public static int [] getSiblings(Hierarchy hier,int node) {
		int parent=hier.getParent(node);
		if (parent==-1) {
			return null;
		}
		int [] children=hier.getChildren(parent);
		int [] ret=new int [children.length-1];
		int dest=0;
		for (int i=0;i<children.length;i++) {
			if (children[i]!=node) {
				ret[dest++]=children[i];
			}
		}
		return ret;
	}
}
