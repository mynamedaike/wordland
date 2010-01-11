package wordland.classifier;

import liblinear.*;
import java.util.*;

import wordland.data.BidirectionalMap;
import wordland.data.FeatureNodeComp;
import wordland.data.Hierarchy;
import wordland.data.IndexValue;
import wordland.data.ParameterExt;
import wordland.data.ProblemExt;
import wordland.distance.EuclideanDistance;

/**
 * It's not an online/incremental algorithm; that is, it is :) but it is
 * not implemented to use it in that way;
 * Ofer Dekel, Joseph Keshet, Yoram Singer: Large Margin Hierarchical Classification
 * ICML 2004
 * see also: BatchHieron 
 */
public class OnlineHieron implements Classifier{
	public ArrayList [] Warray;
	public BidirectionalMap<Integer> allnodes = new BidirectionalMap<Integer>();
	public int traincatnum;
	public Hierarchy trainhier;
	private int iter = 1; //iterations
	
	public OnlineHieron() {
	}
	public void train(ProblemExt prob, ParameterExt param) {
		traincatnum = prob.catnum;
		Hierarchy hier = prob.hierarchy;
		trainhier = prob.hierarchy;
		//reducing the hierarchy:
		hier.reduceHierarchy(hier.root);
		//System.out.println(hier.printHierarchy());
		//System.out.println();
		int [] nodes = hier.getAllChildren(hier.root);
		int lastid = prob.catnum-1;
		for (int i=0; i<nodes.length; i++)
			if (!hier.isLeaf(nodes[i])) {
				allnodes.addFirstSecond(nodes[i], ++lastid);
			} else {
				allnodes.addFirstSecond(nodes[i], prob.getNewCategory(nodes[i]));
			}
		allnodes.addFirstSecond(prob.hierarchy.root, ++lastid);
		int N = nodes.length + 1;
		Warray = new ArrayList [N];
		for (int i=0; i<N; i++)
			Warray[i] = new ArrayList<FeatureNode>();
		for (int T=0; T<iter; T++) //training in epochs...
		for (int i=0; i<prob.l; i++) { //main iteration; visits every training example only once
			//just for showing where the training is 
			if (i%100 == 0)
				System.out.println("trained on ["+i+"] examples");
			///////
			//computing the predicted class:
			double [] dec_values = new double [prob.catnum];
			for (int j=0; j<dec_values.length; j++) {
				int [] path = prob.hierarchy.getPathToRoot(prob.getOldCategory(j));
				dec_values[j] = 0;
				for (int k=0; k<path.length; k++) {
					dec_values[j] += dotProduct(Warray[allnodes.getSecond(path[k])], prob.x[i]);
				}
			}
			IndexValue pred_y = argmax(dec_values); //prediction
			if (pred_y.index != prob.y[i]) { //prediction error occured:
				//path corresponding to the predicted class:
				int [] good_path = prob.hierarchy.getPathToRoot(allnodes.getFirst(prob.y[i]));
				double decv = 0;
				for (int m=0; m<good_path.length; m++) {
					decv += dotProduct(Warray[allnodes.getSecond(good_path[m])], prob.x[i]);
				}
				//calculating the loss:
				double dist = prob.hierarchy.getDistance(allnodes.getFirst(pred_y.index), allnodes.getFirst(prob.y[i]));
				double loss = Math.max(0, pred_y.value - decv + Math.sqrt(dist));
				//paths:
				int [] path = prob.hierarchy.getPathToRoot(allnodes.getFirst(pred_y.index));
				int [] path1 = diff(good_path, path);
				int [] path2 = diff(path, good_path);
				EuclideanDistance d = new EuclideanDistance();
				double d2 = d.distance(prob.x[i], null);
				double alpha = loss/(dist*d2*d2);
				//UPDATE:
				for (int j=0; j<path1.length; j++) {
					Warray[allnodes.getSecond(path1[j])] = add(Warray[allnodes.getSecond(path1[j])], mul(prob.x[i], alpha));
				}
				for (int j=0; j<path2.length; j++) {
					Warray[allnodes.getSecond(path2[j])] = add(Warray[allnodes.getSecond(path2[j])], mul(prob.x[i], -alpha));
				}
			}//if error
		}//endfor i
		
	}
	public void setIterations(int i) {
		iter = i;
	}
	/**
	 * returns a-b 
	 */
	private int [] diff(int [] a, int [] b) {
		ArrayList<Integer> s = new ArrayList<Integer>();
		for (int i=0; i<a.length; i++)
			s.add(a[i]);
		for (int i=0; i<b.length; i++) {
			int ind = s.indexOf(b[i]);
			if (ind >= 0)
				s.remove(ind);
		}
		int [] ret = new int [s.size()];
		for (int i=0; i<s.size(); i++)
			ret[i] = s.get(i);
		return ret;
	}
	private IndexValue argmax(double [] x) {
		if (x.length == 0)
			return new IndexValue(-1,0);
		int index = 0;
		for (int i=1; i<x.length; i++) 
			if (x[i] > x[index]) {
				index = i;
			}
		return new IndexValue(index, x[index]);
	}
	public ArrayList<FeatureNode> add(ArrayList<FeatureNode> x, FeatureNode [] b) {
		ArrayList<FeatureNode> z = new ArrayList<FeatureNode>(x);
		for (int i=0; i<b.length; i++) {
			int j;
			for (j=0; j<z.size() && z.get(j).index<b[i].index; j++);
			if (j<z.size() && z.get(j).index == b[i].index) {
				z.set(j, new FeatureNode(b[i].index, z.get(j).value + b[i].value));
			} else
				if (j<z.size() && z.get(j).index != b[i].index) {
					z.add(j, new FeatureNode(b[i].index, b[i].value));
				}
				else
					z.add(new FeatureNode(b[i].index, b[i].value));
		}
		return z;
	}
	public FeatureNode [] mul(FeatureNode [] x, double a) {
		FeatureNode [] z = new FeatureNode [x.length];
		for (int i=0; i<x.length; i++) {
			z[i] = new FeatureNode(x[i].index, x[i].value*a);
		}
		return z;
	}
	public double dotProduct(ArrayList<FeatureNode> x, FeatureNode [] z) { //copied from CosSimilarity :)
		double sum = 0;
		int i, j;
		for (i=0, j=0; x!=null && z!=null && i<x.size() && j<z.length;) {
			if (x.get(i).index < z[j].index) {
				i++;
			}
			else if (z[j].index < x.get(i).index) {
				j++;
			}
			else {
				sum += x.get(i).value * z[j].value;
				i++;
				j++;
			}
		}		
		return sum;
	}
	public int [] test(ProblemExt prob) {
		int [] labels = new int [prob.l];
		for (int i=0; i<prob.l; i++) {
			if (i%100 == 0) {
				System.out.println("tested ["+i+"]");
			}
			double [] dec_values = new double [traincatnum];
			for (int j=0; j<dec_values.length; j++) {
				int [] path = trainhier.getPathToRoot(prob.getOldCategory(j));
				dec_values[j] = 0;
				for (int k=0; k<path.length; k++) {
					dec_values[j] += dotProduct(Warray[allnodes.getSecond(path[k])], prob.x[i]);
				}
			}
			IndexValue pred_y = argmax(dec_values); //prediction
			labels[i] = pred_y.index;
			///
			/*System.out.print(i+": ");
			for (int j=0; j<dec_values.length; j++)
				System.out.print(dec_values[j]+" ");
			System.out.println();*/
			///
		}
		return labels;
	}
	public Classifier newInstance() {
		return new OnlineHieron();
	}
	///
	public void printF(ArrayList<FeatureNode> x) {
		for (int i=0; i<x.size(); i++)
			System.out.print("("+x.get(i).index+":"+x.get(i).value+")");
		System.out.println();
	}
	public void printF(FeatureNode [] x) {
		for (int i=0; i<x.length; i++)
			System.out.print("("+x[i].index+":"+x[i].value+")");
		System.out.println();
	}
	///
}
