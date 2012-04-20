/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MethodDeclList implements Iterable<OverloadedMethodDecl> {
	public MethodDeclList() {
		methodDeclList = new ArrayList<OverloadedMethodDecl>();
	}

	public void add(MethodDecl md) {
		for (OverloadedMethodDecl omd : methodDeclList) {
			if (omd.id.spelling.equals(md.id.spelling)) {
				omd.add(md);
				return;
			}
		}

		OverloadedMethodDecl omd = new OverloadedMethodDecl(md);
		omd.add(md);
		methodDeclList.add(omd);
	}

	public OverloadedMethodDecl get(int i) {
		return methodDeclList.get(i);
	}

	public int size() {
		return methodDeclList.size();
	}

	public Iterator<OverloadedMethodDecl> iterator() {
		return methodDeclList.iterator();
	}

	private List<OverloadedMethodDecl> methodDeclList;
}
