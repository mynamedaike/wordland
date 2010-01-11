package wordland.dimred;

import wordland.*;
import wordland.data.ProblemExt;

/**
 * interface to collect various feature selection / dimensionality reduction methods
 */
public interface DimensionalityReduction {
	/**
	 * collects statistical information about the corpus
	 */
	public void collect(ProblemExt p);
	/**
	 * does the actual feature selection
	 */
	public void process();
	/**
	 * remaps the corpus to the selected features
	 */
	public ProblemExt remap(ProblemExt p);
	public void setNrFeatures(int i);
	public int getNrFeatures();
	public DimensionalityReduction newInstance();
}
