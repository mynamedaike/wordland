package wordland.cluster;
import wordland.*;
import wordland.data.ParameterExt;
import wordland.data.ProblemExt;
import wordland.distance.*;

public interface Clusterer {
	public int [] cluster(ProblemExt p,DistanceMetric d,ParameterExt par);
}
