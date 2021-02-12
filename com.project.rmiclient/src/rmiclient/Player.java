package rmiclient;

/**
 * The Player contains the result of a game: username, difficulty, result.
 */
public class Player {

  private final String username;
  private final Difficulty difficulty;
  private final Result result;

  public Player(String username, Difficulty difficulty, Result result) {
    this.username = username;
    this.difficulty = difficulty;
    this.result = result;
  }

  @Override
  public String toString() {
    return username + ',' + difficulty + "," + result + ";\n";
  }

  /**
   * The Difficulty enum.
   */
  enum Difficulty {
    EASY,
    MEDIUM,
    HARD;
  }

  /**
   * The Result enum.
   */
  enum Result {
    WIN,
    FAIL;
  }
}
