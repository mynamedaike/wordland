package wordland.classifier;

import org.jblas.*;

import wordland.data.ParameterExt;
import wordland.data.ProblemExt;
import wordland.distance.*;

public class LabelPropagation implements Classifier {
	double eps = 1E-3;
	DistanceMetric dm;
	
	public LabelPropagation() {
	}
	public void train(ProblemExt train, ParameterExt param) {
	}
	public int [] ttrain(ProblemExt train, ProblemExt test, ParameterExt param) {
		DoubleMatrix Pul = new DoubleMatrix(test.l, train.l);
		DoubleMatrix Puu = new DoubleMatrix(test.l, test.l);
		DoubleMatrix fl = new DoubleMatrix(train.l, train.catnum);
		DoubleMatrix fu = new DoubleMatrix(test.l, train.catnum);
		//DistanceMetric dm = new CosSimilarity();
		//GaussSimilarity dm = new GaussSimilarity();
		//dm.setSigma(sigma);
		for (int i=0; i<test.l; i++) {
			for (int j=0; j<train.l; j++) {
				Pul.put(i, j, dm.distance(test.x[i], train.x[j]));
			}
			for (int j=0; j<test.l; j++) {
				Puu.put(i, j, dm.distance(test.x[i], test.x[j]));
			}
		}
		DoubleMatrix d1 = Pul.rowSums();
		DoubleMatrix d2 = Puu.rowSums();
		d1.addi(d2); //the diagonal elements
		for (int i=0; i<d1.length; i++)
			d1.put(i, 1/d1.get(i));
		DoubleMatrix Duu_1 = DoubleMatrix.diag(d1); //the diagonal matrix
		//normalization:
		Pul = Duu_1.mmul(Pul);
		Puu = Duu_1.mmul(Puu);
		//fl calculation:
		fl = DoubleMatrix.zeros(fl.rows, fl.columns);
		for (int i=0; i<train.l; i++) {
			fl.put(i, train.y[i], 1);
		}
		//fu initialization:
		fu = DoubleMatrix.zeros(fu.rows, fu.columns);
		DoubleMatrix f = new DoubleMatrix(fu.rows, fu.columns);
		f = DoubleMatrix.zeros(fu.rows, fu.columns);
		//iterations:
		int it = 0;
		do {
			f = fu;
			fu = (Pul.mmul(fl)).add(Puu.mmul(fu));
			System.out.println((++it) + ". iteration");
		} while ((f.sub(fu)).norm2() > eps);
		int [] labels = new int [test.l]; //labels to be returned
		for (int i=0; i<test.l; i++) {
			int ind = 0;
			double val = fu.get(i, ind);
			for (int j=1; j<train.catnum; j++) {
				if (val < fu.get(i, j)) {
					val = fu.get(i, j);
					ind = j;
				}
			}
			labels[i] = ind;
		}
		return labels;
	}
	public int [] test(ProblemExt prob) {
		return null;
	}
	public void setEpsilon(double e) {
		eps = e;
	}
	public void setDistanceMetric(DistanceMetric d) {
		dm = d;
	}
	public Classifier newInstance() {
		return new LabelPropagation();
	}
}
