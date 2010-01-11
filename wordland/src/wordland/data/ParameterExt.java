package wordland.data;

//hmm, it's final, we have to wrap it
import wordland.classifier.*;
import wordland.cluster.*;
import wordland.distance.*;
import liblinear.Parameter;
import libsvm.svm_parameter;

/**
 * a class that contains all kinds of parameters for all kinds of classifiers
 * the classifiers are supposed to only take the information they need 
 * extended to contain clustering algorithm parameters also
 */
public class ParameterExt {
	//a classifier that may be given as a parameter to other classifiers
	public Classifier embedded=null;
	//parameters to the embedded classifier
	public ParameterExt embeddedparam=null;
	public Classifier embedded2=null;
	public ParameterExt embeddedparam2=null;
	//liblinear SVM parameters
	public Parameter param=null;
	//ECOC parameters
	public int ecoc_nr=0;
	//HSVM parameters... none yet
	//kNN parameters
	public int knn_k=10;
	//LIBSVM SVM parameters
	public svm_parameter libsvm_par = null;
	//a general distance parameter, could be used by many classifiers
	public DistanceMetric distance = null;
	//a general normalization parameter, could be used by many things
	public DistanceMetric normalization = null;
	//k-means parameters
	public int clusters=2;
	//cluster wrapper parameters
	public int maxpoints=3000;
	public DistanceMetric select_distance=null;
	//an embedded clustering algorithm
	public Clusterer embeddedclu=null;	
	public ParameterExt() {
	}
}
