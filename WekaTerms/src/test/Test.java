package test;
/**
 * Class for testing regex and other String manipulations.
 * 
 * Note: THIS CLASS HAS NO FUNCTIONAL PURPOSE REGARDING THE ASSIGNMENT
 * @author ronaldo
 *
 */

public class Test {
	public static void main(String[] args) {
		//String regex
		String s = "//abcd12da/***********/ds¨@#*&!@#)!@#(*)&;;:::aaaaaaaaaa'{', '}', '[', '<'";
//		System.out.println(s.replaceAll("[0-9]|[^\\w]", ""));
		
		
		
//		s = "Teste";
		s = "Teste, teste2, teste3";
		for (String string : s.split(", ")) {
			System.out.println(string);
		}
	}
}
