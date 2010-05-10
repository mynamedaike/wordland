package wordland.data.input;

import wordland.data.*;
import java.io.*;
import liblinear.*;

public class ReutersReader {
	public ReutersReader() {
		
	}
	public ProblemExt [] read(String path) {
		ProblemExt [] ret = new ProblemExt [2];
		WordIndex words = new WordIndex();
		ret[0]=readAll(path+"\\train", words, true);
		ret[1]=readAll(path+"\\test", words, false);
		return ret;
	}
	private ProblemExt readAll(String path,WordIndex wi,boolean modify) {
		ProblemExt ret=new ProblemExt();
		File dir=new File(path);
		File [] f = dir.listFiles();
		for (int i=0;i<f.length;i++) {
			
		}
		return ret;
	}
	private FeatureNode [][] readCategory(String path) {
		File dir=new File(path);
		File [] f = dir.listFiles();
		FeatureNode [][] ret = new FeatureNode [f.length][];
		return ret;
	}
}
