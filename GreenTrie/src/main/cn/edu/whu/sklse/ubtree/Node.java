package cn.edu.whu.sklse.ubtree;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class Node<K, V> implements Serializable {
	private static final long serialVersionUID = 1L;

	Map<K, Node<K, V>> children = new TreeMap<K, Node<K, V>>();
	V value = null;
	boolean isEndOfSet = false;

	public V getValue() {
		return value;
	}

	public void setValue(V value) {
		this.value = value;
	}

	public boolean isEndOfSet() {
		return isEndOfSet;
	}

	public void setEndOfSet(boolean isEndOfSet) {
		this.isEndOfSet = isEndOfSet;
	}

	public Map<K, Node<K, V>> getChildren() {
		return children;
	}

}
