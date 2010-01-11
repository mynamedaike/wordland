package wordland.data;
import java.util.*;
/**
 * a bidirectional map (hopefully it's called liked this)
 * mainly intended to be used for category remapping 
 */
public class BidirectionalMap<T> {
	private Hashtable<T,T> firstsecond=new Hashtable<T,T>();
	private Hashtable<T,T> secondfirst=new Hashtable<T,T>();
	public BidirectionalMap() {}
	public BidirectionalMap(BidirectionalMap<T> o) {
		for (T f : o.getFirsts()) {
			addFirstSecond(f,o.getSecond(f));
		}
	}
	public void addMap(BidirectionalMap<T> o) {
		for (T f : o.getFirsts()) {
			if (getFirst(f)==null) {
				addFirstSecond(f,o.getSecond(f));
			}
		}
	}
	public void addFirstSecond(T f,T s) {
		firstsecond.put(f, s);
		secondfirst.put(s, f);
	}
	public void addSecondFrist(T s,T f) {
		firstsecond.put(f, s);
		secondfirst.put(s, f);
	}
	public boolean containsSecond(T s) {
		return secondfirst.containsKey(s);
	}
	public T getFirst(T s) {
		return secondfirst.get(s);
	}
	public boolean containsFirst(T f) {
		return firstsecond.containsKey(f);
	}
	public T getSecond(T f) {
		return firstsecond.get(f);
	}
	public Set<T> getFirsts() {
		return firstsecond.keySet();
	}
	public Set<T> getSeconds() {
		return secondfirst.keySet();
	}
	public void clear() {
		firstsecond.clear();
		secondfirst.clear();
	}
	/**
	 * performs inner join between this map and another
	 * the values in common will be the first value from this map and the first map from the other map
	 * in the result, the first element comes from this, the second element comes from the other 
	 */
	public BidirectionalMap<T> joinFirstFirst(BidirectionalMap<T> other) {
		if (other==null) {
			return new BidirectionalMap<T>();
		}
		BidirectionalMap<T> ret=new BidirectionalMap<T>();
		for (T joinkey : firstsecond.keySet()) {
			T secondthis=getSecond(joinkey);
			T secondother=other.getSecond(joinkey);
			if (secondthis!=null && secondother!=null) {
				ret.addFirstSecond(secondthis, secondother);
			}
		}
		return ret;
	}
	public BidirectionalMap<T> joinFirstSecond(BidirectionalMap<T> other) {
		if (other==null) {
			return new BidirectionalMap<T>();
		}
		BidirectionalMap<T> ret=new BidirectionalMap<T>();
		for (T joinkey : firstsecond.keySet()) {
			T secondthis=getSecond(joinkey);
			T firstother=other.getFirst(joinkey);
			if (secondthis!=null && firstother!=null) {
				ret.addFirstSecond(secondthis, firstother);
			}
		}
		return ret;		
	}
	public BidirectionalMap<T> reverse() {
		BidirectionalMap<T> ret=new BidirectionalMap<T>();
		for (T key : firstsecond.keySet()) {
			ret.addSecondFrist(key, firstsecond.get(key));
		}
		return ret;
	}
	public int size() {
		return firstsecond.size();		
	}
}
