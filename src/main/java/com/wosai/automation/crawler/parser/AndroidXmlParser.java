package com.wosai.automation.crawler.parser;

import com.wosai.automation.crawler.common.Utils;
import com.wosai.automation.crawler.beans.Config;
import com.wosai.automation.crawler.beans.ElementNode;
import com.wosai.automation.crawler.constants.Actions;
import com.wosai.automation.crawler.constants.Attribute;
import io.appium.java_client.AppiumDriver;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by yangzhixiang on 2017/9/27.
 */
public class AndroidXmlParser extends XmlParser {


    public AndroidXmlParser(AppiumDriver appiumDriver, Config config) {
        super(appiumDriver, config);
    }

    public String getPageMD5Code(String pageSource) {
        Queue<ElementNode> nodeQueue = getCurrentPageNodes(pageSource, "");
        int nodeQueueSize = nodeQueue.size();
        int sumIndex = 0;
        if (nodeQueue == null || nodeQueue.isEmpty()) {
            return "";
        }
        while (!nodeQueue.isEmpty()) {
            sumIndex = sumIndex + nodeQueue.poll().getIndex();
        }
        return EncoderByMD5(String.valueOf(nodeQueueSize) + "+" + String.valueOf(sumIndex));
    }

    @Override
    public Queue<ElementNode> getCurrentPageNodes(String pageSource, String pageId) {
        List<String> clickList = config.getClickList();
        List<String> inputList = config.getInputList();
        String clazz, action, clickable, enabled, xpath, bounds, text, id;
        int index;
        InputStream is = new ByteArrayInputStream(pageSource.getBytes());
        XPath xPath = XPathFactory.newInstance().newXPath();
//        String expression = "//node[@*]";
        String expression = "//*[@index]";
        Queue<ElementNode> queueNodes = new LinkedList<>();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                NamedNodeMap nodeMap = node.getAttributes();
                clazz = getAttributeFromNode(nodeMap, Attribute.ClASS);
                clickable = getAttributeFromNode(nodeMap, Attribute.CLICKABLE);
                enabled = getAttributeFromNode(nodeMap, Attribute.ENABLED);
                text = getAttributeFromNode(nodeMap, Attribute.TEXT);
                xpath = Utils.getFullXPath(node);
                if (enabled != null && enabled.equals("true")) {
                    if (clickList.contains(clazz) || inputList.contains(clazz)) {
                        id = getAttributeFromNode(nodeMap, Attribute.RESOURCE_ID);
                        bounds = getAttributeFromNode(nodeMap, Attribute.BOUNDS);
                        text = getAttributeFromNode(nodeMap, Attribute.TEXT);
                        action = inputList.contains(clazz) ? Actions.INPUT : Actions.CLICK;
                        index = Integer.parseInt(getAttributeFromNode(nodeMap, Attribute.INDEX));
                        queueNodes.add(new ElementNode(id, clazz, xpath, text, bounds, action, false, pageId, index));
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            logger.error("ParserConfigurationException occurs: ", e);
        } catch (SAXException e) {
            logger.error("SAXException occurs: ", e);
        } catch (IOException e) {
            logger.error("IOException occurs: ", e);
        } catch (XPathExpressionException e) {
            logger.error("XPathExpressionException occurs: ", e);
        }
        return queueNodes;
    }

    private String getXpathNode(Node node) {
        Node parent = node.getParentNode();
        NamedNodeMap nodeMap = node.getAttributes();
        if (parent == null || node.getParentNode().getNodeName().equals("hierarchy")) {
            return nodeMap.getNamedItem(Attribute.ClASS).getTextContent() + "[" + nodeMap.getNamedItem(Attribute
                    .INDEX).getTextContent() + "]";
        }
        return getXpathNode(parent) + "/" + node.getAttributes().getNamedItem(Attribute.ClASS).getTextContent() + "[" +
                nodeMap.getNamedItem(Attribute.INDEX).getTextContent() + "]";
    }

    private String getAttributeFromNode(NamedNodeMap nodeMap, String attributeName) {
        Node attributeNode = nodeMap.getNamedItem(attributeName);
        if (attributeName != null) {
            return attributeNode.getTextContent();
        }
        return null;
    }

    @Override
    public boolean isNewPageLoad(List<String> visitedListPage, String currentPageId) {
        boolean isNewPage = true;
        for (String tmp : visitedListPage) {
            if (tmp.equals(currentPageId)) {
                isNewPage = false;
                break;
            }
        }
        return isNewPage;
    }

    @Override
    public boolean isJumpPage(String prePageId, String currentPageId) {
        return prePageId.equals(currentPageId);
    }

    public List<String> getListStringFromSource(String pageSource) {
        List<String> list = new ArrayList<>();
        String expression = "//*[@index]";
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        try {
            InputStream is = new ByteArrayInputStream(pageSource.getBytes());
            XPath xPath = XPathFactory.newInstance().newXPath();
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                NamedNodeMap nodeMap = node.getAttributes();
                list.add(transferNodeMapToString(nodeMap));
            }
        } catch (ParserConfigurationException e) {
            logger.error("ParserConfigurationException occurs: ", e);
        } catch (IOException e) {
            logger.error("IOException occurs: ", e);
        } catch (SAXException e) {
            logger.error("SAXException occurs: ", e);
        } catch (XPathExpressionException e) {
            logger.error("XPathExpressionException occurs: ", e);
        }
        return list;
    }

    private String transferNodeMapToString(NamedNodeMap nodeMap) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nodeMap.getLength(); i++) {
            Node tempNode = nodeMap.item(i);
            sb.append(tempNode.getNodeName() + " : " + tempNode.getNodeValue());
        }
        return sb.toString();
    }
}
