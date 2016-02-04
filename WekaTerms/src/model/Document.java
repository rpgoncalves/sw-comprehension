package model;
import java.util.ArrayList;
import java.util.List;


public class Document {
	
	private String text;
	private List<Term> terms;

	public Document() {
		this(new ArrayList<Term>());
	}

	public Document(List<Term> terms) {
		this.terms = terms;
	}

	public List<Term> getTerms() {
		return terms;
	}

	public void setTerms(List<Term> terms) {
		this.terms = terms;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
}
