import java.sql.*;

public class DataAccess {

    private static DataAccess access;
    private Connection dbConnection;

    private int batchCounter = 0;

    //Crawler
    private PreparedStatement addNewNodeStatement;
    private PreparedStatement findIdForUrlStatement;
    private PreparedStatement createLinkStatementBatch;

/*
        private PreparedStatement createLinkStatement;
        private PreparedStatement increaseOutgoingStatement;
*/

    //Pagerank
    private PreparedStatement prepareCalculationRound;
    private PreparedStatement pageRankCalculation;
    private PreparedStatement dampCalculationStatement;
    private PreparedStatement countNodesStatement;
    private PreparedStatement initMandatoryCalculationValues;
    private PreparedStatement getUnfinishedNodesWithDifBiggerThen;

/*
    private PreparedStatement prepareCalculationStatement;
    private PreparedStatement getWebcrawlerStatement;
    private PreparedStatement increasePagerankStatementBatch;
    private PreparedStatement getLinkStatement;
    private PreparedStatement countOutgoingLinksStatement;
    private PreparedStatement increasePagerankStatement;
    private PreparedStatement getOutgoingLinksStatement;
    private PreparedStatement initVektorStatement;
    private PreparedStatement setOutgoingStatement;
    private PreparedStatement vektorSumStatement;
*/


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
        //dbConnection.setAutoCommit(false);
    }

    private void initPreparedStatements(){
        try {
        //Crawler
            findIdForUrlStatement = dbConnection.prepareStatement("SELECT id FROM Webcrawler WHERE url=?");
            addNewNodeStatement = dbConnection.prepareStatement("INSERT OR IGNORE INTO Webcrawler values(?,?,?,?,?,?);", Statement.RETURN_GENERATED_KEYS);
            createLinkStatementBatch = dbConnection.prepareStatement("INSERT OR IGNORE INTO Link values(?,?);");
    /*
            createLinkStatement = dbConnection.prepareStatement("INSERT OR IGNORE INTO Link values(?,?);");
            increaseOutgoingStatement = dbConnection.prepareStatement("UPDATE Webcrawler SET Outgoing = Outgoing+1 WHERE id=?");
    */

            //Pagerank
            prepareCalculationRound = dbConnection.prepareStatement("Update Webcrawler set vektor = pagerank, value = pagerank/outgoing , pagerank = 0;");
            countNodesStatement = dbConnection.prepareStatement("SELECT Count(*) FROM Webcrawler");
            initMandatoryCalculationValues = dbConnection.prepareStatement("Update Webcrawler set outgoing = (select count(*)from link where source = id), vektor = 0, value = 0, pagerank = ?");
            pageRankCalculation = dbConnection.prepareStatement("update Webcrawler set pagerank = (Select sum(value) from Webcrawler w1 join link l1 on w1.id = l1.source where l1.target = Webcrawler.id)");
            dampCalculationStatement = dbConnection.prepareStatement("Update Webcrawler SET pagerank=pagerank*?+?*?");
            getUnfinishedNodesWithDifBiggerThen = dbConnection.prepareStatement("select abs(vektor - Pagerank) as difference from Webcrawler where difference > ?");


/*
            prepareCalculationStatement = dbConnection.prepareStatement("Update Webcrawler SET vektor=pagerank, pagerank=0");
            getWebcrawlerStatement = dbConnection.prepareStatement("SELECT * FROM Webcrawler");
            getLinkStatement = dbConnection.prepareStatement("SELECT * FROM Link");
            countOutgoingLinksStatement = dbConnection.prepareStatement("SELECT Count(*) AS COUNT FROM Link WHERE source = ?");
            increasePagerankStatement = dbConnection.prepareStatement("Update Webcrawler SET pagerank=pagerank+? WHERE id=?");
            getOutgoingLinksStatement = dbConnection.prepareStatement("SELECT * FROM Link WHERE source=?");
            increasePagerankStatementBatch = dbConnection.prepareStatement("Update Webcrawler SET pagerank=pagerank+? WHERE id=?");
            initVektorStatement = dbConnection.prepareStatement("Update Webcrawler SET vektor=?");
            setOutgoingStatement = dbConnection.prepareStatement("UPDATE Webcrawler SET Outgoing = ? WHERE id=?");
            vektorSumStatement = dbConnection.prepareStatement("SELECT SUM(Vektor) FROM Webcrawler");
    */
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
        //ResultSet idResultSet = dbConnection.createStatement().executeQuery("SELECT id FROM Webcrawler WHERE url='"+url+"'");
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
/*
        dbConnection.commit();
*/
        int id = addNewNodeStatement.getGeneratedKeys().getInt(1);
        return id;
    }

    synchronized public void createLinkBatch(int sourceID, int targetID) throws SQLException {
        createLinkStatementBatch.setInt(1,sourceID);
        createLinkStatementBatch.setInt(2,targetID);
        createLinkStatementBatch.addBatch();
/*
        batchCounter++;
        if (batchCounter==900){
            executeLinkBatch();
        }
*/
    }

    synchronized public void executeLinkBatch() {
        try {
            createLinkStatementBatch.executeBatch();
            createLinkStatementBatch.clearBatch();
/*
            batchCounter=0;
            dbConnection.commit();
*/
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*synchronized public void createLink(int sourceID, int targetID) throws SQLException {
        createLinkStatement.setInt(1,sourceID);
        createLinkStatement.setInt(2,targetID);
        createLinkStatement.execute();
    }*/

    /*synchronized public void increaseOutgoing(int id) throws SQLException {
        increaseOutgoingStatement.setInt(1,id);
        increaseOutgoingStatement.execute();
    }*/

    /*
        synchronized public ResultSet getWebcrawlerTable() throws SQLException {
            return getWebcrawlerStatement.executeQuery();
        }
    */



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

    /*
        synchronized public void prepareCalculation() throws SQLException {
            prepareCalculationStatement.execute();
        }
    */

    /*synchronized public ResultSet getLinkTable() throws SQLException {
        return getLinkStatement.executeQuery();
    }*/
    /*synchronized public int countOutgoingLinks(int sourceID) throws SQLException {
        countOutgoingLinksStatement.setInt(1,sourceID);
        return countOutgoingLinksStatement.executeQuery().getInt(1);
    }*/

    /*synchronized public void increasePageRank(double increasingValue,int targetID) throws SQLException {
        increasePagerankStatement.setDouble(1,increasingValue);
        increasePagerankStatement.setInt(2,targetID);
        increasePagerankStatement.execute();
    }*/

    /*synchronized public void increasePageRankBatch(double increasingValue,int targetID) throws SQLException {
        increasePagerankStatementBatch.setDouble(1,increasingValue);
        increasePagerankStatementBatch.setInt(2,targetID);
        increasePagerankStatementBatch.addBatch();
    }*/

/*
    synchronized public void executePagerankBatch(){
        try {
            increasePagerankStatementBatch.executeBatch();
            increasePagerankStatementBatch.clearBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
*/

    /*synchronized public ResultSet getOutgoingLinks(int sourceID) throws SQLException {
        getOutgoingLinksStatement.setInt(1,sourceID);
        return getOutgoingLinksStatement.executeQuery();
    }*/

    /*synchronized public void initVektor(Double equalDistributionVektorValue) throws SQLException {
        initVektorStatement.setDouble(1,equalDistributionVektorValue);
        initVektorStatement.execute();
    }*/

    /*synchronized public void setOutgoingLinksValue(int outgoing,int id) throws SQLException {
        setOutgoingStatement.setInt(1,outgoing);
        setOutgoingStatement.setInt(2,id);
        setOutgoingStatement.execute();
    }*/

/*    synchronized public double getVektorSum() throws SQLException {
        return vektorSumStatement.executeQuery().getDouble(1);
    }*/

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
