package com.wosai.automation.crawler.engine;

import com.wosai.automation.crawler.beans.Config;
import com.wosai.automation.crawler.beans.ElementNode;
import com.wosai.automation.crawler.common.Utils;
import com.wosai.automation.crawler.constants.Actions;
import com.wosai.automation.crawler.constants.Attribute;
import com.wosai.automation.crawler.parser.XmlParser;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Created by yangzhixiang on 2017/9/13.
 */
public abstract class Engine {

    protected static Logger logger = LoggerFactory.getLogger(Engine.class);
    protected AppiumDriver appiumDriver;
    protected Config config;
    protected XmlParser xmlParser;
    protected List<String> visitedListPage = new ArrayList<>();

    public Engine(AppiumDriver appiumDriver, Config config, XmlParser xmlParser) {
        this.appiumDriver = appiumDriver;
        this.config = config;
        this.xmlParser = xmlParser;
    }

    // BFS广度优先算法
    public void bfsTraverse() {
        Queue<ElementNode> orgQueueNodes, currentQueueNodes;
        Queue<ElementNode> backQueueNodes = new LinkedList<>();
        Stack<ElementNode> elementNodePagePathStack;
        String basePageId, currentPageId, rootPageId, orgPageSource, currentPageSource;
        WebElement element;
        orgPageSource = appiumDriver.getPageSource();
        rootPageId = getCurrentPageIdentify();
        logger.debug("首页页面Id为: " + rootPageId);
        visitedListPage.add(rootPageId);
        orgQueueNodes = xmlParser.getCurrentPageNodes(orgPageSource, rootPageId);
        logger.debug("当前初始TaskQueue大小为: {}", orgQueueNodes.size());
        basePageId = rootPageId;
        while (!orgQueueNodes.isEmpty()) {
            // 获取起始页面第一个元素作为入口
            logger.debug("当前TaskQueue大小为: {}", orgQueueNodes.size());
            ElementNode elementNode = orgQueueNodes.poll();
            // 先判断元素是不是在黑名单
            if (!isBlackElement(elementNode, config.getBlackElementList())) {
                //判断当前elementNode是否是新的页面(需要进入到新的页面开始遍历)
                if (!elementNode.getPageId().equals(getCurrentPageIdentify())) {
                    basePageId = elementNode.getPageId();
                    logger.debug("从新的页面开始遍历，节点Id: {}", basePageId);
                    //先回到首根页
                    logger.debug("尝试先回到首根页，当前节点: {}", elementNode.toString());
                    goToRootPage(rootPageId, config.getBackMaxRetry());
                    elementNodePagePathStack = getElementNodePagePath(elementNode, rootPageId);
                    while (!elementNodePagePathStack.isEmpty()) {
                        ElementNode elementNodePath = elementNodePagePathStack.pop();
                        WebElement elementPath = getElementByXpath(elementNodePath);
                        if (elementPath != null) {
                            elementPath.click();
                        }
                        logger.debug("正在尝试从首根页根据元素路径找到进入该页面的方法，当前尝试的ElementNode: {}, 剩余ElementPathStack大小为: {}",
                                elementNodePath
                                        .toString(), elementNodePagePathStack.size());
                    }
                    logger.debug("已经进入到节点页面，开始遍历工作");
                }
                logger.debug("准备处理节点 -> " + elementNode.toString());
                try {
                    element = getElementByXpath(elementNode);
                    if (element != null) {
                        // 处理action
                        if (elementNode.getAction().equals(Actions.CLICK)) {
                            element.click();
                            logger.debug("处理完点击事件");
                        } else if (elementNode.getAction().equals(Actions.INPUT)) {
                            element.sendKeys(Utils.randomNum(8));
                            logger.debug("处理完输入事件");
                        }
                        elementNode.setVisited(true);
                        backQueueNodes.offer(elementNode);
                        sleepFor(1500);
                        currentPageId = getCurrentPageIdentify();
                        // 预处理手动操作
                        preProcessPage(config.getPreProcessPageMap(), currentPageId);
                        logger.debug("当前页面id为: {}", currentPageId);
                        // 先判断是否跳页
                        if (!xmlParser.isJumpPage(basePageId, currentPageId)) {
                            logger.debug("跳页啦！当前页面为: {}", currentPageId);
                            // 在判断跳页的新页面在之前是否进来过
                            // 是新的页面，则将其对应node加入到orgQueueNodes，并且把该页面加入到已经进来过的页面(保证不重复添加node)
                            // 先判断跳页是否是WebView页面，如果是的话则不记录到TaskQueue
                            sleepFor(3000);
                            currentPageSource = appiumDriver.getPageSource();
                            if (isBlackPage(config.getBlackPageList(), currentPageSource)) {
                                logger.debug("该页面是WebView页面，不做记录到TaskQueue");
                            } else if (xmlParser.isNewPageLoad(visitedListPage, currentPageId)) {
                                logger.debug("有新的页面元素加入到QueueTask, 新的页面Id: " + currentPageId);
                                logger.debug("当前VisitedPage: ", Utils.convertListToString(visitedListPage));
                                currentQueueNodes = xmlParser.getCurrentPageNodes(currentPageSource, currentPageId);
                                addCurrentQueueNodesPrePageNode(currentQueueNodes, elementNode);
                                orgQueueNodes.addAll(currentQueueNodes);
                                visitedListPage.add(currentPageId);
                                logger.debug("追加新的TaskQueue大小为: {}, 当前TaskQueue大小为: {}", currentQueueNodes.size(),
                                        orgQueueNodes.size());
                            }
                            int count = 1;
                            if (isSwitchTabBar(basePageId, currentPageId)) {
                                logger.debug("当前只是tab页面的切换，尝试从{}切回到{}", currentPageId, basePageId);
                                switchTabBar(basePageId, currentPageId);
                            } else {
                                while ((!xmlParser.isJumpPage(basePageId, getCurrentPageIdentify())) && count <=
                                        config.getBackMaxRetry() && !isRootPage()) {
                                    sleepFor(1000);
                                    goBack();
                                    logger.debug("点击返回第{}次", count);
                                    count++;
                                    sleepFor(1000);
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    logger.debug("有异常了，操作元素：{}", elementNode.toString());
                    logger.error("Exception occurs: ", e);
                }
            }
        }
    }

    // 预处理页面操作，例如账本页面不手动点击关闭的话，朦层页不会消失
    protected void preProcessPage(Map<String, String> preProcessMap, String currentPageId) {
        if (preProcessMap.containsKey(currentPageId)) {
            String value = preProcessMap.get(currentPageId);
            String strTime = value.split("\\|")[1].trim();
            String locationType = value.split("::")[0].trim();
            String location = value.split("::")[1].split("\\|")[0].trim();
            By by = null;
            if (locationType.equalsIgnoreCase(Actions.ID)) {
                by = By.id(location);
            } else if (location.equalsIgnoreCase(Actions.XPATH)) {
                by = By.xpath(location);
            }
            int time = Integer.parseInt(strTime);
            if (time > 0) {
                if (isElementLoad(by, 3)) {
                    appiumDriver.findElement(by).click();
                    time--;
                    preProcessMap.put(currentPageId, value.replace(strTime, String.valueOf(time)));
                }
            } else if (time == -1) {
                if (isElementLoad(by, 3)) {
                    appiumDriver.findElement(by).click();
                }
            }
        }
    }

    // 判断元素是否是黑名单
    protected boolean isBlackElement(ElementNode elementNode, List<String> blackElementList) {
        for (String temp : blackElementList) {
            if (elementNode.getTextString().equalsIgnoreCase(temp) || elementNode.getIdString().equalsIgnoreCase
                    (temp) || elementNode.getXpathString().equalsIgnoreCase(temp)) {
                logger.debug("该元素: {}为黑名单元素, 跳过", elementNode.toString());
                return true;
            }
        }
        return false;
    }

    //保证进入到首根页
    protected void goToRootPage(String rootPageId, int backMaxRetry) {
        int count = 1;
        do {
            String currentPageId = getCurrentPageIdentify();
            // 如果仅仅是tab页不一样, 则只需要切换tab
            if (rootPageId.equalsIgnoreCase(currentPageId)) {
                logger.debug("已经回到首根页: {}", rootPageId);
                break;
            } else if (isSwitchTabBar(rootPageId, currentPageId)) {
                logger.debug("尝试返回首根页，仅仅tab页面不一样，只需要切换tab即可，需要切换到首根页: {}, 当前页面: {}", rootPageId, currentPageId);
                switchTabBar(rootPageId, currentPageId);
                break;
            } else {
                goBack();
                logger.debug("尝试通过返回功能回到首根页，当前尝试次数: {}", count);
                count++;
                sleepFor(1000);
            }
        } while (count <= backMaxRetry);
    }

    //根据ElementNode反向计算出PagePath
    protected Stack<ElementNode> getElementNodePagePath(ElementNode currentNode, String rootPageId) {
        Stack<ElementNode> elementNodesPathStack = new Stack<>();
        ElementNode tempElementNode = currentNode;
        while (true) {
            if (tempElementNode.getPageId().equals(rootPageId)) {
                logger.debug("已经计算到当前ElementNode的PagePath: {}", elementNodesPathStack.toString());
                break;
            } else if (isSwitchTabBar(rootPageId, tempElementNode.getPageId())) {
                String tabText = tempElementNode.getPageId().split("-")[1].trim();
                String tempXpath = String.format("//*[@text=\"%s\"]", tabText);
                elementNodesPathStack.push(new ElementNode(tempXpath));
                logger.debug("已经计算当前ElementNode的PagePath: {}, 手动追加一个tab节点: {}", elementNodesPathStack.toString(),
                        tempXpath);
                break;
            }
            ElementNode preElementNode = tempElementNode.getElementNodePreNode();
            elementNodesPathStack.push(preElementNode);
            tempElementNode = preElementNode;
        }
        return elementNodesPathStack;
    }

    // 判断页面是否因为tab不同
    protected boolean isSwitchTabBar(String prePageId, String currentPageId) {
        return prePageId.split("-")[0].trim().equalsIgnoreCase(currentPageId.split("-")[0].trim());
    }

    // 切换tab到之前的pageId，前提是当前页面仅仅因为tab不同
    protected void switchTabBar(String prePageId, String currentPageId) {
        if (isSwitchTabBar(prePageId, currentPageId)) {
            String preTabBarText = prePageId.split("-")[1].trim();
            try {
                appiumDriver.findElement(By.xpath(String.format("//*[@text=\"%s\"]", preTabBarText))).click();
                logger.debug("已经切换tab页面成功, 切换根据text: {}", preTabBarText);
            } catch (Exception e) {
                logger.error("从当前tab页面: {}切换到之前tab页面: {}发生异常", currentPageId, preTabBarText);
                logger.error("Exception occurs: ", e);
            }
        }
    }

    // 获取当前页面tab被选中的名称，例如：首页|服务中心|我
    protected String getCurrentTabBarFocus(String value) {
        String locator = value.split("::")[0].trim();
        String[] texts = value.split("::")[1].trim().split("\\|");
        for (String text : texts) {
            List<WebElement> webElementList;
            WebElement webElement = null;
            try {
                // 好坑，服务中心页面中标题和tab名称一样，所以暂用list，默认只返回最后一个element
                if (locator.equalsIgnoreCase(Actions.TEXT)) {
                    webElementList = appiumDriver.findElements(By.xpath(String.format("//*[@text=\"%s\"]", text)));
                    if (webElementList.size() > 1) {
                        webElement = webElementList.get(webElementList.size() - 1);
                    } else {
                        webElement = webElementList.get(0);
                    }
                } else {
                    //todo
                }
                if (webElement.getAttribute(Attribute.SELECTED).equalsIgnoreCase("true")) {
                    return text;
                }
            } catch (Exception e) {
                logger.error("通过text定位发现异常, 元素：{}" + "\n异常为: {}", text, e);
            }
        }
        return "";
    }

    protected boolean isRootPage() {
        boolean isRootPage = getCurrentActivity().equalsIgnoreCase(config.getRootPage());
        if (isRootPage) {
            logger.debug("别返回了，已经到首根页了～");
        }
        return isRootPage;
    }

    // 获取当前的activity
    protected String getCurrentActivity() {
        String tmp = Utils.adbCmd("adb shell dumpsys activity | grep mFocusedActivity").trim();
        if (tmp != null) {
            return tmp.split(" ")[5].trim();
        }
        return "";
    }

    // 获取当前页面唯一标识，如果当前activity有tab页则取当前activity + "-tab"
    protected String getCurrentPageIdentify() {
        String currentActivity = getCurrentActivity();
        if (currentActivity == "" || !currentActivity.contains(config.getAppPackage())) {
            return "";
        }
        if (config.getTabBarMap().containsKey(currentActivity)) {
            currentActivity = currentActivity + "-" + getCurrentTabBarFocus(config.getTabBarMap().get(currentActivity));
            return currentActivity;
        } else {
            return currentActivity;
        }
    }

    protected By getElementNodeLocator(ElementNode elementNode) {
        By by;
        if (elementNode.getId() != null && !elementNode.getId().equals("")) {
            logger.debug("通过id查找：" + elementNode.getId());
            by = By.id(elementNode.getId());
        } else if (elementNode.getText() != null && !elementNode.getText().equals("")) {
            logger.debug("通过text查找：" + elementNode.getText());
            by = By.xpath(String.format("//*[@text=\"%s\"]", elementNode.getText()));
        } else {
            by = By.xpath(elementNode.getXpath().replace("\\[0]", ""));
        }
        return by;
    }

    protected WebElement getElementByLocator(ElementNode elementNode) {
        if (!elementNode.getXpath().contains("$")) {
            return getElementByXpath(elementNode);
        } else {
            if (elementNode.getId() != null && !elementNode.getId().equals("")) {
                List<WebElement> webElementList = appiumDriver.findElements(By.id(elementNode.getId()));
                if (webElementList.size() == 1) {
                    return appiumDriver.findElement(By.id(elementNode.getId()));
                }
            } else if (elementNode.getText() != null && !elementNode.getText().equals("")) {
                String temp = String.format("//*[@text=\"%s\"]", elementNode.getText());
                List<WebElement> webElementList = appiumDriver.findElements(By.xpath(temp));
                if (webElementList.size() == 1) {
                    return appiumDriver.findElement(By.id(elementNode.getId()));
                }
            }
        }
        logger.debug("无法定位该元素：" + elementNode);
        return null;
    }

    private boolean isBlackPage(List<String> blackPageList, String pageSource) {
        boolean isBlackPage = false;
        for (String tmp : blackPageList) {
            if (pageSource.contains(tmp)) {
                isBlackPage = true;
                break;
            }
        }
        return isBlackPage;
    }

    protected WebElement getElementByXpath(ElementNode elementNode) {
        WebElement element = null;
        try {
            element = appiumDriver.findElement(By.xpath(elementNode.getXpath()));
        } catch (Exception e) {
            logger.error("通过xpath定位发现异常, 元素：{}" + "\n异常为: {}", elementNode, e);
        }
        return element;
    }

    protected boolean isExistPageId(List<String> listPageId, String pageId) {
        boolean isExist = false;
        if (listPageId.contains(pageId)) {
            isExist = true;
            listPageId.add(pageId);
        }
        return isExist;
    }

    protected void addCurrentQueueNodesPrePageNode(Queue<ElementNode> nodeQueue, ElementNode preNode) {
        for (ElementNode topNode : nodeQueue) {
            topNode.setElementNodePreNode(preNode);
        }
    }

    protected abstract void goBack();

    protected boolean isNewPageLoad(List<List<String>> listPageString, List<String> currentPageList, double percent) {
        boolean isNewPage = true;
        start:
        for (List<String> tempList : listPageString) {
            int num = 0;
            for (String temp : tempList) {
                if (currentPageList.contains(temp)) {
                    num++;
                }
            }
            double samePercent = (double) num / currentPageList.size();
            if (samePercent >= percent) {
                isNewPage = false;
                break start;
            }
        }
        return isNewPage;
    }

    protected boolean isExistPageId(Queue<ElementNode> nodeQueue, String PageId) {
        boolean isExist = false;
        for (ElementNode tempNode : nodeQueue) {
            if (tempNode.getPageId().equals(PageId)) {
                isExist = true;
                break;
            }
        }
        return isExist;
    }

    protected Queue<ElementNode> getChildStackNode(Queue<ElementNode> nodeQueue, ElementNode parentNode) {
        Queue<ElementNode> childNodeQueue = new LinkedList<ElementNode>();
        while (!nodeQueue.isEmpty()) {
            ElementNode tmpNode = nodeQueue.poll();
            if (Pattern.matches(tmpNode.getXpath() + "(\\/?\\w+)$", parentNode.getXpath())) {
                childNodeQueue.offer(tmpNode);
            }
        }
        return childNodeQueue;
    }

    public void doGuideFlow(Config config) {
        List<String> guideFlowList = config.getGuideFlowList();
        String actionType, locationType, location, content;
        for (int i = 0; i < guideFlowList.size(); i++) {
            actionType = guideFlowList.get(i).split(">>")[0].trim();
            if (actionType.equalsIgnoreCase(Actions.SLIDE)) {
                int times = Integer.parseInt(guideFlowList.get(i).split(">>")[1].trim());
                swipeRightToLeft(times);
            } else {
                locationType = guideFlowList.get(i).split(">>")[1].split("::")[0];
                if (actionType.equalsIgnoreCase(Actions.CLICK)) {
                    location = guideFlowList.get(i).split(">>")[1].split("::")[1];
                    if (locationType.equalsIgnoreCase(Actions.ID)) {
                        appiumDriver.findElement(By.id(location)).click();
                    } else if (locationType.equalsIgnoreCase(Actions.TEXT)) {
                        appiumDriver.findElement(By.xpath(String.format("//*[@text=\"%s\"]", location))).click();
                    } else if (locationType.equalsIgnoreCase(Actions.XPATH)) {
                        appiumDriver.findElement(By.xpath(location)).click();
                    }
                } else if (actionType.equalsIgnoreCase(Actions.WAIT)) {
                    int waitTime = Integer.parseInt(guideFlowList.get(i).split("\\|")[1]);
                    location = guideFlowList.get(i).split(">>")[1].split("::")[1].split("\\|")[0];
                    if (locationType.equalsIgnoreCase(Actions.ID)) {
                        isElementLoad(By.id(location), waitTime);
                    } else if (locationType.equalsIgnoreCase(Actions.TEXT)) {
                        isElementLoad(By.xpath(String.format("//*[@text=\"%s\"]", location)), waitTime);
                    } else if (locationType.equalsIgnoreCase(Actions.XPATH)) {
                        isElementLoad(By.xpath(location), waitTime);
                    }
                } else if (actionType.equalsIgnoreCase(Actions.SOFTCLICK)) {
                    int waitTime = Integer.parseInt(guideFlowList.get(i).split("\\|")[1]);
                    location = guideFlowList.get(i).split(">>")[1].split("::")[1].split("\\|")[0];
                    if (locationType.equalsIgnoreCase(Actions.ID)) {
                        if (isElementLoad(By.id(location), waitTime)) {
                            appiumDriver.findElement(By.id(location)).click();
                        }
                    } else if (locationType.equalsIgnoreCase(Actions.TEXT)) {
                        if (isElementLoad(By.id(location), waitTime)) {
                            appiumDriver.findElement(By.xpath(String.format("//*[@text=\"%s\"]", location))).click();
                        }
                    } else if (locationType.equalsIgnoreCase(Actions.XPATH)) {
                        if (isElementLoad(By.id(location), waitTime)) {
                            appiumDriver.findElement(By.xpath(location)).click();
                        }
                    }
                } else if (actionType.equalsIgnoreCase(Actions.INPUT)) {
                    location = guideFlowList.get(i).split(">>")[1].split("::")[1].split("\\|")[0];
                    content = guideFlowList.get(i).split("\\|")[1];
                    if (locationType.equalsIgnoreCase(Actions.ID)) {
                        appiumDriver.findElement(By.id(location)).sendKeys(content);
                        if (isKeyboardPopsUp()) {
                            appiumDriver.hideKeyboard();
                        }
                    } else if (locationType.equalsIgnoreCase((Actions.TEXT))) {
                        appiumDriver.findElement(By.xpath(String.format("//*[@text=\"%s\"]", location))).sendKeys
                                (content);
                        if (isKeyboardPopsUp()) {
                            appiumDriver.hideKeyboard();
                        }
                    } else if (locationType.equalsIgnoreCase(Actions.XPATH)) {
                        appiumDriver.findElement(By.xpath(location)).sendKeys(content);
                        if (isKeyboardPopsUp()) {
                            appiumDriver.hideKeyboard();
                        }
                    }
                }
            }
        }
    }

    protected boolean isKeyboardPopsUp() {
        Boolean isKeyboardPresent = false;
        try {
            Process process = Runtime.getRuntime().exec("adb shell dumpsys input_method | grep mInputShown");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output;
            while ((output = bufferedReader.readLine()) != null) {
                String keyboardProperties[] = output.split(" ");
                String keyValue[] = keyboardProperties[keyboardProperties.length - 1].split("=");
                String keyboardPresentValue = keyValue[keyValue.length - 1];
                if (keyboardPresentValue.equalsIgnoreCase("false")) {
                    isKeyboardPresent = false;
                } else {
                    isKeyboardPresent = true;
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            logger.error("IOException occurs: ", e);
        }
        return isKeyboardPresent;
    }

    protected boolean isElementLoad(final By by, int timeout) {
        try {
            appiumDriver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
            WebDriverWait webDriverWait = new WebDriverWait(appiumDriver, timeout);
            webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(by));
            appiumDriver.manage().timeouts().implicitlyWait(config.getImplicitlyWait(), TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            logger.error("Exception occurs: ", e);
        }
        return false;
    }

    private void swipeRightToLeft(int times) {
        int width = appiumDriver.manage().window().getSize().getWidth();
        int height = appiumDriver.manage().window().getSize().getHeight();
        for (int i = 0; i < times; i++) {
            appiumDriver.swipe(width * 9 / 10, height / 2, width * 1 / 10, height / 2, 1000);
            sleepFor(1000);
        }
    }

    private void sleepFor(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            logger.error("InterruptedException occurs: ", e);
        }
    }

}
