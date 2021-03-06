package wikicat.extract;

import org.lemurproject.galago.utility.Parameters;
import wikicat.extract.util.SGML;
import wikicat.extract.util.StrUtil;

import java.util.regex.Pattern;

/**
 * @author jfoley.
 */
public class WikiCleaner {
  public static String removeReferences(String input) {
    input = SGML.removeTag(input, "ref");
    input = StrUtil.removeBetweenNested(input, "{{refbegin", "{{refend}}");
    return input;
  }

  public static String cleanWikiTags(String input) {
    return input.replaceAll("</?(onlyinclude|includeonly)>", "");
  }

  public static String killWikiTables(String input) {
    return StrUtil.removeBetweenNested(input, "{|", "|}");
  }

  public static String convertLinks(String title, String input) {
    return convertExternalLinks(convertInternalLinks(title, input));
  }

  /** Do internal first [[ ]], then external [ ] */
  public static String convertExternalLinks(String input) {
    return StrUtil.transformBetween(input, Pattern.compile("\\["), Pattern.compile("\\]"), new StrUtil.Transform() {
      @Override
      public String transform(String input) {
        String url = input;
        String text = "link";
        if (input.contains(" ")) {
          url = StrUtil.takeBefore(input, " ");
          text = StrUtil.takeAfter(input, " ");
        }
        return String.format("<a href=\"%s\">%s</a>", url, text);
      }
    });
  }

  /** Do internal first [[ ]], then external [ ] */
  public static String convertInternalLinks(final String title, String input) {
    return StrUtil.transformBetween(input, Pattern.compile("\\[\\["), Pattern.compile("\\]\\]"), new StrUtil.Transform() {
      @Override
      public String transform(String input) {
        if(input.isEmpty()) return "";
        if(input.charAt(0) == ':') { // special category sort of link
          return "";
        } else if(input.startsWith("File:") || input.startsWith("Image:")) {
          return "";
        } else if(input.startsWith("Category:")) {
          return handleCategoryLink(title, input);
        }

        String url;
        String text;

        if(input.contains("|")) {
          url = StrUtil.takeBefore(input, "|");
          text = StrUtil.takeAfter(input, "|");
        } else {
          url = input;
          text = input;
        }

        return internalLink(url, text);
      }
    });
  }

  static String handleCategoryLink(String title, String input) {
    Parameters mapping = Parameters.parseArray("src", title, "cat", input);
    System.out.println("CATEGORY: " + mapping.toString());
    //return "<category>"+StrUtil.takeAfter(input, ":")+"</category>";
    return "";
  }

  public static int getHeaderLevel(String input) {
    input = StrUtil.compactSpaces(input);
    if(!input.startsWith("=")) return 0;
    if(input.startsWith("====")) return 4;
    if(input.startsWith("===")) return 3;
    if(input.startsWith("==")) return 2;
    if(input.startsWith("=")) return 1;
    return 0;
  }
  public static String convertHeaders(String input) {
    return StrUtil.transformLines(input, new StrUtil.Transform() {
      @Override
      public String transform(String input) {
        int headerLevel = getHeaderLevel(input);
        if(headerLevel <= 0) return input+'\n';
        String cleaned = StrUtil.compactSpaces(input.replace('=', ' '));
        return String.format("<h%d>%s</h%d>\n", headerLevel, cleaned, headerLevel);
      }
    });
  }

  public static String internalLink(String page, String text) {
    String url = page.replaceAll("\\s", "_");
    return String.format("<a href=\"https://en.wikipedia.org/wiki/%s\">%s</a>", url, text);
  }

  public static String stripWikiUrlToTitle(String url) {
    return makeWikipediaTitle(StrUtil.removeFront(url, "https://en.wikipedia.org/wiki/"));
  }
  public static String makeWikipediaTitle(String input) {
    if(input.isEmpty()) return "";
    String fixed = input.replaceAll("\\s+", "_");
    if(Character.isLowerCase(fixed.charAt(0))) {
      return Character.toUpperCase(fixed.charAt(0))+fixed.substring(1);
    }
    return StrUtil.removeBack(fixed, ".html");
  }
  public static String unescapeAmpersandEscapes(String input) {
    return input.replaceAll("&(n|m)dash;", "-");
  }

  public static String clean(String input) {
    return clean("test", input);
  }

  public static String clean(String title, String input) {
    input = removeReferences(input);
    input = unescapeAmpersandEscapes(input);
    input = cleanWikiTags(input);
    input = killWikiTables(input);
    input = SGML.removeComments(input);
    input = input.replaceAll("'{2,3}", ""); // ditch all italics
    input = WikiTemplateHack.convertTemplates(title, input);
    input = convertLinks(title, input);
    if(input.contains("Category:")) {
      System.out.println("unifinished_business: "+title);
    }
    return input;
  }
}
