package control;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.Document;
import model.Term;
import util.Util;
import util.PorterStemmer;

public class MainController {
	
	static final ArrayList<Document> corpus = new ArrayList<Document>();
	static final HashMap<String, Integer> termFrequency = new HashMap<String, Integer>();
	static final HashMap<String, String> documentFrequency = new HashMap<String, String>();
	static final HashMap<String, Integer> documentUniqueTerms = new HashMap<String, Integer>();

	public static void main(String[] args) {
		loadFiles(Util.loadAddress);
		
		for (Document document : corpus) {
			for (Term term : document.getTerms()) {
				//Compute term frequency
				Integer tf = termFrequency.get(term.getText());
				
				if(tf == null) {
					tf = 0;
				}
				
				termFrequency.put(term.getText(), tf+1);
				
				//Compute document frequency
				String df = documentFrequency.get(term.getText());
				
				if(df == null) {
					documentFrequency.put(term.getText(), document.getText());
				} else {
					if(!df.toLowerCase().contains(document.getText().toLowerCase())) {
						documentFrequency.put(term.getText(), df+", "+document.getText());
					}
				}
				
				//Compute number of unique terms
				boolean unique = true;
				for (Document document2 : corpus) {
					
					//If same document, skip
					if(document.getText().equals(document2.getText())) {
						continue;
					}
					
					for (Term term2 : document2.getTerms()) {
						if(term.getText().equals(term2.getText())) {
							unique = false;
							break;
						}
					}
					
					if(!unique) {
						break;
					}
				}
				
				if(unique) {
					Integer uniqueCount = documentUniqueTerms.get(document.getText());
					
					if(uniqueCount == null) {
						uniqueCount = 0;
					}
					
					documentUniqueTerms.put(document.getText(), uniqueCount + 1);
				}
				
			}
		}
		
		PrintWriter writer;
		try {
			writer = new PrintWriter("tf-results.txt", "UTF-8");
			
			for (String key: termFrequency.keySet()){
				
	            String value = termFrequency.get(key).toString();  
	            writer.println(key + " " + value);  

			}
			
			writer.close();
			writer = new PrintWriter("df-results.txt", "UTF-8");
			
			for (String key: documentFrequency.keySet()){
				
	            String value = documentFrequency.get(key).toString();  
	            writer.println(key + " " + value.split(", ").length);  

			} 
			
			writer.close();
			writer = new PrintWriter("uniqueTerms-results.txt", "UTF-8");
			
			for (String key: documentUniqueTerms.keySet()){
				
	            String value = documentUniqueTerms.get(key).toString();  
	            writer.println(key + " " + value); 

			} 
			
			writer.close();
			
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public static void loadFiles(String address) {
		File folder = new File(address);
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles) {
		    if (file.isFile()) {
		    	
		        String extension = Util.getExtension(file);
		        
		        if("java".equals(extension)) {
		        	corpus.add(preProcess(file));
		        }
		    } else {
		    	loadFiles(file.getAbsolutePath());
		    }
		}
	}
	
	public static Document preProcess(File file) {
		
		Document document = new Document();
		List<Term> terms = new ArrayList<Term>();
		
		try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            try {
                String line = null;
                while ((line = reader.readLine()) != null) {
                	
                	//Remove imports and package declarations
                	if(line.contains("import") || line.contains("package")) {
                		continue;
                	}
                	
                    String[] lineWords = line.split(" ");
                    ArrayList<String> words = new ArrayList<String>();
                    
                    //Add words
                    for (String word : lineWords) {
                    	//Remove special characters before adding original word
                    	String wordAux = word.replaceAll("[0-9]|[^\\w]", "");
                		
                		if(wordAux.length() >= Util.minimumSize) {
                			words.add(wordAux);
                			
                			//Split camelCase
                        	String[] splitWords = word.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
                        	if(splitWords.length > 1) {
                        		for (String string : splitWords) {
                            		//Remove special characters before adding split result
                            		wordAux = string.replaceAll("[0-9]|[^\\w]", "");
                            		
                            		if(wordAux.length() >= Util.minimumSize) {
                            			words.add(wordAux);
                            		}
        						}
                        	}
                    	}
                    }
                    
                    //Remove invalid words
                    for (String word : words) {
                    	
                    	boolean valid = true;
                    	
                    	//Remove Java keywords
                    	for (String keyword : Util.javaKeywords) {
            				if(word.equalsIgnoreCase(keyword)) {
            					valid = false;
            				}
            			}
                    	
                    	//Remove stop words
                    	for (String stopword : Util.stopWords) {
            				if(word.equalsIgnoreCase(stopword)) {
            					valid = false;
            				}
            			}
                    	
                    	if(valid) {
                    		//Lucene Porter Stemmer
                    		PorterStemmer stemmer = new PorterStemmer();
                    		
                    		Term term = new Term(stemmer.stem(word));
                    		
                    		terms.add(term);
                    	}
                    	
					}
                }
            } finally {
                reader.close();
            }
        } catch (IOException ioe) {
            System.err.println("oops " + ioe.getMessage());
        }
		
		document.setText(file.getName());
		document.setTerms(terms);
		
		return document;
	}
	
}
