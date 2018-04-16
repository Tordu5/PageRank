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
    private Connection dbConnection;
    private Queue<String> workQueue;
    private int proccesingCounter = 0;
    private int addedNodesCounter = 0;
    private int maxAllowedNodes;
    private JsonObject webcrawler = new JsonObject();
    private JsonArray nodes = new JsonArray();
    private JsonArray links = new JsonArray();

    public Crawler (int maxAllowedNodes) throws SQLException, ClassNotFoundException {
        this.maxAllowedNodes = maxAllowedNodes;
        getConnection();
        createDatabase();
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
            } else {
                try {
                    addNode(targetUrl);
                    int targetID = getID(targetUrl);
                    createLink(baseID, targetID);
                    workQueue.add(targetUrl);
                } catch (SQLException e) {
                    logger("SQL Exception");
                    //e.printStackTrace();
                }
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
        PreparedStatement createLinkStatement = dbConnection.prepareStatement("INSERT OR IGNORE INTO Link values(?,?);");
        createLinkStatement.setInt(1,sourceID);
        createLinkStatement.setInt(2,targetID);
        createLinkStatement.execute();

        JsonObject link = new JsonObject();
        link.addProperty("source",sourceID);
        link.addProperty("target",targetID);
        links.add(link);
    }

    /*
    adds a node and increase addedNodesCounter
     */
    private void addNode(String url) throws SQLException {
        PreparedStatement addNewNodeStatement = dbConnection.prepareStatement("INSERT OR IGNORE INTO Webcrawler values(?,?,?,?);");
        addNewNodeStatement.setString(2, url);				// URL
        addNewNodeStatement.execute();
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
        if (addedNodesCounter<maxAllowedNodes){
            return false;
        }
        return true;
    }

    /*
    checkes if URL is already added
     */
    private boolean isUrlAlreadyAdded(String url) {
        ResultSet idQuery = null;
        try {
            idQuery = dbConnection.createStatement().executeQuery("SELECT id FROM Webcrawler WHERE url='"+url+"'");
            return idQuery.next();
        } catch (SQLException e) {
            logger("SQL Exception");
            //e.printStackTrace();
        }
        return false;
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
                    .execute("CREATE TABLE Webcrawler (id integer PRIMARY KEY AUTOINCREMENT UNIQUE, url TEXT UNIQUE, Vektor REAL, PageRank REAL);");

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
