import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GoldSet {

	String goldSetFilename;

	GoldSet(String filename) {
		goldSetFilename = filename;
	}
	
	public List<String> getTitles() {

		return readFile("title");
	}
	
	public List<String> getDescriptions() {
		
		return readFile("description");
	}
	
	
	public List<String> getTitleDescriptions() {
	
		List<String> titles = getTitles();
		List<String> descriptions = getDescriptions();
		List<String> titleDescriptions = new ArrayList<String>();
		
		for (int index = 0; index < titles.size(); index++) {
			String ti = titles.get(index);
			String de = descriptions.get(index);
			titleDescriptions.add(ti + " " + de);
		}

		return titleDescriptions;
		
	}
	
	private List<String> readFile(String tag) {
		
		List<String> tagContents = new ArrayList<String>();
		
		try {
			File fXmlFile = new File(goldSetFilename);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			// System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

			// NodeList nList = doc.getElementsByTagName("title");
			NodeList nList = doc.getElementsByTagName(tag);

			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);

				// System.out.println("Current Element :" + nNode.getNodeName());
				// System.out.println("Current Content :" + nNode.getTextContent());
				String s = nNode.getTextContent();
				
				tagContents.add(s.trim());
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return tagContents;
		
	}
	
	
}
