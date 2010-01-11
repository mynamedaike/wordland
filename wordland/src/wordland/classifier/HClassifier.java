package wordland.classifier;

import java.util.Hashtable;

import wordland.data.BidirectionalMap;
import wordland.data.Hierarchy;
import wordland.data.ParameterExt;


/**
 * Hierarchical Classifiers should extend this abstract class and use the hierarchy information of the training data
 */
public abstract class HClassifier implements Classifier{
	/*
	 * the integer here means the node of the hierarchy between whose children
	 * we have to decide, this is our model
	 */
	protected Hashtable<Integer,Classifier> classifiers=new Hashtable<Integer,Classifier>();
	//we have to copy the category map of the data, otherwise we couldn't produce correct category ids in testing
	protected BidirectionalMap<Integer> traincategories;
	//we also need to know what was the base classifier, so we store the training parameters too
	protected ParameterExt trainparams;
	//we also need the train hierarchy
	protected Hierarchy trainhierarchy;
	public Classifier getNodeClassifier(int node) {
		return classifiers.get(node);
	}
}
