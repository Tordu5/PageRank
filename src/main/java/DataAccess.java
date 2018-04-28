import java.sql.*;

public class DataAccess {

    private static DataAccess access;
    private Connection dbConnection;

    //Crawler
    private PreparedStatement addNewNodeStatement;
    private PreparedStatement createLinkStatement;
    private PreparedStatement createLinkStatementBatch;
    private PreparedStatement findIdForUrlStatement;
    private PreparedStatement increaseOutgoingStatement;

    //Pagerank
    private PreparedStatement getWebcrawlerStatement;
    private PreparedStatement getLinkStatement;
    private PreparedStatement countOutgoingLinksStatement;
    private PreparedStatement prepareDampStatement;
    private PreparedStatement prepareCalculationStatement;
    private PreparedStatement increasePagerankStatement;
    private PreparedStatement increasePagerankStatementBatch;
    private PreparedStatement getOutgoingLinksStatement;
    private PreparedStatement countNodesStatement;
    private PreparedStatement initVektorStatement;
    private PreparedStatement setOutgoingStatement;
    private PreparedStatement vektorSumStatement;

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

    public static DataAccess getAccess(){
        if (access==null) {
            access = new DataAccess();
        }
        return access;
    }

    public Connection getConnection(){
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
        addNewNodeStatement = dbConnection.prepareStatement("INSERT OR IGNORE INTO Webcrawler values(?,?,?,?,?);", Statement.RETURN_GENERATED_KEYS);
        createLinkStatement = dbConnection.prepareStatement("INSERT OR IGNORE INTO Link values(?,?);");
        createLinkStatementBatch = dbConnection.prepareStatement("INSERT OR IGNORE INTO Link values(?,?);");
        increaseOutgoingStatement = dbConnection.prepareStatement("UPDATE Webcrawler SET Outgoing = Outgoing+1 WHERE id=?");

        //Pagerank
        getWebcrawlerStatement = dbConnection.prepareStatement("SELECT * FROM Webcrawler");
        getLinkStatement = dbConnection.prepareStatement("SELECT * FROM Link");
        countOutgoingLinksStatement = dbConnection.prepareStatement("SELECT Count(*) AS COUNT FROM Link WHERE source = ?");
        prepareDampStatement = dbConnection.prepareStatement("Update Webcrawler SET pagerank=pagerank*?+?*?");
        prepareCalculationStatement = dbConnection.prepareStatement("Update Webcrawler SET vektor=pagerank, pagerank=0");
        increasePagerankStatement = dbConnection.prepareStatement("Update Webcrawler SET pagerank=pagerank+? WHERE id=?");
        increasePagerankStatementBatch = dbConnection.prepareStatement("Update Webcrawler SET pagerank=pagerank+? WHERE id=?");
        getOutgoingLinksStatement = dbConnection.prepareStatement("SELECT * FROM Link WHERE source=?");
        countNodesStatement = dbConnection.prepareStatement("SELECT Count(*) FROM Webcrawler");
        initVektorStatement = dbConnection.prepareStatement("Update Webcrawler SET vektor=?");
        setOutgoingStatement = dbConnection.prepareStatement("UPDATE Webcrawler SET Outgoing = ? WHERE id=?");
        vektorSumStatement = dbConnection.prepareStatement("SELECT SUM(Vektor) FROM Webcrawler");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Webcrawler
    public boolean isNodeAlreadyExisting(String url) {
        try {
            findIdForUrlStatement.setString(1,url);
            return findIdForUrlStatement.executeQuery().next();
        } catch (SQLException e) {
            //node is not existing ??? is it necessary?
        }
        return false;
    }

    public int getID(String url) throws SQLException {
        findIdForUrlStatement.setString(1,url);
        ResultSet idResultSet = findIdForUrlStatement.executeQuery();
        //ResultSet idResultSet = dbConnection.createStatement().executeQuery("SELECT id FROM Webcrawler WHERE url='"+url+"'");
        if (idResultSet.next()){
            return idResultSet.getInt("id");
        } else {
            return 0;
        }

    }

    public int addNode(String url) throws SQLException {
        addNewNodeStatement.setString(2,url);
        addNewNodeStatement.setInt(3,0);
        addNewNodeStatement.setDouble(4,0);
        addNewNodeStatement.setDouble(5,0);
        addNewNodeStatement.execute();
        int id = addNewNodeStatement.getGeneratedKeys().getInt(1);
        return id;
    }

    public void createLink(int sourceID, int targetID) throws SQLException {
        createLinkStatement.setInt(1,sourceID);
        createLinkStatement.setInt(2,targetID);
        createLinkStatement.execute();
    }

    public void createLinkBatch(int sourceID, int targetID) throws SQLException {
        createLinkStatementBatch.setInt(1,sourceID);
        createLinkStatementBatch.setInt(2,targetID);
        createLinkStatementBatch.addBatch();
    }

    public void executeLinkBatch() {
        try {
            createLinkStatementBatch.executeBatch();
            createLinkStatementBatch.clearBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void increaseOutgoing(int id) throws SQLException {
        increaseOutgoingStatement.setInt(1,id);
        increaseOutgoingStatement.execute();
    }

    //Pagerank
    public ResultSet getWebcrawlerTable() throws SQLException {
        return getWebcrawlerStatement.executeQuery();
    }

    public ResultSet getLinkTable() throws SQLException {
        return getLinkStatement.executeQuery();
    }

    public int countOutgoingLinks(int sourceID) throws SQLException {
        countOutgoingLinksStatement.setInt(1,sourceID);
        return countOutgoingLinksStatement.executeQuery().getInt(1);
    }

    public void dampPageRank(double dampingFactor,double equalDistributionVektorValue) throws SQLException {
        prepareDampStatement.setDouble(1,dampingFactor);
        prepareDampStatement.setDouble(2,equalDistributionVektorValue);
        prepareDampStatement.setDouble(3,1-dampingFactor);
        prepareDampStatement.execute();
    }

    public void prepareCalculation() throws SQLException {
        prepareCalculationStatement.execute();
    }

    public void increasePageRank(double increasingValue,int targetID) throws SQLException {
        increasePagerankStatement.setDouble(1,increasingValue);
        increasePagerankStatement.setInt(2,targetID);
        increasePagerankStatement.execute();
    }

    public void increasePageRankBatch(double increasingValue,int targetID) throws SQLException {
        increasePagerankStatementBatch.setDouble(1,increasingValue);
        increasePagerankStatementBatch.setInt(2,targetID);
        increasePagerankStatementBatch.addBatch();
    }

    public void executePagerankBatch(){
        try {
            increasePagerankStatementBatch.executeBatch();
            increasePagerankStatementBatch.clearBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet getOutgoingLinks(int sourceID) throws SQLException {
        getOutgoingLinksStatement.setInt(1,sourceID);
        return getOutgoingLinksStatement.executeQuery();
    }

    public Integer getNodesCount() throws SQLException {
        return countNodesStatement.executeQuery().getInt(1);
    }

    public void initVektor(Double equalDistributionVektorValue) throws SQLException {
        initVektorStatement.setDouble(1,equalDistributionVektorValue);
        initVektorStatement.execute();
    }

    public void setOutgoingLinksValue(int outgoing,int id) throws SQLException {
        setOutgoingStatement.setInt(1,outgoing);
        setOutgoingStatement.setInt(2,id);
        setOutgoingStatement.execute();
    }

    public double getVektorSum() throws SQLException {
        return vektorSumStatement.executeQuery().getDouble(1);
    }

    private void createDatabase(){
        // DB werden erstellt
        try {
            dbConnection.createStatement()
                    .execute("CREATE TABLE Webcrawler (id integer PRIMARY KEY AUTOINCREMENT UNIQUE," +
                            " url TEXT UNIQUE," +
                            " Outgoing REAL," +
                            " Vektor REAL," +
                            " PageRank REAL);");

            //DB fuer Links wird erstellt
            dbConnection.createStatement()
                    .execute("CREATE TABLE Link (source integer," +
                            " target integer," +
                            " PRIMARY KEY(source, target)," +
                            " FOREIGN KEY(source) REFERENCES Webcrawler (id)," +
                            " FOREIGN KEY(target) REFERENCES Webcrawler (id));");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
