import org.jsoup.Jsoup;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

public class Worker extends Thread {
    Queue<String> workingQueue;
    Connection connection;
    BlockingQueue<String> blockingWorkQueue;
    boolean isFilteredFromBadSites = false;
    int maxNodes = 1000;
    int addedNodesCounter=0;
    int emptyPollsCounter=0;
    String name;
    SQLCrawlerStatements SQLCrawlerStatements;

    public Worker(String name,BlockingQueue<String> blockingWorkQueue){
        this.blockingWorkQueue = blockingWorkQueue;
        SQLCrawlerStatements = new SQLCrawlerStatements();
        connection =SQLCrawlerStatements.getConnection();
        this.name = name;
    }

    @Override
    public void run() {
        while(!blockingWorkQueue.isEmpty()||addedNodesCounter<=maxNodes){
            String url = blockingWorkQueue.poll();
            if (url==null){
                try {
                    System.out.println("Sleep");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                emptyPollsCounter++;
                if (emptyPollsCounter == 5){
                    return;
                }
            } else {
                try {
                    crawl(url);
                } catch (SQLException e) {
                    //e.printStackTrace();
                } catch (IllegalArgumentException e){
                    continue;
                } catch (UncheckedIOException e){
                    continue;
                }
            }
        }
    }

    private void crawl(String url) throws SQLException {
        ArrayList<String> linksList = null;
        int sourceID = SQLCrawlerStatements.getID(url);
        logger(" proccessing on :   ["+sourceID+"]  " + url);
        try {
            linksList = extractUrlsFromSite(url);
        } catch (IOException e) {
            //e.printStackTrace();
        }
        if (linksList!=null) {
            connection.setAutoCommit(false);
            for (String targetUrl : linksList) {
                if (isFilteredFromBadSites) {
                    if (isLinkInvalid(targetUrl)) {
                        continue;
                    }
                }

                int targetID = getID(targetUrl);
                if (targetID == 0) {
                    if (isLimitFullfilled()) {
                        continue;
                    }
                    targetID = addNode(targetUrl);
                }
                createLinkBatch(sourceID, targetID);


            }
            executeLinkBatch();
            connection.commit();
            //connection.setAutoCommit(true);
        }
    }

    private boolean isLinkAlreadyAdded(String link) {
        return SQLCrawlerStatements.isNodeAlreadyExisting(link);
    }

    private void createLinkBatch(int sourceID,int targetID){
        try {
            SQLCrawlerStatements.createLinkBatch(sourceID,targetID);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("SQL EXCEPTION createLink");
        }
    }

    private void executeLinkBatch(){
        SQLCrawlerStatements.executeLinkBatch();
    }

    private void createLink(int sourceID,int targetID){
        try {
            SQLCrawlerStatements.createLink(sourceID,targetID);
            //logger("Link :  " + sourceID + "->" + targetID + "    added");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("SQL EXCEPTION createLink");
        }
    }

    private int addNode(String link) throws SQLException {
            int id = SQLCrawlerStatements.addNode(link);
            addedNodesCounter++;
            blockingWorkQueue.offer(link);
            //logger("Node :  " + link + "    added");
            return id;
    }

    private boolean isLinkInvalid(String link) {
        return isDeadLink(link) || hasNoOutgoingLinks(link);
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

    private ArrayList<String> extractUrlsFromSite(String url) throws IOException {
        Document doc = Jsoup.connect(url).ignoreHttpErrors(true).userAgent("Mozilla").get();
        Elements links = doc.getElementsByTag("a");
        ArrayList<String> urls = getValidateUrls(doc,links);
        return urls;
    }

    /*
    checkes if limit of max Nodes is reached
     */
    private boolean isLimitFullfilled() {
        if (addedNodesCounter<=maxNodes){
            return false;
        }
        return true;
    }

    /*
    returns the Database ID of the given URL
     */
    private int getID(String url) throws SQLException {
        int id = SQLCrawlerStatements.getID(url);
        return id;
    }

    private ArrayList<String> getValidateUrls(Document doc ,Elements links){
        ArrayList<String> urls = new ArrayList<>();

        for (Element link : links) {
            String linkUrl = link.attr("href");

            if (linkUrl.isEmpty()||linkUrl.contains(" ")||linkUrl.contains("void(0)")){
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

    private void logger(String msg){
        System.out.println(name + " : " +msg);
    }
}
