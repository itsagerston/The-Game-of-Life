import java.util.Scanner;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;
import Life.Graphics.*;


/**
 * AGLifeGame simulates the life & death of bacteria
 *
 * @author Aaron Gerston
 */
public class AGLifeGame {
	
	LifeGraphics lg;
	char[][] GameBoard;
	double isFilled; // if Random, fills initial gameboard
	double totalInitialCells;
	int boardHeight;
	int boardLength;
	Scanner sc;
	boolean isClassic; // Classic (true) or Doughnut (false)
	char[][] tempBoard;
	int neighborCount;
	double density;
	boolean isPause; // pauseSim (true) or enter-to-play (false)
	int sameTimes; // number of times the map is the same. if sameTimes reaches 3, game ends
	
	/** Constructor */
	public AGLifeGame() {
		sc = new Scanner(System.in);
		neighborCount = 0;
	}
	
	/**
	 * Choose map file or random assignment
	 */
	public void mapOrRandomAssign() {
		System.out.println("Map (m) or Random assignment (r)?");
		String MorR = sc.next();
		sc.nextLine();
		while (!(MorR.equals("m") || MorR.equals("r"))) {
			System.out.println("Invalid.");
			MorR = sc.next();
			sc.nextLine();
		}
		if (MorR.equals("m")) {
			mapBoard();
		}
		else if (MorR.equals("r")) {
			randomBoard();
		}
	}
	
	/** 
	 * Choose Classic or Doughnut mode
	 *
	 * @return boolean isClassic - dertermines Classic or Doughnut mode
	 */
	public boolean mode() {
		System.out.println("Mode? Classic (c) or Doughnut (d)");
		String CorD = sc.next();
		sc.nextLine();
		while (!(CorD.equals("c") || CorD.equals("d"))) {
			System.out.println("Invalid.");
			CorD = sc.next();
			sc.nextLine();
		}
		if (CorD.equals("c")) {
			isClassic = true;
		}
		else if (CorD.equals("d")) {
			isClassic = false;
		}
		return isClassic;
	}
	
	/**
	 * Choose pauseSim or enter-to-pause mode
	 *
	 * @return true for pauseSim, false if enter-to-play
	 */
	public void pauseOrEnter() {
		System.out.println("Pause (p) or Enter (e)?");
		String PorE = sc.next();
		sc.nextLine();
		while (!(PorE.equals("p") || PorE.equals("e"))) {
			System.out.println("Invalid.");
			PorE = sc.next();
			sc.nextLine();
		}
		if (PorE.equals("p")) {
			isPause = true;
		}
		else {
			isPause = false;
		}
	}
	
	/**
	 * Pause - pauseSim or enter-to-play
	 * Determined in method pauseOrEnter
	 */
	public void pause() {
		if (isPause == true) {
			LifeGraphics.pauseSim();
		}
		else {
			System.out.println("Press enter when ready to move on to next round");
			String pause = sc.nextLine();
		}
	}
	
	/**
	 * Fills initial game board from file
	 */
	public void mapBoard() {
		try {
			System.out.println("Please input the filepath of the file you would like to read from.");
			BufferedReader br = new BufferedReader(new FileReader(sc.next()));
			sc.nextLine();
			boardHeight = Integer.parseInt(br.readLine());
			boardLength = Integer.parseInt(br.readLine());
			GameBoard = new char[boardHeight][boardLength];
			for (int height = 0; height<boardHeight; height++) {
				for (int width = 0; width<boardLength; width++) {
					GameBoard[height][width]=(char)br.read();
				}
				br.readLine();
			}
			br.close();
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		lg = new LifeGraphics(boardHeight, boardLength);
		lg.updateGrid(GameBoard);
	}
	
	/**
	 * Calculates density from user-entered data
	 * Used in Random map initial fill
	 *
	 * @return double density - decimal representation of percent cells alive
	 */
	public double density() {
		try {
			System.out.println("Density % (decimal between 0-1, exclusive)");
			density = Double.parseDouble(sc.next());
			sc.nextLine();
			while (density >= 1 || density <= 0) {
				System.out.println("Density must be given between 0 and 1, exclusively.");
				density = Double.parseDouble(sc.next());
				sc.nextLine();
			}
		}
		catch (Exception e) {
			System.out.println("Invalid.");
			density();
		}
		return density;
	}
	
	/**
	 * Fills initial game board using user-entered data (height, width, density)
	 */
	public void randomBoard() {
		try {
			System.out.println("Gameboard length?");
			boardLength = Integer.parseInt(sc.next());
			sc.nextLine();
			System.out.println("Gameboard height?");
			boardHeight = Integer.parseInt(sc.next());
			sc.nextLine();
			GameBoard = new char[boardHeight][boardLength];
		}
		catch (Exception e) {
			System.out.println("Invalid.");
			randomBoard();
		}
		density();
		totalInitialCells = boardHeight*boardLength*density;
		double cellsLeft = totalInitialCells;
		while (cellsLeft >= .5) {
			cellsLeft = totalInitialCells;
			for (int i=0; i<GameBoard.length; ++i) {
				for (int j=0; j<GameBoard[i].length; ++j) {
					isFilled = Math.random();
					if (isFilled <= density && cellsLeft >= .5) {
						GameBoard[i][j]='X';
						--cellsLeft;
					}
					else {
						GameBoard[i][j]='-';
					}
				}
			}
		}
		lg = new LifeGraphics(boardHeight, boardLength);
		lg.updateGrid(GameBoard);
	}
	
	/**
	 * Runs simulation.
	 *
	 * If the board is the same 3 rounds in a row, Quit.
	 * If the board is determined to be empty, Quit.
	 * Otherwise, pause and play.
	 */
	public void play() {
		while (sameTimes<2) {
			pause();
			update();
		}
		if (sameTimes==2) {
			System.out.println("Equilibrium reached. Press enter to quit.");
			String end = sc.nextLine();
			System.exit(0);
		}
		if (sameTimes >= 100) {
			System.out.println("All cells have died. Press enter to quit.");
			String end = sc.nextLine();
			System.exit(0);
		}
		
	}
	
	/**
	 * Calculates the number of neighbors for a given cell in Classic mode
	 *
	 * @return int neighborcount - number of neighbors for a given cell
	 */
	public int neighborsClassic(char[][] board, int height, int width) {
		int neighborCount = 0;
		for (int h = height-1; h <= height+1; h++) {
			for (int w = width-1; w <= width+1; w++) {
				if (h >= 0 && h<boardHeight && w >= 0 && w<boardLength && !(h == height && w == width)) {
					if (board[h][w] == 'X') {
						neighborCount++;
					}
				}
			}
		}
		return neighborCount;
	}
	
	/** Calculates the number of neighbors for a given cell is Doughnut mode
	 *
	 * @return int neighborcount - number of neighbors for a given cell
	 */
	public int neighborsDoughnut(char[][] board, int height, int width) {
		int neighborCount = 0;
		for (int h = height-1; h <= height+1; h++) {
			for (int w = width-1; w <= width+1; w++) {
				if (!(h >= 0 && h < boardHeight && w >= 0 && w < boardLength && !(h == height && w == width))) {
					if (h < 0 && w >= 0 && w < boardLength) {
						if (GameBoard[boardHeight-1][w] == 'X') {
							neighborCount++;
						}
					}
					else if (h >= boardHeight && w >= 0 && w < boardLength) {
						if (GameBoard[0][w] == 'X') {
							neighborCount++;
						}
					}
					else if (w < 0 && h >= 0 && h < boardHeight) {
						if (GameBoard[h][boardLength-1] == 'X') {
							neighborCount++;
						}
					}
					else if (w >= boardLength && h >= 0 && h < boardHeight) {
						if (GameBoard[h][0] == 'X') {
							neighborCount++;
						}
					}
					else if (w < 0 && h < 0) {
						if (GameBoard[boardHeight-1][boardLength-1] == 'X') {
							neighborCount++;
						}
					}
					else if (w < 0 && h >= boardHeight) {
						if (GameBoard[0][boardLength-1] == 'X') {
							neighborCount++;
						}
					}
					else if (h < 0 && w >= boardLength) {
						if (GameBoard[boardHeight-1][0] == 'X') {
							neighborCount++;
						}
					}
					else if (h >= boardHeight && w >= boardLength) {
						if (GameBoard[0][0] == 'X') {
							neighborCount++;
						}
					}
				}
				else {
					if (board[h][w] == 'X' && h != boardHeight && w != boardLength) {
						neighborCount++;
					}
				}
			}
		}
		return neighborCount;
	}
	
	/**
	 * Creates a temporary gameboard (tempBoard), identical to the original (GameBoard)
	 * Uses neighborsClassic or neighborsDoughnut  to calculate # neighbors for each cell
	 * Determines fate of each cell of the gameboard
	 */
	public void update() {
		char[][] tempBoard = new char[boardHeight][boardLength];
		for (int i = 0; i<boardHeight; i++) {
			for (int j = 0; j<boardLength; j++) {
				tempBoard[i][j] = GameBoard[i][j];
			}
		}
		for (int height = 0; height<boardHeight; height++) {
			for (int width = 0; width<boardLength; width++) {
				int neighbors = 0;
				if (isClassic == true) { // Classic mode
					neighbors = neighborsClassic(GameBoard, height, width);
				}
				else { // Doughnut mode
					neighbors = neighborsDoughnut(GameBoard, height, width);
				}
				if (neighbors >= 4) {
					tempBoard[height][width]='-';
				}
				else if (neighbors == 3) {
					tempBoard[height][width]='X';
				}
				else if (neighbors <= 1) {
					tempBoard[height][width]='-';
				}
			}
		}
		
		if (isEmpty(tempBoard)) {
			sameTimes+=100;
		}
		
		if (isSameBoard(GameBoard, tempBoard)) {
			sameTimes+=1;
		}
		
		lg.updateGrid(tempBoard);
		
		GameBoard = tempBoard;
		
		for (int i = 0; i<boardHeight; i++) {
			for (int j = 0; j<boardLength; j++) {
				GameBoard[i][j] = tempBoard[i][j];
			}
		}
	}
	
	/**
	 * Checks if tempBoard and GameBoard are the same
	 *
	 * @return true if boards are identical, false otherwise
	 */
	public boolean isSameBoard(char[][] board1, char[][] board2) {
		int sameCount = 0;
		for (int i = 0; i<boardHeight; i++) {
			for (int j = 0; j<boardLength; j++) {
				if (board1[i][j] == board2[i][j]) {
					sameCount+=1;
				}
			}
		}
		if (sameCount == boardHeight*boardLength) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Checks if tempBoard is empty
	 *
	 * @return true if board is empty, false otherwise
	 */
	public boolean isEmpty(char[][] board) {
		int isEmpty = 0;
		for (int i = 0; i<boardHeight; i++) {
			for (int j = 0; j < boardLength; j++) {
				if (board[i][j] == 'X') {
					isEmpty++;
				}
			}
		}
		if (isEmpty == 0) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Main method...
	 *
	 * @param String[] args - commandline arguments
	 */
	public static void main(String[] args) {
		System.out.println("\nWelcome to the game of Life.");
		AGLifeGame newGame = new AGLifeGame();
		newGame.pauseOrEnter();
		newGame.mode();
		newGame.mapOrRandomAssign();
		newGame.play();
	}
}