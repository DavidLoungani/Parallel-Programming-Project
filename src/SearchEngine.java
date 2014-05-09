import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


public class SearchEngine {
	public static void main(String[] args) {
		if (args.length != 2) {
			throw new IllegalArgumentException("Usage: java " + SearchEngine.class.getName()
		    + " <index dir> <query>");
		}
		
		String indexDir = args[0];                
		String q = args[1];                       
		search(indexDir, q);
	}
		
	public static void search(String indexDir, String q) {
		try {
			Directory dir = FSDirectory.open(new File(indexDir)); 
			IndexReader indexReader  = DirectoryReader.open(dir);
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);    
			QueryParser parser = new QueryParser(Version.LUCENE_48, "content",  
			                 new StandardAnalyzer(Version.LUCENE_48));  
			Query query = parser.parse(q);              
			long start = System.currentTimeMillis();
			TopDocs hits = indexSearcher.search(query, 10); 
			long end = System.currentTimeMillis();
			
			System.err.println("Found " + hits.totalHits +   
			  " document(s) (in " + (end - start) +        
			  " milliseconds) that matched query '" +     
			  q + "':");                                   
			
			for(ScoreDoc scoreDoc : hits.scoreDocs) {
				Document doc = indexSearcher.doc(scoreDoc.doc);                  
				System.out.println(doc.get("title"));  
			}    
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
