package wordland.classifier;

import wordland.*;
import wordland.data.ParameterExt;
import wordland.data.ProblemExt;

public class NaiveBayes implements Classifier{
	protected double [] Pc; // = P(c_{i})
	protected double [] norm_sum;
	protected ProblemExt trp;
	
	public NaiveBayes() {
	}
	public void train(ProblemExt prob, ParameterExt param) {
		System.out.println("Training...");
		trp = prob;
		class_prob(prob);
		bigsum(prob);
	}
	public int [] test(ProblemExt prob) {
		System.out.println("Testing...");
		int [] pred_class = new int [prob.l];
		for (int i=0; i<pred_class.length; i++) {
			pred_class[i] = class_doc_prob(prob, trp, i);
			if (i%100 == 0) System.out.println("["+i+"]");
		}
		System.out.println();
		return pred_class;
	}
	public Classifier newInstance() {
		return new NaiveBayes();
	}
	/**
	 * Calculates class probabilities
	 */
	private void class_prob(ProblemExt p) {
		Pc = new double[p.catnum];
		for (int i=0; i<p.l; i++)
			Pc[p.y[i]] += 1;
		for (int i=0; i<p.catnum; i++)
			Pc[i] = (1+Pc[i])/(p.catnum+p.l);
			//Pc[i] /= p.l;
	}
	/**
	 * Calculates the returned value:
	 * argmax_{i} log P(c_{i}) + sum_{j} log P(w_{j}|c_{i});
	 * i = document index
	 */
	private int class_doc_prob(ProblemExt testp, ProblemExt trainp, int i) {
		int pos = 0;
		double arg = 0;
		double c [] = new double[trainp.catnum];
		double c1 [] = new double[trainp.catnum];
		for (int k=0; k<testp.x[i].length; k++) {
			for (int m=0; m<trainp.catnum; m++) 
				c[m] = 0;
			for (int m=0; m<trainp.l; m++)
				for (int n=0; n<trainp.x[m].length; n++)
					if (trainp.x[m][n].index == testp.x[i][k].index)
						c[trainp.y[m]] += trainp.x[m][n].value;
			for (int m=0; m<trainp.catnum; m++) {
				c1[m] += Math.log((1+c[m])/(trainp.n+norm_sum[m]));
			}
		}
		pos = 0;
		arg = Math.log(Pc[pos])+c1[pos];
		for (int k=1; k<trainp.catnum; k++)
			if ((Math.log(Pc[k])+c1[k]) > arg) {
				arg = Math.log(Pc[k])+c1[k];
				pos = k;
			}
		return pos;
	}
	/**
	 * Calculates the normalizing sum when calculating P(w_{j}|c_{i}) [Nigam]
	 */
	private void bigsum(ProblemExt p) {
		norm_sum = new double [p.catnum];
		for (int i=0; i<p.n; i++)
			for (int j=0; j<p.l; j++)
				for (int k=0; k<p.x[j].length; k++)
					norm_sum[p.y[j]] += p.x[j][k].value;
	}
}
