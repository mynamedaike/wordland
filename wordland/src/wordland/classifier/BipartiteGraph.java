package wordland.classifier;

import java.io.PrintWriter;
import java.util.*;

import wordland.data.ParameterExt;
import wordland.data.ProblemExt;

import liblinear.*;

/**
 * The bipartite graph-based simple learning algorithm
 * based on
 * Omid Madani, Michael Connor: Large-Scale Many-Class Learning, 
 * SIAM, Data Mining 2008. 
 */
public class BipartiteGraph implements Classifier {
	HashMap<Integer, Double> [] feature_class;

	public void train(ProblemExt prob, ParameterExt param) {
		System.out.println("training...");
		feature_class = new HashMap [prob.n];
		for (int i=0; i<prob.l; i++) {
			if (i%100 == 0) {
				System.out.println("["+i+"]");
			}
			for (int j=0; j<prob.x[i].length; j++) {
				if (feature_class[prob.x[i][j].index] == null) {
					feature_class[prob.x[i][j].index] = new HashMap<Integer, Double>();
				}
				if (feature_class[prob.x[i][j].index].containsKey(prob.y[i])) {
					double v = feature_class[prob.x[i][j].index].get(prob.y[i]);
					feature_class[prob.x[i][j].index].put(prob.y[i], v+prob.x[i][j].value);
				} else {
					feature_class[prob.x[i][j].index].put(prob.y[i], prob.x[i][j].value);
				}
			}
		}
		for (int i=0; i<prob.n; i++) {
			if (feature_class[i] != null) {
				Collection<Integer> keys = feature_class[i].keySet();
				Iterator<Integer> it = keys.iterator();
				double sum = 0;
				while (it.hasNext()) {
					sum += feature_class[i].get(it.next());
				}
				it = keys.iterator();
				while (it.hasNext()) {
					int c = it.next();
					feature_class[i].put(c, feature_class[i].get(c)/sum);
					//System.out.println(i+" - "+c+" - "+feature_class[i].get(c));
				}
			}
		}
	}
	public int [] test(ProblemExt prob) {
		System.out.println("testing...");
		int [] labels = new int [prob.l];
		for (int i=0; i<prob.l; i++) {
			if (i%100 == 0) 
				System.out.println("["+i+"]");
			labels[i] = testone1(prob.x[i]);
		}
		return labels;
	}
	public int testone1(FeatureNode [] x) {
		double t = 0.2; //threshold: 0.1, 0.15, 0.2 (best)
		HashMap<Integer, Double> h = new HashMap<Integer, Double>();
		double max = -1;
		int maxind = -1;
		for (int i=0; i<x.length; i++) {
			if (feature_class[x[i].index] == null)
				continue;
			Iterator<Integer> it = feature_class[x[i].index].keySet().iterator();
			while (it.hasNext()) {
				int c = it.next();
				if (h.containsKey(c)) {
					if (feature_class[x[i].index].get(c) > t)
						h.put(c, h.get(c) + x[i].value * feature_class[x[i].index].get(c));
				} else {
					if (feature_class[x[i].index].get(c) > t)
						h.put(c, x[i].value * feature_class[x[i].index].get(c));
					else
						h.put(c, 0.0);
				}
				if (h.get(c) > max) {
					max = h.get(c);
					maxind = c;
				}
			}
			it = null;
		}
		return maxind;
	}
	public Classifier newInstance() {
		return new BipartiteGraph();
	}
}
