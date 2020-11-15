import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

public class RedBlackTree<T extends Comparable<? super T>>  implements Iterable<RedBlackTree.BinaryNode> {
	public BinaryNode root;
	private int size;
	private int modCount;
	private int balanceCount;
	
	public enum Color {RED, BLACK}
	
	public RedBlackTree(){
		root = null;
		balanceCount = 0;
	}
	
	public RedBlackTree(BinaryNode n){
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
		if(isEmpty()) {
			root = nodeToInsert;
			size++;
			modCount++;
			root.color=Color.BLACK;
			return true;
		}
		modCount++;
		return insert(nodeToInsert);
	}
	
	public boolean insert(BinaryNode node) {
		boolean inserted = false;
		BinaryNode current = root;
		BinaryNode p = root;
		BinaryNode gp = root;
		BinaryNode ggp = root;
		while(!inserted) {
			int c = node.element.compareTo(current.element);
			Trit rotated = new Trit(0);
			current.colorFlip();
			if(c==0) {
				root.color = Color.BLACK;
				return false;
			}
			
			if(c==1) {
				if(current.rightChild==null) {
					current.rightChild = node;
					ggp = gp;
					gp = p;
					p = current;
					size++;
					inserted = true;
				}		
			}
			
			if(c==-1) {
				if(current.leftChild==null) {
					current.leftChild = node;
					ggp = gp;
					gp = p;
					p = current;
					size++;
					inserted = true;
				}
			}
			
			if(gp==root) root = root.handleImbalance(rotated);
			else ggp.setChild(gp.handleImbalance(rotated));
			
			switch (rotated.getValue()){
			case 0:
				ggp = gp;
				gp = p;
				p = current;
				break;
			case 1:
				gp = p;
				p = current;
				break;
			case 2:
				gp = ggp;
				p = current;
				break;
			}
			current = c==1? current.rightChild:current.leftChild;
		}
		root.color = Color.BLACK;
		return inserted;
	}

	public boolean remove(T element) {
		if(element==null) throw new IllegalArgumentException();
		if(isEmpty()) {
			return false;
		}
		boolean removed = innerRemove(element);
		if(removed)size--;
		return removed;
	}
	
	private boolean innerRemove(T element) {
		BinaryNode x, p, gp, s, next, found, nextS, dummy;
		p = gp = found = null;
		
		Color black = Color.BLACK;
		Color red = Color.RED;
		Color nextSiblingColor = black;
		Color nextColor = black;
		if(root.getRightColor()==black&&root.getLeftColor()==black) root.color = red;
		//current starts out as dummy node
		dummy = new BinaryNode(null);
		x = dummy;
		x.rightChild = root;
		int c = 1;
		int pc = 1;
		next = root;
		nextS = null;
		Trit t = new Trit(0);
		T tempV = null;
		BinaryNode saveX = null;
		while(x!=null) {
			if(c==0) found = x;
			if(x.color==black && nextColor==black) {
				if(nextSiblingColor==red) {
					if(found==null && next!=null)p.setChild(x.handleNextSiblingIsRedNextIsBlack());
				}else {
					gp.setChild(p.handleXHasTwoBlackChildren(pc==-1));	
				}
			}
			if(found!=null) {
				if(pc==1) p.rightChild = found.removeYourself(t);
				else p.leftChild = found.removeYourself(t);
				if(t.getValue()==0) {
					if(saveX!=null) {
						saveX.element = tempV;
					}

					if(root!=null) root.color = black;
					return true;
				}
				T temp = found.leftChild.getRightMostChild().element;
				if(t.getValue()==1) {
					//x is red and has two children
					found.element = temp;
					element = temp;
					found = null;
				}else {
					//x is black and has two children
					tempV = temp;
					element = temp;
					saveX = found;
					found=null;
				}
				
			}
			if(t.getValue()!=2) {
				gp = p;
				p = x;
				s = nextS;
				x = next;
			}
			t.setValue(0);
			if(x!=null) {
				pc = c;
				c = element.compareTo(x.element);
				nextS = c==1?  x.leftChild  : x.rightChild;
				next  =	c==1?  x.rightChild : x.leftChild;
				nextSiblingColor = nextS==null? black : nextS.color;
				nextColor = next==null? black : next.color;
			}
			
		
		}
		root = dummy.rightChild;
		if(root!=null) root.color = black;
		return false;
	}
	
	public boolean reverse() {
		if(root==null) return false;
		root = root.reverse();
		return true;
	}
	
	public Iterator<RedBlackTree.BinaryNode> iterator() {
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
		return isEmpty()? -1:root.getHeight();
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
		private Color color;
		
		public BinaryNode(T element){
			this.element = element;
			this.leftChild = null;
			this.rightChild = null;		
			this.color = Color.RED;
		}
		
		private BinaryNode handleImbalance(Trit b) {
			BinaryNode temp = handleRightImbalance(b);
			if(b.getValue()==0) temp = handleLeftImbalance(b);
			return temp;
		}
		
		private BinaryNode handleRightImbalance(Trit b) {
			if(getRightColor()!=Color.RED) return this;
			if(rightChild.getRightColor()==Color.RED) {
				b.setValue(1);
				return singleLeftRotation();
			}
			else if (rightChild.getLeftColor()==Color.RED) {
				b.setValue(2);
				return rightLeftRotation();
			}
			return this;
		}
		
		private BinaryNode handleLeftImbalance(Trit b) {
			if(getLeftColor()!=Color.RED) return this;
			if(leftChild.getLeftColor()==Color.RED) {
				b.setValue(1);
				return singleRightRotation();
			}
			else if (leftChild.getRightColor()==Color.RED) {
				b.setValue(2);
				return leftRightRotation();
			}
			return this;
		}
		
		private boolean colorFlip() {
			if(color==Color.BLACK && getRightColor()==Color.RED && getLeftColor()==Color.RED) {
				this.color= Color.RED;
				leftChild.color = Color.BLACK;
				rightChild.color = Color.BLACK;
				return true;
			}
			return false;
		}
		
		//this method is called on the parent when the target X child has two black children
		//assumes sibling is not null
		private BinaryNode handleXHasTwoBlackChildren(boolean xIsLeftChild) {
			BinaryNode sibling = xIsLeftChild? rightChild:leftChild;
			if(sibling==null) return allBlack();
			
			Color outsideColor = xIsLeftChild? sibling.getRightColor():sibling.getLeftColor();
			
			if(sibling.getRightColor()==Color.BLACK && sibling.getLeftColor()==Color.BLACK) return allBlack();
			if(outsideColor==Color.RED) return siblingOutsideRedRestBlack(xIsLeftChild);
			return siblingInsideRedRestBlack(xIsLeftChild);
		}
		
		//assumes one child is red, called on x when next x is red
		public BinaryNode handleNextSiblingIsRedNextIsBlack() {
			return getLeftColor()==Color.RED? singleRightRotation() : singleLeftRotation();
		}
		
		private BinaryNode allBlack() {
			if(leftChild!=null)leftChild.color = Color.RED;
			if(rightChild!=null)rightChild.color = Color.RED;
			color = Color.BLACK;
			return this;
		}
		
		private BinaryNode siblingInsideRedRestBlack(boolean left) {
			BinaryNode temp;
			if(left) {
				temp = rightLeftRotation();
				temp.leftChild.color = Color.BLACK;
				temp.rightChild.color = Color.BLACK;
				temp.leftChild.leftChild.color = Color.RED;
			}else {
				temp = leftRightRotation();
				temp.rightChild.color = Color.BLACK;
				temp.leftChild.color = Color.BLACK;
				temp.rightChild.rightChild.color = Color.RED;
			}			
			temp.color = Color.RED;
			return temp;
		}
		private BinaryNode siblingOutsideRedRestBlack(boolean left) {
			BinaryNode temp;
			color = Color.BLACK;
			if(left) {
				temp = singleLeftRotation();
				temp.leftChild.color = Color.BLACK;
				temp.rightChild.color = Color.BLACK;
				temp.leftChild.leftChild.color = Color.RED;
			}else {
				temp = singleRightRotation();
				temp.rightChild.color = Color.BLACK;
				temp.leftChild.color = Color.BLACK;
				temp.rightChild.rightChild.color = Color.BLACK;
			}
			temp.color = Color.RED;
			return temp;
		}
/**
 * Returns either this node if no balances need to be made, or the root node of the balanced subtree
 * previously rooted at this node
 * @return balanced subtree rooted at this node
 */
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
			color = Color.RED;
			temp.color = Color.BLACK;
			
			balanceCount++;
			return temp;
		}
		private BinaryNode singleLeftRotation() {
			BinaryNode temp = rightChild;
			rightChild = temp.leftChild;
			temp.leftChild = this;
			color = Color.RED;
			temp.color = Color.BLACK;
			
			
			balanceCount++;
			return temp;
		}
		private BinaryNode removeYourself(Trit t) {
			if(leftChild==null&&rightChild==null) {
				if(this==root) root = null;
				return null;
			}
			if(leftChild==null) {
				rightChild.color = Color.BLACK;
				if(this==root)root = rightChild;
				return rightChild;
			}
			if(rightChild==null) {
				leftChild.color = Color.BLACK;
				if(this==root) root = leftChild;
				return leftChild;
			}
			if(color==Color.RED) t.setValue(1);
			if(color==Color.BLACK) t.setValue(2);
			return this;
			//handle two children deletion node later
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
		private void setChild(BinaryNode node) {
			if(element==null) {
				root = node;
				return;
			}
			int c = node.element.compareTo(element);
			if(c==1) rightChild = node;
			if(c==-1) leftChild = node;
			
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

		public BinaryNode getLeftChild() {
			return leftChild;
		}
		public BinaryNode getRightChild() {
			return rightChild;
		}
		public T getElement(){
			return element;
		}
		private Color getLeftColor() {
			return leftChild==null?Color.BLACK:leftChild.getColor();
		}
		private Color getRightColor() {
			return rightChild==null?Color.BLACK:rightChild.getColor();
		}
		public Color getColor() {
			return color;
		}
		public int getHeight() {
			int leftHeight = leftChild==null? -1 : leftChild.getHeight();
			int rightHeight = rightChild==null? -1 : rightChild.getHeight();
			return 1 + Math.max(leftHeight, rightHeight);
		}
		public String toString() {
			ArrayList<T> temp = new ArrayList<>();
			toArrayList(temp);
			return temp.toString();
		}
		
	
}
	public class PreOrderIterator implements Iterator<RedBlackTree.BinaryNode> {
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

		public BinaryNode next() throws NoSuchElementException, ConcurrentModificationException {
			if(modCount!=modCountAtConstruction) throw new ConcurrentModificationException();
			if(!hasNext()) throw new NoSuchElementException();
			lastNode = stack.pop();
			if(lastNode.rightChild!=null) stack.push(lastNode.rightChild);
			if(lastNode.leftChild!=null) stack.push(lastNode.leftChild);
			return lastNode;
		}
//		public void remove() throws IllegalStateException {
//			if(lastNode==null) throw new IllegalStateException();
//			if(lastNode.rightChild!=null && lastNode.leftChild!=null) {
//				stack.pop();
//				stack.pop();
//				stack.push(lastNode);
//			}
//			RedBlackTree.this.remove(lastNode.element);
//			lastNode = null;
//			modCountAtConstruction++;	
//		}
	}
	public class MyBoolean {
		private boolean value;
		
		public MyBoolean(boolean b) {
			value = b;
		}
		public MyBoolean() {
			value = true;
		}
		public boolean getValue() {
			return value;
		}
		public void setTrue() {
			value = true;
		}
		public void setFalse() {
			value = false;
		}
	}
	public class Trit {
		private int value;
		public Trit (int i) throws IllegalArgumentException {
			if(Math.abs(i)>2) throw new IllegalArgumentException();
			value = i;
		}
		public int getValue() {
			return value;
		}
		public void setValue(int i) throws IllegalArgumentException {
			if(Math.abs(i)>2) throw new IllegalArgumentException();
			value = i;
		}
	}
	

	
}
