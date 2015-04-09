package wikicat.extract.util;

import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class XMLTest {

  @Test
  public void testVisitDocs() throws IOException, XMLStreamException {
    String testInput = "<docs>" +
        "<doc><title>Title0</title><body>Body0</body></doc>" +
        "<doc><title>Title1</title><body>Body1</body></doc>" +
        "<doc><title>Title2</title><body>Body2</body></doc>" +
        "</docs>";

    final ArrayList<String> titles = new ArrayList<>();
    XML.forFieldsInSections(IO.stringStream(testInput), "doc", Arrays.asList("title", "body"), new XML.FieldsFunctor() {
      @Override
      public void process(Map<String, String> fieldValues) {
        String title = fieldValues.get("title");
        String body = fieldValues.get("body");
        if(title.isEmpty() && body.isEmpty()) {
          return;
        }
        titles.add(title);
        assertTrue(title.startsWith("Title"));
        assertTrue(body.startsWith("Body"));
      }
    });

    assertEquals(3,titles.size());
    assertEquals("Title0",titles.get(0));
    assertEquals("Title1",titles.get(1));
    assertEquals("Title2",titles.get(2));
  }

}