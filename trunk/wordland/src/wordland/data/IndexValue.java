package wordland.data;

/**
 * a simple class to store index/value pairs, because FeatureNode doesn't like index 0 and it's annoying sometimes
 * this is comparable and it compares on the index
 */
public class IndexValue implements Comparable<IndexValue>{
    public int    index;
    public double value;

    public IndexValue(int index, double value ) {
        this.index = index;
        this.value = value;
    }
	public int compareTo(IndexValue o) {
		if (!(o instanceof IndexValue)) {
			return -1;
		}
		else {
			return index-((IndexValue)o).index;
		}
	}
	public String toString() {
		return "("+index+":"+value+")";
	}    
}