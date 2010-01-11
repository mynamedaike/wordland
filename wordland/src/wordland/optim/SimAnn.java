package wordland.optim;

public abstract class SimAnn<T> { //Simulated Annealing
	public T sol; //solution
	protected double T0; //temperature
	protected int L0; //procedure length
	protected int it; // iteration count
	public SimAnn (double t, int l, T s) {
		T0 = t; //initial temperature
		L0 = l; //initial length
		sol = s; //initial solution
	}
	/** 
	 * The fitness/energy function
	 */
	abstract protected double f (T x);
	/** 
	 * Returs a random neighbor of "sol"
	 */
	abstract protected T neighbor (); 
	/**
	 * Termination condition; returns "true" when terminates
	 */
	abstract protected boolean TermCond ();
	/**
	 * Next temperature 
	 */
	abstract protected double nextT ();
	/**
	 * Next length
	 */
	abstract protected int nextL ();
	/**
	 * Method for running the optimization procedure  
	 */
	public T optimize () {
		T r;
		it = 0;
		while (!TermCond()) {
			System.out.println("SimAnn: iteration "+it);
			for (int i=0; i<L0; i++) {
				r = neighbor();
				if (f(r) >= f(sol)) {
					sol = r;
				} else {
					double n = Math.random();
					if (Math.exp((-f(sol)+f(r))/T0) > n)
						sol = r;
				}
			}
			it++;
			T0 = nextT();
			L0 = nextL();
		}
		return sol;
	}
	public void setT0 (double t) {
		T0 = t;
	}
	public void setL0 (int l) {
		L0 = l;
	}
	public void setInitSolution (T s) {
		sol = s;
	}
}

