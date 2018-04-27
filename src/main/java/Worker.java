import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Queue;

public class Worker extends Thread {
    Queue<String> workingQueue;
    int maxNodes = 150;
    int addedNodesCounter=0;
    int emptyPollsCounter=0;
    DataAccess dbAccess;

    public Worker(Queue<String> workingQueue){
        this.workingQueue = workingQueue;
        dbAccess = DataAccess.getAccess();
    }

    @Override
    public void run() {
        while(!workingQueue.isEmpty()||addedNodesCounter<=maxNodes){
            String url = workingQueue.poll();
            if (url==null){
                try {
                    System.out.println("Sleep");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                emptyPollsCounter++;
                if (emptyPollsCounter == 15){
                    return;
                }
            } else {
                System.out.println("crawl");
                try {
                    crawl(url);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void crawl(String url) throws SQLException {
        ArrayList<String> linksList = null;
        int sourceID = dbAccess.getID(url);
        try {
            linksList = extractUrlsFromSite(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String link : linksList){
            if (isLinkInvalid(link)){
                continue;
            }
            if (isLinkAlreadyAdded(link)){
                int targetID = dbAccess.getID(link);
                //createLink(sourceID,targetID);
                createLinkBatch(sourceID,targetID);

            }else {
                int targetID = addNode(link);
                //createLink(sourceID,targetID);
                createLinkBatch(sourceID,targetID);
            }


        }
        dbAccess.executeLinkBatch();
    }

    private boolean isLinkAlreadyAdded(String link) {
        return dbAccess.isNodeAlreadyExisting(link);
    }

    private void createLinkBatch(int sourceID,int targetID){
        try {
            dbAccess.createLinkBatch(sourceID,targetID);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("SQL EXCEPTION createLink");
        }
    }

    private void createLink(int sourceID,int targetID){
        try {
            dbAccess.createLink(sourceID,targetID);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("SQL EXCEPTION createLink");
        }
    }

    private int addNode(String link) throws SQLException {
            int id = dbAccess.addNode(link);
            addedNodesCounter++;
            workingQueue.offer(link);
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
        Document doc = Jsoup.connect(url).userAgent("Mozilla").get();
        Elements links = doc.getElementsByTag("a");
        ArrayList<String> urls = getValidateUrls(doc,links);
        return urls;
    }

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
            if (linkUrl.isEmpty()||linkUrl.contains(" ")){
                continue;
            }
            urls.add(linkUrl);
        }

        return urls;
    }
}
