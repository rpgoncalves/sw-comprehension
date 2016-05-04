package srcml;

public class SrcMLClass {
	String name;
	String unitID;
	String classID;
	
	SrcMLClass(){
		this.name = "";
		this.unitID = "";
		this.classID = "";
	}
	
	SrcMLClass(String name){
		this.name = name;
	}
	
	public void setIDs(String unitID, String classID){
		this.unitID = unitID;
		this.classID = classID;
	}
}
