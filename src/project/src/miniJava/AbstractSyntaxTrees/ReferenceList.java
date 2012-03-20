/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import java.util.*;

public class ReferenceList implements Iterable<Reference> {
	public ReferenceList() {
		rlist = new ArrayList<Reference>();
	}

	public void add(Reference r) {
		rlist.add(r);
	}

	public Reference get(int i) {
		return rlist.get(i);
	}

	public int size() {
		return rlist.size();
	}

	public Iterator<Reference> iterator() {
		return rlist.iterator();
	}

	private List<Reference> rlist;
}
