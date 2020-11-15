import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class Sudoku {

	private static int boardSize = 0;
	private static int partitionSize = 0;

	public static void main(String[] args) throws IOException{
		String filename = args[0];
		File inputFile = new File(filename);
		Scanner input = null;
		int[][] vals = null;

		int temp = 0;
    	int count = 0;
    	
	    try {
			input = new Scanner(inputFile);
			temp = input.nextInt();
			boardSize = temp;
			partitionSize = (int) Math.sqrt(boardSize);
			System.out.println("Boardsize: " + temp + "x" + temp);
			vals = new int[boardSize][boardSize];		
			int i = 0;
	    	int j = 0;
	    	while (input.hasNext()){
	    		temp = input.nextInt();
	    		count++;
	    		System.out.printf("%3d", temp);
	    		vals[i][j] = temp;
				j++;
				if (j == boardSize) {
					j = 0;
					i++;
					System.out.println();
				}
				if (j == boardSize) {
					break;
				}
	    	}
			System.out.println("\nOutput\n");
	       	input.close();
	    } catch (FileNotFoundException exception) {
	    	System.out.println("Input file not found: " + filename);
	    }
	    if (count != boardSize*boardSize) throw new RuntimeException("Incorrect number of inputs.");
	    
	    //populates an initialConditions 
	    ArrayList<Integer> initialConditions = new ArrayList<>();
		int rc = 0;
		for(int[] row : vals) {
			for(int cur : row) {
				if(cur!=0) {
					initialConditions.add(rc+cur-1);
				}
				rc+=vals[0].length;
			}
		}
		
		DancingLinks d = new DancingLinks(createECMForSudoku(vals), initialConditions);
		ArrayList<Integer> solution = new ArrayList<>();
		HashMap<Integer, Integer> translatedSols = new HashMap<>();
		
		
	
		boolean solved = d.solve(solution);

		for(int t : solution) {
			translatedSols.put(t/vals[0].length, t%vals[0].length +1);
		}
		
		int counter = 0;
		for(int r = 0; r<vals.length; r++) {
			for(int c = 0; c<vals[0].length; c++) {
				if(translatedSols.containsKey(counter)) vals[r][c] = translatedSols.get(counter);
				counter++;
			}
		}
		// Output
		if (!solved) {
			System.out.println("No solution found.");
			return;
		}
		System.out.println("\nOutput\n");
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				System.out.printf("%3d", vals[i][j]);
			}
			System.out.println();
		}
	}
	
	//This function converts any int[][] sudoku board into an exact cover matrix problem with each row choice
	//representing choosing a possible input (1 through 9) for a possible empty cell
	
	public static boolean[][] createECMForSudoku(int[][] board) {
		int domain = board.length;
		int numberOfCells = domain*domain;
		boolean[][] ecm = new boolean[domain*numberOfCells][numberOfCells*4];
		for(int r = 0; r<ecm.length; r++) {
			int cellNumber = r/domain;
			int boxSideLength = (int)Math.sqrt(domain);
			int sizeOfRowOfBoxes = numberOfCells/boxSideLength;
			int rr = r%domain + (cellNumber/domain)*domain;
			int colr = r%numberOfCells;
			int br = r%domain + ((cellNumber/boxSideLength) % boxSideLength)*domain + (cellNumber/sizeOfRowOfBoxes)*sizeOfRowOfBoxes;
			ecm[r][cellNumber] = true;
			ecm[r][rr+numberOfCells] = true;
			ecm[r][colr+2*numberOfCells] = true;
			ecm[r][br+3*numberOfCells] = true;
		}	
		return ecm;
	}
	
	//mainly used for testing purposes
	public static void writeEcmToFile(boolean[][] ecm, String fileName) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter("fileName"));
		for(int row = 0; row<ecm.length; row++) {
			for(int col = 0; col<ecm[0].length; col++) {
				writer.write(ecm[row][col]?"1":"_");
				if((col+1)%((ecm.length)/4)==0) {
					writer.write(" ");
				}
			}
			writer.newLine();
		}
		writer.close();
	}
	
	//everything after this line pertains to the brute force backTrackSearch which works only for 9x9 sudoku
	private static boolean backTrackSearch(int[][] board) {
		return recBackTrackSearch(board, 0, 0);
	}
	private static boolean recBackTrackSearch(int[][] board, int r, int c) {
		if(r>=board.length)return true;
		int cur = board[r][c];
		int cNext = c;
		int rNext = r;
		if(c+1 == board[0].length) {
			cNext = 0;
			rNext++;
		}else {
			cNext++;
		}
		if(cur!=0) {
			return recBackTrackSearch(board, rNext, cNext);
		}
		for(int i = 1; i<=boardSize; i++) {
			if(validateInstantiation(board, r, c, i) ) {
				board[r][c] = i;
				if(recBackTrackSearch(board, rNext, cNext))return true;
				else board[r][c] = 0;
			}
			
		}
		return false;
	}
	private static boolean validateInstantiation(int[][] board, int r, int c, int assignment) {
		for(int cur : board[r]) {
			if(assignment==cur)return false;
		}
		for(int[] row : board) {
			if(assignment==row[c])return false;
		}
		int rBoxPos = r % partitionSize;
		int cBoxPos = c % partitionSize;
		int rTopLeftBoxPos = r-rBoxPos;
		int cTopLeftBoxPos = c-cBoxPos;
		for(int i = 0; i<partitionSize; i++) {
			if(i==rBoxPos)continue;
			for(int j = 0; j<partitionSize; j++) {
				if(j==cBoxPos)continue;
				if(board[rTopLeftBoxPos+i][cTopLeftBoxPos+j]==assignment)return false;
			}
		}
		return true;
		
	}
	public static boolean solve(int[][] board){
		return backTrackSearch(board);
	}
		
}