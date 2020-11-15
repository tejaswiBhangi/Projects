import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;








public class AVLTreeEC<T extends Comparable<? super T>> extends ArrayList<T> {
	private int size;
	private int modCount;
	private int balanceCount;
	
	public AVLTreeEC() {
		super();
		balanceCount = 0;
		modCount = 0;
		size = 0;
	}
	
	@Override
	public boolean isEmpty() {
		if(super.isEmpty())return true;
		return get(0)==null;
	}
	public boolean insert(T element) {
		MyBoolean b = new MyBoolean();
		if(isEmpty()) {
			add(element);
			ensureSize(3);
			size++;
			modCount++;
			return b.getValue();
		}
		
		set(0, insertAt(element, 0, b));
		return b.getValue();
	}
	public boolean treeRemove(T element) {
		if(element==null) throw new IllegalArgumentException();
		if(isEmpty())return false;
		
		return removeAt(0, element);
		
	}
	private T insertAt(T element, int i, MyBoolean b) {
		if(get(i)==null) {
			ensureSize(rIndex(i)+1);
			size++;
			return element;
		}
		int c = element.compareTo(get(i));
		int leftIndex = lIndex(i);
		int rightIndex = rIndex(i);
		if(c==-1) {
			set(leftIndex, insertAt(element, leftIndex, b));
			return adjust(i);
		}
		if(c==1) {
			set(rightIndex, insertAt(element, rightIndex, b));
			return adjust(i);
		}
		b.setFalse();
		return adjust(i);
	}
	@Override
	public Iterator<T> iterator() {
		return new PreOrderIterator(modCount);
	}
	public boolean removeAt(int i, T element) {
		int compareConstant = element.compareTo(get(i));
		boolean result = false;
		if(get(rIndex(i))==null && get(lIndex(i))==null && compareConstant!=0) return false;
		if(compareConstant==-1) {
			if(get(lIndex(i))!=null) {
				result = removeAt(lIndex(i), element);
			}
			else return false;
		}
		if(compareConstant==1) {
			if(get(rIndex(i))!=null) result = removeAt(rIndex(i), element);
			else return false;
		}
		if(compareConstant!=0) {
			adjust(i);
			return result;
		}
		set(i,null);
		if(get(lIndex(i))==null) {
			moveTreeLeft(rIndex(i), i);
			adjust(i);
			size--;
			return true;
		}
		if(get(rIndex(i))==null) {
			moveTreeRight(lIndex(i), i);
			adjust(i);
			size--;
			return true;
		}
		int temp = getRightMostIndex(lIndex(i));
		set(i, get(temp));
		removeAt(lIndex(i), get(temp));
		adjust(i);
		return true;
	}
	private int getRightMostIndex(int i) {
		if(get(rIndex(i))==null) {
			return i;
		}
		return getRightMostIndex(rIndex(i));
	}
	private T adjust(int i) {
		T temp = get(i);
		int leftHeight = getLeftHeight(i);
		int rightHeight = getRightHeight(i);
		if(leftHeight-rightHeight>1) {
			temp = balanceLeftSide(i);
		}
		if(getRightHeight(i)-getLeftHeight(i)>1) {
			temp = balanceRightSide(i);
		}
//		updateHeight();
		return temp;
	}
	private T balanceLeftSide(int i) {
		int leftRightIndex = rIndex(lIndex(i));
		int leftLeftIndex = lIndex(lIndex(i));
		int leftRightHeight = getHeight(leftRightIndex);
		int leftLeftHeight = getHeight(leftLeftIndex);
		if(leftLeftHeight>=leftRightHeight) return singleRightRotation(i);
		else return leftRightRotation(i);
	}
	private T balanceRightSide(int i) {
		int rightRightIndex = rIndex(rIndex(i));
		int rightLeftIndex = lIndex(rIndex(i));
		int rightRightHeight = getHeight(rightRightIndex);
		int rightLeftHeight = getHeight(rightLeftIndex);
		if(rightRightHeight>=rightLeftHeight) return singleLeftRotation(i);
		else return rightLeftRotation(i);
	}
	private T leftRightRotation(int i) {
		set(lIndex(i), singleLeftRotation(lIndex(i)));
		return singleRightRotation(i);
	}
	private T rightLeftRotation(int i) {
		set(rIndex(i), singleRightRotation(rIndex(i)));
		return singleLeftRotation(i);
	}
	private T singleRightRotation(int i) {
		moveTreeRight(rIndex(i), rIndex(rIndex(i)));
		set(rIndex(i), get(i));
		moveTreeRight(rIndex(lIndex(i)), lIndex(rIndex(i)));
		moveTreeRight(lIndex(i), i);
		balanceCount++;
		return get(i);
	}
	private T singleLeftRotation(int i) {
		moveTreeLeft(lIndex(i), lIndex(lIndex(i)));
		set(lIndex(i), get(i));
		moveTreeLeft(lIndex(rIndex(i)), rIndex(lIndex(i)));
		moveTreeLeft(rIndex(i), i);
		balanceCount++;
		return get(i);
	}
	private void moveTreeRight(int rootIndex, int newRootIndex) {
		
		if(rootIndex==newRootIndex || rootIndex>size()-1 || get(rootIndex)==null) return;
		if(newRootIndex>size()-1) {
			ensureSize(newRootIndex+1);
		}
		
		T temp = get(rootIndex);
		set(rootIndex,null);
		int leftIndex = lIndex(rootIndex);
		int newLeftIndex = lIndex(newRootIndex);
		int rightIndex = rIndex(rootIndex);
		int newRightIndex = rIndex(newRootIndex);
		moveTreeRight(rightIndex, newRightIndex);
		moveTreeRight(leftIndex, newLeftIndex);
		
		
		set(newRootIndex,temp);
		
		
	}
	private void moveTreeLeft(int rootIndex, int newRootIndex) {
		
		if(rootIndex==newRootIndex || rootIndex>size()-1 || get(rootIndex)==null) return;
		if(newRootIndex>size()-1) {
			ensureSize(newRootIndex+1);
		}
		
		T temp = get(rootIndex);
		set(rootIndex,null);
		int leftIndex = lIndex(rootIndex);
		int newLeftIndex = lIndex(newRootIndex);
		int rightIndex = rIndex(rootIndex);
		int newRightIndex = rIndex(newRootIndex);
		moveTreeLeft(leftIndex, newLeftIndex);
		moveTreeLeft(rightIndex, newRightIndex);
		
		
		
		set(newRootIndex,temp);
		
		
	}
	private void ensureSize(int s) {
		ensureCapacity(s);
		while(s>size()) {
			add(null);
		}
	}
	public int lIndex(int i) {
		return 2*i+1;
	}
	public int rIndex(int i) {
		return 2*i+2;
	}
	
	public int getTrueSize() {
		return size;
	}
	public ArrayList<T> toInOrder() {
		ArrayList<T> temp = new ArrayList<>();
		if(get(0)!=null) toInOrderHelper(temp, 0);
		return temp;
	}
	private void toInOrderHelper(ArrayList<T> a, int i) {
		if(get(lIndex(i)) != null) toInOrderHelper(a, lIndex(i));
		a.add(get(i));
		if(get(rIndex(i))!=null) toInOrderHelper(a, rIndex(i));
	}
	public int getHeight(int i) {
		if(i>=size()) return -1;
		if(get(i)==null) return -1;
		int lHeight = -1;
		int rHeight = -1;
		int leftIndex = lIndex(i);
		int rightIndex = rIndex(i);
		if(get(leftIndex)!=null) lHeight = getHeight(leftIndex);
		if(get(rightIndex)!=null) rHeight = getHeight(rightIndex);
		return Math.max(lHeight, rHeight) + 1;
	}
	public int height() {
		return getHeight(0);
	}
	public int getRotationCount() {
		return balanceCount;
	}
	public int getLeftHeight(int i) {
		return getHeight(lIndex(i));
	}
	public int getRightHeight(int i) {
		return getHeight(rIndex(i));
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
	
	public static void main(String[] args) {
		AVLTreeEC<Integer> tree = new AVLTreeEC<>();
		tree.insert(5);
		tree.insert(9);
		tree.insert(3);
		tree.insert(1);
		tree.insert(8);
		tree.treeRemove(1);
	}
	public class PreOrderIterator implements Iterator<T> {
		private Stack<Integer> stack;
		private int modCountAtConstruction;
		private int lastNode;
		public PreOrderIterator(int modCount) {
			this.modCountAtConstruction = modCount;
			stack = new Stack<>();
			if(get(0)!=null) {
				stack.push(0);
			}
		}
		
		public boolean hasNext() {
			return !stack.isEmpty();
		}

		public T next() throws NoSuchElementException, ConcurrentModificationException {
			if(modCount!=modCountAtConstruction) throw new ConcurrentModificationException();
			if(!hasNext()) throw new NoSuchElementException();
			lastNode = stack.pop();
			if(get(rIndex(lastNode))!=null) stack.push(rIndex(lastNode));
			if(get(lIndex(lastNode))!=null) stack.push(lIndex(lastNode));
			return get(lastNode);
		}
//		public void remove() throws IllegalStateException {
//			if(lastNode==null) throw new IllegalStateException();
//			if(lastNode.rightChild!=null && lastNode.leftChild!=null) {
//				stack.pop();
//				stack.pop();
//				stack.push(lastNode);
//			}
//			AVLTree.this.remove(lastNode.element);
//			lastNode = null;
//			modCountAtConstruction++;	
//		}
	}
	
	
	
}
