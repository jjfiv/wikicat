package wikicat.extract.util;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author jfoley.
 */
public class SGML {
  public static interface TransformTag {
    public String process(Map<String,String> attrs, String body);
  }

  /**
   * Hack, doesn't support attributes.
   */
  public static String getTagContents(String xml, String tagName) {
    int startIndex = xml.indexOf("<"+tagName+">")+2+tagName.length();
    int endIndex = xml.indexOf("</"+tagName+">");
    assert(startIndex > 0);
    assert(endIndex > 0);
    return xml.substring(startIndex, endIndex);
  }

  public static String removeTag(String input, String tagName) {
    // do self-closing or content-based
    return StrUtil.removeBetween(input, Pattern.compile("<" + tagName), Pattern.compile("/\\s*>|</" + tagName + ">"));
  }

  public static String removeTagsLeaveContents(String input) {
    return input.replaceAll("<[^>]+>", "");
  }

  public static String removeComments(String input) {
    return StrUtil.removeBetweenNested(input, "<!--", "-->");
  }


}
