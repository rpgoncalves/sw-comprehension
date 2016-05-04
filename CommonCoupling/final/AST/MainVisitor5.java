package seers.astvisitortest;

import java.io.File;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collector;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * Program that extracts AST info from a Java file
 * 
 * @author ojcch
 *
 */
public class MainVisitor5 {

	
	private static List<String> my_fields = new ArrayList<String>();
	private static List<Integer> my_fieldStartPositions = new ArrayList<Integer>();
	private static List<String> my_fieldDeclaringClass = new ArrayList<String>();
	private static List<String> my_fieldStrongCoupling = new ArrayList<String>();
	private static List<String> my_fieldWeakCoupling = new ArrayList<String>();
	
	
	private static List<String> my_qualifiedNameAccesses = new ArrayList<String>();
	private static List<Integer> my_qualifiedNameStartPositions = new ArrayList<Integer>();
	private static List<String> my_filenamesContainingQN = new ArrayList<String>();	
	
	
	// Simple test files for manual verification purpose
	// private static String analysisDir = "/Users/sungsooahn/Documents/workspace/SE6301Assignment4/src/seers/astvisitortest/gg";

	// Open NLP Tools analysis
	// private static String analysisDir = "/Users/sungsooahn/Downloads/opennlp-opennlp-1.6.0-rc6/opennlp-tools/src";

	// FreeMind
	// private static String analysisDir = "/Users/sungsooahn/Downloads/freemind/freemind";
	
	// OpenCMS
	private static String analysisDir = "/Users/sungsooahn/Downloads/opencms-core-ms_9_4_7/src";
	
	
	/**
	 * Main method
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		
		File dir = new File(analysisDir);

		String[] extension = { "java" };
		Collection<File> javaFiles = FileUtils.listFiles(dir, extension, true);

		File file = new File("./common_coupling_detection.txt");
		// File file = new File("./weka_unread_analysis1.txt");
		// creates the file
		file.createNewFile();
		// creates a FileWriter Object
		FileWriter writer = new FileWriter(file); 	      

		System.out.println("\nFinding Global Variables and its uses for the follwoing system");
		System.out.println("\t" + analysisDir);

	 	writer.write("\n*****************************************************************\n");
		writer.write("Finding Global Variables and its uses for the follwoing system\n");
		writer.write("\t" + analysisDir + "\n");
	 	writer.write("*****************************************************************\n");

		
		
		int i = 0;
		for (File f : javaFiles) {
			
			// System.out.println("filename: " + f.getAbsolutePath());
			
			findGlobalVariables(f);	
			System.out.print(".");
			if (i % 80 == 0 )
				System.out.println();
			i++;
		}
		
		detectCommonCoupling(writer);
				
		writer.flush();
		writer.close();
		
		System.out.println("\n\nThe end.");

	}


	public static void findGlobalVariables(File file) throws IOException {
		// System.out.println("\nFile: "+ file.getAbsolutePath());

		// parse the file
		CompilationUnit compUnit = parseFile(file);

		// create and accept the visitor
		SimpleVisitor visitor = new SimpleVisitor();
		compUnit.accept(visitor);

		// get the list of 'Global variables' in (sub) folders
		List<String> fields = visitor.getFields();
		List<Integer> fieldStartPositions = visitor.getFieldStartPositions();
		
		my_fieldDeclaringClass.addAll(visitor.getFieldDeclaringClass());	
		my_fieldStrongCoupling.addAll(visitor.getFieldStrongCoupling());
		my_fieldWeakCoupling.addAll(visitor.getFieldWeakCoupling());
		my_fields.addAll(fields);
		for (Integer positions : fieldStartPositions) {
			int lineNum = compUnit.getLineNumber(positions.intValue());
			my_fieldStartPositions.add(lineNum);
			
		}
		
		List<String> qualifiedNames = visitor.getQualifiedNames();
		List<Integer> qualifiedNameStartPositions = visitor.getQualifiedNameStartPositions();
		my_qualifiedNameAccesses.addAll(qualifiedNames);	
		for (Integer positions : qualifiedNameStartPositions) {
			int lineNum = compUnit.getLineNumber(positions.intValue());
			my_qualifiedNameStartPositions.add(lineNum);
			my_filenamesContainingQN.add(file.getAbsolutePath());
			
		}
		
	}
	
	
	public static void detectCommonCoupling(FileWriter writer) throws IOException {

		System.out.println("\n");
		System.out.println("# of global variable in source files: " + my_fields.size());

	 	writer.write("\n\n\n*****************************************************************\n");
		writer.write("# of global variable in source files: " + my_fields.size() + "\n\n");
		
		int numStrongCoupling = 0;
		for (String field : my_fieldStrongCoupling) {
			if (field.equals("dummy") == false)
				numStrongCoupling++;
		}		
		System.out.println("# of public static (strong coupling) variable in source files: " + numStrongCoupling);
		writer.write("# of public static (strong coupling) variable in source files: " + numStrongCoupling + "\n\n");

		int numWeakCoupling = my_fields.size() - numStrongCoupling;
		/*
		for (String field : my_fieldWeakCoupling) {
			if (field.equals("dummy") == false)
				numWeakCoupling++;
		}		
		*/
		
		System.out.println("# of protected static (weak coupling) variable in source files: " + numWeakCoupling + "\n\n");
		writer.write("# of protected static (weak coupling) variable in source files: " + numWeakCoupling + "\n\n");
	 	writer.write("*****************************************************************\n\n");
		
		
		// Used in detecting the relations between classes 
		// class that declares global variables and classes that use the global variable 
		Map< String, Map<String, Integer> > classDeclaringGlobalVariableMap = new HashMap();

		Map<String, Integer> ccMap= new HashMap(); // ccMap = commonCouplingMap
		int index = 0;
		for (String field : my_fields) {

			/*			
			if (field.equals(my_fieldStrongCoupling.get(index))) {
				System.out.println("The public static (strong coupling) variable: " + field);
				System.out.print(", which was declared in a class,  " + my_fieldDeclaringClass.get(index));
				System.out.print(" (line #: " + my_fieldStartPositions.get(index) + ")");
				System.out.println(" was used in following file(s)");
				writer.write("The public static (strong coupling) variable: " + field + "\n");
				writer.write(", which was declared in a class,  " + my_fieldDeclaringClass.get(index));
				writer.write(" (line #: " + my_fieldStartPositions.get(index) + ")");
				writer.write(" was used in following file(s)\n");
			}
			else {
				System.out.println("The protected static (weak coupling) variable: " + field);
				System.out.print(", which was declared in a class,  " + my_fieldDeclaringClass.get(index));
				System.out.print(" (line #: " + my_fieldStartPositions.get(index) + ")");
				System.out.println(" was used in following file(s)");
				writer.write("The protected static (weak coupling) variable: " + field + "\n");
				writer.write(", which was declared in a class,  " + my_fieldDeclaringClass.get(index));
				writer.write(" (line #: " + my_fieldStartPositions.get(index) + ")");
				writer.write(" was used in following file(s)\n");
			}
*/
			
			int i = 0;
			int count = 0;
			Map<String, Integer> classUsingGlobalVariableMap = new HashMap();
			for(String qualifiedName : my_qualifiedNameAccesses) {
				if(field.equals(qualifiedName)) {

/*					
					int num = count + 1;
					System.out.println("\t" + num + ". " + my_filenamesContainingQN.get(i) + " (line #: " + my_qualifiedNameStartPositions.get(i) + ")");
 					writer.write("\t" + num + ". " + my_filenamesContainingQN.get(i) + " (line #: " + my_qualifiedNameStartPositions.get(i) + ")\n");
*/
					// Check whether this global variable is used in the same file where it is declared 
					String declClass = my_fieldDeclaringClass.get(index);
					String filename = my_filenamesContainingQN.get(i).replaceAll("/", ".");
					
// 05-03-2016 : Final check
					if (filename.contains(declClass)) {
						;
						
						// System.out.println("\n" + field + " is used in the same file where it was declared");
						// System.out.println("It is same file");
						// System.out.println("declClass: " + declClass);
						// System.out.println("filename: " + filename);
					}
					else {
						// check for the classUsingGlobalVariableMap
						Integer x = classUsingGlobalVariableMap.get(my_filenamesContainingQN.get(i));
						if (x == null) {
							classUsingGlobalVariableMap.put(my_filenamesContainingQN.get(i), 1);
						}
						else {
							classUsingGlobalVariableMap.put(my_filenamesContainingQN.get(i), x.intValue() + 1);
						}
	
						count++;
					}
// 

/*					
					// check for the classUsingGlobalVariableMap
					Integer x = classUsingGlobalVariableMap.get(my_filenamesContainingQN.get(i));
					if (x == null) {
						classUsingGlobalVariableMap.put(my_filenamesContainingQN.get(i), 1);
					}
					else {
						classUsingGlobalVariableMap.put(my_filenamesContainingQN.get(i), x.intValue() + 1);
					}

					count++;
*/
					
				}
				i++;
			}			
			ccMap.put(field,  count);
			
			String declaringClassKey1 = my_fieldDeclaringClass.get(index);
			String declaringClassKey2 = "";
			int x = field.lastIndexOf(".");
			if (x == -1) {
				System.out.println("Somthing wrong");
				System.exit(-1);
			}
			else
				declaringClassKey2 = field.substring(x);

			// System.out.println("Class Declaring Variable : Key: " + declaringClassKey);
			// System.out.println("Summary: " + classUsingGlobalVariableMap.toString());
			String declaringClassKey = declaringClassKey1 + declaringClassKey2;
			classDeclaringGlobalVariableMap.put(declaringClassKey, classUsingGlobalVariableMap);
			
/*						
			System.out.println("The " + field + " was used " + count + " times.");
			System.out.println("The " + field + " was used in " + classUsingGlobalVariableMap.size() + " classes.");
			System.out.println();

			writer.write("The " + field + " was used " + count + " times.\n");
			writer.write("The " + field + " was used in " + classUsingGlobalVariableMap.size() + " classes.\n");
			writer.write("\n");
*/
			
			index++;
		}

		Map<String, Integer> topTenVariablesMap= new HashMap(); // ccMap = commonCouplingMap
	
		for (Map.Entry<String, Map<String, Integer>> entry : classDeclaringGlobalVariableMap.entrySet()) {
		    // System.out.println("Global Variable = " + entry.getKey() + " was used in " + entry.getValue().size() + " times");
		    // System.out.println(entry.getValue().size() + " classes used " + entry.getKey());
		    writer.write("\n" + entry.getValue().size() + " classes used " + entry.getKey() + "\n");

		    
		    for (Map.Entry<String, Integer> occurrence : entry.getValue().entrySet()) {
		        // System.out.println("\tKey = " + occurrence.getKey() + ", Value = " + occurrence.getValue());
		        writer.write("\tKey = " + occurrence.getKey() + ", Value = " + occurrence.getValue() + "\n");

		    }
		    
		    // System.out.println("\n");
		    writer.write("\n");

		    topTenVariablesMap.put(entry.getKey(), entry.getValue().size());
		}
		
		System.out.println("Top Ten Global Variables Used by Multiple Classes:");
	 	writer.write("\n*****************************************************************\n");
		writer.write("Top Ten Global Variables Used by Multiple Classes:\n");
	 	writer.write("*****************************************************************\n");
		
		TreeMap<String, Integer> sortedTopTenVariablesMap = MapUtil.sortMapByValue((HashMap<String, Integer>) topTenVariablesMap);    
		
		int sortedTopTen = 0;
        for(Map.Entry<String, Integer> entry : sortedTopTenVariablesMap.entrySet()) {
        	if (sortedTopTen < 10) {
        		sortedTopTen++;
        		System.out.println(sortedTopTen + ". " + entry.getKey() + " were used in " + entry.getValue().intValue() + " classes");
        		writer.write(sortedTopTen + ". " + entry.getKey() + " were used in " + entry.getValue().intValue() + " classes\n");
        	}
        	else {
        		break;
        	}
        }

		System.out.println("\n");
		writer.write("\n\n");

/*
		int topTen = 0;
		System.out.println("Top ten global variables used");
		writer.write("Top ten global variables used\n");

		// http://www.programcreek.com/2013/03/java-sort-map-by-value/
		// sort by value of Map
		TreeMap<String, Integer> sortedMap = MapUtil.sortMapByValue((HashMap<String, Integer>) ccMap);    

        for(Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
        	if (topTen < 10) {
        		topTen++;
        		System.out.println(topTen + ". " + entry.getKey() + ": " + entry.getValue().intValue());
        		writer.write(topTen + ". " + entry.getKey() + ": " + entry.getValue().intValue() + "\n");
        	}
        	else {
        		break;
        	}
        }
*/
		
		TreeMap<String, Integer> sortedMap = MapUtil.sortMapByValue((HashMap<String, Integer>) ccMap);    
        int neverUsed = 0;
        for(Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
        	if (entry.getValue().intValue() == 0) {
        		neverUsed++;
        	}
        }

        System.out.println("\n\n" + neverUsed + " of global variables are never used in other files");
	 	writer.write("\n\n*****************************************************************\n");
        writer.write(neverUsed + " of global variables are never used in other files\n");
		writer.write("*****************************************************************\n");


	 	System.out.println("\n*****************************************************************\n");
		System.out.println("Analysis - TO BE CONTINUED\n");
		System.out.println("1. # of global variables used through a subtype, ");
		System.out.println("although it was not used directlry by qualifying names\n"); 
		System.out.println("2. # of variables used in the same file which declares the variable");
		System.out.println("The class has no direct known subclasses");
		
	}
	
	

	/**
	 * Parses a java file
	 * 
	 * @param file
	 *            the file to parse
	 * @return the CompilationUnit of a java file (i.e., its AST)
	 * @throws IOException
	 * @throws CoreException 
	 */
	private static CompilationUnit parseFile(File file) throws IOException {

		// read the content of the file
		char[] fileContent = FileUtils.readFileToString(file).toCharArray();

		// create the AST parser
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setUnitName(file.getName());
		parser.setSource(fileContent);		
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		// set some default configuration
		setParserConfiguration(parser);

		// parse and return the AST
		return (CompilationUnit) parser.createAST(null);

	}

	/**
	 * Sets the default configuration of an AST parser
	 * 
	 * Make it possible to get dynamic binding
	 * 
	 * @param parser
	 *            the AST parser
	 */
	public static void setParserConfiguration(ASTParser parser) {
		@SuppressWarnings("unchecked")
		Map<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);

		parser.setCompilerOptions(options);
		parser.setResolveBindings(true);

		// Open NLP Tools analysis
		String jarDirPath = "/Users/sungsooahn/Downloads/cs6301-visitor-example-master/TestASTVisitor/lib/";
		File jarDir = new File(jarDirPath);
		String[] extension = { "jar" };
		Collection<File> found = FileUtils.listFiles(jarDir, extension, true);
		List<String> paths = new ArrayList<String>();
		for ( File f : found ) {
			paths.add(f.getAbsolutePath());
		}	
		String[] classPaths = paths.toArray(new String[0]);		
		
		// Open NLP Tools analysis
		// String analysisDir = "/Users/sungsooahn/Downloads/opennlp-tools/src/";
		File dir = new File(analysisDir);
		Collection<File> folders = FileUtils.listFiles(dir, FileFilterUtils.directoryFileFilter(), TrueFileFilter.INSTANCE );
		
		List<String> dirs = new ArrayList<String>();
		for (File f : folders) {
			dirs.add(f.getAbsolutePath());
		}
		String[] sourceFolders = dirs.toArray(new String[0]);	
		
		parser.setEnvironment(classPaths, sourceFolders, null, true);
		
	}
}
