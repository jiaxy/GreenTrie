package gov.nasa.jpf.symbc.green.trie;

import java.io.Serializable;
import java.util.List;

public class UBTree<K, V> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	Node<K, V> root = new Node<K, V>();

	void insert(final List<K> list, V value) {
		Node<K, V> cur = root;
		for (K item : list) {
			Node<K, V> next = cur.children.get(item);
			if (next == null) {
				next = new Node<K, V>();
				cur.children.put(item, next);
			}
			cur = next;
		}
		cur.isEndOfSet = true;
		cur.value = value;
	}

	Node<K, V> findSubset(Node<K, V> n, final List<K> list, int i) {
		if (n.isEndOfSet) {
			return n;
		}
		if (i >= list.size()) {
			return null;
		}
		Node<K, V> next = n.children.get(list.get(i));
		if (next != null) {
			return findSubset(next, list, i + 1);
		} else {
			return findSubset(n, list, i + 1);
		}
	}

	Node<K, V> findSuperSet(Node<K, V> n, final List<K> list, int i) {
		if (i == list.size()) {
			if (n.isEndOfSet) {
				return n;
			}
			for (Node<K, V> next : n.children.values()) {
				Node<K, V> r = findSuperSet(next, list, i);
				if (r != null) {
					return r;
				}
			}
		} else {
			Node<K, V> next = n.children.get(list.get(i));
			if (next != null) {
				Node<K, V> r = findSuperSet(next, list, i + 1);
				if (r != null) {
					return r;
				}
			}
			for (Node<K, V> next2 : n.children.values()) {
				Node<K, V> r = findSuperSet(next2, list, i);
				if (r != null) {
					return r;
				}
			}
		}
		return null;
	}

}
