package probsym.modify;

import probsym.BSTNode;

/**
 * Taken from issta2006.BinTree
 *
 */
public class BinTree2 {

	private BSTNode root;

	public BinTree2() {
		root = null;
	}
	
	public static void main(String[] args){
		BinTree2 t = new BinTree2();
		for (int i=0; i < 5; i++){
			t.add(i);
		}
	
	}

	public void add(int x) {
		BSTNode current = root;

		if (root == null) {
			root = new BSTNode(x+1);
			return;
		}

		while (current.value != x) {
			if (x <= current.value) {
				if (current.left == null) {
					current.left = new BSTNode(x);
				} else {
					current = current.left;
				}
			} else {
				if (current.right == null) {
					current.right = new BSTNode(x);
				} else {
					current = current.right;
				}
			}
		}
	}

	public boolean find(int x) {
		BSTNode current = root;

		while (current != null) {

			if (current.value == x) {
				return true;
			}

			if (x < current.value) {
				current = current.left;
			} else {
				current = current.right;
			}
		}

		return false;
	}

	public boolean remove(int x) {
		BSTNode current = root;
		BSTNode parent = null;
		boolean branch = true; //true =left, false =right

		while (current != null) {

			if (current.value == x) {
				BSTNode bigson = current;
				while (bigson.left != null || bigson.right != null) {
					parent = bigson;
					if (bigson.right != null) {
						bigson = bigson.right;
						branch = false;
					} else {
						bigson = bigson.left;
						branch = true;
					}
				}

				//		System.out.println("Remove: current "+current.value+" parent "+parent.value+" bigson "+bigson.value);
				if (parent != null) {
					if (branch) {
						parent.left = null;
					} else {
						parent.right = null;
					}
				}

				if (bigson != current) {
					current.value = bigson.value;
				} else {;
				}

				return true;
			}

			parent = current;
			//	    if (current.value <x ) { // THERE WAS ERROR
			if (current.value > x) {
				current = current.left;
				branch = true;
			} else {
				current = current.right;
				branch = false;
			}
		}

		return false;
	}

}
