package wordland.classifier;

import java.math.BigInteger;

import org.jblas.*;

import wordland.*;
import wordland.competitions.lsthc09.Params;
import wordland.data.ParameterExt;
import wordland.data.ProblemExt;
import wordland.distance.*;
import wordland.optim.Rep_ECOC;
import wordland.optim.SA_ECOC;
import wordland.utils.ProblemUtils;
import liblinear.*;

/*
 * class to deal with multiple classes using ECOC
 */
public class ECOC_SA implements Classifier{
	private Linear svm=new Linear();
	private int [][] classcodes;
	private Model [] models;
	public double sparsity = Params.probEcoc;
	public ECOC_SA () {
	}
	/*
	 * train an ecoc classifier for data, based on svms parametrized by param,
	 * using nr number of them 
	 */
	public void train(ProblemExt train,ParameterExt param) {
		int nr=param.ecoc_nr; 
		//createClassCodes(train,nr);
		createClassCodes_withHier(train, nr);
		models = new Model [nr];
		for (int i=0;i<nr;i++) {
			System.out.println("ecoc training nr "+i);
			ProblemExt newproblem=ProblemUtils.regroupData(train,classcodes[i]);
			models[i]=svm.train(newproblem, param.param);
		}		
	}
	/*
	 * test the ecoc classifier on a whole data set
	 */
	public int [] test(ProblemExt test) {
		int [] ret=new int [test.l];
		for (int i=0;i<test.l;i++) {
			if (test.x[i]!=null) {
				int [] classes=predictClasses(test.x[i]);
				ret[i]=getClass(classes);
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
	 * tells from a code produced by prediction which code of an original class is
	 * most similar to it by simple manhattan distance
	 */
	private int getClass(int [] predicted) {
		int minindex=0;
		int mindiff=models.length;
		for (int col=0;col<classcodes[0].length;col++) {
			int diff=0;
			for (int row=0;row<classcodes.length;row++) {
				if (classcodes[row][col]!=predicted[row]) {
					diff++;
				}
			}
			if (diff<mindiff) {
				mindiff=diff;
				minindex=col;
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
					classcodes[cnum][i]=0;
				}
			}
			if (checkClassCodes(cnum)) {
				cnum++;
			}
		}
	}
	/**
	 * Creating error correcting codes using the hierarchy information
	 */
	private void createClassCodes_withHier(ProblemExt train, int nr) {
		Rep_ECOC r = new Rep_ECOC();
		createClassCodes(train, nr);
		r.matrix = classcodes.clone();
		r.value = 0;
		SA_ECOC SA = new SA_ECOC(train, 100, 1, r);
		SA.optimize();
		classcodes = SA.sol.matrix.clone();
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
		return new ECOC();
	}
}

