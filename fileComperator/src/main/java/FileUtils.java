import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;;

/**
 * Utility class for managing files
 *
 * @author tstoyanov
 */
public class FileUtils {
  /**
   * method iterate through folders and collect files name
   *
   * @param rootFolder
   * @return Map contains key: file name, value: file path - to all file in folders and sub-folders
   */
  public static Map<String, Path> getAllFilePathsFrom(File rootFolder) {
    List<String> files = new ArrayList<>();
    Map<String, Path> result = new LinkedHashMap<>();

    Deque<File> directories = new LinkedList<>();
    String nameRootFolder = rootFolder.getName();
    directories.addFirst(rootFolder);

    StringBuilder sb = new StringBuilder();
    while (directories.size() != 0) {
      File file = directories.removeFirst();

      if (file.isDirectory()) {
        directories.addAll(Arrays.asList(file.listFiles()));
      }
      else {
        Path p = Paths.get(file.getAbsolutePath());
        String currentFolderName = String.valueOf(p.getName(p.getNameCount() - 2));

        if (!nameRootFolder.equals(currentFolderName)) {
          String key = currentFolderName + "/" + file.getName();
          result.putIfAbsent(key, p);
        }
        else {
          result.putIfAbsent(file.getName(), p);
        }

      }
    }

    return result;
  }
}
