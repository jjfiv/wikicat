package wikicat.extract;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.TagTokenizer;
import org.lemurproject.galago.core.parse.stem.KrovetzStemmer;
import org.lemurproject.galago.core.parse.stem.Stemmer;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.RetrievalFactory;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.utility.Parameters;
import wikicat.extract.util.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * Created by mhjang on 5/4/2015.
 */
public class FeatureFactory {

    HashMap<String, Integer> queryIdMap = new HashMap<String, Integer>();
    HashMap<String, Integer> featureIdMap = new HashMap<String, Integer>();
    private int queryIdCursor = 1;
    private int featureIdCursor = 1;
 //   NGramReader nGramReader;
    StopWordRemover stRemover;
    public FeatureFactory() throws IOException {
        stRemover = new StopWordRemover();
    //    nGramReader = new NGramReader();
     }

    private int encodeQuery(String q) {
        if(!queryIdMap.containsKey(q)) {
            queryIdMap.put(q, queryIdCursor++);
        }
        return queryIdMap.get(q)%1000000;
    }

    private int encodeFeature(String f) {
        if(!featureIdMap.containsKey(f)) {
            featureIdMap.put(f, featureIdCursor++);
        }
        return featureIdMap.get(f)%100000;
    }


    public void fixFile() throws IOException {
        SimpleFileReader sr = new SimpleFileReader("C:\\Users\\mhjang\\IdeaProjects\\wikicatnew\\train.txt");
        SimpleFileWriter sw = new SimpleFileWriter("C:\\Users\\mhjang\\IdeaProjects\\wikicatnew\\train_.txt");
        while(sr.hasMoreLines()) {
            String line = sr.readLine();
            line = line.replaceAll(": 1", ":1");
            sw.writeLine(line);
        }
        sw.close();
    }
    public void generateFeature() throws Exception {
        SimpleFileReader sr = new SimpleFileReader("C:\\Users\\mhjang\\IdeaProjects\\wikicatnew\\list.txt");
        LinkedList<String> docList = new LinkedList<>();
        int count = 0;
        while (sr.hasMoreLines()) {
            String line = sr.readLine();
            docList.add(line.split("\t")[1]);
            if (count++ == 1000) break;
        }

        SimpleFileReader sr2 = new SimpleFileReader("C:\\Users\\mhjang\\Desktop\\graph.tsv");
        // First count the number of relevant categories to get the number of positive examples
        HashMap<String, LinkedList<String>> relCat = new HashMap<String, LinkedList<String>>();
        while (sr2.hasMoreLines()) {
            String line = sr2.readLine();
         //   System.out.println(line);
            String[] tokens = line.split("\t");
            if(tokens.length == 2) {
                String query = tokens[0];
                String category = tokens[1];
                if (query.contains("Category:") || !docList.contains(query)) continue;
                if (!relCat.containsKey(category))
                    relCat.put(category, new LinkedList<String>());
                relCat.get(category).add(query);
        //        System.out.println(query + "\t" + category);
            }
        }

        // for each (wp, category) generate features
        Document.DocumentComponents dc = new Document.DocumentComponents(true, false, true);
        String jsonConfigFile = "C:\\Users\\mhjang\\IdeaProjects\\wikicatnew\\extract-dataset\\search.params";
        Parameters globalParams = Parameters.parseFile(jsonConfigFile);
        Retrieval retrieval = RetrievalFactory.instance(globalParams);

        System.out.println("# of categories: " + relCat.size());
        for(String category : relCat.keySet()) {
            LinkedList<String> wikiPages = relCat.get(category);
            for(String wp : wikiPages) {
                Multiset<Integer> featureSet = HashMultiset.create();
                Document d = retrieval.getDocument(wp, dc);
                // generate feature
                if(d.terms == null) continue;
                List<String> grams = getNGramFeatures(d.terms);
                // generate query
                System.out.print("1 qid:" + encodeQuery(category) + " ");
                for (String g : grams) {
                    featureSet.add(encodeFeature(g));
                }
                ArrayList<Integer> featureIds = new ArrayList<Integer>(featureSet.elementSet());
                Collections.sort(featureIds);
                for (Integer i : featureIds) {
                    System.out.print(i + ":" + 1 + " ");
                }
                System.out.println();
            }
        }

        Parameters p = Parameters.create();
        p.set("startAt", 0);
        p.set("requested", 20);
        p.set("metrics", "map");
        TagTokenizer tt = new TagTokenizer();

        for(String category : relCat.keySet()) {
            Document tokenizedCat = tt.tokenize(category);
            String categoryQuery = StrUtil.join(tokenizedCat.terms, " ");
            if(categoryQuery.isEmpty()) continue;
      //      System.out.println(categoryQuery);
            Node root = StructuredQuery.parse(categoryQuery);
            Node transformed = retrieval.transformQuery(root, p);
            List<ScoredDocument> results = retrieval.executeQuery(transformed).scoredDocuments;
            int cnt = 0;
            LinkedList<String> wikiPages = relCat.get(category);

            for(ScoredDocument sd : results) {
                if(!wikiPages.contains(sd.documentName)) {
                    Multiset<Integer> featureSet = HashMultiset.create();
                    // generate feature
                    Document d = retrieval.getDocument(sd.documentName, dc);
                    List<String> grams = getNGramFeatures(d.terms);
                    // generate query
                    System.out.print("0 qid:" + encodeQuery(category) + " ");
                    for (String g : grams) {
                        featureSet.add(encodeFeature(g));
                    }

                    ArrayList<Integer> featureIds = new ArrayList<Integer>(featureSet.elementSet());
                    Collections.sort(featureIds);
                    for (Integer i : featureIds) {
                        System.out.print(i + ":" + 1 + " ");
                    }
                    System.out.println();
                    cnt++;
                }
                if(cnt == wikiPages.size()) break;
            }

        }

    }


    public void generateTestData() throws Exception {
        SimpleFileReader sr = new SimpleFileReader("C:\\Users\\mhjang\\IdeaProjects\\wikicatnew\\list.txt");
        HashMap<String, String> qidMap = new HashMap<String, String>();
        while(sr.hasMoreLines()) {
            String line = sr.readLine();
            String[] tokens = line.split("\t");
            String docName =  tokens[0];
            String qid = tokens[1];
            qidMap.put(qid, docName);
        }

        sr = new SimpleFileReader("C:\\Users\\mhjang\\Desktop\\split0.direct.title-only.sdm.trecrun");
        HashMap<String, LinkedList<String>> categoryRankedList = new HashMap<String, LinkedList<String>>();
        while(sr.hasMoreLines()) {
            String line = sr.readLine();
            String[] tokens = line.split(" ");
            String qid = tokens[0];
            String category = "Category:" + tokens[2].replaceAll("\\_", " ");
            if(!categoryRankedList.containsKey(category))
                categoryRankedList.put(category, new LinkedList<String>());
            categoryRankedList.get(category).add(qidMap.get(qid));
        }

        LinkedList<String> docList = new LinkedList<>();
        SimpleFileReader sr2 = new SimpleFileReader("C:\\Users\\mhjang\\Desktop\\graph.tsv");

        int count = 0;
        while (sr2.hasMoreLines()) {
            if(count >= 10000) {
                String line = sr2.readLine();
                docList.add(line.split("\t")[1]);
            }
            if (count++ == 11000) break;
        }

        // First count the number of relevant categories to get the number of positive examples
        HashMap<String, LinkedList<String>> relCat = new HashMap<String, LinkedList<String>>();
        while (sr2.hasMoreLines()) {
            String line = sr2.readLine();
            String[] tokens = line.split("\t");
            if(tokens.length == 2) {
                String query = tokens[0];
                String category = tokens[1];
                if (query.contains("Category:") || !docList.contains(query)) continue;
                if (!relCat.containsKey(category))
                    relCat.put(category, new LinkedList<String>());
                relCat.get(category).add(query);
                //        System.out.println(query + "\t" + category);
            }
        }

        // for each (wp, category) generate features
        Document.DocumentComponents dc = new Document.DocumentComponents(true, false, true);
        String jsonConfigFile = "C:\\Users\\mhjang\\IdeaProjects\\wikicatnew\\extract-dataset\\search.params";
        Parameters globalParams = Parameters.parseFile(jsonConfigFile);
        Retrieval retrieval = RetrievalFactory.instance(globalParams);

        for(String category : relCat.keySet()) {
            LinkedList<String> wikiPages = relCat.get(category);
            for(String wp : wikiPages) {
                Multiset<Integer> featureSet = HashMultiset.create();
                Document d = retrieval.getDocument(wp, dc);
                // generate feature
                if(d.terms == null) continue;
                List<String> grams = getNGramFeatures(d.terms);
                // generate query
                System.out.print("1 qid:" + encodeQuery(category) + " ");
                for (String g : grams) {
                    featureSet.add(encodeFeature(g));
                }
                ArrayList<Integer> featureIds = new ArrayList<Integer>(featureSet);
                Collections.sort(featureIds);
                for (Integer i : featureIds) {
                    System.out.print(i + ":" + 1 + " ");
                }
                System.out.println();
            }
        }

        Parameters p = Parameters.create();
        p.set("startAt", 0);
        p.set("requested", 20);
        p.set("metrics", "map");
        TagTokenizer tt = new TagTokenizer();

        for(String category : relCat.keySet()) {

            LinkedList<String> wikiPages = categoryRankedList.get(category);
            LinkedList<String> relevant = relCat.get(category);

            for(String wp : wikiPages) {
                if(!relevant.contains(wp)) {
                    Multiset<Integer> featureSet = HashMultiset.create();
                    // generate feature
                    Document d = retrieval.getDocument(wp, dc);
                    List<String> grams = getNGramFeatures(d.terms);
                    // generate query
                    System.out.print("0 qid:" + encodeQuery(category) + " ");
                    for (String g : grams) {
                        featureSet.add(encodeFeature(g));
                    }

                    ArrayList<Integer> featureIds = new ArrayList<Integer>(featureSet.elementSet());
                    Collections.sort(featureIds);
                    for (Integer i : featureIds) {
                        System.out.print(i + ":" + 1 + " ");
                    }
                    System.out.println();
                }
           }

        }

    }
    private List<String> getNGramFeatures(List<String> terms) {
        List<String> unigrams = new LinkedList<String>();
        List<String> allGrams = new LinkedList<String>();
        List<String> bigrams = null;

        Stemmer st = new KrovetzStemmer();
        // stemmed unigram
        for(String t : terms) {
            String term = st.stem(t);
      //      if(nGramReader.lookUpTerm(term) > 1)
                unigrams.add(term);
        }
        // removing stopwords in unigrams
        // unigrams = stRemover.removeStopwords(unigrams);

        allGrams.addAll(stRemover.removeStopwords(unigrams));

        if(unigrams.size() > 2) {
            bigrams = new LinkedList<String>();
            String bigram = unigrams.get(0) + " " + unigrams.get(1);
            int length = unigrams.size();
      //      if(nGramReader.lookUpTerm(bigram) > 1)
                bigrams.add(bigram);
            for (int i = 2; i < length; i++) {
                bigram = unigrams.get(i - 1) + " " + unigrams.get(i);
      //          if(nGramReader.lookUpTerm(bigram) > 1)
                    bigrams.add(bigram);
            }
            allGrams.addAll(bigrams);
        }

        if(unigrams.size() > 3) {
            List<String> trigrams = new LinkedList<String>();
            int length = bigrams.size();

            for (int i = 2; i < length; i++) {
                String trigram = bigrams.get(i - 2) + " " + unigrams.get(i);
       //         if(nGramReader.lookUpTerm(trigram) > 1)
                    trigrams.add(trigram);
            }
            allGrams.addAll(trigrams);
        }

        return allGrams;

    }
    public void writeDic() throws Exception {
        SimpleFileWriter sw = new SimpleFileWriter("category_dic.txt");
        for(String q : queryIdMap.keySet()) {
            sw.writeLine(q + "\t" + queryIdMap.get(q));
        }
        sw.close();
        SimpleFileWriter sw2 = new SimpleFileWriter("feature_dic.txt");

        for(String q : featureIdMap.keySet()) {
            sw2.writeLine(q + "\t" + featureIdMap.get(q));
        }
        sw2.close();

    }
    public static void main(String[] args) throws Exception {
         System.setOut(new PrintStream(new File("test.txt")));
        FeatureFactory ff = new FeatureFactory();
        ff.generateTestData();
  //      ff.fixFile();
 //       ff.generateFeature();
 //       ff.writeDic();
    }
}
