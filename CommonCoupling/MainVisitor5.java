package seers.astvisitortest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

	private static List<String> my_qualifiedNameAccesses = new ArrayList<String>();
	private static List<Integer> my_qualifiedNameStartPositions = new ArrayList<Integer>();
	private static List<String> my_filenames = new ArrayList<String>();	
	
	// Simple test files for manual verification purpose
	// private static String analysisDir = "/Users/sungsooahn/Documents/workspace/SE6301Assignment4/src/seers/astvisitortest/gg";

	// Open NLP Tools analysis
	private static String analysisDir = "/Users/sungsooahn/Downloads/opennlp-tools/src";

	
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

		System.out.println("Finding Global Variables and its uses \n\n");
		
		for (File f : javaFiles) {
			findGlobalVariables(f);						
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
		
		List<String> qualifiedNames = visitor.getQualifiedNames();
		List<Integer> qualifiedNameStartPositions = visitor.getQualifiedNameStartPositions();
			
		

		
		my_fields.addAll(fields);
		for (Integer positions : fieldStartPositions) {
			int lineNum = compUnit.getLineNumber(positions.intValue());
			my_fieldStartPositions.add(lineNum);
			
		}
		
		my_qualifiedNameAccesses.addAll(qualifiedNames);	
		for (Integer positions : qualifiedNameStartPositions) {
			int lineNum = compUnit.getLineNumber(positions.intValue());
			my_qualifiedNameStartPositions.add(lineNum);
			my_filenames.add(file.getAbsolutePath());
			
		}
		
	}
	
	
	public static void detectCommonCoupling(FileWriter writer) throws IOException {

		System.out.println("\n");
		System.out.println("# of global variable in this systems: " + my_fields.size());
		writer.write("# of global variable in this systems: " + my_fields.size() + "\n\n");
		// System.out.println("# of qualifiedNames: " + my_qualifiedNameAccesses.size());;

	
		for (String field : my_fields) {
			System.out.println("The global variable: " + field + " was used in following file(s)");
			writer.write("The global variable: " + field + " was used in following file(s)\n");
			
			int i = 0;
			int count = 0;
			for(String qualifiedName : my_qualifiedNameAccesses) {
				if(field.equals(qualifiedName)) {
					// System.out.println("field: " + field);
					// System.out.println("Qualified Name: " + qualifiedName);
					// System.out.println("GLOBAL VARIABLE USED: Identifier: " + qualifiedName);
					int num = count + 1;
					System.out.println("\t" + num + ". " + my_filenames.get(i));
					System.out.println("\t" + "line number: " + my_qualifiedNameStartPositions.get(i));
					writer.write("\t" + num + ". " + my_filenames.get(i) + "\n");
					writer.write("\t" + "line number: " + my_qualifiedNameStartPositions.get(i) + "\n");
					
					count++;
				}
				i++;
			}
			System.out.println("In total, the " + field + " was used " + count + " times.");
			System.out.println();
			writer.write("In total, the " + field + " was used " + count + " times.\n");
			writer.write("\n");

		}

			
		
/*
		writer.write("Strong: " + visitor.getStrongGlobalVariableNumber() + "\n");
		writer.write("Weak: " + visitor.getWeakGlobalVariableNumber() + "\n");
		
		writer.write("\n\n");
*/		
		
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
