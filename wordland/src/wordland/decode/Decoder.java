package wordland.decode;

public interface Decoder {
	/**
	 * does the actual deocoding
	 * @param matrix is a dense matrix with the edge weights between the vertices. 
	 * It has to have two additional rows/columns for the source and the sink vertex
	 * basically encoding the cost to start the chain with each node.  
	 */
	public int [] decode(double [][] matrix,int k);
}
