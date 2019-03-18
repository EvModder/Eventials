package Eventials.splitworlds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
//import java.util.HashSet;
import java.util.List;

public class UnionFind<T>{
	HashMap<T, T> parent;

	public UnionFind(){
		parent = new HashMap<T, T>();
	}

	public T add(final T t){
		T p = find(t);
		if(p != null) return p;
		parent.putIfAbsent(t, t);
		return t;
	}

	public void addToSet(T t, T v){
		T tp = find(t), vp = find(v);
		if(tp == null){
			if(vp == null){add(v); parent.put(t, v);}
			else{parent.put(t, vp);} 
		}
		else if(vp == null) parent.put(v, tp);
		else parent.put(tp, vp);
	}

	public T find(T t){
		T p = parent.get(t);
		if(p != null && !t.equals(p) && !p.equals(p=find(p))) parent.put(t, p);
		return p;
	}

	public void insertSet(Collection<T> ts){
		T k = add(ts.iterator().next());
		for(T t : ts) addToSet(t, k);
	}

	public void insertSets(Collection<List<T>> tss){
		for(Collection<T> ts : tss) insertSet(ts);
	}

	public boolean sameSet(T u, T v){
		//u and v are both not in any set
		if(u == null || find(u) == null) return v == null || find(v) == null;
		else return find(u).equals(find(v));
	}

	public Collection<List<T>> getSets(){
		HashMap<T, List<T>> sets = new HashMap<T, List<T>>();
		for(T t : parent.keySet()){
			T k = find(t);
			if(!sets.containsKey(k)) sets.put(k, new ArrayList<T>());
			sets.get(k).add(t);
		}
		return sets.values();
	}

	public HashMap<T, T> getSets2(){
		HashMap<T, T> sets = new HashMap<T, T>();
		for(T t : parent.keySet()){
			T k = find(t);
			if(!sets.containsKey(k)) sets.put(k, k);
			if(!k.equals(t)) sets.put(t, k);
		}
		return sets;
	}
}