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
    private int counter = 0;

    public Crawler () throws SQLException, IOException, ClassNotFoundException {
        getConnection();
        createDatabase();
        workQueue = new LinkedList<>();
    }

    public void startAtSite(String url) throws IOException, SQLException {
        addNodeAndReturnId(url);
        process(url);
        crawl();
    }

    private void crawl() throws IOException, SQLException {
        while (!workQueue.isEmpty()){
            process(workQueue.poll());
        }
    }

    /*
    search all links on page and for each link create the DB entrys and add them to
    the Queue.
     */
    private void process(String baseUrl) throws IOException, SQLException {
        ArrayList<String> embededUrls = getUrls(baseUrl);
        logger(counter++ +"    :Processing on " + baseUrl);
        int baseID = getID(baseUrl);

        for (String targetUrl: embededUrls){
            int targetID = addNodeAndReturnId(targetUrl);
            createLink(baseID,targetID);
            workQueue.add(targetUrl);
        }
    }

    /*
    get all urls from url as arraylist of Strings
     */
    private ArrayList<String> getUrls(String url) throws IOException {
        Document doc;
        doc = Jsoup.connect(url).userAgent("Mozilla").get();
        Elements links = doc.getElementsByTag("a");
        ArrayList<String> urls = new ArrayList<>();

        // Durchlaufen der Links
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
        //logger(urls.size() + " Links on the Side");
        return urls;
    }

    /*
    create a entry in the Link Table
     */
    private void createLink(int fromID,int toID) throws SQLException {
        PreparedStatement createLinkStatement = dbConnection.prepareStatement("INSERT OR IGNORE INTO Link values(?,?);");
        createLinkStatement.setInt(1,fromID);
        createLinkStatement.setInt(2,toID);
        createLinkStatement.execute();
    }

    /*
    adds the url as a new node and return its new id
     */
    private int addNodeAndReturnId(String url) throws SQLException {
        PreparedStatement addNewNodeStatement = dbConnection.prepareStatement("INSERT OR IGNORE INTO Webcrawler values(?,?);");
        addNewNodeStatement.setString(2, url);				// URL
        addNewNodeStatement.execute();
        //logger(url + " added");
        return getID(url);
    }

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
                    .execute("CREATE TABLE Link (id integer, id2 integer, PRIMARY KEY(id, id2), FOREIGN KEY(id) REFERENCES Webcrawler (id), FOREIGN KEY(id2) REFERENCES Webcrawler (id));");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
    Creating connectioin to Database
     */
    private void getConnection() throws ClassNotFoundException, SQLException, IOException {
        //Erstelle Verbindung zu DB
        Class.forName("org.sqlite.JDBC");
        dbConnection = DriverManager.getConnection("jdbc:sqlite:WebcrawlerData.db");
    }

    private void logger(String msg){
        System.out.println(msg);
    }
}
