package rmiserver;

/**
 * The Sudoku operations.
 */
public class SudokuOperations {

  /**
   * Find sudoku row based on the boxIdx and the sudoku column.
   *
   * @param boxIdx       the box idx
   * @param sudokuColumn the sudoku column
   * @return the row.
   */
  public static int findSudokuRow(int boxIdx, int sudokuColumn) {
    if (boxIdx >= 0 && boxIdx <= 2) { // row = 0,1,2
      if (sudokuColumn <= 2) {
        return 0;
      }
      return sudokuColumn <= 5 ? 1 : 2;
    }
    if (boxIdx >= 3 && boxIdx <= 5) { // row = 3,4,5
      if (sudokuColumn <= 2) {
        return 3;
      }
      return sudokuColumn <= 5 ? 4 : 5;
    }
    // row = 6,7,8
    if (sudokuColumn <= 2) {
      return 6;
    }
    return sudokuColumn <= 5 ? 7 : 8;
  }

  /**
   * Find sudoku column based on the boxIdx and the sudoku column.
   *
   * @param boxIdx        the box idx
   * @param currentColumn the current column - a column in the box
   * @return the sudoku column.
   */
  public static int findSudokuColumn(int boxIdx, int currentColumn) {
    if (boxIdx % 3 == 0) { // column = 0,1,2
      if (currentColumn % 3 == 0) {
        return 0;
      }
      return (currentColumn == 1 || currentColumn == 4 || currentColumn == 7) ? 1 : 2;
    }
    if (boxIdx == 1 || boxIdx == 4 || boxIdx == 7) {  // column = 3,4,5
      if (currentColumn % 3 == 0) {
        return 3;
      }
      return (currentColumn == 1 || currentColumn == 4 || currentColumn == 7) ? 4 : 5;
    }
    // column = 6,7,8
    if (currentColumn % 3 == 0) {
      return 6;
    }
    return (currentColumn == 1 || currentColumn == 4 || currentColumn == 7) ? 7 : 8;
  }

}
