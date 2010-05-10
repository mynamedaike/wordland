package wordland.data.input;

import wordland.data.*;
import wordland.nlp.*;
import liblinear.*;
import java.util.*;

public class BagOfWordsReader {
	public BagOfWordsReader() {
			
	}
	
	/**
	 * Simple transformation of a string into a bag of words, it can perform Porter stemming
	 */
	public static BagOfWords readSimple(String line, boolean stem) {
		BagOfWords ret=new BagOfWords();
		
		Stemmer stemmer=new Stemmer();
		
		line=line.toLowerCase();
		line=line.replaceAll("#3;","");  //removes this string that appears in very short documents
		line=line.replaceAll("\\s{2,}"," ");
		line=line.replaceAll("[<>()-/\"']"," ");					
		line=line.replaceAll("\\d+","num ");  //replaces digits with num
		line=line.replaceAll("[.!?&]"," ");  //removes punctuation
		line=line.replaceAll("_+", "");
		
		String [] words=line.split(" ");
		
		for (String w : words) {
			if (w.length()>0) {
				if (stem) {
					stemmer.add(w.toCharArray(), w.length());
					stemmer.stem();
					w=stemmer.toString();
				}
				ret.incOccurence(w);
			}
		}
		
		return ret;
	}
	
	/**
	 * adds the words in a bag of words to a  map that maps words to integers, useful for converting
	 * lots of bags of words into FeatureNode arrays.
	 */
	public static void addBOWWordMap(BagOfWords what, WordIndex where) {
		int min=where.size();
		for (String w : what.getWords()) {
			if (!where.containsWord(w)) {
				where.put(w, min++);
			}
		}
	}
	
	/**
	 * transforms a bag of words into a FeatureNode array
	 */
	public static FeatureNode [] transformBOW(BagOfWords what, WordIndex how) {
		FeatureNode [] ret=new FeatureNode [what.size()];
		ArrayList<FeatureNodeComp> list = new ArrayList<FeatureNodeComp>();
		for (String w : what.getWords()) {
			Integer index = how.get(w);
			if (index!=null) {
				list.add(new FeatureNodeComp(index.intValue(),what.get(w).doubleValue()));
			}
		}
		Collections.sort(list);
		list.toArray(ret);
		return ret;
	}
	
}
