

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

public class ReadXMLFile {

	public static void main(String argv[]) {

		try {

			File fXmlFile = new File("/Users/sungsooahn/Documents/Lucene-Goldset.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

			// NodeList nList = doc.getElementsByTagName("title");
			NodeList nList = doc.getElementsByTagName("description");

			System.out.println("----------------------------");

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				System.out.println("Current Element :" + nNode.getNodeName());
				System.out.println("Current Content :" + nNode.getTextContent());

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}