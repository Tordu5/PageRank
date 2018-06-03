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
            logger("IOException : " + e.getMessage() + " on Site : " + focusedUrl);
            return;
        }

        logger(proccesingCounter++ +"    :Processing on " + focusedUrl);
        int sourceID = getID(focusedUrl);
        for (String targetUrl: embededUrls){
            if (isFilteredFromBadSites){
                if (isUrlNotConform(targetUrl)){
                    continue;
                }
            }

            int targetID = getID(targetUrl);
            if (targetID  == 0){
                if (isLimitFullfilled()){
                    continue;
                }
                targetID  = addNode(targetUrl);
                workQueue.add(targetUrl);
            }
/*
            createLink(sourceID,targetID);
*/


            /*

            int targetID = 0 ;
            if (!isUrlAlreadyAdded(targetUrl)){
                if (isLimitFullfilled()){
                    continue;
                }
                targetID = addNode(targetUrl);
                workQueue.add(targetUrl);
            }
            if (targetID==0){
                targetID = getID(targetUrl);
            }
            createLink(sourceID,targetID);
            //createLinkBatch(baseID, targetID);
            */

        }

        //dbAccess.executeLinkBatch();
    }

    private boolean isUrlNotConform(String targetUrl) {
        if (isDeadLink(targetUrl)){
            return true;
        } else if (hasNoOutgoingLinks(targetUrl)){
            return true;
        }
        return false;
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
        Document doc = Jsoup.connect(url).ignoreHttpErrors(true).userAgent("Mozilla").get();
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

            if (linkUrl.isEmpty() || linkUrl.contains(" ") || linkUrl.contains("void(0)")){
                continue;
            }
            if (linkUrl.length() > 0) {
                if (linkUrl.length() < 4) {
                    linkUrl = doc.baseUri() + linkUrl.substring(1);
                } else if (!linkUrl.substring(0, 4).equals("http")) {
                    linkUrl = doc.baseUri() + linkUrl.substring(1);
                }
            }
            urls.add(linkUrl);
        }

        return urls;
    }

    /*
    create a entry in the Link Table
     */
    /*private void createLink(int sourceID,int targetID) throws SQLException {
        dbAccess.createLink(sourceID,targetID);

        JsonObject link = new JsonObject();
        link.addProperty("source",sourceID);
        link.addProperty("target",targetID);
        links.add(link);
    }*/
    private void createLinkBatch(int sourceID,int targetID) throws SQLException {
        dbAccess.createLinkBatch(sourceID,targetID);

        JsonObject link = new JsonObject();
        link.addProperty("source",sourceID);
        link.addProperty("target",targetID);
        links.add(link);
    }

    /*
    adds a node and increase addedNodesCounter
     */
    private int addNode(String url) throws SQLException {
        int urlID = dbAccess.addNode(url);
        addedNodesCounter++;

        JsonObject node = new JsonObject();
        node.addProperty("id",urlID);
        node.addProperty("url",url);
        nodes.add(node);

        return urlID;
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
        int id = dbAccess.getID(url);
        return id;
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
