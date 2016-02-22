

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;


public class SE6301Assignment2 
{
	public static void main(String[] args)
	{
		try
		{
			////////////////////////////////////////
			// 1. Make index for the corpus
			////////////////////////////////////////
			//	Specify the analyzer for tokenizing text.
		    //	The same analyzer should be used for indexing and searching
			StandardAnalyzer analyzer = new StandardAnalyzer();
			
			//	Code to create the index
			Directory index = new RAMDirectory();
			
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			IndexWriter indexWriter = new IndexWriter(index, config);
			
			// Apache Lucene 
			// String baseFolder = "/Users/sungsooahn/Downloads/lucene-solr-branch_5_4";
			
			// Eclipse.jdt.core 
			String baseFolder = "/Users/sungsooahn/Downloads/eclipse.jdt.core-R4_5";
						
			
			File dir = new File(baseFolder);

			String[] extension = { "corpus" };
			Collection<File> found = FileUtils.listFiles(dir, extension, true);
			
			System.out.println("found size: " + found.size());
			
			// System.in.read();
			

			long inum = 1;
			for (File f : found) {
				System.out.println(inum + ", Adding a file: " + f.getName());
							
				String contents = readFile(f);
				// System.out.println("contents: " + contents);
				addDoc(indexWriter, contents, f.getAbsolutePath());
				inum++;

			}
			indexWriter.close();

		 
			
			////////////////////////////////////////
			// 2. Get Docs for the query using the index
			////////////////////////////////////////			
			// Apache Lucene 
			// String goldSetFilename = "/Users/sungsooahn/Documents/goldset-lucene.xml";			

			// Eclipse.jdt.core 
			String goldSetFilename = "/Users/sungsooahn/Documents/goldset-eclipse.xml";
			
			GoldSet goldSet = new GoldSet(goldSetFilename);
			
			List<String> titles = goldSet.getTitles();
			List<String> descs = goldSet.getDescriptions();
			List<String> titleDescs = goldSet.getTitleDescriptions();
			
			String searchResultsFile = "/Users/sungsooahn/Documents/searchResults";
			File file = new File(searchResultsFile);
			FileWriter writer = null;
			try {
				// creates the file
				file.createNewFile();
				// creates a FileWriter Object
				writer = new FileWriter(file);
				
				// writer.write(preprocessedElements);
				// writer.flush();
				// writer.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			while (true) {
				System.out.print("Enter your query or to quit 'the-end' : ");
				Scanner userInput = new Scanner( System.in );
				String querystr = userInput.next( );
				
				//	Text to search
				// String querystr = args.length > 0 ? args[0] : "default";
				if (querystr.equals("the-end")) {
					userInput.close();
					System.out.println("\nDone. Good Job !!");
					break;
				}
			
				
				// Title Part
				writer.write("\n\n\nTITLE : ************************************\n");
				for (String mq : titles) {

					System.out.println("\ntitle: " + mq);
					writer.write("\n\n\ntitle: " + mq + "\n");

					
					Preprocessor pre = new Preprocessor();				
					querystr = pre.doPreprocess(mq);
					System.out.println("\nquerystr: " + querystr);
					writer.write("\nquerystr: " + querystr + "\n");
					
					
					//	The "contents" arg specifies the default field to use when no field is explicitly specified in the query
					Query q = new QueryParser("contents", analyzer).parse(querystr);
					
					// Searching code
					int hitsPerPage = 20;
					
				    IndexReader reader = DirectoryReader.open(index);
				    IndexSearcher searcher = new IndexSearcher(reader);
				    TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
				    searcher.search(q, collector);
				    ScoreDoc[] hits = collector.topDocs().scoreDocs;
				    
				    //	Code to display the results of search
				    System.out.println("Found " + hits.length + " hits.");
				    writer.write("Found " + hits.length + " hits\n");
				    for(int i=0;i<hits.length;++i) 
				    {
				      int docId = hits[i].doc;
				      Document d = searcher.doc(docId);
				      // System.out.println((i + 1) + ". " + d.get("contents") + "\t" + d.get("filename"));
				      System.out.println((i + 1) + ". " + "\t" + d.get("filename"));
				      writer.write((i + 1) + ". " + "\t" + d.get("filename") + "\n");  
				      
					    
				    }
				    
				    // reader can only be closed when there is no need to access the documents any more
				    reader.close();					
				} 
				
				
				
				// Description part
				writer.write("\n\n\nDESCRIPTION : ************************************\n");
				for (String mq : descs) {

					// System.out.println("\ntitle: " + mq);
					System.out.println("\ndescription: " + mq);
					// System.out.println("\ntitle+description: " + mq);
					writer.write("\n\n\ndescription: " + mq + "\n");
					
					Preprocessor pre = new Preprocessor();				
					querystr = pre.doPreprocess(mq);
					System.out.println("\nquerystr: " + querystr);
					writer.write("\nquerystr: " + querystr + "\n");
					
					
					//	The "contents" arg specifies the default field to use when no field is explicitly specified in the query
					Query q = new QueryParser("contents", analyzer).parse(querystr);
					
					// Searching code
					int hitsPerPage = 20;
					
				    IndexReader reader = DirectoryReader.open(index);
				    IndexSearcher searcher = new IndexSearcher(reader);
				    TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
				    searcher.search(q, collector);
				    ScoreDoc[] hits = collector.topDocs().scoreDocs;
				    
				    //	Code to display the results of search
				    System.out.println("Found " + hits.length + " hits.");
				    writer.write("Found " + hits.length + " hits\n");
				    for(int i=0;i<hits.length;++i) 
				    {
				      int docId = hits[i].doc;
				      Document d = searcher.doc(docId);
				      // System.out.println((i + 1) + ". " + d.get("contents") + "\t" + d.get("filename"));
				      System.out.println((i + 1) + ". " + "\t" + d.get("filename"));
				      writer.write((i + 1) + ". " + "\t" + d.get("filename") + "\n");  
					    
				    }
				    
				    // reader can only be closed when there is no need to access the documents any more
				    reader.close();					
				}

				
				// Title + Description Part
				writer.write("\n\n\nTITLE + DESCRIPTION : ************************************\n");
				for (String mq : titleDescs) {

					// System.out.println("\ntitle: " + mq);
					// System.out.println("\ndescription: " + mq);
					System.out.println("\ntitle+description: " + mq);
					writer.write("\n\n\ntitle+description: " + mq +"\n");  
					
					Preprocessor pre = new Preprocessor();				
					querystr = pre.doPreprocess(mq);
					System.out.println("\nquerystr: " + querystr);
					writer.write("\nquerystr: " + querystr + "\n");
					
					//	The "contents" arg specifies the default field to use when no field is explicitly specified in the query
					Query q = new QueryParser("contents", analyzer).parse(querystr);
					
					// Searching code
					int hitsPerPage = 20;
					
				    IndexReader reader = DirectoryReader.open(index);
				    IndexSearcher searcher = new IndexSearcher(reader);
				    TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
				    searcher.search(q, collector);
				    ScoreDoc[] hits = collector.topDocs().scoreDocs;
				    
				    //	Code to display the results of search
				    System.out.println("Found " + hits.length + " hits.");
				    writer.write("Found " + hits.length + " hits\n");
				    for(int i=0;i<hits.length;++i) 
				    {
				      int docId = hits[i].doc;
				      Document d = searcher.doc(docId);
				      // System.out.println((i + 1) + ". " + d.get("contents") + "\t" + d.get("filename"));
				      System.out.println((i + 1) + ". " + "\t" + d.get("filename"));
				      writer.write((i + 1) + ". " + "\t" + d.get("filename") + "\n");  
				    }
				    
				    // reader can only be closed when there is no need to access the documents any more
				    reader.close();					
				} // for

				writer.flush();
				writer.close();
				
			} // while 

			
		}		
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	
/*	
	// later;; refactoring
	// 
	public static void getDocsForQuery(StandardAnalyzer analyzer, Directory index, List<String> queries) {
		try {
			// Searching code
			int hitsPerPage = 20;
			
		    IndexReader reader = DirectoryReader.open(index);
		    IndexSearcher searcher = new IndexSearcher(reader);
		    TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
		    
			for (String mq : queries) {
				System.out.println("\nmy query: " + mq);
				
				Preprocessor pre = new Preprocessor();
				String querystr = pre.doPreprocess(mq);	
				System.out.println("\nquerystr: " + querystr);
				
				//	The "contents" arg specifies the default field to use when no field is explicitly specified in the query
				Query q = new QueryParser("contents", analyzer).parse(querystr);
				
			    searcher.search(q, collector);
			    ScoreDoc[] hits = collector.topDocs().scoreDocs;
			    
			    //	Code to display the results of search
			    System.out.println("Found " + hits.length + " hits.");
			    for(int i=0; i<hits.length; ++i) {
			      int docId = hits[i].doc;
			      Document d = searcher.doc(docId);
			      System.out.println((i + 1) + ". " + "\t" + d.get("filename"));
				    
			    }
			}
		    
		    // reader can only be closed when there is no need to access the documents any more
		    reader.close();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
*/
	
	
	
	
	private static void addDoc(IndexWriter w, String contents, String filename) throws IOException 
	{
		  Document doc = new Document();
		  // A text field will be tokenized
		  doc.add(new TextField("contents", contents, Field.Store.YES));
		  // We use a string field for filename because we don't want it tokenized
		  doc.add(new StringField("filename", filename, Field.Store.YES));
		  w.addDocument(doc);
	}
	
	
	public static String readFile(File file) {

		String contents = "";

		Charset charset = Charset.forName("ISO-8859-1");

		try (BufferedReader reader = Files.newBufferedReader(file.toPath(), charset)) {

			// System.out.println(".");
			// System.out.println("file: " + file.getName());
			
		    String line = null;

		    while ((line = reader.readLine()) != null) {
		        // System.out.println(line);		        
		        contents += line;
		        
		    }
		    
		    reader.close();
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}
       	       	
		return contents; 		 
		
	}

	
	
}