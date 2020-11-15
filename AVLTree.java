import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;



public class AVLTree<T extends Comparable<? super T>>  implements Iterable<T> {
	public BinaryNode root;
	private int size;
	private int modCount;
	private int balanceCount;
	
	public AVLTree(){
		root = null;
		balanceCount = 0;
	}
	
	public AVLTree(BinaryNode n){
		root = n;
		size = 0;
		balanceCount = 0;
	}

	public boolean isEmpty() {
		return root==null;
	}
	
	public boolean insert(T element) throws IllegalArgumentException {
		if(element == null) throw new IllegalArgumentException();	
		BinaryNode nodeToInsert = new BinaryNode(element);
		MyBoolean b = new MyBoolean();
		if(isEmpty()) {
			root = nodeToInsert;
			size++;
			modCount++;
			return b.getValue();
		}
		modCount++;
		root = root.insert(nodeToInsert, b);
		return b.getValue();
	}
	public boolean remove(T element) throws IllegalArgumentException {
		if(element == null) throw new IllegalArgumentException();
		if(root==null) return false;
		MyBoolean b = new MyBoolean();
		root = root.remove(element, b);
		if(b.getValue()) {
			size--;
			modCount++;
		}
		return b.getValue();
	}
	public boolean reverse() {
		if(root==null) return false;
		root = root.reverse();
		return true;
	}
	public Iterator<T> iterator() {
		return new PreOrderIterator(modCount);
	}
	public ArrayList<T> toArrayList() {
		ArrayList<T> temp = new ArrayList<>();
		if(!isEmpty())root.toArrayList(temp);
		return temp;
	}
	public Object[] toArray(){
		Object[] temp = toArrayList().toArray();
		return temp;
	}
	public int size() {
		return size;
	}
	public int height() {
		return isEmpty()? -1:root.height;
	}
	public String toString() {
		return toArrayList().toString();
		
	}
	public int getRotationCount() {
		return balanceCount;
	}
	
	public class BinaryNode {
		private T element;
		private BinaryNode leftChild;
		private BinaryNode rightChild;
		private int height;
		
		public BinaryNode(T element){
			this.element = element;
			this.leftChild = null;
			this.rightChild = null;		
			this.height = 0;
		}
		

		public BinaryNode insert(BinaryNode node, MyBoolean b) {
			int c = node.element.compareTo(element);
			boolean hasRightChild = rightChild!=null;
			boolean hasLeftChild = leftChild!=null;
			if(c==1) {
				if(hasRightChild) rightChild = rightChild.insert(node, b);
				else {
					rightChild = node;
					size++;
				}
				return adjust();
			}
			if(c==-1) {
				if(hasLeftChild) leftChild = leftChild.insert(node, b);
				else {
					leftChild = node;
					size++;
				}
				return adjust();
			}
			b.setFalse();
			return this;
		}
/**
 * Returns either this node if no balances need to be made, or the root node of the balanced subtree
 * previously rooted at this node
 * @return balanced subtree rooted at this node
 */
		private BinaryNode adjust() {
			BinaryNode temp = this;
			if(getLeftHeight()-getRightHeight()>1) {
				temp = balanceLeftSide();
			}
			if(getRightHeight()-getLeftHeight()>1) {
				temp = balanceRightSide();
			}

			return temp;
		}
		private BinaryNode balanceLeftSide() {
			int leftRightHeight = leftChild.getRightHeight();
			int leftLeftHeight = leftChild.getLeftHeight();
			if(leftLeftHeight>=leftRightHeight) return singleRightRotation();
			else return leftRightRotation();
		}
		private BinaryNode balanceRightSide() {
			int rightRightHeight = rightChild.getRightHeight();
			int rightLeftHeight = rightChild.getLeftHeight();
			if(rightRightHeight>=rightLeftHeight) return singleLeftRotation();
			else return rightLeftRotation();
		}
		private BinaryNode leftRightRotation() {
			leftChild = leftChild.singleLeftRotation();
			return singleRightRotation();
		}
		private BinaryNode rightLeftRotation() {
			rightChild = rightChild.singleRightRotation();
			return singleLeftRotation();
		}
		private BinaryNode singleRightRotation() {
			BinaryNode temp = leftChild;
			leftChild = temp.rightChild;
			temp.rightChild = this;
			
			updateHeight();
			temp.updateHeight();
			
			balanceCount++;
			return temp;
		}
		private BinaryNode singleLeftRotation() {
			BinaryNode temp = rightChild;
			rightChild = temp.leftChild;
			temp.leftChild = this;
			
			updateHeight();
			temp.updateHeight();
			
			balanceCount++;
			return temp;
		}
		public BinaryNode reverse() {
			if(rightChild==null && leftChild==null) {
				return this;
			}
			if(leftChild==null) {
				leftChild = rightChild.reverse();
				rightChild = null;
				return this;
			}
			if(rightChild==null) {
				rightChild = leftChild.reverse();
				leftChild = null;
				return this;
			}
			BinaryNode temp = leftChild;
			leftChild = rightChild.reverse();
			rightChild = temp.reverse();
			return this;
		}

		public BinaryNode remove(T element, MyBoolean b) {
			int compareConstant = element.compareTo(this.element);
			if(rightChild==null && leftChild==null && compareConstant!=0) {
				b.setFalse();
			}
			if(compareConstant == -1) {
				if(leftChild!=null)leftChild = leftChild.remove(element, b);
				else b.setFalse();
			}
			if(compareConstant == 1) {
				if(rightChild!=null) rightChild = rightChild.remove(element, b);
				else b.setFalse();
			}
			
			if(compareConstant!=0) return adjust();
			if(leftChild==null) return rightChild;
			if(rightChild==null) return leftChild;
			
			this.element = leftChild.getRightMostChild().element;
			leftChild = leftChild.remove(this.element, b);
			return adjust();

		}
		public BinaryNode getRightMostChild() {
			if(rightChild==null) return this;
			else return rightChild.getRightMostChild();
		}
		public void toArrayList(ArrayList<T> a) {
			if(leftChild != null) leftChild.toArrayList(a);
			a.add(element);
			if(rightChild!=null) rightChild.toArrayList(a);
		}
		private void updateHeight() {
			this.height =  1 + (Math.max(getLeftHeight(), getRightHeight()));
		}
		private int getRightHeight() {
			return rightChild==null? -1:rightChild.height;
		}
		private int getLeftHeight() {
			return leftChild==null? -1:leftChild.height;
		}
		public BinaryNode getLeftChild() {
			return leftChild;
		}
		public BinaryNode getRightChild() {
			return rightChild;
		}
		public int getHeight() {
			return height;
		}
		public T getElement(){
			return element;
		}
		public String toString() {
			ArrayList<T> temp = new ArrayList<>();
			toArrayList(temp);
			return temp.toString();
		}
}
	public class PreOrderIterator implements Iterator<T> {
		private Stack<BinaryNode> stack;
		private int modCountAtConstruction;
		private BinaryNode lastNode;
		public PreOrderIterator(int modCount) {
			this.modCountAtConstruction = modCount;
			stack = new Stack<>();
			if(root!=null) {
				stack.push(root);
			}
		}
		
		public boolean hasNext() {
			return !stack.isEmpty();
		}

		public T next() throws NoSuchElementException, ConcurrentModificationException {
			if(modCount!=modCountAtConstruction) throw new ConcurrentModificationException();
			if(!hasNext()) throw new NoSuchElementException();
			lastNode = stack.pop();
			if(lastNode.rightChild!=null) stack.push(lastNode.rightChild);
			if(lastNode.leftChild!=null) stack.push(lastNode.leftChild);
			return lastNode.element;
		}
		public void remove() throws IllegalStateException {
			if(lastNode==null) throw new IllegalStateException();
			if(lastNode.rightChild!=null && lastNode.leftChild!=null) {
				stack.pop();
				stack.pop();
				stack.push(lastNode);
			}
			AVLTree.this.remove(lastNode.element);
			lastNode = null;
			modCountAtConstruction++;	
		}
	}
	public class MyBoolean {
		private boolean value = true;
		
		public boolean getValue() {
			return value;
		}
		public void setFalse() {
			value = false;
		}
	}
	


}
