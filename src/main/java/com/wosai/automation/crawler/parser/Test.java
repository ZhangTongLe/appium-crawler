package com.wosai.automation.crawler.parser;

import com.wosai.automation.crawler.beans.ElementNode;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * Created by yangzhixiang on 2017/9/27.
 */
public class Test {
    @org.junit.Test
    public void test2() {
        Stack<String> stack = new Stack<String>();
        stack.add("4");
        stack.add("3");
        stack.add("2");
        stack.add("1");
        Stack<String> stack1 = new Stack<String>();
        stack1.add("5");
        stack1.add("6");
        Stack<String> node = reversalStack(stack);
        node.addAll(stack1);
        while (!node.isEmpty()) {
            System.out.println(node.pop());
        }

    }

    protected Stack<String> reversalStack(Stack<String> nodeStack) {
        Stack<String> stackNodes = new Stack<String>();
        while (!nodeStack.isEmpty()) {
            stackNodes.push(nodeStack.pop());
        }
        return stackNodes;
    }

    @org.junit.Test
    public void test3() {
        Queue<Integer> queue = new LinkedList<Integer>();
        for (int i = 0; i < 5; i++) {
            queue.offer(i);
        }
        Queue<Integer> queue1 = new LinkedList<Integer>();
        queue1.offer(5);
        queue1.offer(6);
        queue.addAll(queue1);
        while (!queue.isEmpty()) {
            System.out.println(queue.poll());
        }
    }

    @org.junit.Test
    public void test4() {
        String test1 = "a/b/c";
        String test2 = "a/b/c/d";
        String test3 = "a/b/c/d/e";
        String test4 = "a/b/c/d/e/f";
        if (Pattern.matches(test3 + "(\\/?\\w+)$", test4)) {
            System.out.println("match");
        } else {
            System.out.println("No match");
        }
    }

    @org.junit.Test
    public void test5() {
        String test1 = "sdsdsds[0]/sdadsd[1]/dsdsd[0]";
        String text = test1.replaceAll("\\[0]", "");
        System.out.println(text);
    }

    @org.junit.Test
    public void test6() {
        String test1 = "dsads$dsdsdsa";
        System.out.println(test1.contains("$"));
    }

    @org.junit.Test
    public void test7() {
        String dir = System.getProperty("user.dir") + "/config/" + "test.xml";

        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser saxParser = spf.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            xmlReader.setContentHandler(new FragmentContentHandler(xmlReader));
            xmlReader.parse(new InputSource(new FileInputStream(dir)));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @org.junit.Test
    public void test8() {
        String tmp = "mFocusedActivity: ActivityRecord{35180b46 u0 com.wosai.cashbar/.core.setting.changePhone" +
                ".ChangePhoneActivity t936}";
        String app = "com.wosai.cashbar";
        String result = tmp.substring(tmp.indexOf(app), tmp.lastIndexOf
                (" ")).trim();
        String[] result1 = tmp.split(" ");
        System.out.println(result1[3].trim());
    }

    @org.junit.Test
    public void test9(){
        String tmp ="com.wosai.cashbar/.core.accountBook.AccountBookActivity";
        System.out.println(tmp.split("-")[0]);
    }

    @org.junit.Test
    public void test10(){
        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            queue.offer(i);
        }
        while(queue.peek()!=null){

        }
    }



}
