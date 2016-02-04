package model;

public class Term {
	
	private String text;
	
	public Term() {
		this("");
	}
	
	public Term(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
}