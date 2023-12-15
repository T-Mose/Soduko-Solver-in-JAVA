Soduko Solver written in java by me T3D. An extension of my prevoius project of this solver written in c#. This can currently solve up to hard level soduko puzzles. 
Features that wont be implemented are: XYZ wings, Swordfish and Bruteforce methods.
Features that are not yet implemented are Naked pairs and tripples for rows, columns and 3x3:s. Once these are implemented all puzzles upp to and including most hard puzzles can be solved.
The code is very poorly optemized and a lot of code is written redundently. This is due to me not completly knowing the strategies for solving a soduko puzzle before begining, beyond the easy to medium strategies.

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
