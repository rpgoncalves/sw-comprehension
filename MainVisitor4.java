package seers.astvisitortest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
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
public class MainVisitor4 {

	/**
	 * Main method
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		System.out.println("main starts");

		// Eclipse analysis
		String analysisDir = "/Users/sungsooahn/Downloads/eclipse.jdt.core-20130227-164313";

		// Weka analysis
		// String analysisDir = "/Users/sungsooahn/Downloads/weka-3-7-13";

		File dir = new File(analysisDir);

		String[] extension = { "java" };
		Collection<File> found = FileUtils.listFiles(dir, extension, true);


		File file = new File("./eclipse_unread_analysis1.txt");
		// File file = new File("./weka_unread_analysis1.txt");
		// creates the file
		file.createNewFile();
		// creates a FileWriter Object
		FileWriter writer = new FileWriter(file); 	      
		
		for (File f : found) {

			detectUnreadFieldsVariables(f, writer);
						
		}
		
		writer.flush();
		writer.close();
		
	}
	
	
	public static void detectUnreadFieldsVariables (File file, FileWriter writer) throws IOException {

		// parse the file
		CompilationUnit compUnit = parseFile(file);

		// create and accept the visitor
		SimpleVisitor visitor = new SimpleVisitor();
		compUnit.accept(visitor);

		// print the list of methods and fields of the Java file
		List<String> fields = visitor.getFields();
		List<String> variables = visitor.getVariables();
		List<String> identifiersRead = visitor.getIdentifiersRead();
		
		List<Integer> fieldStartPositions = visitor.getFieldStartPositions();
		List<Integer> variableStartPositions = visitor.getVariableStartPositions();
		
		System.out.println("File: "+file.getName());

		boolean read; 
		int index = 0;
		
		boolean fieldFirst = true;
		boolean variableFirst = true;
		boolean fileCreated = false;
		for (String field : fields) {
			
			read = false;
			
			for(String identifier : identifiersRead) {
				if(field.equals(identifier)) {
					read = true;
					break;
				}
			}
			
			if(!read) {
				
				if (fieldFirst) {
					writer.write("File: "+ file.getAbsolutePath() + "\n");
					fieldFirst = false;
					fileCreated = true;
				}
				
				Integer position = fieldStartPositions.get(index);
				int lineNum = compUnit.getLineNumber(position);
				System.out.println("* The [field] [" + field + "] is declared but never read in the code (line:[" + lineNum + "])");
				
				writer.write("* The [field] [" + field + "] is declared but never read in the code (line:[" + lineNum + "])\n");

			}
			
			index++;
		}
		

		int num = 0;
		for (String variable : variables) {

			read = false;
			
			for(String identifier : identifiersRead) {
				if(variable.equals(identifier)) {
					read = true;
					break;
				}
			}
			
			if(!read) {
				if (variableFirst && !fileCreated) {						
					writer.write("File: "+ file.getAbsolutePath() + "\n");
					variableFirst = false;
				}
				
				Integer position = variableStartPositions.get(num);
				int lineNum = compUnit.getLineNumber(position);

				System.out.println("* The [variable] [" + variable + "] is declared but never read in the code (line:[" + lineNum + "])");
				writer.write("* The [variable] [" + variable + "] is declared but never read in the code (line:[" + lineNum + "])\n");
				
			}
			
			num++;
		}

		
	}
	
	
	

	/**
	 * Parses a java file
	 * 
	 * @param file
	 *            the file to parse
	 * @return the CompilationUnit of a java file (i.e., its AST)
	 * @throws IOException
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

		// parser.setEnvironment(classPaths, sourceFolders, encodings, true);
	}
}
