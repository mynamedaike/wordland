package wordland.optim;

public class Rep_ECOC {
	public int[][] matrix;
	public double value; 
	
	public Rep_ECOC () {
	}
	public Rep_ECOC clone () {
		Rep_ECOC r = new Rep_ECOC();
		r.matrix = this.matrix.clone();
		r.value = this.value;
		return r;
	}
}
