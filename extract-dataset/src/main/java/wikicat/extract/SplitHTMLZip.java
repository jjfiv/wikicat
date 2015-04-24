package wikicat.extract;

import org.lemurproject.galago.utility.ByteUtil;
import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.utility.StreamCreator;
import org.lemurproject.galago.utility.ZipUtil;
import wikicat.extract.util.IO;
import wikicat.extract.util.StrUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Take an ${input} zip file full of HTML files and push it to a smaller ine under split%d/${output}
 * @author jfoley
 */
public class SplitHTMLZip {
  public static void main(String[] args) throws IOException {
    Parameters argp = Parameters.parseArgs(args);
    String inputFile = argp.getString("input");
    String outputName = argp.getString("output");

    List<ZipOutputStream> outputBySplit = new ArrayList<>();
    for (int i = 0; i < Constants.NumSplits; i++) {
      outputBySplit.add(new ZipOutputStream(StreamCreator.openOutputStream(
          String.format("split%d/%s", i, outputName))));
    }

    try (ZipFile zf = ZipUtil.open(inputFile)) {
      for (String entry : ZipUtil.listZipFile(zf)) {
        String name = StrUtil.removeBack(entry, ".html");
        int split = name.hashCode() % Constants.NumSplits;
        String data = IO.slurp(ZipUtil.readZipEntry(zf, entry));
        if(data.contains("#REDIRECT")) {
          continue;
        }
        ZipOutputStream zos = outputBySplit.get(split);
        ZipUtil.write(zos, entry +".html", ByteUtil.fromString(data));
      }
    }

    for (ZipOutputStream zipOutputStream : outputBySplit) {
      zipOutputStream.close();
    }
  }
}
