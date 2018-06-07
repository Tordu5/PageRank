import java.sql.*;

public class DataAccess {

    private static DataAccess access;
    private Connection dbConnection;

    //Crawler
    private PreparedStatement addNewNodeStatement;
    private PreparedStatement findIdForUrlStatement;
    private PreparedStatement createLinkStatementBatch;

    //Pagerank
    private PreparedStatement prepareCalculationRound;
    private PreparedStatement pageRankCalculation;
    private PreparedStatement dampCalculationStatement;
    private PreparedStatement countNodesStatement;
    private PreparedStatement initMandatoryCalculationValues;
    private PreparedStatement getUnfinishedNodesWithDifBiggerThen;
    private PreparedStatement getWebcrawlerStatement;
    private PreparedStatement getLinkStatement;

    private DataAccess() {
        try {
            createConnection();
            createDatabase();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initPreparedStatements();
    }

    synchronized public static DataAccess getAccess(){
        if (access==null) {
            access = new DataAccess();
        }
        return access;
    }

    synchronized public Connection getConnection(){
        return dbConnection;
    }

    private void createConnection() throws ClassNotFoundException, SQLException {
        //Erstelle Verbindung zu DB
        Class.forName("org.sqlite.JDBC");
        dbConnection = DriverManager.getConnection("jdbc:sqlite:WebcrawlerData.db");
    }

    private void initPreparedStatements(){
        try {
            //Crawler
            findIdForUrlStatement = dbConnection.prepareStatement("SELECT id FROM Webcrawler WHERE url=?");
            addNewNodeStatement = dbConnection.prepareStatement("INSERT OR IGNORE INTO Webcrawler values(?,?,?,?,?,?);", Statement.RETURN_GENERATED_KEYS);
            createLinkStatementBatch = dbConnection.prepareStatement("INSERT OR IGNORE INTO Link values(?,?);");

            //Pagerank
            prepareCalculationRound = dbConnection.prepareStatement("Update Webcrawler set vektor = pagerank, value = pagerank/outgoing , pagerank = 0;");
            countNodesStatement = dbConnection.prepareStatement("SELECT Count(*) FROM Webcrawler");
            initMandatoryCalculationValues = dbConnection.prepareStatement("Update Webcrawler set outgoing = (select count(*)from link where source = id), vektor = 0, value = 0, pagerank = ?");
            pageRankCalculation = dbConnection.prepareStatement("update Webcrawler set pagerank = (Select sum(value) from Webcrawler w1 join link l1 on w1.id = l1.source where l1.target = Webcrawler.id)");
            dampCalculationStatement = dbConnection.prepareStatement("Update Webcrawler SET pagerank=pagerank*?+?*?");
            getUnfinishedNodesWithDifBiggerThen = dbConnection.prepareStatement("select abs(pagerank-vektor)/max(pagerank,vektor) as diff from Webcrawler where diff > ?");
            getWebcrawlerStatement = dbConnection.prepareStatement("SELECT * FROM Webcrawler");
            getLinkStatement = dbConnection.prepareStatement("SELECT * FROM Link");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Webcrawler
    synchronized public boolean isNodeAlreadyExisting(String url) {
        try {
            findIdForUrlStatement.setString(1,url);
            return findIdForUrlStatement.executeQuery().next();
        } catch (SQLException e) {
            //node is not existing ??? is it necessary?
        }
        return false;
    }

    synchronized public int getID(String url) throws SQLException {
        findIdForUrlStatement.setString(1,url);
        ResultSet idResultSet = findIdForUrlStatement.executeQuery();
        if (idResultSet.next()){
            return idResultSet.getInt("id");
        } else {
            return 0;
        }
    }

    synchronized public int addNode(String url) throws SQLException {
        addNewNodeStatement.setString(2,url);
        addNewNodeStatement.setInt(3,0);
        addNewNodeStatement.setDouble(4,0);
        addNewNodeStatement.setDouble(5,0);
        addNewNodeStatement.execute();

        int id = addNewNodeStatement.getGeneratedKeys().getInt(1);
        return id;
    }

    synchronized public void createLinkBatch(int sourceID, int targetID) throws SQLException {
        createLinkStatementBatch.setInt(1,sourceID);
        createLinkStatementBatch.setInt(2,targetID);
        createLinkStatementBatch.addBatch();
    }

    synchronized public void executeLinkBatch() {
        try {
            createLinkStatementBatch.executeBatch();
            createLinkStatementBatch.clearBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    synchronized public ResultSet getWebcrawlerTable() throws SQLException {
        return getWebcrawlerStatement.executeQuery();
    }

    //Pagerank

    synchronized public void initMandatoryCalculationValues(Double equalDistributionValue) throws SQLException {
        initMandatoryCalculationValues.setDouble(1,equalDistributionValue);
        initMandatoryCalculationValues.execute();
    }

    synchronized public void prepareCalculationRound() throws SQLException {
        prepareCalculationRound.execute();
    }

    synchronized public void calculatePageRank() throws SQLException {
        pageRankCalculation.execute();
    }

    synchronized public void dampPageRank(double dampingFactor,double equalDistributionVektorValue) throws SQLException {
        dampCalculationStatement.setDouble(1,dampingFactor);
        dampCalculationStatement.setDouble(2,equalDistributionVektorValue);
        dampCalculationStatement.setDouble(3,1-dampingFactor);
        dampCalculationStatement.execute();
    }

    synchronized public Integer getNodesCount() throws SQLException {
        return countNodesStatement.executeQuery().getInt(1);
    }

    synchronized public ResultSet getUnfinishedNodesWithDifBiggerThen (double delta) throws SQLException {
        getUnfinishedNodesWithDifBiggerThen.setDouble(1,delta);
        return getUnfinishedNodesWithDifBiggerThen.executeQuery();
    }

    synchronized public ResultSet getLinkTable() throws SQLException {
        return getLinkStatement.executeQuery();
    }

    private void createDatabase(){
        // DB werden erstellt
        try {
            dbConnection.createStatement()
                    .execute("CREATE TABLE Webcrawler (id integer PRIMARY KEY AUTOINCREMENT UNIQUE," +
                            " url TEXT UNIQUE," +
                            " Outgoing REAL," +
                            " Vektor REAL," +
                            " PageRank REAL," +
                            " value REAL);");

            //DB fuer Links wird erstellt
            dbConnection.createStatement()
                    .execute("CREATE TABLE Link (source integer," +
                            " target integer," +
                            " PRIMARY KEY(source, target)," +
                            " FOREIGN KEY(source) REFERENCES Webcrawler (id)," +
                            " FOREIGN KEY(target) REFERENCES Webcrawler (id));");
        } catch (SQLException e) {
            //e.printStackTrace();
        }
    }
}
