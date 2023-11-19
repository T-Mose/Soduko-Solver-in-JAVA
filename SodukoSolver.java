import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
// Naked pairs for rows and columns are almoste done, just needs some debugging

/**
 * @author Theodor Malmgren
 *         Solves upp to hard soduko puzzles. It does this thorugh logic, not
 *         bruteforce
 *         though bruteforce will be implemented as a backup just so that every
 *         puzzle
 *         has a solution (that can be solved). The basic premise is to solve
 *         the puzzle like
 *         a human would. First palce all the possible values in each cell. This
 *         is in the form of an
 *         arraylist on each cell object of the 2d matrix soduko grid. If this
 *         list just has one object
 *         then said cell is solved. This is frequently redone, since everytime
 *         a cell gets solved
 *         all the cells needs to be uppdated with their posssible values.
 *         The solving of the grid is done by checking for "hidden" singels
 *         where only one possible value
 *         exists on one of the axis or 3x3. This can also be widened to hidden
 *         pairs and tripples. Here if two
 *         or three cells, more broadly if x cells have the same x possible
 *         values, all other possible values, of
 *         said cell can be eliminated. Pointed pairs/tripples are also used to
 *         deduce if possible values can be
 *         eliminated from other cells. For example if two only can exist on
 *         adjacent tiles in a row (9) and these
 *         adjacent cells are all in a 3x3, no other such values can exist in
 *         the 3x3. After each time a value has
 *         been set, the game redoes the possible values for all other cells.
 */
public class SodukoSolver {
    // https://www.sudoku-solutions.com/index.php?section=sudoku9by9 - To auto solve
    public static String gameString = "010020300002003040050000006004700050000100008070068000300004060000500104009000000";
    public static Matrix matrix;
    // Things to add
    // Unsure if naked pairs for 3x3, are solved
    // Naked pairs and tripples for rows, columns
    // X, Y and XY wings, whatever those are
    // Swordfirsh, whatever that is
    // Brute force
    // Try to put the checkPossible, to only occur when somthing of importance has
    // changed
    public static final boolean DEBUG = false;

    public static void main(String[] args) {
        if (args.length == 1) {
            gameString = args[0];
        } // Instead of having to change the hard coded gameString, take input
        matrix = new Matrix();
        matrix.displayMatrix();
        Solve();
        matrix.displayMatrix();
    }

    /**
     * Where all the different methods that solves the puzzle is called from
     */
    public static void Solve() {
        int len;
        int maxIterations = 20; // In case something goes wrong
        int currentIteration = 0;
        int previousLen;

        while (currentIteration++ < maxIterations) {
            len = matrix.matrixToString().length();
            checkPossible();

            do { // Start with simple solving methods
                previousLen = len;
                checkSingelValues(0); // Rows
                checkSingelValues(1); // Columns
                checkSingelValues(2); // 3x3 Squares
                len = matrix.matrixToString().length();
            } while (len != previousLen);
            if (len == 81) {
                break; // Puzzle solved
            }
            pointed(1); // Columns
            pointed(0); // Rows
            pointedSquare(true); // rows
            pointedSquare(false); // Columns
            hiddenPairSquare(); // Hidden pairs in 3x3
            hiddenTripplesSquare(); // Should work, but have only tested on two games, does not appear to be super
            hiddenPairsColumn();
            hiddenPairsRow();
            nakedPairsColumns(); // Should work now
            nakedPairsRow(); // Should work but not tested
            // common

            // Add check for hidden pairs in rows/columns
            if (len == matrix.matrixToString().length()) {
                System.out.println("Could not be solved");
                break;
            }
            // brute force requred if it could not be doen
        }

        if (matrix.matrixToString().length() == 81) {
            System.out.println("Sudoku solved! it took this many iterations: " + currentIteration);
            System.out.println("The complete string is: " + matrix.matrixToString());
        } else {
            System.out.println("Sudoku not solved in " + currentIteration + " iterations.");
        }
    }

    public static void Sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public static void nakedPairsRow() {
        String location;
        for (int row = 0; row < 9; row++) {
            // Find cells in this row with only two possible values
            Map<List<Integer>, List<String>> pairsMap = new HashMap<>();
            for (int col = 0; col < 9; col++) {
                Cell cell = matrix.getCell(row, col);
                if (cell.getPossibleVals().size() == 2) {
                    List<Integer> possibleVals = cell.getPossibleVals();
                    location = String.valueOf(row) + "-" + String.valueOf(col);
                    pairsMap.computeIfAbsent(possibleVals, x -> new ArrayList<>()).add(location);
                }
            }
            // matrix.displayMatrix();
            for (Map.Entry<List<Integer>, List<String>> entry : pairsMap.entrySet()) {
                if (entry.getValue().size() == 2) {
                    // A naked double is found
                    String[] first = entry.getValue().get(0).split("-");
                    String[] second = entry.getValue().get(1).split("-");
                    int num1 = entry.getKey().get(0);
                    int num2 = entry.getKey().get(1);

                    for (int i = 0; i < 9; i++) {
                        if (i != Integer.parseInt(first[1]) && i != Integer.parseInt(second[1])) {
                            // These two are the original naked pair
                            Cell cell = matrix.getCell(row, i);
                            if (cell.getPossibleVals().contains(num1)) {
                                // Remove it num1 from this cell
                                cell.removePossibleVals(num1);
                            }
                            if (cell.getPossibleVals().contains(num2)) {
                                // Remove num2 from this cell
                                cell.removePossibleVals(num2);
                            }
                        }
                    }
                }
            }
        }
        checkPossible();
    }

    public static void nakedPairsColumns() {
        String location;
        for (int col = 0; col < 9; col++) {
            // Find cells in this row with only two possible values
            Map<List<Integer>, List<String>> pairsMap = new HashMap<>();
            for (int row = 0; row < 9; row++) {
                Cell cell = matrix.getCell(row, col);
                if (cell.getPossibleVals().size() == 2) {
                    List<Integer> possibleVals = cell.getPossibleVals();
                    location = String.valueOf(col) + "-" + String.valueOf(row);
                    pairsMap.computeIfAbsent(possibleVals, x -> new ArrayList<>()).add(location);
                }
            } // This creates and populates a map of all cells and their location whoose cells has two values
            // matrix.displayMatrix();
            for (Map.Entry<List<Integer>, List<String>> entry : pairsMap.entrySet()) {
                if (entry.getValue().size() == 2) {
                    // A naked double is found
                    String[] first = entry.getValue().get(0).split("-");
                    String[] second = entry.getValue().get(1).split("-");
                    int num1 = entry.getKey().get(0);
                    int num2 = entry.getKey().get(1);

                    for (int i = 0; i < 9; i++) {
                        if (i != Integer.parseInt(first[1]) && i != Integer.parseInt(second[1])) {
                            // These two are the original naked pair
                            Cell cell = matrix.getCell(i, col);
                            if (cell.getPossibleVals().contains(num1)) {
                                // Remove it num1 from this cell
                                cell.removePossibleVals(num1);
                            }
                            if (cell.getPossibleVals().contains(num2)) {
                                // Remove num2 from this cell
                                cell.removePossibleVals(num2);
                            }
                        }
                    }
                }
            }
        }
        checkPossible();
    }

    public static void moreInfor(String method, String type, String change, String where) {
        if (DEBUG) {
            System.out.println("In the: " + method + " of this type: " + type + " we made this change: " + change
                    + " at this location: " + where);
        }
    }

    public static void hiddenPairsRow() {
        // My prevoius combinationn of both axis at the same time will no longer
        // continue
        List<String> locationsNum1;
        List<String> locationsNum2;
        for (int row = 0; row < 9; row++) {
            Map<Integer, List<String>> numberLocations = new HashMap<>();

            // Populate numberLocations for this row
            for (int col = 0; col < 9; col++) {
                List<Integer> possibleVals = matrix.getCell(row, col).getPossibleVals();
                for (Integer val : possibleVals) {
                    numberLocations.computeIfAbsent(val, x -> new ArrayList<>()).add(row + "-" + col);
                }
            } // This should populate the map based on location and value

            for (int num1 = 1; num1 <= 9; num1++) { // The same broad öpgoc as the 3x3
                for (int num2 = num1 + 1; num2 <= 9; num2++) {
                    locationsNum1 = numberLocations.get(num1);
                    locationsNum2 = numberLocations.get(num2);

                    if (locationsNum1 != null && locationsNum2 != null &&
                            locationsNum1.equals(locationsNum2) && locationsNum1.size() == 2) {
                        // Found a hidden pair (num1, num2) in this row
                        for (String location : locationsNum1) {
                            String[] parts = location.split("-");
                            int row1 = Integer.parseInt(parts[0]);
                            int col1 = Integer.parseInt(parts[1]);
                            matrix.getCell(row1, col1).setPossibleVals(new ArrayList<>(Arrays.asList(num1, num2)));
                        }
                    }
                }
            }
            // Check for hidden pairs/triples in numberLocations
            // Similar logic to your 3x3 squares method but applied to this row
        }
        checkPossible();
    }

    public static void hiddenTripplesColumn() {
        for (int col = 0; col < 9; col++) {
            Map<Integer, List<String>> numberLocations = new HashMap<>();

            // Populate numberLocations for this column
            for (int row = 0; row < 9; row++) {
                List<Integer> possibleVals = matrix.getCell(row, col).getPossibleVals();
                for (Integer val : possibleVals) {
                    numberLocations.computeIfAbsent(val, x -> new ArrayList<>()).add(row + "-" + col);
                }
            }

            // Check for hidden pairs
            for (int num1 = 1; num1 <= 9; num1++) {
                for (int num2 = num1 + 1; num2 <= 9; num2++) {
                    for (int num3 = num2 + 1; num3 <= 9; num3++) {
                        List<String> locationsNum1 = numberLocations.get(num1);
                        List<String> locationsNum2 = numberLocations.get(num2);
                        List<String> locationsNum3 = numberLocations.get(num3);
                        if (locationsNum1 != null && locationsNum2 != null && locationsNum3 != null) {
                            Set<String> combinedLocations = new HashSet<String>();
                            combinedLocations.addAll(locationsNum1);
                            combinedLocations.addAll(locationsNum2);
                            combinedLocations.addAll(locationsNum3);

                            if (combinedLocations.size() == 3) {
                                // That means they are all the same
                                for (String location : locationsNum1) {
                                    String[] parts = location.split("-");
                                    int row1 = Integer.parseInt(parts[0]);
                                    int col1 = Integer.parseInt(parts[1]);
                                    matrix.getCell(row1, col1)
                                            .setPossibleVals(new ArrayList<>(Arrays.asList(num1, num2, num3)));
                                }
                            }
                        }
                    }
                }
            }
        }
        checkPossible();
    }

    public static void hiddenTripplesRow() {
        List<String> locationsNum1;
        List<String> locationsNum2;
        List<String> locationsNum3;
        Set<String> combinedLocations;
        for (int row = 0; row < 9; row++) {
            Map<Integer, List<String>> numberLocations = new HashMap<>();

            // Populate numberLocations for this row
            for (int col = 0; col < 9; col++) {
                List<Integer> possibleVals = matrix.getCell(row, col).getPossibleVals();
                for (Integer val : possibleVals) {
                    numberLocations.computeIfAbsent(val, x -> new ArrayList<>()).add(row + "-" + col);
                }
            } // This should populate the map based on location and value

            for (int num1 = 1; num1 <= 9; num1++) { // The same broad öpgoc as the 3x3
                for (int num2 = num1 + 1; num2 <= 9; num2++) {
                    for (int num3 = num2 + 1; num3 <= 9; num3++) {
                        locationsNum1 = numberLocations.get(num1);
                        locationsNum2 = numberLocations.get(num2);
                        locationsNum3 = numberLocations.get(num3);

                        if (locationsNum1 != null && locationsNum2 != null && locationsNum3 != null) {
                            combinedLocations = new HashSet<String>();
                            combinedLocations.addAll(locationsNum1);
                            combinedLocations.addAll(locationsNum2);
                            combinedLocations.addAll(locationsNum3);
                            if (combinedLocations.size() == 3) {
                                // Then they were the same
                                for (String location : locationsNum1) {
                                    String[] parts = location.split("-");
                                    int row1 = Integer.parseInt(parts[0]);
                                    int col1 = Integer.parseInt(parts[1]);
                                    matrix.getCell(row1, col1)
                                            .setPossibleVals(new ArrayList<>(Arrays.asList(num1, num2, num3)));
                                }
                            }
                        }
                    }
                }
            }
            // Check for hidden pairs/triples in numberLocations
            // Similar logic to your 3x3 squares method but applied to this row
        }
        checkPossible();
    }

    public static void hiddenPairsColumn() {
        for (int col = 0; col < 9; col++) {
            Map<Integer, List<String>> numberLocations = new HashMap<>();

            // Populate numberLocations for this column
            for (int row = 0; row < 9; row++) {
                List<Integer> possibleVals = matrix.getCell(row, col).getPossibleVals();
                for (Integer val : possibleVals) {
                    numberLocations.computeIfAbsent(val, x -> new ArrayList<>()).add(row + "-" + col);
                }
            }

            // Check for hidden pairs
            for (int num1 = 1; num1 <= 9; num1++) {
                for (int num2 = num1 + 1; num2 <= 9; num2++) {
                    List<String> locationsNum1 = numberLocations.get(num1);
                    List<String> locationsNum2 = numberLocations.get(num2);

                    if (locationsNum1 != null && locationsNum2 != null &&
                            locationsNum1.equals(locationsNum2) && locationsNum1.size() == 2) {
                        // Found a hidden pair (num1, num2) in this column
                        for (String location : locationsNum1) {
                            String[] parts = location.split("-");
                            int row1 = Integer.parseInt(parts[0]);
                            int col1 = Integer.parseInt(parts[1]);
                            matrix.getCell(row1, col1).setPossibleVals(new ArrayList<>(Arrays.asList(num1, num2)));
                        }
                    }
                }
            }
        }
        checkPossible();
    }

    public static void hiddenTripplesSquare() {
        // Based on the same logic of the hiddenPairSquare, but
        // Still different enough to warrent a different method
        // First the 3x3:s
        Map<Integer, List<String>> numberLocations; // 1-9 and location of each
        List<String> locationsNum1;
        List<String> locationsNum2;
        List<String> locationsNum3;
        Set<String> combinedLocations;
        for (int i = 0; i < 9; i += 3) {
            for (int j = 0; j < 9; j += 3) { // Iterates all the 9 3x3:s in the grid
                numberLocations = populateSquare(i, j);
                // We have now populated the 3x3, time to check
                for (int num1 = 1; num1 <= 9; num1++) {
                    for (int num2 = num1 + 1; num2 <= 9; num2++) {
                        for (int num3 = num2 + 1; num3 <= 9; num3++) {
                            // Since three values has to be the same a tripple nested loop is required
                            if (num1 != num2 && num1 != num2 && num2 != num3) {
                                // This check is also not needed since the for loops are now based on the
                                // prevoius one
                                // Could be simplified, these 3 Lists are redundant
                                locationsNum1 = numberLocations.get(num1);
                                locationsNum2 = numberLocations.get(num2);
                                locationsNum3 = numberLocations.get(num3);
                                if (locationsNum1 != null && locationsNum2 != null && locationsNum3 != null) {
                                    // Not sure if they can be null, but never hurts to check
                                    combinedLocations = new HashSet<String>();
                                    combinedLocations.addAll(locationsNum1);
                                    combinedLocations.addAll(locationsNum2);
                                    combinedLocations.addAll(locationsNum3);
                                    // Since only unique elements are added, if the size is three then they were all
                                    // the same
                                    if (combinedLocations.size() == 3) {
                                        // Found a hidden triple
                                        for (String location : combinedLocations) {
                                            // Update cells at these locations to only contain num1, num2, and num3
                                            String[] parts = location.split("-");
                                            int row = Integer.parseInt(parts[0]);
                                            int col = Integer.parseInt(parts[1]);
                                            // These three lines are just from how the location i stored
                                            matrix.getCell(row, col)
                                                    .setPossibleVals(new ArrayList<>(Arrays.asList(num1, num2, num3)));
                                            moreInfor("HiddenTripple", "Square",
                                                    String.valueOf(num1) + " " + String.valueOf(num2),
                                                    location);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        checkPossible(); // Redo the possible values
    }

    /**
     * @return a populated hashmap of the 3x3 with each value and their location
     */
    public static Map<Integer, List<String>> populateSquare(int i, int j) {
        Map<Integer, List<String>> numberLocations = new HashMap<>();

        for (int k = i; k < i + 3; k++) {
            for (int l = j; l < j + 3; l++) { // Iterates all the cells in each 3x3
                // Add logic if 2 cells are the only ones in the entire 3x3 that have two values
                // So keep track of the cell, and their unique values
                List<Integer> possibleVals = new ArrayList<>();
                if (matrix.getCell(k, l).getPossibleVals().size() != 1) {
                    possibleVals = matrix.getCell(k, l).getPossibleVals();
                }
                for (Integer val : possibleVals) {
                    numberLocations.computeIfAbsent(val, x -> new ArrayList<>()).add(k + "-" + l);
                    // Should add the values from 1-9 and a string array of each values location
                }
            }
        } // Now we have a populated hashmap

        return numberLocations;
    }

    public static void hiddenPairSquare() {
        // If x values in a 3x3 only exists in x cells all other possible values can be
        // removed from these cells
        // Ie if 5,6 only can exist in two cells then other possible values in these
        // cells can be removed

        // First the 3x3:s
        Map<Integer, List<String>> numberLocations; // 1-9 and location of each
        List<String> locationsNum1;
        List<String> locationsNum2;
        for (int i = 0; i < 9; i += 3) {
            for (int j = 0; j < 9; j += 3) { // Iterates all the 9 3x3:s in the grid
                numberLocations = populateSquare(i, j);
                // Iterate over all of these elements and check if there are any two values that
                // only show up in two cells
                for (int num1 = 1; num1 <= 9; num1++) {
                    for (int num2 = num1 + 1; num2 <= 9; num2++) {
                        // Should half iterations num1 + 1, and make sure duplicates are no more,
                        // Additionally the if comparison is redundant, but does not break anything
                        // There is a possibility that this updates the two cells twice, since the map
                        // is made
                        // With the values and not updated if the first iteration of this loop changes
                        // the values
                        // Though inefficient it does not break the code, since replacing 1,5 with 1,5
                        // is no change
                        if (num1 != num2) {
                            locationsNum1 = numberLocations.get(num1);
                            locationsNum2 = numberLocations.get(num2);
                            // Nested loop to iterate thorugh all the possible values
                            // And since we are looking for pairs, we have to run the loop nested
                            if (locationsNum1 != null && locationsNum2 != null &&
                                    locationsNum1.equals(locationsNum2) && locationsNum1.size() == 2) {
                                // If there exists a list with the key num1 and num2
                                // Check if these are the same and of size 2
                                for (String location : locationsNum1) {
                                    String[] parts = location.split("-");
                                    int row = Integer.parseInt(parts[0]);
                                    int col = Integer.parseInt(parts[1]);
                                    // These three lines are just from how the location i stored
                                    matrix.getCell(row, col)
                                            .setPossibleVals(new ArrayList<>(Arrays.asList(num1, num2)));
                                    moreInfor("HiddenPair", "Square", String.valueOf(num1) + " " + String.valueOf(num2),
                                            location);
                                }
                            }
                        }
                    }
                }
            }
        }
        checkPossible();
    }

    public static void pointedSquare(boolean row) {
        // Will try to merge this method with pointed, wont happen
        Map<String, ArrayList<Integer>> order;
        String location;
        Set<Integer> three;
        Set<Integer> six;
        Set<Integer> uniqueElements;
        int rowCol;
        for (int i = 0; i < 9; i += 3) {
            for (int j = 0; j < 9; j += 3) {
                order = new HashMap<>();
                for (int k = i; k < i + 3; k++) {
                    for (int l = j; l < j + 3; l++) {
                        location = l + "-" + k; // Row-column , could be column-row
                        order.put(location, matrix.getCell(k, l).getPossibleVals());
                    }
                } // That should populate the hashmap

                // Do some chechs in each 3x3 for adjacent unique values
                for (int a = 0; a < 3; a++) { // Since there are 3 columns/rows that need exploring
                    three = new LinkedHashSet<>();
                    six = new LinkedHashSet<>();
                    for (int k = i; k < i + 3; k++) {
                        for (int l = j; l < j + 3; l++) {
                            // Iterate through all the cells inside the 3x3
                            location = l + "-" + k; // Info about the cells location
                            if (order.get(location).size() != 1) {
                                if (row) {
                                    if (k % 3 == a) { // Row
                                        three.addAll(order.get(location));
                                    } else {
                                        six.addAll(order.get(location));
                                    }
                                    // The modulous checks are since we want to distinguish the sub-row/column we
                                    // are comparing
                                    // With the rest of the entire axis
                                } else if (!row) {
                                    if (l % 3 == a) { // The sought after column
                                        three.addAll(order.get(location));
                                    } else {
                                        six.addAll(order.get(location));
                                    }
                                }
                            }
                        }
                    }
                    uniqueElements = new HashSet<>(three);
                    uniqueElements.removeAll(six); // The ones left should be the unique elements
                    rowCol = row ? i + a : j + a; // Since rows and columns are different
                    for (int o = 0; o < 9; o++) {
                        if (row && !(j <= o && o < j + 3)) {
                            for (Integer integer : uniqueElements) {
                                // Iterate and remove the values from other cells in the column
                                if (matrix.getCell(rowCol, o).getPossibleVals().contains(integer)) {
                                    matrix.getCell(rowCol, o).removePossibleVals(integer);
                                    moreInfor("PointedSquare", "Row", String.valueOf(integer),
                                            matrix.getCell(rowCol, o).getName());
                                }
                            }
                        } else if (!row && !(i <= o && o < i + 3)) {
                            for (Integer integer : uniqueElements) {
                                if (matrix.getCell(o, rowCol).getPossibleVals().contains(integer)) {
                                    matrix.getCell(o, rowCol).removePossibleVals(integer);
                                    moreInfor("PointedSquare", "Column", String.valueOf(integer),
                                            matrix.getCell(o, rowCol).getName());
                                }
                            }
                        }
                    }
                }
            }
        }
        checkPossible();
    }

    public static void pointed(int row) {
        // This removes pointed double/tripples from a column/rows - squares perspective
        // Does not completly determin what the cell is going to be, but can remove what
        // options it has
        // Assume in a 3x3 7 can only be on two places on the same row
        // Then other 7:s on the same row elsewhere can be eliminated
        // Basically if one rule type influences anothers possibilities

        // Check all rows and columns
        // Find any number that exists only within a square
        // Eliminate this number from any other cell within the same square
        Map<Integer, ArrayList<Integer>> order;
        Set<Integer> three;
        Set<Integer> six;
        for (int i = 0; i < 9; i++) {
            order = new HashMap<>();
            for (int j = 0; j < 9; j++) {
                if (row == 0) { // Rows
                    order.put(j, new ArrayList<>(matrix.getCell(i, j).getPossibleVals()));
                } else if (row == 1) { // Columns
                    order.put(j, new ArrayList<>(matrix.getCell(j, i).getPossibleVals()));
                }
            } // The ordered hashmaps are created
              // Check if pointers occur

            // iterate first 3 - check if any values here never show upp later in the
            // hashmap
            Set<Integer> uniqueElements;
            for (int j = 0; j < 3; j++) { // Three different squares
                three = new LinkedHashSet<>();
                six = new LinkedHashSet<>();

                for (int k = 0; k < 9; k++) {
                    if (order.get(k).size() != 1) {
                        if (k < j * 3 + 3 && k >= j * 3) {
                            three.addAll(order.get(k));
                        } else {
                            six.addAll(order.get(k));
                        }
                    }
                }
                uniqueElements = new HashSet<>(three);
                uniqueElements.removeAll(six);

                if (!uniqueElements.isEmpty()) {
                    // These elements should be removed from other cells within the 3x3 that also
                    // i:th row, j:th 3x3
                    // loop through the 3x3
                    // halt for the specefied column
                    // if any cell cointains values that also exists in the uniqueelements
                    // remove them from the cell
                    if (row == 0) {
                        for (int c = (i / 3) * 3; c < (i / 3) * 3 + 3; c++) {
                            for (int d = 3 * j; d < 3 + 3 * j; d++) {
                                if (c != i % 3 + (i / 3) * 3) { // Rows
                                    for (Integer integer : uniqueElements) {
                                        matrix.getCell(c, d).removePossibleVals(integer);
                                        moreInfor("Pointed", "Row", String.valueOf(integer),
                                                matrix.getCell(c, d).getName());
                                    }
                                }
                            }
                        }
                    } else if (row == 1) {
                        for (int c = j * 3; c < j * 3 + 3; c++) {
                            for (int d = (i / 3) * 3; d < (i / 3) * 3 + 3; d++) {
                                if (d != i) { // Rows
                                    for (Integer integer : uniqueElements) {
                                        matrix.getCell(c, d).removePossibleVals(integer);
                                        moreInfor("Pointed", "Column", String.valueOf(integer),
                                                matrix.getCell(c, d).getName());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        checkPossible();
    }

    public static void checkSingelValues(int row) { // 0-row, 1-col, 2-3x3
        // If a naked single exists on the row, column or 3x3, set that location to that
        // value
        // For example checks the row for any unique value, that cell has to be said
        // value
        Map<Integer, Integer> frequency;
        ArrayList<Integer> nums;
        for (int i = 0; i < 9; i++) {
            frequency = new HashMap<>();
            for (int j = 1; j <= 9; j++) {
                frequency.put(j, 0);
            } // Populate the hashmap with keys from 1-9

            if (row < 2) { // Rows and columns
                for (int j = 0; j < 9; j++) {
                    nums = row == 0 ? new ArrayList<>(matrix.getCell(i, j).getPossibleVals())
                            : new ArrayList<>(matrix.getCell(j, i).getPossibleVals());
                    if (nums.size() > 1) { // Dont want already solved cells, value
                        for (Integer value : nums) {
                            frequency.put(value, frequency.get(value) + 1);
                        }
                        // Add each number from every cell in the row to the frequency hashmap
                        // So that if a number only occurs once, we know it should be solved
                    }
                }
            } else if (i % 3 == 0) { // 0,3,6 and this is for the 3x3
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
            // For all rows, columns and 3x3 that has one number only occuring once
            // Set said cell to that number, in accordance to the rules of sudoku
            // Sets the value to an one sized arraylist
            for (Map.Entry<Integer, Integer> entry : frequency.entrySet()) {
                if (entry.getValue() == 1) { // The numbers that only show upp once
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
        checkPossible();
    }

    /**
     * Iterates thorugh all cells and each cells adjacent list
     * This list keeps track of every cell in the same row, column and 3x3
     * Ie all the cells whoose value affect this cell
     * Uppdates all the possible values for each cell based on the once adjacent
     */
    public static void checkPossible() {
        int len = matrix.matrixToString().length();
        while (true) { // Since the updated matrix might lead to changes - new update needed
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
                // This update is repeated, since there is a possibility that the update created
                // a new solved cell
                // Ie if the cell 1.1 has the possible values: 1, 7. And from the row 7 is
                // removed, this cell is now solved
                // But the adjacent cells need to be updated that this cell is not set at 1.
            } else {
                len = matrix.matrixToString().length();
            }
        }
    }
}

/**
 * For initiating the puzzle matrix, to displayand some getters and setters for
 * the matrix object
 */
class Matrix {
    public Cell[][] matrix;
    public static final boolean SIMPLE = false; // Display formatting
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

    public void setCells() { // Only for initializing
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                matrix[i][j] = new Cell(i, j, Character.getNumericValue(SodukoSolver.gameString.charAt(i * 9 + j)));
            }
        }
    }

    /**
     * Displays the entire soduko matrix with formating
     * If a cell i solved it is displayed in green.
     * Can be full or simplified all one char or all possible values for each cell
     */
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

/**
 * An object of each cell in the soduko matrix
 * Keeps track of possible values, adjacent values
 * Has some setters and getters for this, primarly the
 * set possible vals, and remove possible val
 */

class Cell {
    public ArrayList<Integer> possibleVals = new ArrayList<>(); // Every number this cell can be
    public ArrayList<Cell> adjacentCells = new ArrayList<>(); // All cells that influence what this cells number can be
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

    public void removePossibleVals(int i) {
        if (possibleVals.contains(i)) {
            possibleVals.remove((Integer) i);
        }
        if (possibleVals.size() == 1) {
            System.out.println("WE her");
            SodukoSolver.checkPossible();
        }
    }

    public void setPossibleVals(ArrayList<Integer> possible) {
        if (possible.size() != 0) {
            this.possibleVals = new ArrayList<>(possible);
        }
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
// Brute forcing - take the incomplete map, guess one of the cells, preferebly
// one with few possible
// Set said value, keep track of the others. Return the new map and try to solve
// it
// Implement a check to se if somthings gone wrong, in that case return to the
// other matrix, and
// Guess the other value/s. Will be difficult if multiple values needs to be
// guessed in a row.
