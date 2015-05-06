package wikicat.extract;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.stem.KrovetzStemmer;
import org.lemurproject.galago.core.parse.stem.Stemmer;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.RetrievalFactory;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.utility.Parameters;
import wikicat.extract.util.SimpleFileReader;

import java.io.IOException;
import java.util.*;

/**
 * Created by mhjang on 5/4/2015.
 */
public class FeatureFactory {

    HashMap<String, Integer> queryIdMap = new HashMap<String, Integer>();
    HashMap<String, Integer> featureIdMap = new HashMap<String, Integer>();
    private int queryIdCursor = 0;
    private int featureIdCursor = 0;
    StopWordRemover stRemover;
    public FeatureFactory() {
        stRemover = new StopWordRemover();
    }

    private int encodeQuery(String q) {
        if(!queryIdMap.containsKey(q)) {
            queryIdMap.put(q, queryIdCursor++);
            return queryIdCursor - 1;
        }
        return queryIdMap.get(q);
    }

    private int encodeFeature(String f) {
        if(!featureIdMap.containsKey(f)) {
            featureIdMap.put(f, featureIdCursor++);
            return featureIdCursor - 1;
        }
        return featureIdMap.get(f);
    }

    public void generateFeature() throws Exception {
        SimpleFileReader sr = new SimpleFileReader("C:\\Users\\mhjang\\IdeaProjects\\wikicatnew\\list.txt");
        LinkedList<String> docList = new LinkedList<>();
        int count = 0;
        while (sr.hasMoreLines()) {
            String line = sr.readLine();
            docList.add(line.split("\t")[1]);
            if (count++ == 10000) break;
        }


        SimpleFileReader sr2 = new SimpleFileReader("C:\\Users\\mhjang\\Desktop\\graph.tsv");
        // document - # of categories
        Multiset labelCount = HashMultiset.create();
        while (sr2.hasMoreLines()) {
            String line = sr2.readLine();
            String[] tokens = line.split("\t");
            String query = tokens[0];
            String category = tokens[1];
            if (query.contains("Category:") || !docList.contains(query)) continue;

            labelCount.add(query);
        }

        Document.DocumentComponents dc = new Document.DocumentComponents(true, false, true);
        String jsonConfigFile = "C:\\Users\\mhjang\\IdeaProjects\\wikicatnew\\extract-dataset\\search.params";
        Parameters globalParams = Parameters.parseFile(jsonConfigFile);
        Retrieval retrieval = RetrievalFactory.instance(globalParams);

        for(String query : labelCount.entrySet()) {
            Multiset<Integer> featureSet = HashMultiset.create();
            Document d = retrieval.getDocument(query, dc);
            // generate feature
            List<String> grams = getNGramFeatures(d.terms);
            // generate query
            System.out.print("1 qid:" + encodeQuery(category) + " ");
            for (String g : grams) {
                featureSet.add(encodeFeature(g));
            }

            ArrayList<Integer> featureIds = new ArrayList<Integer>(featureSet);
            Collections.sort(featureIds);
            for (Integer i : featureIds) {
                System.out.print(i + ": " + 1 + " ");
            }
        }

            // get the ranked list back
            // take random # articles and encode it as negative features
            String jsonConfigFile2 = "C:\\Users\\mhjang\\IdeaProjects\\wikicatnew\\extract-dataset\\catsearch.params";
            Parameters globalParams2 = Parameters.parseFile(jsonConfigFile2);
            Retrieval catRetrieval = RetrievalFactory.instance(globalParams2);
            Node root = StructuredQuery.parse(d.text);
            Parameters p = Parameters.create();
            p.set("startAt", 0);
            p.set("requested", 20);
            p.set("metrics", "map");
            Node transformed = catRetrieval.transformQuery(root, p);
            List<ScoredDocument> results = null;
            results = catRetrieval.executeQuery(transformed, p).scoredDocuments;

            for(ScoredDocument sd:results){ // print results
                Document doc = catRetrieval.getDocument(sd.documentName, new Document.DocumentComponents(true, true, true));
                if(sd.documentName.contains("category:"))
                    List<String> grams = getNGramFeatures(doc.terms);


            }
            //    }


        }
    }

    private List<String> getNGramFeatures(List<String> terms) {
        List<String> unigrams = new LinkedList<String>();
        List<String> allGrams = new LinkedList<String>();
        List<String> bigrams = null;

        Stemmer st = new KrovetzStemmer();
        // stemmed unigram
        System.out.println("Unigrams");
        for(String t : terms) {
            unigrams.add(st.stem(t));
        }
        // removing stopwords in unigrams
        unigrams = stRemover.removeStopwords(unigrams);
        for(String t : unigrams) {
            System.out.print(t + " ");
        }
        System.out.println();

        allGrams.addAll(unigrams);

        if(unigrams.size() > 2) {
            System.out.println("bigrams");

            bigrams = new LinkedList<String>();
            String bigram = unigrams.get(0) + " " + unigrams.get(1);
            int length = unigrams.size();

            for (int i = 2; i < length; i++) {
                bigram = unigrams.get(i - 1) + " " + unigrams.get(i);
                bigrams.add(bigram);
            }
            for(String t : bigrams) {
                System.out.print(t + ", ");
            }
            allGrams.addAll(bigrams);
        }
        System.out.println();

        if(unigrams.size() > 3) {
            System.out.println("trigrams");

            List<String> trigrams = new LinkedList<String>();
            int length = bigrams.size();
            for (int i = 2; i < length; i++) {
                trigrams.add(bigrams.get(i - 2) + " " + unigrams.get(i));
            }
            for(String t : trigrams) {
                System.out.print(t + ", ");
            }
            allGrams.addAll(trigrams);
        }

        return allGrams;

    }
    public static void main(String[] args) throws Exception {
        FeatureFactory ff = new FeatureFactory();
        ff.generateFeature();
    }
}
