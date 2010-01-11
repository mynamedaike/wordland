package wordland.data;

/**
 * a simple class to store index/value pairs, because FeatureNode doesn't like index 0 and it's annoying sometimes
 * this is comparable and it compares on the value
 */
public class ValueIndex implements Comparable<ValueIndex>{
    public int    index;
    public double value;

    public ValueIndex(int index, double value ) {
        this.index = index;
        this.value = value;
    }
	public int compareTo(ValueIndex o) {
		if (!(o instanceof ValueIndex)) {
			return -1;
		}
		else {
			return (int)Math.signum(value-((ValueIndex)o).value);
		}
	}
	public String toString() {
		return "("+index+":"+value+")";
	}    
}