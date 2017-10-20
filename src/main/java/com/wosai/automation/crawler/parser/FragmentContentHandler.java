package com.wosai.automation.crawler.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

public class FragmentContentHandler extends DefaultHandler {

    private String xPath = "/";
    private XMLReader xmlReader;
    private FragmentContentHandler parent;
    private StringBuilder characters = new StringBuilder();
    private Map<String, Integer> elementNameCount = new HashMap<String, Integer>();

    public FragmentContentHandler(XMLReader xmlReader) {
        this.xmlReader = xmlReader;
    }

    private FragmentContentHandler(String xPath, XMLReader xmlReader, FragmentContentHandler parent) {
        this(xmlReader);
        this.xPath = xPath;
        this.parent = parent;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        Integer count = elementNameCount.get(qName);
        if (null == count) {
            count = 1;
        } else {
            count++;
        }
        elementNameCount.put(qName, count);
        String childXPath;
        if (count == 1) {
            childXPath = xPath + "/" + qName;
        } else {
            childXPath = xPath + "/" + qName + "[" + count + "]";
        }
        System.out.println(childXPath);
        FragmentContentHandler child = new FragmentContentHandler(childXPath, xmlReader, this);
        xmlReader.setContentHandler(child);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String value = characters.toString().trim();
        if (value.length() > 0) {
            System.out.println("Out Line");
            System.out.println(xPath + "='" + characters.toString() + "'");
        }
        xmlReader.setContentHandler(parent);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        characters.append(ch, start, length);
    }

}