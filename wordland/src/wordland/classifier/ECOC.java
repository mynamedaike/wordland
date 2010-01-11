package wordland.classifier;

import java.math.BigInteger;

import org.jblas.*;

import wordland.*;
import wordland.competitions.lsthc09.Params;
import wordland.data.ParameterExt;
import wordland.data.ProblemExt;
import wordland.distance.*;
import wordland.utils.ProblemUtils;
import liblinear.*;

/*
 * class to deal with multiple classes using ECOC
 */
public class ECOC implements Classifier{
	private Linear svm=new Linear();
	private int [][] classcodes;
	private Model [] models;
	public double sparsity = Params.probEcoc;
	public ECOC () {
	}
	/*
	 * train an ecoc classifier for data, based on svms parametrized by param,
	 * using nr number of them 
	 */
	public void train(ProblemExt train,ParameterExt param) {
		int nr=param.ecoc_nr; 
		createClassCodes(train,nr);
		//createClassCodes_withGrayCodes(train, nr);
		//createClassCodes_withHier(train, nr);
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
		for (int i=1; i<train.catnum; i++)
			System.out.print(hammingDistCol(i-1, i) + ", ");
		System.out.println();
		for (int i=1; i<nr; i++)
			System.out.print(hammingDistRow(i-1, i) + ", ");
		System.out.println();		
	}
	/*
	 * Creating "evenly" distributed codes for the classes
	 */
	private void createClassCodes_withGrayCodes(ProblemExt train, int nr) {
		classcodes=new int [nr][train.catnum];
		//int delta = Params.ecocCodeLength;
		BigInteger delta = BigInteger.valueOf(2);
		delta = delta.pow(Params.ecocCodeLength);
		delta = delta.divide(BigInteger.valueOf(train.catnum));
		System.out.println("delta = " + delta);
		BigInteger value = BigInteger.ONE;
		for (int i=0; i<train.catnum; i++) {
			int [] code = binToGray(toBinaryBig(value, nr));
			for (int j=0; j<nr; j++)
				classcodes[j][i] = code[j];
			value = value.add(delta);
		}
		for (int i=1; i<train.catnum; i++)
			System.out.print(hammingDistCol(i-1, i) + ", ");
		System.out.println();
		for (int i=1; i<nr; i++)
			System.out.print(hammingDistRow(i-1, i) + ", ");
		System.out.println();
	}
	/**
	 * Creating error correcting codes using the hierarchy information
	 */
	private void createClassCodes_withHier(ProblemExt train, int nr) {
		ClassDistances D = new ClassDistances();
		D.CalcGaussDistances(train, nr);
		classcodes = D.codes;
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
	/*
	 * Hamming distance for binary valued int arrays
	 */
	public int hammingDist(int[] a, int[] b){
		int d = 0;
		for (int i=0; i<a.length; i++)
			d = d + Math.abs(a[i]-b[i]);
		return d;
	}
	private int hammingDistRow(int i, int j) {
		int d = 0;
		for (int k=0; k<classcodes[0].length; k++)
			d = d + Math.abs(classcodes[i][k] - classcodes[j][k]);
		return d;
	}
	private int hammingDistCol(int i, int j) {
		int d = 0;
		for (int k=0; k<classcodes.length; k++)
			d = d + Math.abs(classcodes[k][i] - classcodes[k][j]);
		return d;
	}
	private int[] getCol(int i){
		int [] col = new int[classcodes.length];
		for (int j=0; j<classcodes.length; j++)
			col[j] = classcodes[j][i];
		return col;
	}
	private int[] getRow(int i){
		int [] row = new int[classcodes[i].length];
		row = classcodes[i].clone();
		return row;
	}
	/*
	 * Gray to binary codes and vice versa 
	 */
	private int [] grayToBinary(int [] g) {
		int [] b = new int[g.length];
		b[0] = g[0];
		for (int i=1; i<g.length; i++)
			b[i] = b[i-1]^g[i];
		return b;
	}
	private int [] binToGray(int [] b) {
		int [] g = new int[b.length]; 
		g[0] = b[0];
		for (int i=1; i<b.length; i++)
			g[i] = b[i-1]^b[i];
		return g;
	}
	private int [] toBinary(int n, int length) {
		String s = Integer.toBinaryString(n);
		//System.out.println(s);
		byte [] b = new byte [s.length()]; 
		b = s.getBytes();
		int [] r = new int[length];
		for (int i=0; i<length; i++)
			if (i<b.length)
				r[length-1-i] = b[b.length-1-i]-48;
			else
				r[length-1-i] = 0;
		return r;
	}
	private int [] toBinaryBig(BigInteger n, int length) {
		if (n.bitLength() > length)
			return null;
		int [] b = new int [length];
		for (int i=0; i<length; i++)
			if (i<n.bitLength())
				b[b.length-1-i] = n.testBit(i)?1:0;
			else
				b[b.length-1-i] = 0;
		return b;
	}
	public Classifier newInstance() {
		return new ECOC();
	}
}
