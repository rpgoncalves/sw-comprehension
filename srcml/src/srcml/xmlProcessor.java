package srcml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class xmlProcessor {
	
	public static void main(String[] args) throws IOException {
		
		List<SrcMLClass> CList = processClasses("srcmlClasses.txt");
		List<GlobalVariable> GVList = processGlobVar("globalVariables.txt", CList);
		
		System.out.println("///////////////////////////////////"
				+ "\n////SrcMLClasses:unitID:classID////"
				+ "\n///////////////////////////////////");
		for(SrcMLClass c : CList){
			System.out.println(c.name +":"+ c.unitID +":"+ c.classID);
		}

		System.out.println("///////////////////////////////////////////////////////////////"
				+ "\n////GlobalVariable:SrcMLClasses.unitID:SrcMLClasses.classID////"
				+ "\n///////////////////////////////////////////////////////////////");
		for(GlobalVariable g : GVList){
			System.out.println(g.name +":"+ g.c.unitID +":"+ g.c.classID);
		}
	}

	////////////////////////////////
	////////// SrcMLClass //////////
	
	/*
	 * go through srcmlClasses.txt, and add created SrcMLClass object to CList
	 * return CList
	 */
	private static List<SrcMLClass> processClasses(String path) throws IOException{
		List<SrcMLClass> CList = new ArrayList<SrcMLClass>();
		List<String> classesIn = Files.readAllLines(Paths.get(path));

		for(String cIn : classesIn){
			//ignore blank lines
			if(!cIn.isEmpty()){	
				String[] parsedLine = cIn.split(":");
				CList.add(createClass(parsedLine));
				//System.out.println(cIn); for(String x:parsedLine){System.out.println(x);}System.out.println();
			}
		}
		return CList;
	}
	
	/*
	 * create a new SrcMLClass object with class name, unitID, and classID
	 * return newly created SrcMLClass object
	 */
	private static SrcMLClass createClass(String[] s){
		SrcMLClass c = new SrcMLClass(s[0]);
		String[] IDs = s[2].split("/");
		c.setIDs(IDs[2], IDs[3]);
		return c;
	}
	
	////////// end SrcMLClass //////////
	////////////////////////////////////
	

	////////////////////////////////////
	////////// GlobalVariable //////////
	
	/*
	 * go through globalVariables.txt, and add created GlobalVariable object to GVList
	 * return GVList
	 */
	private static List<GlobalVariable> processGlobVar(String path, List<SrcMLClass> CList) throws IOException{
		List<GlobalVariable> GVList = new ArrayList<GlobalVariable>();
		List<String> globVarIn = Files.readAllLines(Paths.get(path));
		
		for(String gIn : globVarIn){
			//ignore blank lines
			if(!gIn.isEmpty()){	
				String[] parsedLine = gIn.split(":");
				GVList.add(createGlobVar(parsedLine, CList));
				//System.out.println(gIn); for(String x:parsedLine){System.out.println(x);}System.out.println();
			}
		}		
		return GVList;
	}
	
	/*
	 * create a new GlobalVariable object with variable name, and the SrcMLClass it belongs to
	 * return GlobalVariable
	 */
	private static GlobalVariable createGlobVar(String[] s, List<SrcMLClass> CList){
		GlobalVariable g = new GlobalVariable(s[0]);
		String[] IDs = s[2].split("/");
		g.setClass(findClass(IDs[2], IDs[3], CList));
		return g;
	}
	
	/*
	 * find the SrcMLClass that GlobalVariable belongs to given GlobalVariable's IDs
	 * return SrcMLClass
	 */
	private static SrcMLClass findClass(String unitID, String classID, List<SrcMLClass> CList){
		SrcMLClass c2 = new SrcMLClass();
		
		for(SrcMLClass c : CList){
			if(c.unitID.equals(unitID) && c.classID.equals(classID)){
				return c;
			}
		}
		
		return c2;	//in case where SrcMLClass is not found for given GlobalVariable's IDs
	}

	////////// end GlobalVariable //////////
	////////////////////////////////////////
}
