/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucenedemo;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.classification.ClassificationResult;
import org.apache.lucene.classification.KNearestNeighborClassifier;
import org.apache.lucene.classification.SimpleNaiveBayesClassifier;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author andymememe
 */
public class LuceneDemo {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws org.apache.lucene.queryparser.classic.ParseException
     */
    public static void main(String[] args) throws IOException, ParseException {
        StandardAnalyzer analyzer;
        Directory index;
        IndexWriterConfig config;
        IndexWriter w;
        IndexReader reader;
        Query q;
        MatchAllDocsQuery allQ;
        IndexSearcher searcher;
        TopDocs docs;
        ScoreDoc[] hits;
        Scanner scanner;
        KNearestNeighborClassifier knn;
        SimpleNaiveBayesClassifier snb;
        ClassificationResult classes;
        
        final String querystr = "Lucene";
        final int hitsPerPage = 10;
        
        analyzer = new StandardAnalyzer();
        index = new RAMDirectory();
        config = new IndexWriterConfig(analyzer);
        
        w = new IndexWriter(index, config);
        addDocs(w);
        w.close();
        
        q = new QueryParser("title", analyzer).parse(querystr);
        
        reader = DirectoryReader.open(index);
        searcher = new IndexSearcher(reader);
        docs = searcher.search(q, hitsPerPage);
        hits = docs.scoreDocs;
        
        System.out.println("Found " + hits.length + " hits.");
        for(int i = 0; i < hits.length; ++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println((i + 1) + ". " + d.get("isbn") + "\t"
                    + d.get("title") + "\t" + d.get("class"));
        }
        
        System.out.println("===============================");
        scanner = new Scanner(System.in);
        System.out.print("Enter a book name: ");
        String newBookName = scanner.nextLine();
        allQ = new MatchAllDocsQuery();
        
        // TODO: KNN Classification
        System.out.println("============= KNN =============");
        knn = new KNearestNeighborClassifier(reader,
                new BM25Similarity(), analyzer, allQ, 3, 1, 1,
                "class", "title");
        classes = knn.assignClass(newBookName);
        System.out.println("[" + newBookName + "] Class: " +
                ((BytesRef)classes.getAssignedClass()).utf8ToString());
        
        System.out.println("========= Naive Bayes =========");
        snb = new SimpleNaiveBayesClassifier(reader, analyzer, allQ, "class",
                "title");
        classes = snb.assignClass(newBookName);
        System.out.println("[" + newBookName + "] Class: " +
                ((BytesRef)classes.getAssignedClass()).utf8ToString());
        
        reader.close();
    }
    
    private static void addDocs(IndexWriter w) {
        try {
            addDoc(w, "Lucene in Action", "193398817", "comp");
            addDoc(w, "Lucene for Dummies", "55320055Z", "comp");
            addDoc(w, "Managing Gigabytes", "55063554A", "comp");
            addDoc(w, "The Art of Computer Science", "9900333X", "comp");
            addDoc(w, "The Algorithm Design Manual", "1848000693", "comp");
            addDoc(w, "Everybody Lies", "0062390856", "business");
            addDoc(w, "Radical Candor", "1250103509", "business");
            addDoc(w, "Unshakeable", "1501164589", "business");
            addDoc(w, "The Healthy Habit Revolution", "B00RIONNPU", "business");
            addDoc(w, "Case Interview Secrets", "0984183523", "business");
            addDoc(w, "Fragmented Covenants: "
                    + "Lucene Search and Re-discovery of Ancient Documents",
                    "1507685327", "bible");
            addDoc(w, "Apocrypha", "0521506743", "bible");
            addDoc(w, "Adventure Bible", "0310727472", "bible");
            addDoc(w, "Holy Bible", "1414309473", "bible");
            addDoc(w, "The Message", "1600065945", "bible");
        } catch (IOException ex) {
            Logger.getLogger(LuceneDemo.class.getName()).log(Level.SEVERE, null,
                    ex);
        }
    }
    
    private static void addDoc(IndexWriter w, String title, String isbn,
            String className)
            throws IOException
    {
        Document doc = new Document();
        doc.add(new TextField("title", title, Field.Store.YES));
        // use a string field for isbn because we don't want it tokenized
        doc.add(new StringField("isbn", isbn, Field.Store.YES));
        doc.add(new StringField("class", className, Field.Store.YES));
        w.addDocument(doc);
    }
}
