package com.wosai.automation.crawler.beans;

/**
 * Created by yangzhixiang on 2017/9/27.
 */
public class ElementNode {

    private String id;
    private String clazz;
    private String xpath;
    private String text;
    private String bounds;
    private String action;
    private boolean isVisited;
    private String pageId;
    private int index;
    private ElementNode elementNodePreNode;

    public ElementNode(String id, String clazz, String xpath, String text, String bounds, String
            action, boolean isVisited, String pageId, int index) {
        this.id = id;
        this.clazz = clazz;
        this.xpath = xpath;
        this.text = text;
        this.bounds = bounds;
        this.action = action;
        this.isVisited = isVisited;
        this.pageId = pageId;
        this.index = index;
    }

    public ElementNode(String xpath) {
        this.xpath = xpath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getBounds() {
        return bounds;
    }

    public void setBounds(String bounds) {
        this.bounds = bounds;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isVisited() {
        return isVisited;
    }

    public void setVisited(boolean visited) {
        isVisited = visited;
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public ElementNode getElementNodePreNode() {
        return elementNodePreNode;
    }

    public void setElementNodePreNode(ElementNode elementNodePreNode) {
        this.elementNodePreNode = elementNodePreNode;
    }

    @Override
    public String toString() {
        return "ElementNode{" +
                "id='" + id + '\'' +
                ", clazz='" + clazz + '\'' +
                ", xpath='" + xpath + '\'' +
                ", text='" + text + '\'' +
                ", bounds='" + bounds + '\'' +
                ", action='" + action + '\'' +
                ", isVisited=" + isVisited +
                ", pageId='" + pageId + '\'' +
                ", index=" + index +
                ", elementNodePreNode=" + elementNodePreNode +
                '}';
    }

    public String getXpathString() {
        return "xpath::" + xpath;
    }

    public String getIdString() {
        return "id::" + id;
    }

    public String getTextString() {
        return "text::" + text;
    }
}
