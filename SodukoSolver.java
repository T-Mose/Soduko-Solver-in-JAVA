import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SodukoSolver {
    // https://www.sudoku-solutions.com/index.php?section=sudoku9by9 - To auto solve
    public static String gameString = "010020300004005060070000008006900070000100002030048000500006040000800106008000000";
    public static Matrix matrix;

    public static void main(String[] args) {
        matrix = new Matrix();
        Solve();
        matrix.displayMatrix();
    }

    public static void Solve() {
        int i = 0;
        while (i++ < 20 && matrix.matrixToString().length() > 81) {
            checkPossible();
            checkSingelValues(0); // Checks rows
            checkPossible();
            checkSingelValues(1); // Checks columns
            checkPossible();
            checkSingelValues(2); // Checks 3x3

            // Do pointing pair check
            // Bruteforce
        }
        if (i > 20)
            System.out.println("The string could not be solved, heres so far: ");
        else
            System.out.println("Solved, solution string: " + matrix.matrixToString() + " it took iterations: " + i);
    }

    public static void checkSingelValues(int row) { // 0-row, 1-col, 2-3x3
        Map<Integer, Integer> frequency;
        ArrayList<Integer> nums;
        for (int i = 0; i < 9; i++) {
            frequency = new HashMap<>();
            for (int j = 1; j <= 9; j++) {
                frequency.put(j, 0);
            }

            if (row < 2) {
                for (int j = 0; j < 9; j++) {
                    nums = row == 0 ? new ArrayList<>(matrix.getCell(i, j).getPossibleVals())
                            : new ArrayList<>(matrix.getCell(j, i).getPossibleVals());
                    if (nums.size() > 1) {
                        for (Integer value : nums) {
                            frequency.put(value, frequency.get(value) + 1);
                        }
                    }
                }
            } else if (i % 3 == 0) { // 0,3,6
                for (int j = 0; j < 9; j += 3) { // 0-2, 3-5, 6-8
                    for (int k = i; k < i + 3; k++) {
                        for (int l = j; l < j + 3; l++) {
                            nums = new ArrayList<>(matrix.getCell(k, l).getPossibleVals());
                            if (nums.size() > 1) {
                                for (Integer value : nums) {
                                    frequency.put(value, frequency.get(value) + 1);
                                }
                            }
                        }
                    }
                }
            }

            for (Map.Entry<Integer, Integer> entry : frequency.entrySet()) {
                if (entry.getValue() == 1) {
                    nums = new ArrayList<>();
                    nums.add(entry.getKey());
                    if (row < 2) { // Columns and rows
                        for (int j = 0; j < 9; j++) {
                            if (row == 0 && matrix.getCell(i, j).getPossibleVals().contains(entry.getKey())) {
                                matrix.getCell(i, j).setPossibleVals(nums);
                            } else if (row == 1 && matrix.getCell(j, i).getPossibleVals().contains(entry.getKey())) {
                                matrix.getCell(j, i).setPossibleVals(nums);
                            }
                        }
                    } else if (row == 2 && i % 3 == 0) { // 3x3 - This iteration not requred for all i
                        for (int j = 0; j < 9; j += 3) { // 0-2, 3-5, 6-8
                            for (int k = i; k < i + 3; k++) {
                                for (int l = j; l < j + 3; l++) {
                                    if (matrix.getCell(k, l).getPossibleVals().contains(entry.getKey())) {
                                        matrix.getCell(k, l).setPossibleVals(nums);
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    public static void checkPossible() {
        int len = matrix.matrixToString().length();
        while (true) { // Since the updated matrix might lead to changes that in turn have to be
                       // updated
            Cell cell;
            ArrayList<Integer> vals;
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    cell = matrix.getCell(i, j);
                    vals = new ArrayList<>(cell.getPossibleVals());
                    if (vals.size() > 1) {
                        for (Cell adjacent : cell.getAdjacentCells()) {
                            if (adjacent.getValsInString().length() == 1) {
                                vals.remove(adjacent.getPossibleVals().get(0));
                            }
                        }
                    }
                    cell.setPossibleVals(vals);
                }
            }
            if (matrix.matrixToString().length() == len) {
                break;
            } else {
                len = matrix.matrixToString().length();
            }
        }
    }
}

class Matrix {
    public Cell[][] matrix;
    public static final boolean SIMPLE = false;
    public static final String ANSI_GREEN = "\u001B[32m"; // Display it in green
    public static final String ANSI_RESET = "\u001B[0m"; // Back to white

    public Matrix() {
        matrix = new Cell[9][9];
        setCells();
        setAdjacent();
    }

    public String matrixToString() {
        String all = "";
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                all += matrix[i][j].getValsInString();
            }
        }
        return all;
    }

    public Cell[][] getMatrix() {
        return matrix;
    }

    public Cell getCell(int i, int j) {
        return matrix[i][j];
    }

    public void setCells() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                matrix[i][j] = new Cell(i, j, Character.getNumericValue(SodukoSolver.gameString.charAt(i * 9 + j)));
            }
        }
    }

    public void displayMatrix() {
        System.out.println();
        String line = "";
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                line = SIMPLE && matrix[i][j].getValsInString().length() > 1 ? "0" : matrix[i][j].getValsInString();
                line += ((j + 1) % 3 == 0 && j + 1 != 9) ? "|" : " ";
                if (line.length() == 2 && line.charAt(0) != '0') {
                    System.out.print(ANSI_GREEN + line.charAt(0) + ANSI_RESET);
                    line = line.substring(1); // If solved, its green
                }
                System.out.print(line);
            }
            System.out.println();
            if ((i + 1) % 3 == 0 && i + 1 != 9) {
                System.out.println("-----------------"); // Formatting
            }
        }
    }

    public void setAdjacent() {
        // In the same square, row and column
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                for (int k = 0; k < 9; k++) {
                    // Add cells in the same row and column
                    if (i != k)
                        matrix[i][j].addAdjacentCell(matrix[k][j]); // Column
                    if (j != k)
                        matrix[i][j].addAdjacentCell(matrix[i][k]); // Row
                }
            }
        }
        for (int squareRow = 0; squareRow < 9; squareRow += 3) {
            for (int squareCol = 0; squareCol < 9; squareCol += 3) {
                addCellsInSquare(squareRow, squareCol);
            }
        }
    }

    private void addCellsInSquare(int startRow, int startCol) {
        for (int row = startRow; row < startRow + 3; row++) {
            for (int col = startCol; col < startCol + 3; col++) {
                for (int adjRow = startRow; adjRow < startRow + 3; adjRow++) {
                    for (int adjCol = startCol; adjCol < startCol + 3; adjCol++) {
                        if (row != adjRow || col != adjCol) {
                            matrix[row][col].addAdjacentCell(matrix[adjRow][adjCol]);
                        }
                    }
                }
            }
        }
    }
}

class Cell {
    public ArrayList<Integer> possibleVals = new ArrayList<>();
    public ArrayList<Cell> adjacentCells = new ArrayList<>();
    public int xCoordinate;
    public int yCoordinate;

    public Cell(int x, int y, int valPosition) {
        this.xCoordinate = x;
        this.yCoordinate = y;

        if (valPosition != 0) {
            possibleVals.add(valPosition);
        } else {
            for (int i = 1; i <= 9; i++) {
                possibleVals.add(i);
            }
        }
    }

    public String getName() {
        return "Cell" + String.valueOf(xCoordinate) + "_" + String.valueOf(yCoordinate);
    }

    public void setPossibleVals(ArrayList<Integer> possible) {
        this.possibleVals = new ArrayList<>(possible);
    }

    public ArrayList<Integer> getPossibleVals() {
        return this.possibleVals;
    }

    public int getX() { // Redundant
        return xCoordinate;
    }

    public int getY() { // Redundant
        return yCoordinate;
    }

    public void addAdjacentCell(Cell cell) {
        if (this != cell && !this.getAdjacentCells().contains(cell))
            adjacentCells.add(cell);
    }

    public ArrayList<Cell> getAdjacentCells() {
        return this.adjacentCells;
    }

    public String getValsInString() { // Turns the Cell possible values into a string that can be displayed
        String inLetters = "";
        for (int ints : possibleVals) {
            inLetters += String.valueOf(ints);
        }
        return inLetters;
    }
}
// To implement - Checking 3x3, pointing pairs, brute force
