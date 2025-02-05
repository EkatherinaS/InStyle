package mse.instyle;

import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.File;

import javax.xml.transform.TransformerFactory;

public class XmlFileEditor {
    public static void editBuildprofiles () {
        String filePath = FileExplorer.getInstance().getBuildprofilesPath();
        String cssPath = FileExplorer.getInstance().getCssFilename();

        try {
            File xmlToParse = new File(filePath);
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            //Чтобы не вызывалась загрузка dtd (падает с 403) от:
            //<!DOCTYPE buildprofiles SYSTEM "https://resources.jetbrains.com/writerside/1.0/build-profiles.dtd">
            docFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(xmlToParse);

            XPath xpath = XPathFactory.newInstance().newXPath();

            //Поиск/создание ноды variables
            Node variablesNode = (Node) xpath.evaluate("//buildprofiles/variables", doc, XPathConstants.NODE);
            if (variablesNode == null) {
                Node buildprofilesNode = (Node) xpath.evaluate("//buildprofiles", doc, XPathConstants.NODE);
                variablesNode = doc.createElement("variables");
                buildprofilesNode.appendChild(variablesNode);
            }

            //Поиск/создание ноды custom-css
            Node customCssNode = (Node) xpath.evaluate("//buildprofiles/variables/custom-css", doc, XPathConstants.NODE);
            if (customCssNode == null) {
                customCssNode = doc.createElement("custom-css");
                //Добавление пути к css файлу со стилем из конфига
                customCssNode.setTextContent(cssPath);
                variablesNode.appendChild(customCssNode);
            }

            //Сохранение измененного файла
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(new DOMSource(doc), new StreamResult(xmlToParse));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}