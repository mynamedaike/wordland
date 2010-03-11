package wordland.test;
import wordland.decode.*;

public class TestDecode {
	public static void main(String [] a) {
		int [] dec;
		RandomizedDecoder d=new RandomizedDecoder();
		double [][] mtx={{0,3,2,3},{0,0,1,1},{0,3,0,0},{0,0,1,0}};
		double [][] mtx2=d.extendMatrix(mtx);
		dec=d.decode(mtx2, 3);
	}
}
