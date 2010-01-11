package wordland.classifier;

import liblinear.*;

import java.util.*;

import wordland.competitions.lsthc09.Main;
import wordland.data.BidirectionalMap;
import wordland.data.FeatureNodeComp;
import wordland.data.Hierarchy;
import wordland.data.IndexValue;
import wordland.data.ParameterExt;
import wordland.data.ProblemExt;
import wordland.distance.EuclideanDistance;
import wordland.utils.EvalMeasures;
import wordland.utils.ProblemUtils;


/**
 * It's not an online/incremental algorithm; that is, it is :) but it is
 * not implemented to use it in that way;
 * Ofer Dekel, Joseph Keshet, Yoram Singer: Large Margin Hierarchical Classification
 * ICML 2004
 * see also: BatchHieron 
 */
public class HierMultiPerceptron implements Classifier{
	public ArrayList [] Warray;
	public BidirectionalMap<Integer> allnodes = new BidirectionalMap<Integer>();
	public int traincatnum;
	public Hierarchy hier;
	private int iter = 1; //iterations
	private int [][] path2root; //paths in the hierarchy
	public int evalstep = 10;
	
	public HierMultiPerceptron() {
	}
	public void train(ProblemExt prob, ParameterExt param) {
		traincatnum = prob.catnum;
		hier = prob.hierarchy;
		//reducing the hierarchy:
		hier.reduceHierarchy(hier.root);
		//storing the paths:
		path2root = new int[prob.catnum][];
		for (int i=0; i<prob.catnum; i++) {
			path2root[i] = hier.getPathToRoot(prob.getOldCategory(i));
		}
		///
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
		allnodes.addFirstSecond(hier.root, ++lastid);
		int N = nodes.length + 1;
		Warray = new ArrayList [N];
		for (int i=0; i<N; i++)
			Warray[i] = new ArrayList<FeatureNode>();
		for (int T=0; T<iter; T++){ //training in epochs...
			for (int i=0; i<prob.l; i++) { //main iteration; visits every training example only once
				//just for showing where the training is 
				if (i%100 == 0)
					System.out.println("trained on ["+i+"] examples");
				///////
				//computing the predicted class:
				double [] dec_values = new double [prob.catnum];
				for (int j=0; j<dec_values.length; j++) {
					dec_values[j] = 0;
					for (int k=0; k<path2root[j].length; k++) {
						dec_values[j] += dotProduct(Warray[allnodes.getSecond(path2root[j][k])], prob.x[i]);
					}
				}
				IndexValue pred_y = argmax(dec_values); //prediction
				if (pred_y.index != prob.y[i]) { //prediction error occured:
					//path2root[prob.y[i]] <- good path
					double decv = 0;
					for (int m=0; m<path2root[prob.y[i]].length; m++) {
						decv += dotProduct(Warray[allnodes.getSecond(path2root[prob.y[i]][m])], prob.x[i]);
					}
					//path = path2root[pred_y.index] <- predicted path
					int [] path1 = diff(path2root[prob.y[i]], path2root[pred_y.index]);
					int [] path2 = diff(path2root[pred_y.index], path2root[prob.y[i]]);
					//UPDATE:
					for (int j=0; j<path1.length; j++) {
						Warray[allnodes.getSecond(path1[j])] = add(Warray[allnodes.getSecond(path1[j])], prob.x[i]);
					}
					for (int j=0; j<path2.length; j++) {
						Warray[allnodes.getSecond(path2[j])] = sub(Warray[allnodes.getSecond(path2[j])], prob.x[i]);
					}
				}//if error
			}//endfor i
			/*printF(Warray[0]);
			printF(Warray[1]);			
			System.out.println("==============");*/
		}//epochs
		
	}
	public void train_and_test(ProblemExt prob, ProblemExt ptest, ParameterExt param) {
		traincatnum = prob.catnum;
		hier = prob.hierarchy;
		//reducing the hierarchy:
		hier.reduceHierarchy(hier.root);
		//storing the paths:
		path2root = new int[prob.catnum][];
		for (int i=0; i<prob.catnum; i++) {
			path2root[i] = hier.getPathToRoot(prob.getOldCategory(i));
		}
		///
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
		allnodes.addFirstSecond(hier.root, ++lastid);
		int N = nodes.length + 1;
		Warray = new ArrayList [N];
		for (int i=0; i<N; i++)
			Warray[i] = new ArrayList<FeatureNode>();
		for (int T=0; T<iter; T++){ //training in epochs...
			System.out.println("Epoch:"+T);
			if (T%evalstep == 0 && T>0) { //perform evaluation
				int [] pred = test(ptest);
				Main.saveResults(ProblemUtils.transformNewLabelsToOld(pred,prob),"res"+T+".txt");
				//EvalMeasures e = new EvalMeasures(ptest, hier, pred, prob.catnum);
				//e.printMeasures();
			}
			for (int i=0; i<prob.l; i++) { //main iteration; visits every training example only once
				//just for showing where the training is 
				if (i%100 == 0)
					System.out.println("trained on ["+i+"] examples");
				///////
				//computing the predicted class:
				double [] dec_values = new double [prob.catnum];
				for (int j=0; j<dec_values.length; j++) {
					dec_values[j] = 0;
					for (int k=0; k<path2root[j].length; k++) {
						dec_values[j] += dotProduct(Warray[allnodes.getSecond(path2root[j][k])], prob.x[i]);
					}
				}
				IndexValue pred_y = argmax(dec_values); //prediction
				if (pred_y.index != prob.y[i]) { //prediction error occured:
					//path2root[prob.y[i]] <- good path
					double decv = 0;
					for (int m=0; m<path2root[prob.y[i]].length; m++) {
						decv += dotProduct(Warray[allnodes.getSecond(path2root[prob.y[i]][m])], prob.x[i]);
					}
					//path = path2root[pred_y.index] <- predicted path
					int [] path1 = diff(path2root[prob.y[i]], path2root[pred_y.index]);
					int [] path2 = diff(path2root[pred_y.index], path2root[prob.y[i]]);
					//UPDATE:
					for (int j=0; j<path1.length; j++) {
						Warray[allnodes.getSecond(path1[j])] = add(Warray[allnodes.getSecond(path1[j])], prob.x[i]);
					}
					for (int j=0; j<path2.length; j++) {
						Warray[allnodes.getSecond(path2[j])] = sub(Warray[allnodes.getSecond(path2[j])], prob.x[i]);
					}
				}//if error
			}//endfor i
			/*printF(Warray[0]);
			printF(Warray[1]);			
			System.out.println("==============");*/
		}//epochs
		
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
	public ArrayList<FeatureNode> sub(ArrayList<FeatureNode> x, FeatureNode [] b) {
		ArrayList<FeatureNode> z = new ArrayList<FeatureNode>(x);
		for (int i=0; i<b.length; i++) {
			int j;
			for (j=0; j<z.size() && z.get(j).index<b[i].index; j++);
			if (j<z.size() && z.get(j).index == b[i].index) {
				z.set(j, new FeatureNode(b[i].index, z.get(j).value - b[i].value));
			} else
				if (j<z.size() && z.get(j).index != b[i].index) {
					z.add(j, new FeatureNode(b[i].index, -b[i].value));
				}
				else
					z.add(new FeatureNode(b[i].index, -b[i].value));
		}
		return z;
	}
	public ArrayList<FeatureNode> add(ArrayList<FeatureNode> a, ArrayList<FeatureNode> b) {
		ArrayList<FeatureNode> z = new ArrayList<FeatureNode>(a);
		for (int i=0; i<b.size(); i++) {
			int j;
			for (j=0; j<z.size() && z.get(j).index<b.get(i).index; j++);
			if (j<z.size() && z.get(j).index == b.get(i).index) {
				z.set(j, new FeatureNode(b.get(i).index, z.get(j).value + b.get(i).value));
			} else
				if (j<z.size() && z.get(j).index != b.get(i).index) {
					z.add(j, new FeatureNode(b.get(i).index, b.get(i).value));
				}
				else
					z.add(new FeatureNode(b.get(i).index, b.get(i).value));
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
				dec_values[j] = 0;
				for (int k=0; k<path2root[j].length; k++) {
					dec_values[j] += dotProduct(Warray[allnodes.getSecond(path2root[j][k])], prob.x[i]);
				}
			}
			IndexValue pred_y = argmax(dec_values); //prediction
			labels[i] = pred_y.index;
		}
		return labels;
	}
	public Classifier newInstance() {
		return new HierMultiPerceptron();
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
