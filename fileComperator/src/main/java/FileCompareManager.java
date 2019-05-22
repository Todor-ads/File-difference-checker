/**
 * $Id: FileCompareManager.java 5388 2019-04-17 07:08:15Z tstoyanov $
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TODO description
 *
 * @author tstoyanov
 */
public class FileCompareManager {
  public static void main(String[] args) throws IOException {

    BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));

    File rootSourceFolder = new File(
        System.getProperty("Enter sorce folder"));
    File rootDestinationFolder = new File(
        System.getProperty("Enter destination folder"));
    FileComparator compareLineByLine = new FileComparator();

    Map<String, Path> sourceMap;
    Map<String, Path> destinationMap;

    sourceMap = FileUtils.getAllFilePathsFrom(rootSourceFolder);
    destinationMap = FileUtils.getAllFilePathsFrom(rootDestinationFolder);
    System.out.println(sourceMap.size());
    System.out.println(destinationMap.size());
    Map<String, Path> missingSourceFile = new LinkedHashMap<>();

    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, Path> entry : sourceMap.entrySet()) {
      if (destinationMap.containsKey(entry.getKey())) {
        sb.append(compareLineByLine.Compare(entry.getValue(), destinationMap.get(entry.getKey()),
            String.valueOf(entry.getKey())) + System.lineSeparator());
        destinationMap.remove(entry.getKey());
      }
      else {
        missingSourceFile.putIfAbsent(entry.getKey(), entry.getValue());
      }
    }
    if (!destinationMap.isEmpty()) {
      for (Map.Entry<String, Path> res : sourceMap.entrySet()) {
        sb.append(String.format("this file %s is missing %n", res.getValue().getFileName()));
      }
    }
    if (!missingSourceFile.isEmpty()) {
      for (Map.Entry<String, Path> res : missingSourceFile.entrySet()) {
        sb.append(
            String.format("this file %s is add in new version%n", res.getValue().getFileName()));
      }
    }

    Files.deleteIfExists(
        Paths.get(System.getProperty("Enter location to prev result file")));
    File result = new File(
        System.getProperty("Enter Location to save result file and give name"));
    byte[] data = sb.toString().getBytes();
    Files.write(Paths.get(result.getAbsolutePath()), data);
  }
}
