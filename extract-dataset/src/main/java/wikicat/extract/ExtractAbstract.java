package wikicat.extract;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.utility.ZipUtil;
import wikicat.extract.util.DirectoryReader;
import wikicat.extract.util.SimpleFileWriter;
import wikicat.extract.util.StrUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipFile;

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
       //  Parameters argp = Parameters.parseArgs(args);
       //  String inputDir = argp.getString("input");
         String inputDir = "C:/Users/mhjang/Desktop/split_wiki_html/split0";
        String inputDir2 = "C:/Users/mhjang/Desktop/split_wiki_html/split1";
        String inputDir3 = "C:/Users/mhjang/Desktop/split_wiki_html/split2";
        String inputDir4 = "C:/Users/mhjang/Desktop/split_wiki_html/split3";

        String[] dirs = {inputDir, inputDir2, inputDir3, inputDir4};

        //       String outputName = argp.getString("output")

        DirectoryReader dr = new DirectoryReader(dirs);

        SimpleFileWriter sw;
        SimpleFileWriter sw0 = new SimpleFileWriter("mini_split0.txt");
        SimpleFileWriter sw1 = new SimpleFileWriter("mini_split1.txt");
        SimpleFileWriter sw2 = new SimpleFileWriter("mini_split2.txt");
        SimpleFileWriter sw3 = new SimpleFileWriter("mini_split3.txt");
        SimpleFileWriter sw4 = new SimpleFileWriter("mini_split4.txt");
        SimpleFileWriter docList = new SimpleFileWriter("list.txt");



        int docCount = 0;
        sw = sw0;
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
                    if(docCount == 50000) {
                        sw.close();
                        break;
                    }
                    else if(docCount == 40000) {
                        sw.close();
                        sw = sw4;
                    }
                    else if(docCount == 30000) {
                        sw.close();
                        sw = sw3;
                    }
                    else if(docCount == 20000) {
                        sw.close();
                        sw = sw2;
                    }
                    else if(docCount == 10000) {
                        sw.close();
                        sw = sw1;
                    }
                    String title = StrUtil.removeBack(entry, ".html.html");
                    sw.writeLine("<DOC>");
                    sw.writeLine("<DOCNO>" + title  + "</DOCNO>");
                    sw.writeLine("<TEXT>");
                    sw.writeLine(abstractText);
                    sw.writeLine("</TEXT>");
                    sw.writeLine("</DOC>");
                    docList.writeLine(docCount + "\t" + title);
                    docCount++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        docList.close();


    }
}
