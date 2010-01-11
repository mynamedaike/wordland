package wordland.distance;
import liblinear.*;

public interface DistanceMetric {
	public double distance(FeatureNode [] x,FeatureNode [] y);
}
