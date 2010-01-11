package wordland.kernels;

import java.util.ArrayList;

import wordland.*;
import wordland.data.ProblemExt;
import wordland.distance.*;

import liblinear.*;

public class GVSMTransform {
	public static DistanceMetric d = new CosSimilarity();

	public GVSMTransform() {
	}
	/**
	 * Transforms the data by the linear GVSM transform,
	 * d = D'*d, where d is a document (vector) and 
	 * D is the term x document matrix 
	 */
	public static ProblemExt[] transform(ProblemExt[] data) {
		ProblemExt [] pr = new ProblemExt[2]; //pr[0] - train, p[1] - test
		pr[0] = new ProblemExt();
		pr[0].l = data[0].l;
		pr[0].n = data[0].n;
		pr[0].catnum = data[0].catnum;
		pr[0].bias = data[0].bias;
		pr[0].y = data[0].y;
		pr[0].hierarchy = data[0].hierarchy;
		pr[0].copyHashes(data[0]);
		pr[1] = new ProblemExt();
		pr[1].l = data[1].l;
		pr[1].n = data[1].n;
		pr[1].catnum = data[1].catnum;
		pr[1].bias = data[1].bias;
		pr[1].y = data[1].y;
		pr[1].hierarchy = data[1].hierarchy;
		pr[1].copyHashes(data[1]);
		//train:
		System.out.println("Transforming train...");
		pr[0].x = new FeatureNode [data[0].x.length][];
		for (int i=0; i<data[0].l; i++) {
			if (i%100 == 0) 
				System.out.println("["+i+"]");
			ArrayList<Integer> pos = new ArrayList<Integer>();
			ArrayList<Double> val = new ArrayList<Double>();
			for (int j=0; j<data[0].l; j++) {
				double v = DotProduct(data[0].x[i], data[0].x[j]);
				if (v != 0) {
					pos.add(j);
					val.add(v);
				}
			}
			pr[0].x[i] = new FeatureNode[pos.size()];
			for (int j=0; j<pos.size(); j++)
				pr[0].x[i][j] = new FeatureNode(pos.get(j)+1, val.get(j));
		}
		//test:
		System.out.println("Transforming test...");
		pr[1].x = new FeatureNode [data[1].x.length][];
		for (int i=0; i<data[1].l; i++) {
			if (i%100 == 0) 
				System.out.println("["+i+"]");
			ArrayList<Integer> pos = new ArrayList<Integer>();
			ArrayList<Double> val = new ArrayList<Double>();
			for (int j=0; j<data[0].l; j++) {
				double v = DotProduct(data[1].x[i], data[0].x[j]);				
				if (v != 0) {
					pos.add(j);
					val.add(v);
				}
			}
			pr[1].x[i] = new FeatureNode[pos.size()];
			for (int j=0; j<pos.size(); j++)
				pr[1].x[i][j] = new FeatureNode(pos.get(j)+1, val.get(j));
		}		
		return pr;
	}
	private static double DotProduct(FeatureNode [] x, FeatureNode [] z) {
		return d.distance(x, z);
	}
	
}
