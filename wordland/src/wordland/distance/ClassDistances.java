package wordland.distance;

import org.jblas.*;

import wordland.*;
import wordland.competitions.lsthc09.Main;
import wordland.competitions.lsthc09.Params;
import wordland.data.Hierarchy;
import wordland.data.ProblemExt;

public class ClassDistances {
	private Hierarchy hier;
	public int [][] codes; //codes for ECOC
	public double sigma = Params.sigmaClassDist;
	
	public ClassDistances() {
		hier = Main.loadHierarchy1();
	}
	public void CalcGaussDistances(ProblemExt train, int nr) {
		DoubleMatrix K = new DoubleMatrix(train.catnum, train.catnum); //kernel matrix (Gaussian)
		int [] leaves = hier.getOnlyLeafChildren(hier.root); //leaves of the hierarchy (the categories) 
		double d;
		for (int i=0; i<leaves.length; i++) {
			for (int j=0; j<=i; j++) {
				d = hier.getDistance(leaves[i], leaves[j]);
				int m = train.getNewCategory(leaves[i]);
				int n = train.getNewCategory(leaves[j]);
				d = Math.exp(-(d*d)/(2*sigma*sigma));
				K.put(m, n, d);
				K.put(n, m, d);
			}
		}
		System.out.println("[Distances/kernel calculated.]");
		DoubleMatrix EV [] = new DoubleMatrix [2];
		EV = Eigen.symmetricEigenvectors(K);
		System.out.println("[Eigenvalues/-vectors calculated.]");
		//Sorting the eigenvalues/eigenvectors:
		int [] p = new int [EV[1].rows];
		for (int i=0; i<p.length; i++) p[i] = i;
		for (int i=0; i<EV[1].rows-1; i++)
			for (int j=i+1; j<EV[1].rows; j++)
				if (EV[1].get(i,i) < EV[1].get(j,j)) {
					double a = EV[1].get(i,i);
					EV[1].put(i,i,EV[1].get(j,j));
					EV[1].put(j,j,a);
					int b = p[i];
					p[i] = p[j];
					p[j] = b;
				}
		for (int i=0; i<EV[0].columns; i++) {
			if (i != p[i])
				for (int j=0; j<EV[0].rows; j++) {
					double a = EV[0].get(j,i);
					EV[0].put(j,i,EV[0].get(j,p[i]));
					EV[0].put(j,p[i],a);
				}
		}
		System.out.println("[Eigenvalues sorted.]");		
		//Building the ECOC coding matrix:
		codes = new int [nr][train.catnum];
		//Multiplying:
		K = EV[0].mmul(EV[1]);
		//Thresholding: <-- this should change
		for (int i=0; i<K.rows; i++) {
			for (int j=0; j<nr; j++) {
				if (K.get(i,j) <= 0)
					K.put(i,j,0);
				else
					K.put(i,j,1);
				codes[j][i] = (int)K.get(i,j);
			}
		}
		/*
		for (int i=0; i<EV[1].rows; i++)
			System.out.println(EV[1].get(i,i));
		System.out.println();
		*/
	}
}
