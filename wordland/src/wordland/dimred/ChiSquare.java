package wordland.dimred;

import liblinear.*;

import java.util.*;

import wordland.*;
import wordland.competitions.lsthc09.Params;
import wordland.data.BidirectionalMap;
import wordland.data.FeatureNodeComp2;
import wordland.data.ProblemExt;


public class ChiSquare extends RankingMethod {
	private double [][] stats; //term x class
	private double [] chi2;
	private int catsize []; //category probabilities
	private int termfreq []; //term frequencies, i.e. in how many docs they appear
	private int chi2type = Params.Chi2Type; 

	public ChiSquare() {
	}
	
	public void collect(ProblemExt p) {
		//category sizes:
		catsize = new int [p.catnum];
		for (int i=0; i<p.l; i++)
			catsize[p.y[i]]++;
		//term frequencies:
		termfreq = new int [p.n];
		for (int i=0; i<p.l; i++)
			for (int j=0; j<p.x[i].length; j++)
				termfreq[p.x[i][j].index]++;		
		//chi2 quantities:
		chi2 = new double [p.n];
		stats = new double [p.n][p.catnum]; //term x class
		for (int i=0; i<p.l; i++) {
			for (int j=0; j<p.x[i].length; j++) {
				stats[p.x[i][j].index][p.y[i]] += 1; //update A
			}
		}
		for (int i=0; i<stats.length; i++) {
			for (int j=0; j<stats[0].length; j++) {
				double a = stats[i][j];
				double b = termfreq[i]-a; 
				double c = catsize[j]-a;
				double d = p.l-a-b-c;
				if (((double)termfreq[i]/p.l <= 0.005) || ((double)termfreq[i]/p.l >= 0.5))
					stats[i][j] = 0;
				else
					//chi2:
					stats[i][j] = (p.l*Math.pow(a*d-c*b,2))/((a+c)*(b+d)*(a+b)*(c+d));
					//odds ratio:
					/*
					if ((c == 0) || (b == 0))
						stats[i][j] = 0;
					else
						stats[i][j] = Math.log(a*d/(c*b));
					*/
					//dia:
					//stats[i][j] = a/(a+b);
					//pointwise mi:
					//stats[i][j] = Math.log(a*p.l/((a+c)*(a+b)));
			}
			if (chi2type == 0) { //max:
				double m = stats[i][0];
				for (int j=1; j<stats[i].length; j++)
					if (stats[i][j] > m) m = stats[i][j];
				chi2[i] = m;
			} else { //avg:
				double m = 0;
				for (int j=0; j<stats[i].length; j++)
					m += (catsize[j]/p.l)*stats[i][j];
				chi2[i] = m;
			}
		}
	}
	
	public void collect1(ProblemExt p) {
		//category sizes:
		catsize = new int [p.catnum];
		for (int i=0; i<p.l; i++)
			catsize[p.y[i]]++;
		//term frequencies:
		termfreq = new int [p.n];
		for (int i=0; i<p.l; i++)
			for (int j=0; j<p.x[i].length; j++)
				termfreq[p.x[i][j].index]++;
		//chi2:
		chi2 = new double [p.n];
		// A = (t,c), B = termfreq(t)-A, C = catsize(c)-A, D = N-A-B-C:
		for (int i=0; i<p.n; i++) {
			if (i%100 == 0) System.out.println("["+i+"]"); ///
			double txc [] = new double [p.catnum];
			for (int j=0; j<p.l; j++) {
				for (int k=0; k<p.x[j].length; k++) {
					if (p.x[j][k].index == i)
						txc[p.y[j]] += 1;
				}
			}
			for (int j=0; j<txc.length; j++) {
				double a = txc[j];
				double b = termfreq[i]-a; 
				double c = catsize[j]-a;
				double d = p.l-a-b-c;
				txc[j] = ((double)p.l*Math.pow(a*d-c*b,2))/((a+c)*(b+d)*(a+b)*(c+d));
			}
			if (chi2type == 0) { //max:
				double m = txc[0];
				for (int j=1; j<txc.length; j++)
					if (txc[j] > m) m = txc[j];
				chi2[i] = m;
			} else { //avg:
				double m = 0;
				for (int j=0; j<txc.length; j++)
					m += (catsize[j]/p.l)*txc[j];
				chi2[i] = m;
			}
			//if (((double)termfreq[i]/p.l <= 0.005) || ((double)termfreq[i]/p.l >= 0.5))
			//	chi2[i] = 0;
		}
	}
	
	public void process(){
		ArrayList<FeatureNodeComp2> chi2indexed=new ArrayList<FeatureNodeComp2>();
		for (int i=1; i<chi2.length; i++) { //+1
			chi2indexed.add(new FeatureNodeComp2(i, chi2[i]));  
		}
		Collections.sort(chi2indexed);
		Collections.reverse(chi2indexed);
		termmap=new BidirectionalMap<Integer>();
		for (int i=0; i<nrf && i<chi2indexed.size(); i++) {
			termmap.addFirstSecond(chi2indexed.get(i).index, i+1);
			//System.out.println(chi2indexed.get(i).value + "," + chi2indexed.get(i).index);
		}
	}
	public DimensionalityReduction newInstance() {
		return new ChiSquare();
	}
}
