package wikicat.extract;

import org.lemurproject.galago.utility.ByteUtil;
import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.utility.StreamCreator;
import org.lemurproject.galago.utility.ZipUtil;
import org.lemurproject.galago.utility.tools.AppFunction;
import wikicat.extract.util.SGML;
import wikicat.extract.util.StrUtil;
import wikicat.extract.util.Util;
import wikicat.extract.util.XML;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;


/**
 * @author jfoley.
 */
public class WikipediaToHTML extends AppFunction {
  @Override
  public String getName() {
    return "wikipedia-to-html";
  }

  @Override
  public String getHelpString() {
    return makeHelpStr("input", "path to raw wikipedia xml bz2 inputs");
  }

  @Override
  public void run(Parameters argp, PrintStream out) throws Exception {
    System.err.println(argp);
    List<File> inputFiles = Util.checkAndExpandPaths(argp.getAsList("input", String.class));

    System.err.println(argp.getAsList("input", String.class));
    System.err.println(inputFiles);
    // write zip file:
    final ZipOutputStream zos = new ZipOutputStream(StreamCreator.openOutputStream(argp.getString("output")));

    try {
      for (File fp : inputFiles) {
        XML.forFieldsInSections(fp, "page", Arrays.asList("title", "text"), new XML.FieldsFunctor() {
          @Override
          public void process(Map<String, String> data) {
            String pageTitle = data.get("title").replace(' ', '_');
            System.err.println(pageTitle);
            if (pageTitle.isEmpty() || pageTitle.startsWith("Template") || pageTitle.startsWith("User"))
              return;
            String body = WikipediaToHTML.process(pageTitle, data.get("text"));
            String html = String.format("<html><head><title>%s</title></head><body>%s</body></html>", pageTitle, body);

            try {
              ZipUtil.write(zos, pageTitle + ".html", ByteUtil.fromString(html));
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }
        });
      }
    } finally {
      zos.close();
    }
  }

  private static String process(final String title, String text) {
    text = SGML.removeComments(text);
    text = SGML.removeTag(text, "ref");
    text = WikiCleaner.unescapeAmpersandEscapes(text);
    text = WikiCleaner.cleanWikiTags(text);
    text = WikiCleaner.killWikiTables(text);
    text = text.replaceAll("''", ""); // ditch all italics
    text = StrUtil.transformBetween(text, WikiTemplateHack.templateStart, WikiTemplateHack.templateEnd, new StrUtil.Transform() {
      @Override
      public String transform(String input) {
        return processTemplate(title, input);
      }
    });
    text = WikiCleaner.convertLinks(title, text);
    text = WikiCleaner.convertHeaders(text);

    return text;
  }

  private static String processTemplate(String title, String input) {
    try {
      if (input.isEmpty()) return "";
      String targs[] = input.split("\\|");
      if (targs.length == 0) return "";
      String templateName = StrUtil.compactSpaces(targs[0].toLowerCase());

      if (WikiTemplateHack.isSimpleIgnore(templateName))
        return "";
      if (WikiTemplateHack.isCitationNeeded(templateName))
        return "";
      if (WikiTemplateHack.isCitation(templateName))
        return "";

      if (templateName.equals("refbegin") || templateName.equals("refend")) {
        return "";
      }

      String output = WikiTemplateHack.processStylisticTemplate(targs);
      if (output != null) return output;

      if (targs.length == 2) {
        return WikiCleaner.internalLink(targs[0], targs[1]);
      }
    } catch (Exception ex) {
      System.err.println("#caught!");
      ex.printStackTrace(System.err);
    }

    System.err.println("#unk "  + title + ": " + input);
    return " <template>"+input.replace('|', ' ')+"</template> ";
  }
}
