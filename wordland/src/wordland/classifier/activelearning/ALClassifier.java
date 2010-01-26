package wordland.classifier.activelearning;

import wordland.classifier.Classifier;

/**
 * A classifier that uses active learning strategies using an Oracle object.
 */
public interface ALClassifier extends Classifier {
	public void setOracle(Oracle o);
}
