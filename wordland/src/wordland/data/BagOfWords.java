package wordland.data;

public class BagOfWords extends WordMap<Double> {
	public BagOfWords() {
		super();
	}
	public void incOccurence(String w) {
		if (containsWord(w)) {
			put(w,get(w).doubleValue()+1.0);
		}
		else {
			put(w,1.0);
		}
	}
}
