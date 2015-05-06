package wikicat.extract.experiments;

import gnu.trove.list.array.TDoubleArrayList;
import org.lemurproject.galago.core.eval.QueryJudgments;
import org.lemurproject.galago.core.eval.QuerySetJudgments;
import org.lemurproject.galago.utility.Parameters;
import wikicat.extract.util.IO;
import wikicat.extract.util.StrUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author jfoley
 */
public class TrecRunReranker {


  public static void main(String[] args) throws IOException {
    Parameters argp = Parameters.parseArgs(args);
    String outputFileName = argp.getString("output");
    Map<String, Map<String, Map<String, Double>>> queryToDocumentToFeatures = new HashMap<>();
    Map<String, QueryJudgments> judgments = QuerySetJudgments.loadJudgments(argp.getString("judgments"), true, true);

    Set<String> featureNames = new HashSet<>();
    for (String inputName : argp.getAsList("input", String.class)) {
      // load each trecrun file:
      for (String line : IO.slurpLines(inputName)) {
        String[] cols = line.split("\\s+");
        String qid = cols[0];
        String doc = cols[2];
        int rank = Integer.parseInt(cols[3]);
        double score = Double.parseDouble(cols[4]);

        Map<String, Map<String, Double>> queryInfo = queryToDocumentToFeatures.get(qid);
        if(queryInfo == null) {
          queryInfo = new HashMap<>();
          queryToDocumentToFeatures.put(qid, queryInfo);
        }
        Map<String,Double> docFeatures = queryInfo.get(doc);
        if(docFeatures == null) {
          docFeatures = new HashMap<>();
          queryInfo.put(doc, docFeatures);
        }
        docFeatures.put(inputName, score);
        featureNames.add(inputName);
      }
    }

    List<String> featureNumTable = new ArrayList<>(featureNames);

    try (PrintWriter out = IO.printWriter(outputFileName)) {
      for (Map.Entry<String, Map<String, Map<String, Double>>> qidToRest : queryToDocumentToFeatures.entrySet()) {
        String qid = qidToRest.getKey();
        QueryJudgments queryJudgments = judgments.get(qid);

        for (Map.Entry<String, Map<String, Double>> docToFeatures : qidToRest.getValue().entrySet()) {
          String doc = docToFeatures.getKey();

          int rel = 0;
          if (queryJudgments != null) {
            rel = queryJudgments.get(doc);
          }

          StringBuilder featureBuilder = new StringBuilder();
          TDoubleArrayList scores = new TDoubleArrayList();
          for (Map.Entry<String, Double> kv : docToFeatures.getValue().entrySet()) {
            String feature = kv.getKey();
            int featureNum = featureNumTable.indexOf(feature);
            double value = kv.getValue();
            scores.add(value);
            featureBuilder.append(' ').append(featureNum).append(":").append(value);
          }

          int MAX_FEATURE = featureNumTable.size();
          int MIN_FEATURE = MAX_FEATURE + 1;
          int SUM_FEATURE = MIN_FEATURE + 1;
          int AVG_FEATURE = SUM_FEATURE + 1;
          featureBuilder.append(' ').append(MAX_FEATURE).append(":").append(scores.max());
          featureBuilder.append(' ').append(MIN_FEATURE).append(":").append(scores.min());
          featureBuilder.append(' ').append(SUM_FEATURE).append(":").append(scores.sum());
          featureBuilder.append(' ').append(AVG_FEATURE).append(":").append(scores.sum() / ((double) scores.size()));

          out.printf("%d qid:%s %s # %s\n", rel, qid, StrUtil.compactSpaces(featureBuilder.toString()), doc);
        }
      }
    }


  }
}
