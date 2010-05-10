package wordland.data;

import java.util.*;

public class WordMap<T> {
	protected Hashtable<String,T> map=new Hashtable<String,T>();
	public WordMap() {
		
	}
	public void put(String w,T v) {
		map.put(w, v);
	}
	public boolean containsWord(String w) {
		return map.containsKey(w);
	}
	public T get(String w) {
		return map.get(w);
	}
	public Set<String> getWords() {
		return map.keySet();
	}	
	public void addElements(WordMap<T> wm) {
		for (String w : wm.getWords()) {
			put(w,wm.get(w));
		}
	}
	public int size() {
		return map.size();
	}
}
