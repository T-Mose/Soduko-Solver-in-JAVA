import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SodukoSolver {
    // https://www.sudoku-solutions.com/index.php?section=sudoku9by9 - To auto solve
    // any game and some hints
    public static String gameString = "010020300002003040080000006004700030000600008070098000300004090000800104006000000";
    public static Matrix matrix;
    // Hardcoded game - string, add option to do it with the runnning, ie args[0]

    public static void main(String[] args) {
        matrix = new Matrix();
        Solve();
        matrix.displayMatrix();
    }

    public static void Solve() {
        int i = 0;
        while (i++ < 20 && matrix.matrixToString().length() > 81) {
            checkPossible();
            checkSingelValues(true); // Checks rows
            checkPossible();
            checkSingelValues(false); // Checks columns
            // Do square check
        }
        if (i > 20) 
            System.out.println("The string could not be solved, heres so far: ");
        else    
            System.out.println("Solved, solution string: " + matrix.matrixToString());
    }

    public static void checkSingelValues(boolean row) {
        Map<Integer, Integer> frequency;
        ArrayList<Integer> nums;
        for (int i = 0; i < 9; i++) {
            frequency = new HashMap<>();
            for (int j = 1; j <= 9; j++) {
                frequency.put(j, 0);
            }

            for (int j = 0; j < 9; j++) {
                nums = row ? new ArrayList<>(matrix.getCell(i, j).getPossibleVals())
                        : new ArrayList<>(matrix.getCell(j, i).getPossibleVals());
                if (nums.size() > 1) {
                    for (Integer value : nums) {
                        frequency.put(value, frequency.get(value) + 1);
                    }
                }
            }

            for (Map.Entry<Integer, Integer> entry : frequency.entrySet()) {
                if (entry.getValue() == 1) {
                    nums = new ArrayList<>();
                    nums.add(entry.getKey());
                    for (int j = 0; j < 9; j++) {
                        if (row && matrix.getCell(i, j).getPossibleVals().contains(entry.getKey())) {
                            matrix.getCell(i, j).setPossibleVals(nums);
                        } else if (!row && matrix.getCell(j, i).getPossibleVals().contains(entry.getKey())) {
                            matrix.getCell(j, i).setPossibleVals(nums);
                        }
                    }
                }
            }
        }
    }

    public static void checkPossible() {
        int len = matrix.matrixToString().length();
        while (true) { // Since the updated matrix might lead to changes that in turn have to be updated
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
            }   else {
                len = matrix.matrixToString().length();
            }
        }
    }
}

class Matrix {
    public Cell[][] matrix;
    public static final boolean SIMPLE = true;
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
                System.out.println("-----------------");
            }
        }
    }

    public static int toThreeByThree(int j) {
        if (j < 3)
            j = 0;
        else if (j < 6)
            j = 3;
        else // i < 9
            j = 6;
        return j;
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

                    // Add cells in the same 3x3 square
                    int startRow = toThreeByThree(i);
                    int startCol = toThreeByThree(j);
                    for (int row = startRow; row < startRow + 3; row++) {
                        for (int col = startCol; col < startCol + 3; col++) {
                            if (row != i || col != j) {
                                matrix[i][j].addAdjacentCell(matrix[row][col]);
                            }
                        }
                    } // This is extreamly inefficient, but probelby works
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

    public int getX() {
        return xCoordinate;
    }

    public int getY() {
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
