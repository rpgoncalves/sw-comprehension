package seers.astvisitortest;

import java.io.File;
import java.io.IOException;
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
public class MainVisitor3 {

	/**
	 * Main method
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// file to parse
		// String baseFolder = "jabref-src";
		String baseFolder = "/Users/sungsooahn/Downloads/cs6301-visitor-example-master/SampleCode";
		String filePath = baseFolder + File.separator + "Countdown.java";
		File file = new File(filePath);

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
		for (String field : fields) {
			
			read = false;
			
			for(String identifier : identifiersRead) {
				if(field.equals(identifier)) {
					read = true;
					break;
				}
			}
			
			if(!read) {
				Integer position = fieldStartPositions.get(index);
				int lineNum = compUnit.getLineNumber(position);
				System.out.println("* The [field] [" + field + "] is declared but never read in the code (line:[" + lineNum + "])");
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
				Integer position = variableStartPositions.get(num);
				int lineNum = compUnit.getLineNumber(position);

				System.out.println("* The [variable] [" + variable + "] is declared but never read in the code (line:[" + lineNum + "])");
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
