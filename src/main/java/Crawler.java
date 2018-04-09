import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Crawler {
    private Connection dbConnection;
    private Queue<String> workQueue;
    private int proccesingCounter = 0;
    private int addedNodesCounter = 0;
    private int maxAllowedNodes;

    public Crawler (int maxAllowedNodes) throws SQLException, IOException, ClassNotFoundException {
        this.maxAllowedNodes = maxAllowedNodes;
        getConnection();
        createDatabase();
        workQueue = new LinkedList<>();
    }

    public void startAtSite(String url) throws IOException, SQLException {
        addNode(url);
        process(url);
        crawl();
    }

    private void crawl() throws IOException, SQLException {
        while (!workQueue.isEmpty()){
            process(workQueue.poll());
        }
        logger("finished");
    }

    /*
    search all links on page and for each link create the DB entrys and add them to
    the Queue.
     */
    private void process(String focusedUrl) throws IOException, SQLException {
        ArrayList<String> embededUrls = extractUrlsFromSite(focusedUrl);
        logger(proccesingCounter++ +"    :Processing on " + focusedUrl);
        int baseID = getID(focusedUrl);

        for (String targetUrl: embededUrls){
            if (isUrlAlreadyAdded(targetUrl)){
                int targetID = getID(targetUrl);
                createLink(baseID,targetID);
            } else if (isLimitFullfilled()){
                continue;
            } else {
                addNode(targetUrl);
                int targetID = getID(targetUrl);
                createLink(baseID, targetID);
                workQueue.add(targetUrl);
            }
        }
    }

    /*
    get all urls from url as arraylist of Strings
     */
    private ArrayList<String> extractUrlsFromSite(String url) throws IOException {
        Document doc = Jsoup.connect(url).userAgent("Mozilla").get();
        Elements links = doc.getElementsByTag("a");
        ArrayList<String> urls = getValidateUrls(doc,links);
        //logger(urls.size() + " Links on the Side");
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

            urls.add(linkUrl);
        }

        return urls;
    }

    /*
    create a entry in the Link Table
     */
    private void createLink(int sourceID,int targetID) throws SQLException {
        PreparedStatement createLinkStatement = dbConnection.prepareStatement("INSERT OR IGNORE INTO Link values(?,?);");
        createLinkStatement.setInt(1,sourceID);
        createLinkStatement.setInt(2,targetID);
        createLinkStatement.execute();
    }

    /*
    adds a node and increase addedNodesCounter
     */
    private void addNode(String url) throws SQLException {
        PreparedStatement addNewNodeStatement = dbConnection.prepareStatement("INSERT OR IGNORE INTO Webcrawler values(?,?);");
        addNewNodeStatement.setString(2, url);				// URL
        addNewNodeStatement.execute();
        addedNodesCounter++;
    }

    /*
    checkes if limit of max Nodes is reached
     */
    private boolean isLimitFullfilled() {
        if (addedNodesCounter<maxAllowedNodes){
            return false;
        }
        return true;
    }

    /*
    checkes if URL is already added
     */
    private boolean isUrlAlreadyAdded(String url) throws SQLException {
        ResultSet idQuery = dbConnection.createStatement().executeQuery("SELECT id FROM Webcrawler WHERE url='"+url + "'");
        return idQuery.next();
    }

    /*
    returns the Database ID of the given URL
     */
    private int getID(String url) throws SQLException {
        ResultSet idQuery = dbConnection.createStatement().executeQuery("SELECT id FROM Webcrawler WHERE url='"+url + "'");
        return idQuery.getInt("id");
    }

    /*
    creates the Databases
     */
    private void createDatabase(){
        // DB werden erstellt
        try {
            dbConnection.createStatement()
                    .execute("CREATE TABLE Webcrawler (id integer PRIMARY KEY AUTOINCREMENT UNIQUE, url TEXT UNIQUE);");

            //DB fuer Links wird erstellt
            dbConnection.createStatement()
                    .execute("CREATE TABLE Link (source integer, target integer, PRIMARY KEY(source, target), FOREIGN KEY(source) REFERENCES Webcrawler (id), FOREIGN KEY(target) REFERENCES Webcrawler (id));");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
    Creating connection to Database
     */
    private void getConnection() throws ClassNotFoundException, SQLException {
        //Erstelle Verbindung zu DB
        Class.forName("org.sqlite.JDBC");
        dbConnection = DriverManager.getConnection("jdbc:sqlite:WebcrawlerData.db");
    }

    private void logger(String msg){
        System.out.println(msg);
    }
}
