package srcml;

public class GlobalVariable {
	String name;
	SrcMLClass c;
	
	GlobalVariable(String name){
		this.name = name;
	}
	
	public void setClass(SrcMLClass c){
		this.c = c;
	}
}
