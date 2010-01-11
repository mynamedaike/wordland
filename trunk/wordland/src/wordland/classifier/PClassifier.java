package wordland.classifier;

import wordland.*;
import wordland.data.IndexValue;
import wordland.data.ProblemExt;

/**
 * Classifier capable of returning probabilities of class membership
 */
public interface PClassifier extends Classifier {
	/**
	 * the indices are class ids, the values are probabilities
	 * theoretically every row should contain the same number of columns
	 * though it would be nice not to rely on this
	 * (as category ids need not be consecutive numbers starting from 0, introducing indices was necessary)
	 * every implementing class should put the indices in ascending order
	 */
	public IndexValue [][] testp(ProblemExt p);
}
