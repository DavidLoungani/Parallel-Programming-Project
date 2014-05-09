import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class IndexBuilder {
	private IndexWriter writer;
	
	public IndexBuilder(String indexDir) {
	    Directory dir;
		try {
			dir = FSDirectory.open(new File(indexDir));
			StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_48, analyzer);
		    writer = new IndexWriter(dir, config);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close() throws IOException {
	    writer.close();                             //4
	}

	public int index(String dataDir, FileFilter filter)
		throws Exception {

	    File[] files = new File(dataDir).listFiles();
	
	    for (File f: files) {
	    	if (!f.isDirectory() &&
	          !f.isHidden() &&
	          f.exists() &&
	          f.canRead() &&
	          (filter == null || filter.accept(f))) {
	    		indexFile(f);
	    	}
	    }

	    return writer.numDocs();                     //5
	}
	
	private static class TextFilesFilter implements FileFilter {
	    public boolean accept(File path) {
	    	return path.getName().toLowerCase()        //6
	             .endsWith(".txt");                  //6
	    }
	}
	
	protected Document getDocument(File f) throws Exception {
	    Document doc = new Document();
	    BufferedReader reader = new BufferedReader(new FileReader(f));
	    String line = reader.readLine().trim();
	    FieldType filetype = new FieldType();
	    filetype.setStored(true);
	    filetype.setIndexed(true);
	    filetype.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
	    doc.add(new Field("title", line, filetype));  
	    
	    line = reader.readLine().trim();
	    filetype = new FieldType();
	    filetype.setStored(false);
	    filetype.setIndexed(true);
	    filetype.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
	    doc.add(new Field("content", line, filetype));
	    reader.close();
	    return doc;
	}
	
	private void indexFile(File f) throws Exception {
	    System.out.println("Indexing " + f.getCanonicalPath());
	    Document doc = getDocument(f);
	    writer.addDocument(doc);                              //10
	}
	  
	public static void main(String[] args) {
		if (args.length != 2) {
			throw new IllegalArgumentException("Usage: java " + IndexBuilder.class.getName()
	        + " <index dir> <data dir>");
	    }
	    String indexDir = args[0];         
	    String dataDir = args[1];          

	    long start = System.currentTimeMillis();
	    int numIndexed = 0;
	    try {
		    IndexBuilder indexer = new IndexBuilder(indexDir);
		    numIndexed = indexer.index(dataDir, new TextFilesFilter());
	
		    indexer.close();
	    } catch (Exception e) {
			e.printStackTrace();
		}
	    
	    long end = System.currentTimeMillis();
	    System.out.println("Indexing " + numIndexed + " files took "
	      + (end - start) + " milliseconds");
	}
}