package wordland.classifier;

import liblinear.*;
import java.util.*;

import wordland.*;
import wordland.competitions.lsthc09.Main;
import wordland.competitions.lsthc09.Params;
import wordland.data.Hierarchy;
import wordland.data.ParameterExt;
import wordland.data.ProblemExt;
import wordland.utils.ProblemUtils;

/*
 * class to deal with multiple classes using ECOC
 */
public class ECOC5 implements Classifier{
	private Linear svm=new Linear();
	private int [][] classcodes;
	private Model [] models;
	public double sparsity = Params.probEcoc;
	public Hierarchy hier; 
	public ECOC5 () {
		hier = Main.loadHierarchy1();
	}
	/*
	 * train an ecoc classifier for data, based on svms parametrized by param,
	 * using nr number of them 
	 */
	public void train(ProblemExt train, ParameterExt param) {
		int nr=param.ecoc_nr; 
		createClassCodes(train,nr);
		models = new Model [nr];
		for (int i=0;i<nr;i++) {
			System.out.println("ecoc training nr "+i);
			ProblemExt newproblem = ProblemUtils.regroupData(train,classcodes[i]);
			//double c = classDistance(train, hier, i, 2)/(2*hier.depth);
			double c = classDistance(train, hier, i, 2);
			//double c = Math.pow(2, classDistance(train, hier, i, 2)/(2*hier.depth));
			System.out.println("C="+c);
			param.param.setC(c);
			models[i] = svm.train(newproblem, param.param);
		}
	}
	/*
	 * test the ecoc classifier on a whole data set
	 */
	public int [] test(ProblemExt test) {
		int [] ret=new int [test.l];
		for (int i=0;i<test.l;i++) {
			if (i%100==0) {
				System.out.println("["+i+"]");
			}
			if (test.x[i]!=null) {
				double [] decval = decisionValues(test.x[i]);
				ret[i] = getClass(decval, 1);
			}
			else {
				ret[i]=0;
			}
		}
		return ret;
	}
	private double classDistance(ProblemExt p, Hierarchy h, int x, int k) {
		ArrayList<Double> dist = new ArrayList<Double>();
		double d = 0;
		switch (k) {
			case 0: d = 0; break;
			case 1: d = 10000; break;
			case 2: d = 0; break;
		}
		double aux = 0;
		int n = 0;
		switch (k) {
		case 0://max
			for (int i=0; i<classcodes[0].length-1; i++)
				for (int j=i+1; j<classcodes[0].length; j++)
					if (((classcodes[x][i] == 1) && (classcodes[x][j] == -1)) ||
						((classcodes[x][i] == -1) && (classcodes[x][j] == 1))) {
						aux = h.getDistance(p.getOldCategory(i), p.getOldCategory(j));
						d = Math.max(d, aux);
						dist.add(aux);
					}
			break;
		case 1://min
			for (int i=0; i<classcodes[0].length-1; i++)
				for (int j=i+1; j<classcodes[0].length; j++)
					if (((classcodes[x][i] == 1) && (classcodes[x][j] == -1)) ||
						((classcodes[x][i] == -1) && (classcodes[x][j] == 1))) {
						aux = h.getDistance(p.getOldCategory(i), p.getOldCategory(j));
						d = Math.min(d, aux);
						dist.add(aux);
					}
			break;
		case 2://avg
			for (int i=0; i<classcodes[0].length-1; i++)
				for (int j=i+1; j<classcodes[0].length; j++)
					if (((classcodes[x][i] == 1) && (classcodes[x][j] == -1)) ||
						((classcodes[x][i] == -1) && (classcodes[x][j] == 1))) {
						aux = h.getDistance(p.getOldCategory(i), p.getOldCategory(j));
						d += aux;
						n++;
						dist.add(aux);
					}
			d /= n;
			break;
		}
		if (k==2) {
			double d2 = 0;
			for (int i=0; i<dist.size(); i++) {
				d2 += (dist.get(i)-d)*(dist.get(i)-d);
			}
			return Math.sqrt(d2/n);
		}
		return d;
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
			if (Params.normSVM == 0) {
				Linear.predictValues(models[i], x, dec);
				decv[i] = labs[0]*dec[0];
			} else {
				decv[i] = labs[0]*decValueNorm(i, x);
			}
		}
		return decv;
	}
	/**
	 * Returns the modified decision value for a test example in
	 * case of normalized data, using the idea to make the marginal
	 * hyperplanes to define equal distances on the hypersphere 
	 * @param i
	 * @param x
	 * @return
	 */
	private double decValueNorm(int i, FeatureNode [] x) {
		double s = 0;
		double [] a = models[i].getFeatureWeights();
		double wnorm = 0;
		for (int j=0; j<a.length-1; j++) {
			wnorm += Math.pow(a[j], 2);
		}
		wnorm = Math.sqrt(wnorm);
		for (int k=0; k<x.length; k++) {
			s += x[k].value*a[x[k].index-1];
		}
		s -= models[i].getBias(); //this is going to be changed only
		double bias = Math.cos((Math.acos(models[i].getBias()-(1/wnorm)) +
				Math.acos(models[i].getBias()+(1/wnorm)))/2);
		s += bias;
		return s;
	}
	/*
	 * tells from a code produced by prediction which code of an original class is
	 * most similar to it; 0 - L1, 1 - L2 distance
	 */
	private int getClass(double [] predicted, int metric) {
		int minindex = 0;
		double mindist = 0;
		for (int row=0; row<classcodes.length; row++) {
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
	/*
	 * creates some codes for nr number of classifiers
	 */
	private void createClassCodes(ProblemExt train,int nr) {
		classcodes=new int [nr][];
		for (int cnum=0;cnum<nr;) {
			classcodes[cnum]=new int [train.catnum];
			for (int i=0;i<train.catnum;i++) {
				double rnd=Math.random();
				if (rnd<sparsity) {
					classcodes[cnum][i]=1;
				}
				else {
					classcodes[cnum][i]=-1;
				}
			}
			if (checkClassCodes(cnum)) {
				cnum++;
			}
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
		return new ECOC5();
	}
}
