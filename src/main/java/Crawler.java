import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Crawler {
    private DataAccess dbAccess;
    private Queue<String> workQueue;
    private int proccesingCounter = 0;
    private int addedNodesCounter = 0;
    private int maxAllowedNodes;
    private boolean isFilteredFromBadSites = false;
    private JsonObject webcrawler = new JsonObject();
    private JsonArray nodes = new JsonArray();
    private JsonArray links = new JsonArray();

    public Crawler (int maxAllowedNodes) throws SQLException, ClassNotFoundException {
        dbAccess = DataAccess.getAccess();
        this.maxAllowedNodes = maxAllowedNodes;
        workQueue = new LinkedList<>();
    }

    public void startAtSite(String url) throws SQLException {
        addNode(url);
        process(url);
        crawl();
    }

    private void crawl() throws SQLException {
        while (!workQueue.isEmpty()){
            process(workQueue.poll());
        }
        writeJsonToFile();
        logger("finished");
    }

    /*
    search all links on page and for each link create the DB entrys and add them to
    the Queue.
     */
    private void process(String focusedUrl) throws SQLException {
        ArrayList<String> embededUrls = null;
        try {
            embededUrls = extractUrlsFromSite(focusedUrl);
        } catch (IOException e) {
            logger("IOException");
            //e.printStackTrace();
            return;
        }

        logger(proccesingCounter++ +"    :Processing on " + focusedUrl);
        int baseID = getID(focusedUrl);

        for (String targetUrl: embededUrls){
            if (isUrlAlreadyAdded(targetUrl)){
                int targetID = getID(targetUrl);
                createLink(baseID,targetID);
            } else if (isLimitFullfilled()){
                continue;
            } else if (isDeadLink(targetUrl) && isFilteredFromBadSites){
                continue;
            } else if (hasNoOutgoingLinks(targetUrl) && isFilteredFromBadSites){
                continue;
            } else {
                try {
                    addNode(targetUrl);
                    int targetID = getID(targetUrl);
                    createLink(baseID, targetID);
                    workQueue.add(targetUrl);
                } catch (SQLException e) {
                    logger("SQL Exception");
                }
            }
        }
    }

    private boolean isSpiderTrap(String url){
        try {
            ArrayList<String>  embeddedUrls = extractUrlsFromSite(url);
            if (embeddedUrls.isEmpty()){
                return true;
            }
            if (embeddedUrls.get(0).equalsIgnoreCase(url)&&embeddedUrls.size()<2){
                return true;
            }
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    private boolean isDeadLink(String url){
        try {
            Document doc = Jsoup.connect(url).userAgent("Mozilla").get();
        } catch (IOException e) {
            return true;
        }
        return false;
    }

    private boolean hasNoOutgoingLinks(String url) {
        try{
            return !(extractUrlsFromSite(url).size()>0);
        } catch (IOException e){
            return true;
        }

    }

    /*
    get all urls from url as arraylist of Strings
     */
    private ArrayList<String> extractUrlsFromSite(String url) throws IOException {

        Document doc = Jsoup.connect(url).userAgent("Mozilla").get();
        Elements links = doc.getElementsByTag("a");
        ArrayList<String> urls = getValidateUrls(doc,links);
        return urls;
    }

    /*
    return Arraylist of URLS
     */
    //TODO: validations of URL
    private ArrayList<String> getValidateUrls(Document doc ,Elements links){
        ArrayList<String> urls = new ArrayList<>();

        for (Element link : links) {
            String linkUrl = link.attr("href");

            if (linkUrl.length() > 0) {
                if (linkUrl.length() < 4) {
                    linkUrl = doc.baseUri() + linkUrl.substring(1);
                } else if (!linkUrl.substring(0, 4).equals("http")) {
                    linkUrl = doc.baseUri() + linkUrl.substring(1);
                }
            }
            if (linkUrl.isEmpty()){
                continue;
            }
            urls.add(linkUrl);
        }

        return urls;
    }

    /*
    create a entry in the Link Table
     */
    private void createLink(int sourceID,int targetID) throws SQLException {
        dbAccess.createLink(sourceID,targetID);

        JsonObject link = new JsonObject();
        link.addProperty("source",sourceID);
        link.addProperty("target",targetID);
        links.add(link);
    }

    /*
    adds a node and increase addedNodesCounter
     */
    private void addNode(String url) throws SQLException {
        dbAccess.addNode(url);
        addedNodesCounter++;


        JsonObject node = new JsonObject();
        node.addProperty("id",getID(url));
        node.addProperty("url",url);
        nodes.add(node);
    }

    /*
    checkes if limit of max Nodes is reached
     */
    private boolean isLimitFullfilled() {
        if (addedNodesCounter<=maxAllowedNodes){
            return false;
        }
        return true;
    }

    /*
    checkes if URL is already added
     */
    private boolean isUrlAlreadyAdded(String url) {
        return dbAccess.isNodeAlreadyExisting(url);
    }

    /*
    returns the Database ID of the given URL
     */
    private int getID(String url) throws SQLException {
        return dbAccess.getID(url);
    }

    private void logger(String msg){
        System.out.println(msg);
    }

    /*
    writes to a json file for the vizualisation
     */
    public void writeJsonToFile() {
        webcrawler.add("nodes",nodes);
        webcrawler.add("links",links);

        try (Writer writer = new FileWriter("./src/main/resources/Webcrawler.json")) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(webcrawler, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
