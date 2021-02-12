package rmiclient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * CsvWriter creates a file and writes player's outcome into it.
 */
public class CsvWriter {

  private static final String HEADER = "Username,Difficulty,Result;\n";
  private static final String FILE_NAME = "players_stats.csv";

  /**
   * Write result to file: if the file doesn't exist, it's created.
   * Each row of the file is represented by the toString method of the {@link Player}.
   *
   * @param player the player {@link Player}
   */
  public static void writeResultToFile(Player player) {
    if (!Files.exists(Path.of(FILE_NAME))) {
      File csv = new File(FILE_NAME);
      if (!addHeader(csv)) {
        return;
      }
    }

    File csv = new File(FILE_NAME);
    try (FileOutputStream outputStream = new FileOutputStream(csv, true)) {
      outputStream.write(player.toString().getBytes(StandardCharsets.UTF_8));
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  private static boolean addHeader(File csv) {
    try (FileOutputStream outputStream = new FileOutputStream(csv)) {
      outputStream.write(HEADER.getBytes(StandardCharsets.UTF_8));
    } catch (IOException ex) {
      ex.printStackTrace();
      return false;
    }
    return true;
  }

}
