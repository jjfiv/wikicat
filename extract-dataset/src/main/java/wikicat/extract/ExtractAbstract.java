package wikicat.extract;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.lemurproject.galago.utility.ByteUtil;
import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.utility.ZipUtil;
import wikicat.extract.util.*;
import org.jsoup.Jsoup;

import java.io.*;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Created by mhjang on 4/29/2015.
 * Read split zip files and extract abstracts
 */
public class ExtractAbstract {

    /**
     * return text before the first occurrence of <h3> </h3> tag, which is a marker for subsection heading
     * @param htmlText
     * @return
     */
    public static String extractAbstract(String htmlText) {
        Document d = Jsoup.parse(htmlText);
        Elements e = d.select("h3").prepend("END_OF_ABS");
        String plainText = d.text();
        if(e.size() > 0) {
            String abstractText = plainText.substring(0, plainText.indexOf("END_OF_ABS"));
            return abstractText;
        }
        else return plainText;
    }

    public static void main(String[] args) throws IOException {
         Parameters argp = Parameters.parseArgs(args);
         String inputDir = argp.getString("input");
        // String inputDir = "C:/Users/mhjang/Desktop/split_wiki_html/split1";
        //   String outputName = argp.getString("output")

        DirectoryReader dr = new DirectoryReader(inputDir);
        SimpleFileWriter sw = new SimpleFileWriter("split0.txt");
        for (String filePath : dr.getFilePathList()) {
            try (ZipFile zf = ZipUtil.open(filePath)) {
                for (String entry : ZipUtil.listZipFile(zf)) {
                    InputStream is = zf.getInputStream(zf.getEntry(entry));
                    BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    String line = null;
                    StringBuilder wikiHtmlBuilder = new StringBuilder();
                    while((line = br.readLine())!=null) {
                        wikiHtmlBuilder.append(line + "\n");
                    }
                    String abstractText = extractAbstract(wikiHtmlBuilder.toString());
                    sw.writeLine("<DOC>");
                    sw.writeLine("<DOCNO> " + StrUtil.removeBack(entry, ".html.html") + "</DOCNO>");
                    sw.writeLine("<TEXT>");
                    sw.writeLine(abstractText);
                    sw.writeLine("</TEXT>");
                    sw.writeLine("</DOC>");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
