package rmiclient;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import rmiserver.RMIServer;
import rmiserver.ServerInterface;
import rmiserver.Sudoku;
import rmiserver.SudokuOperations;


/**
 * The Rmi client: JavaFX application, which request a sudoku from the RMI Server and starts the game.
 */
public class RMIClient extends Application {

  // User related elements:
  private static final String DEFAULT_USER = "default";
  private Player.Difficulty difficulty;
  private Player.Result result = Player.Result.FAIL;
  // Sudoku related elements:
  private static final String EMPTY_CELL = "  ";
  private Sudoku sudoku;
  private int chosenNumber;
  private int difficultyOption; // Represents how many of the cells will be removed from the generated sudoku board.
  // JavaFX related elements:
  private final GridPane table = new GridPane();
  private Timeline timeline;
  private final TextArea timeArea = new TextArea();
  private final GridPane numberOptions = new GridPane();
  private final Popup popup = new Popup();
  private final VBox initialButtons = new VBox();
  private final Label usernameLabel = new Label("Username:");
  private final TextField usernameText = new TextField(DEFAULT_USER);
  private final Button startGame = new Button("Start Game");
  private final Button solve = new Button("Solve");
  private Stage stage;

  @Override
  public void start(Stage stage) throws Exception {
    // make a connection to the RMI Server
    Registry registry = LocateRegistry.getRegistry(1099);
    ServerInterface serverInterface = (ServerInterface) registry.lookup(RMIServer.SERVICE);

    this.stage = stage;
    // A popup, containing the user info and the difficulty buttons.
    popup.getContent().add(getDifficultyOptions(serverInterface));

    // When the game is started, only the popup is shown.
    startGame.setOnAction(e -> {
      hideComponents();
      popup.show(stage);
    });

    setSolveButton();
    setInitialButtons();
    startTimeLine();
    setNumberOptions();

    // Structures the game layout.
    BorderPane layout = new BorderPane();
    layout.setTop(initialButtons);
    layout.setCenter(table);
    layout.setBottom(numberOptions);

    Scene scene = new Scene(layout, 595, 600, Color.BEIGE);
    stage.setScene(scene);
    stage.setTitle("Sudoku");
    stage.show();
    // When the game is terminated, the player's outcome is saved to a file.
    stage.setOnCloseRequest(windowEvent -> CsvWriter.writeResultToFile(new Player(usernameText.getText(), difficulty, result)));
  }

  public static void main(String[] args) {
    Application.launch(args);
  }

  /**
   * Fills a VBox with all initial buttons & fields.
   */
  private void setInitialButtons() {
    initialButtons.setSpacing(10);
    initialButtons.setPadding(new Insets(16, 250, 0, 250));
    initialButtons.setAlignment(Pos.CENTER);
    initialButtons.getChildren().add(startGame);
    initialButtons.getChildren().add(timeArea);
    initialButtons.getChildren().add(solve);
  }

  /**
   * The solve button triggers filling the sudoku table with the right numbers
   * and saving to a FILE the user's failed result.
   * It's shown only during the game is going.
   */
  private void setSolveButton() {
    solve.setVisible(false); // it must not be shown at the very beginning.
    solve.setOnAction(e -> {
      table.setVisible(false); // hide the sudoku table
      setSudokuTable(true); // fill the sudoku table with the right numbers
      table.setVisible(true); // show the sudoku table
      CsvWriter.writeResultToFile(new Player(usernameText.getText(), difficulty, Player.Result.FAIL)); // write player's outcome to file
      solve.setVisible(false); // hide the solve button
    });
  }

  /**
   * Creates a {@link VBox} with username and difficulty options.
   * Request a sudoku game from the server interface, depending on the difficulty chosen.
   *
   * @param serverInterface the "connection" to the server {@link ServerInterface}
   * @return the VBox with user info and difficulty buttons.
   */
  private VBox getDifficultyOptions(ServerInterface serverInterface) {
    HBox usernameBox = new HBox();

    usernameBox.getChildren().addAll(usernameLabel, usernameText);
    usernameBox.setSpacing(10);

    Button easy = new Button("Easy");
    Button medium = new Button("Medium");
    Button hard = new Button("Hard");
    easy.setOnAction(actionEvent -> {
      difficultyOption = 15;
      difficulty = Player.Difficulty.EASY;
      requestSudoku(serverInterface);
    });
    medium.setOnAction(actionEvent -> {
      difficultyOption = 35;
      difficulty = Player.Difficulty.MEDIUM;
      requestSudoku(serverInterface);
    });
    hard.setOnAction(actionEvent -> {
      difficultyOption = 55;
      difficulty = Player.Difficulty.HARD;
      requestSudoku(serverInterface);
    });

    VBox difficultyOptions = new VBox();
    difficultyOptions.setSpacing(10);
    difficultyOptions.setPadding(new Insets(16, 0, 0, 0));
    difficultyOptions.setAlignment(Pos.CENTER);
    difficultyOptions.getChildren().add(usernameBox);
    difficultyOptions.getChildren().add(easy);
    difficultyOptions.getChildren().add(medium);
    difficultyOptions.getChildren().add(hard);

    return difficultyOptions;
  }

  /**
   * Request a sudoku from the server. If something failed, error alert is shown.
   * Triggers creating a sudoku table, resets the timer and show the needed game components.
   *
   * @param serverInterface the "connection" to the RMI Server {@link ServerInterface}
   */
  private void requestSudoku(ServerInterface serverInterface) {
    popup.hide(); // the popup is not needed anymore.
    try {
      sudoku = serverInterface.startSudoku(difficultyOption); // request a sudoku from the server
    } catch (RemoteException e) {
      Alert alert = new Alert(Alert.AlertType.ERROR); // in case of error, alert is shown
      alert.setTitle("End of game");
      alert.setHeaderText("Something went wrong. Please, try again later.");
      alert.showAndWait();

      e.printStackTrace();
      return;
    }
    setSudokuTable(false); // the sudoku table is generated with unsolved numbers
    resetTimer(); // the timer is started/reset
    showComponents(); // the needed game components are shown.
  }

  /**
   * Shows the following components: timer, sudoku table, number options, solve button.
   */
  private void showComponents() {
    timeArea.setVisible(true);
    solve.setVisible(true);
    numberOptions.setVisible(true);
    table.setVisible(true);
  }

  /**
   * Hide the following components: timer, sudoku table, number options, solve button.
   */
  private void hideComponents() {
    timeArea.setVisible(false);
    solve.setVisible(false);
    numberOptions.setVisible(false);
    table.setVisible(false);
  }

  /**
   * Based on the sudoku boxes, triggers creation of JavaFx boxes, represented by List of {@link GridPane}.
   * The boxes are put in the sudoku table.
   *
   * @param solved based on the button the full sudoku board is generated or the one with the missing numbers.
   */
  private void setSudokuTable(boolean solved) {
    table.setVgap(8);
    table.setHgap(8);
    table.setAlignment(Pos.CENTER);

    List<GridPane> gridBoxes = getGridBoxes(sudoku.getBoxes(solved));
    for (int i = 0; i < 9; i++) {
      table.add(gridBoxes.get(i), i % 3, i / 3);
    }
  }

  /**
   * Fills the GirdPane with number options at the bottom.
   * Represents the numbers which may be placed at the cells.
   */
  private void setNumberOptions() {
    numberOptions.setHgap(2);
    numberOptions.setPadding(new Insets(0, 0, 16, 0));
    numberOptions.setAlignment(Pos.CENTER);
    numberOptions.setVisible(false);

    ToggleGroup toggleGroup = new ToggleGroup(); // Allows only 1 element from the group to be chosen and highlights it.

    for (int i = 0; i < 9; i++) {
      ToggleButton number = new ToggleButton(String.valueOf(i + 1));
      number.setOnAction(actionEvent -> chosenNumber = Integer.parseInt(number.getText()));
      toggleGroup.getToggles().add(number);
      numberOptions.add(number, i, 0);
    }
  }

  /**
   * Fills the TextArea with a {@link Timeline} outcome, which counts the seconds from the game start.
   */
  private void startTimeLine() {
    timeArea.setWrapText(true);
    timeArea.setPrefRowCount(1);
    timeArea.setVisible(false);

    Date start = Calendar.getInstance().getTime();
    timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
      long countUp = Calendar.getInstance().getTime().getTime() - start.getTime(); // the time passed from the start
      timeArea.setText("Time: " + TimeUnit.SECONDS.convert(countUp, TimeUnit.MILLISECONDS)); // convert to seconds from milliseconds
    }));
    timeline.setCycleCount(Animation.INDEFINITE); // no end specified.
    timeline.play();
  }

  /**
   * Creates a {@link List<GridPane>} which represents the sudoku boxes.
   * Defines needed actions when an empty cell is filled. Disables the filled cells and changes their style.
   *
   * @param sudokuBoxes the sudoku boxes, represented by a {@link List<List<Integer>>}
   * @return the JavaFx sudoku boxes. {@link List<GridPane>}
   */
  private List<GridPane> getGridBoxes(List<List<Integer>> sudokuBoxes) {
    List<GridPane> gridBoxes = new ArrayList<>();

    for (List<Integer> box : sudokuBoxes) {
      int start = 0, end = 3;

      GridPane gridBox = new GridPane();

      for (int row = 0; row < 3; row++) { // each box has 3 rows
        int column = 0; // column in the box [0,3)

        for (int i = start; i < end; i++) { // For each column from the sudoku -> [0;9)
          Button number = new Button(EMPTY_CELL); // creates an empty cell

          if (box.get(i) != 0) {   // If the number is not 0, it cannot be changed and its style is bold.
            number.setStyle("-fx-opacity: 1.0;-fx-font-weight: bold;");
            number.setDisable(true);
            number.setText(String.valueOf(box.get(i)));
          }

          final int currentColumn = i; // needed for the operations in the lambda expression.
          number.setOnMouseClicked(m -> actionOnEmptyCellClick(sudokuBoxes.indexOf(box), number, currentColumn));

          gridBox.add(number, column++, row); // add the number to the current box.
        }

        start += 3;
        end += 3;
      }
      gridBoxes.add(gridBox);
    }
    return gridBoxes;
  }

  /**
   * Sets the action when an empty cell is chosen: filling it with the chosen number option,
   * find the real sudoku row and column from the sudoku board and change the number in the sudoku board.
   * Then check if the board is already filled with right numbers, if so, end the game with success.
   *
   * @param boxIdx        needed for finding the row and the column
   * @param number        the clicked button
   * @param currentColumn the column in the sudoku board.
   */
  private void actionOnEmptyCellClick(int boxIdx, Button number, int currentColumn) {
    if (chosenNumber == 0) { // if a number from the options is not chosen, do nothing.
      return;
    }
    number.setText(String.valueOf(chosenNumber)); // fill cell with the chosen number.

    // Find the real sudoku row and column, so that the number can be changed in sudoku board.
    int sudokuRow = SudokuOperations.findSudokuRow(boxIdx, currentColumn);
    int sudokuColumn = SudokuOperations.findSudokuColumn(boxIdx, currentColumn);
    sudoku.setNumber(sudokuRow, sudokuColumn, chosenNumber);

    if (sudoku.checkIfSolved()) {
      endOfGame();
    }
  }

  /**
   * Shows an info alert with success message. Triggers writing the player's outcome to a file.
   * Resets the timer, hides all the components ans shows the initial popup with the difficulty options.
   */
  private void endOfGame() {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("End of game");
    alert.setHeaderText("Congratulations!");
    alert.showAndWait();

    CsvWriter.writeResultToFile(new Player(usernameText.getText(), difficulty, Player.Result.WIN));

    resetTimer();
    hideComponents();
    popup.show(stage);
  }

  /**
   * Clears the timeArea field. Stops the timeline. And restarts the timer.
   */
  private void resetTimer() {
    timeArea.clear();
    timeline.stop();
    startTimeLine();
  }

}

