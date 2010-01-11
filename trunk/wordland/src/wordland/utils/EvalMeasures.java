package wordland.utils;

import java.io.*;

import wordland.data.Hierarchy;
import wordland.data.ProblemExt;

public class EvalMeasures {
	double [] tp;
	double [] fp;
	double [] fn;
	ProblemExt p;
	int [] predicted;
	int catnum;
	Hierarchy h;
	int computed;
	
	public EvalMeasures(ProblemExt p, Hierarchy h, int [] predicted, int catnum) {
		if (predicted.length != p.l) {
			System.err.println("Length error!");
			return;
		}
		this.p = p;
		this.predicted = predicted;
		this.catnum = catnum;
		this.h = h; 
		computed = 0;
	}
	public void countStuff() {
		tp = new double [catnum];
		fp = new double [catnum];
		fn = new double [catnum];
		for (int i=0; i<p.l; i++) {
			if (p.y[i] == predicted[i]) 
				tp[p.y[i]]++;
			else {
				fp[predicted[i]]++;
				fn[p.y[i]]++;
			}
		}
		computed = 1;
	}
	public double Accuracy() {
		int ret = 0;
		for (int i=0; i<p.l; i++) {
			if (p.y[i] == predicted[i]) {
				ret++;
			}
		}
		return (double)ret/p.l;
	}
	public double MacroF(double beta) {
		if (computed == 0) countStuff();
		double mprec = MacroPrecision(); 
		double mrec = MacroRecall();
		return ((beta*beta + 1)*mprec*mrec/(beta*beta*mprec + mrec));
	}
	public double MacroF() {
		return MacroF(1);
	}
	public double MacroPrecision() {
		if (computed == 0) countStuff();
		double mprec = 0;
		for (int i=0; i<catnum; i++) {
			if ((tp[i] != 0) && (tp[i]+fp[i] != 0))
				mprec += tp[i]/(tp[i]+fp[i]);
		}
		return mprec/catnum;
	}
	public double MacroRecall() {
		if (computed == 0) countStuff();
		double mrec = 0;
		for (int i=0; i<catnum; i++) {
			if ((tp[i] != 0) && (tp[i]+fn[i] != 0))
				mrec += tp[i]/(tp[i]+fn[i]);
		}
		return mrec/catnum;
	}
	public double TreeError() {
		if (h == null)
			return -1;
		double error = 0;
		for (int i=0; i<p.l; i++) {
			if (p.y[i] != predicted[i]) {
				int m = p.getOldCategory(p.y[i]);
				int n = p.getOldCategory(predicted[i]);
				error += h.getDistance(m, n);
			}
		}
		return error/p.l;
	}
	public void printMeasures (){
		System.out.println(printMeasuresToString());
	}
	public String printMeasuresToString() {
		if (computed == 0) countStuff();
		double A = Accuracy();
		double P = MacroPrecision();
		double R = MacroRecall();
		double F = MacroF(1);
		double E = TreeError();
		StringWriter sw=new StringWriter();
		PrintWriter pr=new PrintWriter(sw);
		pr.println("===============");
		pr.println("Accuracy: " + A);
		pr.println("Macro F-measure: " + F);
		pr.println("Macro Precision: " + P);
		pr.println("Macro Recall: " + R);
		pr.println("Tree Induced Error: " + E);
		pr.println("===============");
		return sw.toString();
	}
}
