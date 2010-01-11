package wordland.optim;

import wordland.competitions.lsthc09.Main;
import wordland.competitions.lsthc09.Params;
import wordland.data.Hierarchy;
import wordland.data.ProblemExt;


public class SA_ECOC extends SimAnn<Rep_ECOC>{
	private ProblemExt train;
	private int [][] dist;
	private Hierarchy hier;
	//protected int iteration = 10000;
	protected int iteration = 10;
	protected int maxL = 10;
	protected double minT = 1E-3;  
	protected double alpha;
	
	protected int maxH = Params.ecocCodeLength;
	protected int minD = 10000;
	
	public SA_ECOC (ProblemExt tr, double t, int l, Rep_ECOC code) {
		super(t, l, code);
		// Calculating class distances based on hierarchy:
		train = tr;
		dist = new int [tr.catnum][tr.catnum];
		hier = Main.loadHierarchy1();
		int [] leaves = hier.getOnlyLeafChildren(hier.root); // leaves of the hierarchy (the categories) 
		double d;
		for (int i=0; i<leaves.length; i++) {
			for (int j=0; j<=i; j++) {
				d = hier.getDistance(leaves[i], leaves[j]);
				int m = train.getNewCategory(leaves[i]);
				int n = train.getNewCategory(leaves[j]);
				//d = Math.exp(-(d*d)/(2*sigma*sigma));
				dist[m][n] = (int)d;
				dist[n][m] = (int)d;
				if (minD > d)
					minD = (int)d;
			}
		}
		//initial value of alpha:
		//alpha = (double)maxH/(2*minD);
		sol.value = (double)maxH/(2*minD);
	}
	/**
	 * Need for a good fitness function...
	 */
	protected double f (Rep_ECOC code) {
		double fitness = 0;
		double d = 0;
		for (int i=0; i<code.matrix[0].length-1; i++)
			for (int j=i+1; j<code.matrix[0].length; j++) {
				d = hammingDistCol(i, j, code.matrix);
				fitness += d - Math.abs((d/dist[i][j])-code.value);
			}
		fitness /= Math.pow(code.matrix[0].length,2);
		//fitness += maxH;
		System.out.println("f=" + fitness);
		return fitness;
	}
	/**
	 *  Randomly changes one value in a row of the code matrix;
	 *  changes alpha too
	 */
	protected Rep_ECOC neighbor () {
		Rep_ECOC nb = sol.clone();
		int r;
		for (int i=0; i<sol.matrix.length; i++) {
			if (Math.random() < 0.5) { // probability of a change in a row
				r = (int)Math.floor(Math.random() * sol.matrix[0].length);
				nb.matrix[i][r] = 1-nb.matrix[i][r];
			}
		}
		if (Math.random() < 0.5)
			nb.value = nb.value + delta(it, (double)maxH/minD - nb.value);
		else
			nb.value = nb.value - delta(it, nb.value);
		System.out.println("nb.value=" + nb.value);		
		return nb;
	}
	private double delta (double t, double y) {
		double r = Math.random();
		int b = 5;
		return y*(1-Math.pow(r,Math.pow(1-(t/iteration),b)));
	}
	/**
	 * ...
	 */
	protected double nextT () {
		return ((1+minT-((double)it/iteration))*T0);
	}
	/**
	 * Increasing the length; if length reaches maxL, then remains maxL
	 * for the rest of the iterations
	 */
	protected int nextL () {
		return ((L0<maxL)?++L0:maxL);
	}
	/** 
	 * Terminates at a given iteration count, determined by "iteration" 
	 */
	protected boolean TermCond () {
		return ((it<iteration)?false:true);
	}
	/**
	 * Returns the Hamming distance between 2 columns of a matrix
	 */
	private int hammingDistCol(int i, int j, int [][] codes) {
		int d = 0;
		for (int k=0; k<codes.length; k++)
			d = d + Math.abs(codes[k][i] - codes[k][j]);
		return d;
	}
}
