package seers.astvisitortest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * General visitor that extracts methods and fields of a Java compilation unit
 *
 */
public class SimpleVisitor extends ASTVisitor {

	private List<String> fields;
	private List<Integer> fieldStartPositions;

	private List<String> qualifiedNames;
	private List<Integer> qualifiedNameStartPositions;

	
	private List<String> variables;
	private List<Integer> variableStartPositions;

	private List<String> identifiersRead;
	
	private int strongGlobalVariableNumber;

	private int weakGlobalVariableNumber;

	
	public SimpleVisitor() {
		fields = new ArrayList<String>();
		fieldStartPositions = new ArrayList<Integer>();

		
		variables = new ArrayList<String>();
		variableStartPositions = new ArrayList<Integer>();

		
		identifiersRead = new ArrayList<String>();

		
		qualifiedNames = new ArrayList<String>();
		qualifiedNameStartPositions = new ArrayList<Integer>();

		
		strongGlobalVariableNumber = 0;
		weakGlobalVariableNumber = 0;

	}

	/**
	 * Method that visits the field declarations of the AST
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.FieldDeclaration)
	@SuppressWarnings("unchecked")
	@Override
	*/

	public boolean visit(FieldDeclaration node) {		
		
		// get the fragments of the field declaration
		List<VariableDeclarationFragment> varFragments = node.fragments();
		for (VariableDeclarationFragment fragment : varFragments) {			
			
			int modifiers = node.getModifiers();
			if (Modifier.isStatic(modifiers)) {

				if (Modifier.isPublic(modifiers)) {
					// System.out.println("XXX: " + fragment.getName().getFullyQualifiedName());
					strongGlobalVariableNumber++;
					
					// fragment.resolveBinding();
					IVariableBinding binding = fragment.resolveBinding();
					if (binding == null) {
						// TODO: Why Error ?
						System.out.println("1. BINDING-NULL: " + fragment.getName().getFullyQualifiedName());

						continue; 
					}
					
					ITypeBinding typeBinding = binding.getDeclaringClass();			
					String binaryName = typeBinding.getBinaryName();
										
					int index = binaryName.lastIndexOf(".");
					String globalClassVariable = binaryName.substring(index+1) + ".";
					globalClassVariable += fragment.getName().getFullyQualifiedName();

					System.out.println("strongClassVariable: " + globalClassVariable);
					fields.add(globalClassVariable);
					fieldStartPositions.add(fragment.getStartPosition());
				}
				
				if (Modifier.isProtected(modifiers)) {
					weakGlobalVariableNumber++;

					// fragment.resolveBinding();
					IVariableBinding binding = fragment.resolveBinding();
					
					if (binding == null) {
						// TODO: Why Error ?
						System.out.println("2. BINDING-NULL: " + fragment.getName().getFullyQualifiedName());
						continue;
					}
					
					ITypeBinding typeBinding = binding.getDeclaringClass();			
					String binaryName = typeBinding.getBinaryName();
										
					int index = binaryName.lastIndexOf(".");
					String globalClassVariable = binaryName.substring(index+1) + ".";
					globalClassVariable += fragment.getName().getFullyQualifiedName();

					System.out.println("WeakClassVariable: " + globalClassVariable);
					fields.add(globalClassVariable);
					fieldStartPositions.add(fragment.getStartPosition());
				}
				
			}
			
		}
		return super.visit(node);
	}

	public boolean visit(QualifiedName node) {
		
		IBinding ibinding = node.resolveBinding();		

		if (ibinding == null) {
			return super.visit(node);
		}
			
		int modifiers = ibinding.getModifiers();
		
		if (Modifier.isStatic(modifiers)) {
			if (Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)) {
				System.out.println("QualifiedName: " + node.getFullyQualifiedName());
				qualifiedNames.add(node.getFullyQualifiedName());				
				qualifiedNameStartPositions.add(node.getStartPosition());
				
			}

		}
		
		
		return super.visit(node);
	}

	
	public boolean visit(SimpleName node) {
/*
		System.out.println("***********");
		System.out.println("Identifier: " + node.getIdentifier());
		
		
		IBinding ibinding = node.resolveBinding();
		System.out.println("ibinding.getName(): " + ibinding.getName());
		System.out.println("ibinding.getKey(): " + ibinding.getKey());
		
		
		ITypeBinding typeBinding = node.resolveTypeBinding();

		if (typeBinding == null) {
			return super.visit(node);
		}

		
		ITypeBinding declaringClass = typeBinding.getDeclaringClass();
		if (declaringClass == null) {
			System.out.println("DeclaringClass is null");
			return super.visit(node);
		}
		else 
			System.out.println("name::::" + declaringClass.getName());
		

		String binaryName = typeBinding.getBinaryName();
		System.out.println("binaryName: " + binaryName);
		System.out.println("====\n");
*/
		
		
//		int index = binaryName.lastIndexOf(".");
//		String qualifiedName = binaryName.substring(index+1) + ".";
// 		qualifiedName = qualifiedName + node.getIdentifier();
// 		System.out.println("Qualified Name: " + qualifiedName);

		
		/*
		int modifiers = typeBinding.getModifiers();
	
		if (Modifier.isStatic(modifiers)) {
			if (Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)) {

				String binaryName = typeBinding.getBinaryName();
				int index = binaryName.lastIndexOf(".");
				String qualifiedName = binaryName.substring(index+1) + ".";
				qualifiedName = qualifiedName + node.getIdentifier();
				System.out.println("binaryName: " + binaryName);
				System.out.println("Identifier: " + node.getIdentifier());
				System.out.println("Qualified Name: " + qualifiedName);

				
				switch(ASTNode.nodeClassForType(node.getParent().getNodeType()).toString()) {
				case "class org.eclipse.jdt.core.dom.QualifiedName": 
					identifiersRead.add(node.getIdentifier()); break;

				case "class org.eclipse.jdt.core.dom.FieldAccess": 
					identifiersRead.add(node.getIdentifier()); break;
				case "class org.eclipse.jdt.core.dom.ArrayAccess": 
					identifiersRead.add(node.getIdentifier()); break;
				case "class org.eclipse.jdt.core.dom.AssertStatement": 
					identifiersRead.add(node.getIdentifier()); break;
				case "class org.eclipse.jdt.core.dom.DoStatement": 
					identifiersRead.add(node.getIdentifier()); break;
				case "class org.eclipse.jdt.core.dom.EnhancedForStatement": 
					identifiersRead.add(node.getIdentifier()); break;
				case "class org.eclipse.jdt.core.dom.ExpressionStatement": 
					identifiersRead.add(node.getIdentifier()); break;
				case "class org.eclipse.jdt.core.dom.ForStatement": 
					identifiersRead.add(node.getIdentifier()); break;
				case "class org.eclipse.jdt.core.dom.IfStatement": 
					identifiersRead.add(node.getIdentifier()); break;
				case "class org.eclipse.jdt.core.dom.InstanceofExpression": 
					identifiersRead.add(node.getIdentifier()); break;
				case "class org.eclipse.jdt.core.dom.ReturnStatement": 
					identifiersRead.add(node.getIdentifier()); break;
				case "class org.eclipse.jdt.core.dom.SwitchCase": 
					identifiersRead.add(node.getIdentifier()); break;
				case "class org.eclipse.jdt.core.dom.SwitchStatement": 
					identifiersRead.add(node.getIdentifier()); break;
				case "class org.eclipse.jdt.core.dom.WhileStatement": 
					identifiersRead.add(node.getIdentifier()); break;
				case "class org.eclipse.jdt.core.dom.ArrayInitializer": 
					identifiersRead.add(node.getIdentifier()); break;
				case "class org.eclipse.jdt.core.dom.MethodInvocation": 
					if(node.getLocationInParent().getId().equals("arguments") || 
							node.getLocationInParent().getId().equals("expression")) { 
						identifiersRead.add(node.getIdentifier()); 
						} break;
				case "class org.eclipse.jdt.core.dom.InfixExpression": 
					identifiersRead.add(node.getIdentifier()); break;
				case "class org.eclipse.jdt.core.dom.Assignment": 
					if(node.getLocationInParent().getId().equals("rightHandSide")) {
						identifiersRead.add(node.getIdentifier()); } break;
				case "class org.eclipse.jdt.core.dom.CastExpression": 
					identifiersRead.add(node.getIdentifier()); break;
				}
				
				// System.out.println("======");
				// System.out.println(ASTNode.nodeClassForType(node.getParent().getNodeType()).toString());
				// System.out.println("node.getIdentifier(): " + node.getIdentifier());
				// System.out.println("======");
				
			}
			
		}
		*/
		
		
		return super.visit(node);
	}

	
	
	/**
	 * 
	 */
/*
	public boolean visit(FieldAccess node) {

		IVariableBinding binding = node.resolveFieldBinding();

		if (binding == null) {
			// TODO: Why Error ?
			System.out.println("FIELD-ACCESS; BINDING-NULL: "); ///  + fragment.getName().getFullyQualifiedName());
			return super.visit(node);
		}
		
		SimpleName sname = node.getName();
		// System.out.println("sname: " + sname.getIdentifier());
		
		ITypeBinding typeBinding = binding.getDeclaringClass();			

		if (typeBinding == null) {
			return super.visit(node);
		}	
		String binaryName = typeBinding.getBinaryName();
							
		int index = binaryName.lastIndexOf(".");
		String globalClassVariable = binaryName.substring(index+1) + ".";
		globalClassVariable += sname.getIdentifier();
		// System.out.println("\nFull Variable Name: " + globalClassVariable);

		fieldAccesses.add(globalClassVariable);
		// fieldStartPositions.add(fragment.getStartPosition());

		
		return super.visit(node);
		
	}
*/	
	
	public List<String> getQualifiedNames() {
		return qualifiedNames;
	}
	
	public List<Integer> getQualifiedNameStartPositions() {
		return qualifiedNameStartPositions;
	}
	

	public List<String> getFields() {
		return fields;
	}

	public List<String> getVariables() {
		return variables;
	}

	public List<Integer> getFieldStartPositions() {
		return fieldStartPositions;
	}

	public List<Integer> getVariableStartPositions() {
		return variableStartPositions;
	}

	public List<String> getIdentifiersRead() {
		return identifiersRead;
	}
	
	public int getStrongGlobalVariableNumber() {
		return strongGlobalVariableNumber;
	}
	
	public int getWeakGlobalVariableNumber() {
		return weakGlobalVariableNumber;
	}
	
	
}
