package rmiserver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The Sudoku.
 */
public class Sudoku implements Serializable {

  private List<List<Integer>> board; // The internal lists are the columns.
  private List<List<Integer>> solvedBoard;

  public Sudoku(int difficulty) {
    clearBoard(); // Fill the board with 0.
    generate(); // Generates a filled board.
    copy();  // Save it to the solved board.
    playerBoard(difficulty); // Remove "difficulty" numbers from the board.
    printBoard();
  }

  /**
   * Sets number into the board by a given row, column and value.
   *
   * @param row    the row
   * @param column the column
   * @param value  the value
   */
  public void setNumber(int row, int column, int value) {
    board.get(row).set(column, value);
  }

  /**
   * Check if solved: checks the rows, columns and boxes for any zeros and if they all contain 9 numbers.
   *
   * @return the boolean true if solved, false if not.
   */
  public boolean checkIfSolved() {
    if (board.stream().anyMatch(integers -> integers.contains(0))) {
      return false;
    }

    //Check rows
    for (List<Integer> row : board) {
      if (row.stream().distinct().count() != 9) {
        return false;
      }
    }

    //Check columns
    for (int i = 0; i < 9; i++) {
      List<Integer> column = new ArrayList<>();
      for (List<Integer> row : board) {
        column.add(row.get(i));
      }
      if (column.stream().distinct().count() != 9) {
        return false;
      }
    }

    //Check boxes
    return getBoxes(false).stream()
        .filter(integers -> integers.stream().distinct().count() != 9)
        .findAny()
        .isEmpty();
  }

  /**
   * Gets boxes.
   *
   * @param solved the solved - needed for the decision from which board the boxes are needed.
   * @return the boxes
   */
  public List<List<Integer>> getBoxes(boolean solved) {
    List<List<Integer>> boxes = new ArrayList<>();
    int startColumn = 0, endColumn = 3;
    int startRow = 0, endRow = 3;

    for (int column = 0; column < 9; column++) { // 9 boxes in total

      List<Integer> box = new ArrayList<>();
      for (int i = startRow; i < endRow; i++) {
        for (int j = startColumn; j < endColumn; j++) {  //get all elements from the current box
          box.add(solved ? solvedBoard.get(i).get(j) : board.get(i).get(j));
        }
      }
      boxes.add(box);

      if (column == 2 || column == 5) { // move to the first (resetting columns) box on the row below
        startRow += 3;
        endRow += 3;
        startColumn = 0;
        endColumn = 3;
      } else {
        startColumn += 3;  // move to the right box
        endColumn += 3;
      }
    }
    return boxes;
  }

  private void copy() {
    solvedBoard = new ArrayList<>();
    for (List<Integer> row : board) {
      List<Integer> current = new ArrayList<>(row);
      solvedBoard.add(current);
    }
  }

  private void playerBoard(int difficulty) { // difficulty = numbers to remove
    while (difficulty > 0) {

      for (List<Integer> row : board) {

        int index = getRandomIndex();  // може да се падне един и същи индеск повече от 1 път (to be improved)
        row.set(index, 0);

        difficulty--;

        if (difficulty == 0) {
          break;
        }
      }
    }
  }

  private int getRandomIndex() {
    return new Random().nextInt(9);
  }

  private void generate() {
    int startFrom = 1, currentNumber;

    for (List<Integer> row : board) {
      currentNumber = startFrom;

      for (int j = 0; j < 9; j++) {   // Fill current row
        if (currentNumber > 9) { // Reset currentNumber if it's above the limit
          currentNumber = 1;
        }

        row.set(j, currentNumber++); // Add the currentNumber and increment it
      }

      if (board.indexOf(row) == 0) { // the second row should start with 4;
        startFrom = 4;
        continue;
      }

      startFrom = currentNumber + 3; // next row should start with the first number from the next box of the current row

      if (startFrom > 9) {  // check the limit, if exceeded (when currentNumber = 7 or 8), then decrease startFrom = 2 or 3
        startFrom = (startFrom % 9) + 1;
      }
    }
  }

  private void clearBoard() {
    this.board = new ArrayList<>();
    for (int i = 0; i < 9; i++) {
      List<Integer> row = new ArrayList<>();
      for (int j = 0; j < 9; j++) {
        row.add(0);
      }
      board.add(row);
    }
  }

  private void printBoard() {
    for (int j = 0; j < board.size(); j++) {
      for (int i = 0; i < board.get(j).size(); i++) {
        String delimeter = i == 2 || i == 5 ? " | " : " ";
        System.out.print(board.get(j).get(i) + delimeter);
      }
      String delimeter = j == 2 || j == 5 ? "\n---------------------\n" : "\n";
      System.out.print(delimeter);
    }
  }
}
