import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;


/**
 * @author bhangit
 *
 */
public class DancingLinks{
	private ListHeader head;	
	private int domain;
	private int numberOfCells;
	private boolean[][] ecm;
	private ArrayList<ListHeader> columns;
	private ArrayList<DancingNode> firstNodes;
	private int c = 0;
	private int c1 = 0;
	private int c2 = 0;
	private int c3 = 0;
	public DancingLinks(boolean[][] ecm) {
		head = new ListHeader();
		head.left = head;
		head.right = head;
		this.ecm = ecm;
		numberOfCells = ecm[0].length/4;
		domain = (int) Math.sqrt(numberOfCells);
		columns = new ArrayList<>();
		firstNodes = new ArrayList<>();
		createColumns();
		createNodes();
	}
	public DancingLinks(boolean[][] ecm, ArrayList<Integer> rowsToCover) {
		this(ecm);
		initialCover(rowsToCover);
	}
	

	private void createColumns() {
		for(int i = 0; i<ecm[0].length; i++) {
			ListHeader newColumn = new ListHeader();
			columns.add(newColumn);
			newColumn.up = newColumn.down = newColumn;
			newColumn.left = head.left;
			newColumn.right = head;
			head.left.right = newColumn;
			head.left = newColumn;
		}
	}
	private void createNodes() {
		
		for(int r = 0; r<ecm.length; r++) {
			boolean[] rowArray = ecm[r];
			DancingNode first = null;
			for(int c = 0; c<ecm[0].length; c++) {
				ListHeader columnHeader = columns.get(c);
				if(rowArray[c]) {
					DancingNode d = new DancingNode(columnHeader);
					d.up = columnHeader.up;
					d.down = columnHeader;
					columnHeader.up.down = d;
					columnHeader.up = d;
					if(first==null) {
						first = d;
						d.right = d;
						d.left = d;
						firstNodes.add(first);
					}else {
						d.right = first;
						d.left = first.left;
						first.left.right = d;
						first.left = d;
					}
					d.rowIndex = r;
					columnHeader.size++;
				}
			}
		}
	}
	private void initialCover(ArrayList<Integer> rowsToCover) {
		for(int i : rowsToCover) {
			DancingNode first = firstNodes.get(i);
			first.column.cover();
			DancingNode cur = first.right;
			while(cur!=first) {
				cur.column.cover();
				cur = cur.right;
			}
		}
	}
	boolean solve(ArrayList<Integer> sol) {
		boolean b = algorithmXRec(sol);
		System.out.println("algorithmX was called : " + c + " times");
		System.out.println("cover was called " + c1 + " times");
		System.out.println("uncover was called " + c2 + " times");
		System.out.println(c3);
		
		return b;
	}
	private boolean algorithmXRec(ArrayList<Integer> sols){
		c++;
		if(head.right == head) {
			return true;
		}
		DancingNode cur;
		ListHeader chosenColumn = choose();
		chosenColumn.cover();
		DancingNode chosenNode = chosenColumn.down;
		while(chosenNode!=chosenColumn) {
			
			//cover the rows
			cur = chosenNode.right;
			while(cur!=chosenNode) {
				cur.column.cover();
				cur = cur.right;
			}
			
			if(algorithmXRec(sols)) {
				sols.add(cur.rowIndex);
				return true;
			}
			
			//uncover the rows
			cur = chosenNode.left;
			while(cur!=chosenNode) {
				cur.column.uncover();
				cur = cur.left;
			}
			
			chosenNode = chosenNode.down;
		}
		chosenColumn.uncover();
		return false;
		
	}
	private ListHeader choose() {
		ListHeader col = (ListHeader) head.right;
		ListHeader smallest = col;
		while(col!=head) {
			if(col.size<smallest.size)smallest=col;
			col = (ListHeader) col.right;
		}
		return smallest;
	}
	private class DancingNode {
		DancingNode up;
		DancingNode down;
		DancingNode left;
		DancingNode right;
		ListHeader column;
		int rowIndex;
		private DancingNode(ListHeader column) {
			this.column = column;
		}
	}
	class ListHeader extends DancingNode {
		int size;
		
		private ListHeader() {
			super(head);
			this.size = 0;
			this.down = this;
			this.up = this;
		}
		private void cover() {
			c1++;
			right.left = left;
			left.right = right;
			DancingNode node = this.down;
			DancingNode cur;
			while(node!=this) {
				cur = node.right;
				while(cur!=node) {
					cur.down.up = cur.up;
					cur.up.down = cur.down;
					cur.column.size--;
					cur = cur.right;
				}
				node = node.down;
			}
		}
		private void uncover() {
			c2++;
			DancingNode node = up;
			DancingNode cur;
			while(node!=this) {
				cur = node.left;
				while(cur!=node) {
					cur.column.size++;
					cur.down.up = cur;
					cur.up.down = cur;
					cur = cur.left;
				}
				node = node.up;
			}
			right.left = this;
			left.right = this;
		}
	}
}