package wikicat.extract;

import org.lemurproject.galago.utility.Parameters;
import wikicat.extract.util.IO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Clean up errors and encode category information in SRC, LABEL format.
 * When SRC matches Category:* then it's information about the category labels themselves.
 * @author jfoley
 */
public class ConvertRawData {
  public static void main(String[] args) throws IOException {
    int total = 0;
    try (
        PrintWriter out = IO.printWriter("data/graph.tsv.gz");
        BufferedReader reader = IO.fileReader("data/graph.gz")
    ) {
      while(true) {
        if(++total % 10000 == 0) {
          System.err.println(total);
        }
        String line = reader.readLine();
        if(line == null) break;
        try {
          Parameters link = Parameters.parseString(line);
          String page = link.getString("src");
          String categoryLabel = link.getString("cat");
          out.printf("%s\t%s\n", page, categoryLabel);
        } catch (IOException e) {
          System.err.println(line);
        }
      }
    }
  }
}
