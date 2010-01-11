package wordland.classifier;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.math.BigInteger;

import org.jblas.DoubleMatrix;
import org.jblas.FloatMatrix;

import wordland.*;
import wordland.competitions.lsthc09.Main;
import wordland.competitions.lsthc09.Params;
import wordland.data.Hierarchy;
import wordland.data.ParameterExt;
import wordland.data.ProblemExt;
import wordland.utils.ProblemUtils;
import liblinear.*;

import java.util.*;
import java.io.*;


/*
 * class to deal with multiple classes using ECOC
 */
public class ECOC4 implements Classifier{
	private Linear svm=new Linear();
	private int [][] classcodes;
	private Model [] models;
	public double sparsity = Params.probEcoc;
	private Hierarchy hier;
	public ECOC4 () {
		hier = Main.loadHierarchy1();
		hier.reduceHierarchy(hier.root);
	}
	/*
	 * train an ecoc classifier for data, based on svms parametrized by param,
	 * using nr number of them 
	 */
	public void train(ProblemExt train, ParameterExt param) {
		int nr = param.ecoc_nr;
		//createClassCodes(train, nr);
		createClassCodes1(train, nr);
		//createClassCodes2(train, nr);
		models = new Model [nr];
		for (int i=0; i<nr; i++) {
			System.out.println("ecoc training nr "+i);
			ProblemExt newproblem = ProblemUtils.regroupData3(train, classcodes[i]);
			models[i] = svm.train(newproblem, param.param);
		}
	}
	/*
	 * test the ecoc classifier on a whole data set
	 */
	public int [] test(ProblemExt test) {
		int [] ret = new int [test.l];
		for (int i=0; i<test.l; i++) {
			if (test.x[i] != null) {
				double [] decval = decisionValues(test.x[i]);
				ret[i] = getClass(decval, 1);
			}
			else {
				ret[i]=0;
			}
		}
		return ret;
	}
	/*
	 * uses each of the trained svms to predict their output so that it can be
	 * compared to the codes classes
	 */
	private int [] predictClasses(FeatureNode [] x) {
		int [] ret=new int [models.length];
		for (int i=0;i<models.length;i++) {
			ret[i]=svm.predict(models[i], x);
		}
		return ret;
	}
	/*
	 * Returns the decision values
	 */
	private double [] decisionValues(FeatureNode [] x) {
		double [] dec = new double [models[0].getNrClass()];
		int [] labs = new int [models[0].getNrClass()];
		double [] decv = new double [models.length];
		for (int i=0; i<models.length; i++) {
			labs = models[i].getLabels();
			Linear.predictValues(models[i], x, dec);
			decv[i] = labs[0]*dec[0];
		}
		return decv;
	}
	/*
	 * tells from a code produced by prediction which code of an original class is
	 * most similar to it; 0 - L1, 1 - L2 distance
	 */
	private int getClass(double [] predicted, int metric) {
		int minindex = 0;
		double mindist = 0;
		for (int row=0; row<classcodes.length; row++) {
			if (classcodes[row][0] == 0) continue;
			switch (metric) {
				case 0: //L1
					mindist += Math.abs(classcodes[row][0] - predicted[row]);
					break;
				case 1: //L2
					mindist += Math.pow(classcodes[row][0] - predicted[row], 2);
					break;
			}
		}
		for (int col=1; col<classcodes[0].length; col++) {
			double dist = 0;
			for (int row=0; row<classcodes.length; row++) {
				if (classcodes[row][col] == 0) continue;
				switch (metric) {
					case 0: //L1
						dist += Math.abs(classcodes[row][col] - predicted[row]);
						break;
					case 1: //L2
						dist += Math.pow(classcodes[row][col] - predicted[row], 2);
						break;
				}
			}
			if (dist < mindist) {
				mindist = dist;
				minindex = col;
			}
		}
		return minindex;
	}
	/**
	 * Creating EC codes based on class hierarchy (3nd version);
	 * Idea: Choose 2 inner nodes at random. Verify if none of
	 * them is a subclass of the other. Take the classes under the
	 * first node as positives and classes under the other node
	 * as negatives.    
	 */
	private void createClassCodes2(ProblemExt train, int nr) {
		classcodes = new int [nr][];
		int [] innernodes = hier.getAllChildren(hier.root);
		ArrayList<Integer> index = new ArrayList<Integer>();
		for (int i=0; i<innernodes.length; i++)
			if (!hier.isLeaf(innernodes[i]))
				index.add(i);
		for (int cnum=0; cnum<nr; ) {
			//first index:
			int r = (int)Math.floor(Math.random()*index.size());
			int c1 = innernodes[index.get(r)];
			////index.remove(r);
			//second index:
			int s = (int)Math.floor(Math.random()*index.size());
			int c2 = innernodes[index.get(s)];
			while ((hier.getLCA(c1, c2) == c1) || (hier.getLCA(c1, c2) == c2)) {
				s = (int)Math.floor(Math.random()*index.size());
				c2 = innernodes[index.get(s)];
			}
			////index.remove(s);
			//building the codeword:
			int [] classes1 = hier.getOnlyLeafChildren(c1);
			int [] classes2 = hier.getOnlyLeafChildren(c2);
			classcodes[cnum] = new int [train.catnum];
			for (int i=0; i<classes1.length; i++) {
				classcodes[cnum][train.getNewCategory(classes1[i])] = +1;
			}
			for (int i=0; i<classes2.length; i++) {
				classcodes[cnum][train.getNewCategory(classes2[i])] = -1;
			}
			cnum++;
		}
		//output the coding matrix for verification:
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("out.txt"));
			for (int i=0; i<nr; i++) {
				for (int j=0; j<classcodes[i].length; j++) {
					if (classcodes[i][j] == 1)
						out.write("1 ");
					else
						if (classcodes[i][j] == -1)
							out.write("2 ");
						else
							out.write("0 ");
				}
				out.write("\n");
			}
			out.close();
		} catch (IOException e) {
		}
	}	
	/**
	 * Creating EC codes based on class hierarchy (2nd version);
	 * Idea: Choose randomly an inner node from the hierarchy, and
	 * take the classes under the node as positives, and the rest
	 * of the classes as negative classes.   
	 */
	private void createClassCodes1(ProblemExt train, int nr) {
		classcodes = new int [nr][];
		int [] innernodes1 = hier.getAllChildren(hier.root);
		int [] innernodes = new int[innernodes1.length+1];
		for (int i=0; i<innernodes1.length; i++)
			innernodes[i] = innernodes1[i];
		innernodes[innernodes.length-1] = hier.root;
		ArrayList<Integer> index = new ArrayList<Integer>();
		for (int i=0; i<innernodes.length; i++)
			index.add(i);
		for (int cnum=0; cnum<nr; ) {
			int r = (int)Math.floor(Math.random()*index.size());
			if (hier.isLeaf(innernodes[index.get(r)])) { //we don't want leaves
				index.remove(r);
				continue;
			}
			classcodes[cnum] = new int [train.catnum];
			int [] pos_classes = hier.getOnlyLeafChildren(innernodes[index.get(r)]);
			for (int i=0; i<train.catnum; i++)
				classcodes[cnum][i] = -1;
			for (int i=0; i<pos_classes.length; i++)
				classcodes[cnum][train.getNewCategory(pos_classes[i])] = 1;
			index.remove(r);
			cnum++;
		}
		//output the coding matrix for verification:
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("out.txt"));
			for (int i=0; i<nr; i++) {
				for (int j=0; j<classcodes[i].length; j++) {
					if (classcodes[i][j] == 1)
						out.write("1 ");
					else
						if (classcodes[i][j] == -1)
							out.write("2 ");
						else
							out.write("0 ");
				}
				out.write("\n");
			}
			out.close();
		} catch (IOException e) {
		}	
	}
	/**
	 * Creating EC codes based on class hierarchy (1st version);
	 * Idea: Choose an inner node randomly, and determine the no.
	 * of its children (n). Then using the 1-vs-rest scheme, generate
	 * n classifiers, every step taking one class as positive and the 
	 * others as negatives. The rest of the classes are ignored (0).
	 */
	private void createClassCodes(ProblemExt train, int nr) {
		boolean end = false;
		classcodes = new int [nr][];
		int [] innernodes1 = hier.getAllChildren(hier.root);
		int [] innernodes = new int[innernodes1.length+1];
		for (int i=0; i<innernodes1.length; i++)
			innernodes[i] = innernodes1[i];
		innernodes[innernodes.length-1] = hier.root;
		ArrayList<Integer> index = new ArrayList<Integer>();
		for (int i=0; i<innernodes.length; i++)
			index.add(i);
		for (int cnum=0; cnum<nr; ) {
			int r = (int)Math.floor(Math.random()*index.size());
			if (hier.isLeaf(innernodes[index.get(r)])) {
				index.remove(r);
				continue;
			}
			int [] classes = hier.getChildren(innernodes[index.get(r)]);
			int [][] subclasses = new int [classes.length][];
			for (int i=0; i<classes.length; i++) {
				if (hier.isLeaf(classes[i])) {
					subclasses[i] = new int [1];
					subclasses[i][0] = classes[i]; 
				} else
					subclasses[i] = hier.getOnlyLeafChildren(classes[i]);
			}
			for (int i=0; i<classes.length; i++) {
				for (int j=0; j<subclasses[i].length; j++) {
					if (cnum >= nr) {
						end = true;
						break;
					}
					classcodes[cnum] = new int [train.catnum];
					classcodes[cnum][train.getNewCategory(subclasses[i][j])] = +1;
				}
				if (end) break;
				for (int k=0; k<classes.length; k++) {
					if (k != i) {
						for (int j=0; j<subclasses[k].length; j++) {
							if (cnum >= nr) {
								end = true;
								break;
							}
							classcodes[cnum][train.getNewCategory(subclasses[k][j])] = -1;
						}
					}
					if (end) break;
				}
				if (end) break;
				cnum++;
			}
			index.remove(r);
			/*if (checkClassCodes(cnum)) {
				cnum++;
			}*/
		}
		//output the coding matrix for verification:
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("out.txt"));
			for (int i=0; i<nr; i++) {
				for (int j=0; j<classcodes[i].length; j++) {
					if (classcodes[i][j] == 1)
						out.write("1 ");
					else
						if (classcodes[i][j] == -1)
							out.write("2 ");
						else
							out.write("0 ");
				}
				out.write("\n");
			}
			out.close();
		} catch (IOException e) {
		}
	}	
	/*
	 * checks if the last code has already been made previously
	 */
	private boolean checkClassCodes(int last) {
		if (last==0) {
			return true;
		}
		for (int row=0;row<last;row++) {
			boolean diff=false;
			for (int col=0;col<classcodes[last].length && !diff;col++) {
				if (classcodes[row][col]!=classcodes[last][col]) {
					diff=true;
				}
			}
			if (!diff) {
				return false;
			}
		}
		return true;
	}
	public Classifier newInstance() {
		return new ECOC4();
	}
}
