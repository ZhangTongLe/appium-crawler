package com.wosai.automation.crawler.Common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.UUID;

/**
 * Created by yangzhixiang on 2017/10/16.
 */
public class Utils {
    private static Logger logger = LoggerFactory.getLogger(Utils.class);

    public static String getFullXPath(Node n) {
        if (null == n)
            return null;
        Node parent;
        Stack<Node> hierarchy = new Stack<>();
        StringBuffer buffer = new StringBuffer();
        hierarchy.push(n);
        switch (n.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
                parent = ((Attr) n).getOwnerElement();
                break;
            case Node.ELEMENT_NODE:
                parent = n.getParentNode();
                break;
            case Node.DOCUMENT_NODE:
                parent = n.getParentNode();
                break;
            default:
                throw new IllegalStateException("Unexpected Node type" + n.getNodeType());
        }
        while (null != parent && parent.getNodeType() != Node.DOCUMENT_NODE) {
            // push on stack
            hierarchy.push(parent);

            // get parent of parent
            parent = parent.getParentNode();
        }
        Object obj;
        while (!hierarchy.isEmpty() && null != (obj = hierarchy.pop())) {
            Node node = (Node) obj;
            boolean handled = false;
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) node;
                // is this the root element?
                if (buffer.length() == 0) {
                    // root element - simply append element name
                    buffer.append(node.getNodeName());
                } else {
                    // child element - append slash and element name
                    buffer.append("/");
                    buffer.append(node.getNodeName());

                    if (node.hasAttributes()) {
                        // see if the element has a name or id attribute
                        if (e.hasAttribute("id")) {
                            // id attribute found - use that
                            buffer.append("[@id='" + e.getAttribute("id") + "']");
                            handled = true;
                        } else if (e.hasAttribute("name")) {
                            // name attribute found - use that
                            buffer.append("[@name='" + e.getAttribute("name") + "']");
                            handled = true;
                        }
                    }
                    if (!handled) {
                        // no known attribute we could use - get sibling index
                        int prev_siblings = 1;
                        Node prev_sibling = node.getPreviousSibling();
                        while (null != prev_sibling) {
                            if (prev_sibling.getNodeType() == node.getNodeType()) {
                                if (prev_sibling.getNodeName().equalsIgnoreCase(
                                        node.getNodeName())) {
                                    prev_siblings++;
                                }
                            }
                            prev_sibling = prev_sibling.getPreviousSibling();
                        }
                        buffer.append("[" + prev_siblings + "]");
                    }
                }
            } else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
                buffer.append("/@");
                buffer.append(node.getNodeName());
            }
        }
        return buffer.toString().replace("hierarchy", "/").replaceAll("\\[1]", "");
    }

    public static String adbCmd(String cmd) {
        String s = null;
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                s += line + "\n";
            }
        } catch (IOException e) {
            logger.error("IOException occurs; ", e);
        }
        return s;
    }

    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String randomNum(int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public static String convertListToString(List<String> stringList) {
        StringBuilder sb = new StringBuilder();
        for (String temp : stringList) {
            sb.append(temp + ", ");
        }
        return sb.toString();
    }
}
