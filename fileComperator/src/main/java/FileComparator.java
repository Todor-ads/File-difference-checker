import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * LCS based file comparator
 *
 * @author tstoyanov, Ivo Popov
 */
public class FileComparator {

  private ArrayList<String> sequencesList;

  private boolean isHTMLFormat;

  private static final String EDITED_COLOR = "#CB6D6D";

  private static final String DELETED_COLOR = "#CB6D6D";

  private static final String INSERTED_COLOR = "#99FFCC";

  /**
   * Compares the two files given as parameters
   *
   * @param file1
   * @param file2
   * @param fileName
   * @return string containing only the differences between the two files
   * @throws IOException
   */
  public String Compare(Path file1, Path file2, String fileName) throws IOException {

    String text1 = new String(Files.readAllBytes(Paths.get(file1.toUri())), StandardCharsets.UTF_8);
    String text2 = new String(Files.readAllBytes(Paths.get(file2.toUri())), StandardCharsets.UTF_8);
    this.isHTMLFormat = false;

    text1 = this.removeNewLineBetweenOpenAndCloseTags(text1);
    text2 = this.removeNewLineBetweenOpenAndCloseTags(text2);

    text1 = this.normalizeTextAndPrepareForHTMLOutput(text1);
    text2 = this.normalizeTextAndPrepareForHTMLOutput(text2);
    this.sequencesList = this.createSequencesList(text1, text2);
    String result = this.putColorMarksSideBySide(text1, text2, this.sequencesList, EDITED_COLOR,
        INSERTED_COLOR, DELETED_COLOR, fileName);

    return result;
  }

  /**
   * Compares the two files given as parameters and write the differences in HTML formatted file
   *
   * @param file1
   * @param file2
   * @param output
   * @param fileName
   * @throws IOException
   */
  public void Compare(Path file1, Path file2, Path output, String fileName) throws IOException {

    String text1 = new String(Files.readAllBytes(Paths.get(file1.toUri())), StandardCharsets.UTF_8);
    String text2 = new String(Files.readAllBytes(Paths.get(file2.toUri())), StandardCharsets.UTF_8);

    this.isHTMLFormat = true;
    text1 = this.normalizeTextAndPrepareForHTMLOutput(text1);
    text2 = this.normalizeTextAndPrepareForHTMLOutput(text2);
    this.sequencesList = this.createSequencesList(text1, text2);
    String result = this.putColorMarksSideBySide(text1, text2, this.sequencesList, EDITED_COLOR,
        INSERTED_COLOR, DELETED_COLOR, fileName);
    PrintWriter pw = new PrintWriter(output.toFile());
    pw.write(result);
    pw.close();
  }

  private String removeNewLineBetweenOpenAndCloseTags(String text) {
    text = text.replace("\n", "\r\n");
    String[] el = Arrays.stream(text.split("\r\n")).filter(x -> !x.trim().equals(""))
        .toArray(String[]::new);
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < el.length; i++) {
      String tempEl = el[i];
      if (!tempEl.trim().endsWith(">")) {
        int count = i;

        while (!tempEl.trim().endsWith(">")) {
          sb.append(tempEl.replaceAll("\r", " ").replaceAll("\r\n", " ").replaceAll("\n", " "));
          count++;
          if (count >= el.length) {
            System.out.println();
          }
          tempEl = el[count];
        }
        tempEl = tempEl.replaceAll(" />", "/>").replaceAll(" >", ">").replaceAll("> ", ">")
            .replaceAll("/> ", "/>");
        sb.append(tempEl + "\n");
        i = count;
      }
      else {
        tempEl = tempEl.replaceAll(" />", "/>").replaceAll(" >", ">").replaceAll("> ", ">")
            .replaceAll("/> ", "/>");
        sb.append(tempEl + "\n");
      }
    }
    return sb.toString();
  }

  /**
   * Normalizes given string with XML/HTML contents by replace <, >, \n, and \t with HTML tags
   * and/or entities.
   *
   * @param text - raw input string
   * @return normalized string
   */
  private String normalizeTextAndPrepareForHTMLOutput(String text) {

    text = text.trim();
    text = text.replace("<", "&lt;");
    text = text.replace(">", "&gt;");
    text = text.replace("\r\n", "<br> ");
    text = text.replace("\n", "<br> ");
    text = text.replace("\t", " ");

    while (text.contains("  ")) {
      text = text.replace("  ", " ");
    }

    return text;
  }

  /**
   * Finds a list of common sequences of given two texts.
   *
   * @param text1
   * @param text2
   * @return sequences list
   */
  private ArrayList<String> createSequencesList(String text1, String text2) {

    String[] text1Words = text1.split(" ");
    String[] text2Words = text2.split(" ");
    int text1WordCount = text1Words.length;
    int text2WordCount = text2Words.length;

    int[][] solutionMatrix = new int[text1WordCount + 1][text2WordCount + 1];

    for (int i = text1WordCount - 1; i >= 0; i--) {
      for (int j = text2WordCount - 1; j >= 0; j--) {
        if (text1Words[i].equals(text2Words[j])) {
          solutionMatrix[i][j] = solutionMatrix[i + 1][j + 1] + 1;
        }
        else {
          solutionMatrix[i][j] = Math.max(solutionMatrix[i + 1][j], solutionMatrix[i][j + 1]);
        }
      }
    }

    int i = 0, j = 0;
    ArrayList<String> resultList = new ArrayList<String>();
    while (i < text1WordCount && j < text2WordCount) {
      if (text1Words[i].equals(text2Words[j])) {
        resultList.add(text2Words[j]);
        i++;
        j++;
      }
      else if (solutionMatrix[i + 1][j] >= solutionMatrix[i][j + 1]) {
        i++;
      }
      else {
        j++;
      }
    }

    return resultList;
  }

  /**
   * Put colored marks for edited/inserted/deleted text. Views a two compared files side by side.
   *
   * @param text1
   * @param text2
   * @param sequencesList
   * @param editedColor
   * @param insertedColor
   * @param deletedColor
   * @param fileName
   * @return colored result text in HTML format or txt file whit difference
   */
  private String putColorMarksSideBySide(String text1, String text2,
      ArrayList<String> sequencesList, String editedColor, String insertedColor,
      String deletedColor, String fileName) {

    StringBuffer stringBufferLeft = new StringBuffer();
    StringBuffer stringBufferRight = new StringBuffer();

    StringBuilder finalLeftSide = new StringBuilder();
    StringBuilder finalRightSide = new StringBuilder();

    finalLeftSide
        .append("<div style=\"float:left;width:50%;overflow:scroll;white-space: nowrap;\">");
    finalRightSide
        .append("<div style=\"float:left;width:50%;overflow:scroll;white-space: nowrap;\">");

    if (text1 != null && sequencesList != null) {
      String[] text1Words = text1.split(" ");
      String[] text2Words = text2.split(" ");

      int i = 0, j = 0, word1LastIndex = 0, word2LastIndex = 0;
      for (int k = 0; k < sequencesList.size(); k++) {
        for (i = word1LastIndex, j = word2LastIndex; i < text1Words.length
            && j < text2Words.length;) {

          if (text1Words[i].equals(sequencesList.get(k))
              && text2Words[j].equals(sequencesList.get(k))) {
            stringBufferLeft.append("<SPAN>" + sequencesList.get(k) + " </SPAN>");
            stringBufferRight.append("<SPAN>" + sequencesList.get(k) + " </SPAN>");
            word1LastIndex = i + 1;
            word2LastIndex = j + 1;
            i = text1Words.length;
            j = text2Words.length;
          }
          else if (!text1Words[i].equals(sequencesList.get(k))
              && !text2Words[j].equals(sequencesList.get(k))) {
            for (; i < text1Words.length && !text1Words[i].equals(sequencesList.get(k)); i++) {
              stringBufferLeft.append("<SPAN style=\"background-color:" + deletedColor + "\">"
                  + text1Words[i] + " </SPAN>");
            }
            for (; j < text2Words.length && !text2Words[j].equals(sequencesList.get(k)); j++) {
              stringBufferRight.append("<SPAN style=\"background-color:" + insertedColor + "\">"
                  + text2Words[j] + " </SPAN>");
            }
          }
          else if (!text1Words[i].equals(sequencesList.get(k))) {
            for (; i < text1Words.length && !text1Words[i].equals(sequencesList.get(k)); i++) {
              stringBufferLeft.append("<SPAN style=\"background-color:" + deletedColor + "\">"
                  + text1Words[i] + " </SPAN>");
              if (text1Words[i].endsWith("<br>")) {
                stringBufferRight.append("<br> ");

              }
            }
          }
          else if (!text2Words[j].equals(sequencesList.get(k))) {
            for (; j < text2Words.length && !text2Words[j].equals(sequencesList.get(k)); j++) {
              stringBufferRight.append("<SPAN style=\"background-color:" + insertedColor + "\">"
                  + text2Words[j] + " </SPAN>");
              if (text2Words[j].endsWith("<br>")) {
                stringBufferLeft.append("<br> ");
              }
            }
          }
          if (stringBufferLeft.toString().endsWith("<br> </SPAN>")
              || stringBufferRight.toString().endsWith("<br> </SPAN>")) {
            boolean isAddedLeft = false;
            boolean isAddedRight = false;
            long countBRLeft = Arrays.stream(stringBufferLeft.toString().split(" "))
                .filter(x -> x.endsWith("<br>")).count();
            long countBRRight = Arrays.stream(stringBufferRight.toString().split(" "))
                .filter(x -> x.endsWith("<br>")).count();
            if (countBRLeft != countBRRight) {
              if (countBRLeft > countBRRight) {
                if (stringBufferRight.toString().contains("background-color")) {
                  finalRightSide.append(stringBufferRight);
                  isAddedRight = true;
                }
                for (int l = 0; l < Math.abs(countBRLeft - countBRRight); l++) {
                  finalRightSide.append("<SPAN style=\"background-color:#CB6D6D\"><br> </SPAN>");
                }
              }
              else {
                if (stringBufferLeft.toString().contains("background-color")) {
                  finalLeftSide.append(stringBufferLeft);
                  isAddedLeft = true;
                }
                for (int l = 0; l < Math.abs(countBRLeft - countBRRight); l++) {
                  finalLeftSide.append("<SPAN style=\"background-color:#CB6D6D\"><br> </SPAN>");
                }
              }
            }

            if (!isAddedLeft) {
              finalLeftSide.append(stringBufferLeft);
            }
            if (!isAddedRight) {
              finalRightSide.append(stringBufferRight);
            }

            stringBufferLeft = new StringBuffer();
            stringBufferRight = new StringBuffer();

          }
        }
      }
      for (; word1LastIndex < text1Words.length; word1LastIndex++) {
        stringBufferLeft.append("<SPAN style=\"background-color:" + deletedColor + "\">"
            + text1Words[word1LastIndex] + " </SPAN>");
        if (text1Words[word1LastIndex].endsWith("<br>")) {
          stringBufferRight.append("<br>");

        }
      }
      for (; word2LastIndex < text2Words.length; word2LastIndex++) {
        stringBufferRight.append("<SPAN style=\"background-color:" + insertedColor + "\">"
            + text2Words[word2LastIndex] + " </SPAN>");
        if (text2Words[word2LastIndex].endsWith("<br>")) {
          stringBufferLeft.append("<br>");

        }
      }
    }

    stringBufferLeft.append("</div>");
    stringBufferRight.append("</div>");

    finalLeftSide.append("</div>");
    finalRightSide.append("</div>");

    if (this.isHTMLFormat) {
      return finalLeftSide.toString() + finalRightSide.toString();
    }
    else {
      return this.fixBrokenTags(finalLeftSide, finalRightSide, fileName);
    }
  }

  /**
   * Fix broken tags and add sequential row number
   *
   * @param leftInput
   * @param rightInput
   * @return a string containing only the differences between the two texts and the order they were
   *         found
   */
  private String fixBrokenTags(StringBuilder leftInput, StringBuilder rightInput, String fileName) {
    String[] inputLeftArr = leftInput.toString().split("<br> ");
    String[] inputRightArr = rightInput.toString().split("<br> ");

    List<String> resultSetLeft = new ArrayList<>();
    List<String> resultSetRight = new ArrayList<>();
    StringBuilder result = new StringBuilder();
    result.append(fileName + System.lineSeparator());

    for (int i = 0; i < inputLeftArr.length; i++) {
      String left = inputLeftArr[i];
      String right = inputRightArr[i];

      if (!left.contains("background-color:") && !left.equals("</SPAN>") && !left.equals("")
          && right.contains("background-color:")) {
        int count = i;
        String tempEl = "";
        while (tempEl.equals("")) {
          count++;
          tempEl = inputLeftArr[count];
        }
        // resultSetLeft.add(left + " " + tempEl+ "<br >" );
        resultSetLeft.add("<br>");
        resultSetRight.add(right + "<br>");
        inputLeftArr[i] = "";
        inputLeftArr[count] = (left + "style=\"background-color:#ffffff\"" + tempEl);
      }
      else if (left.contains("background-color:") && !right.contains("background-color:")
          && !right.equals("</SPAN>") && !right.equals("")) {
        int count = i;
        String tempEl = "";
        while (tempEl.equals("")) {
          count++;
          tempEl = inputRightArr[count];
        }
        resultSetLeft.add(left + "<br>");
        resultSetRight.add("<br>");

        inputRightArr[i] = "";
        inputRightArr[count] = (right + "style=\"background-color:#ffffff\"" + tempEl);

      }
      else {
        resultSetLeft.add(left + "<br>");
        resultSetRight.add(right + "<br>");
      }
    }

    for (int i = 1; i <= resultSetLeft.size(); i++) {
      String leftEl = resultSetLeft.get(i - 1);
      String rightEl = resultSetRight.get(i - 1);
      String replaceLeftEl = leftEl.replaceAll("<(?!/?span\\b)[^>]*>", "").replaceAll("&lt;", "<")
          .replaceAll("&gt;", ">");
      String replaceRightEl = rightEl.replaceAll("<(?!/?span\\b)[^>]*>", "").replaceAll("&lt;", "<")
          .replaceAll("&gt;", ">");

      if (!leftEl.equals(rightEl) && !replaceLeftEl.equals(replaceRightEl)) {
        if (leftEl.equals("") || leftEl.equals("</SPAN>") || leftEl.equals("<br>")
            || leftEl.equals("</SPAN><br>")
            || leftEl.equals("</SPAN><SPAN style=\"background-color:#CB6D6D\"><br>")) {
          result.append(String.format("Line %d %s is add in new version%n", i,
              rightEl.replaceAll("<(?!/?span\\b)[^>]*>", "").replaceAll("&lt;", "<")
                  .replaceAll("&gt;", ">")));
        }
        else if (rightEl.equals("") || rightEl.equals("</SPAN>") || rightEl.equals("<br>")
            || rightEl.equals("</SPAN><br>")
            || rightEl.equals("</SPAN><SPAN style=\"background-color:#99FFCC\"><br>")) {
          result.append(String.format("Line %d %s is missing in new version%n", i,
              leftEl.replaceAll("<(?!/?span\\b)[^>]*>", "").replaceAll("&lt;", "<")
                  .replaceAll("&gt;", ">")));
        }
        else if (!leftEl.contains("style=\"background-color:#ffffff\"")
            || rightEl.contains("style=\"background-color:#ffffff\"")) {
          result.append(String.format("Line %d element %s is edit to %s in new version%n", i,
              replaceLeftEl, replaceRightEl));
        }

      }
    }
    if (result.toString().trim().equals(fileName)) {
      result.append("No difference found!");
      return result.toString();
    }

    return result.toString();
  }
}
