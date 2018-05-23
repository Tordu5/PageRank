import java.sql.*;

public class SQLCrawlerStatements {
    private static DataAccess access;
    private Connection dbConnection;

    //Crawler
    private PreparedStatement addNewNodeStatement;
    private PreparedStatement createLinkStatement;
    private PreparedStatement createLinkStatementBatch;
    private PreparedStatement findIdForUrlStatement;
    private PreparedStatement increaseOutgoingStatement;

    public SQLCrawlerStatements(){
        access = DataAccess.getAccess();
        dbConnection = access.getConnection();
        initPreparedStatements();
    }

    public Connection getConnection(){
        return dbConnection;
    }

    private void initPreparedStatements(){
        try {
            //Crawler
            findIdForUrlStatement = dbConnection.prepareStatement("SELECT id FROM Webcrawler WHERE url=?");
            addNewNodeStatement = dbConnection.prepareStatement("INSERT OR IGNORE INTO Webcrawler values(?,?,?,?,?,?);", Statement.RETURN_GENERATED_KEYS);
            createLinkStatement = dbConnection.prepareStatement("INSERT OR IGNORE INTO Link values(?,?);");
            createLinkStatementBatch = dbConnection.prepareStatement("INSERT OR IGNORE INTO Link values(?,?);");
            increaseOutgoingStatement = dbConnection.prepareStatement("UPDATE Webcrawler SET Outgoing = Outgoing+1 WHERE id=?");
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
        ResultSet resultSet = addNewNodeStatement.getGeneratedKeys();
        //ResultSet resultSet =dbConnection.createStatement().executeQuery("INSERT OR IGNORE INTO Webcrawler(url) values("+url+");" +
        //       "SELECT id from Webcrawler where url ='"+url+"'");
        if (resultSet.next()){
            return resultSet.getInt(1);
        } else {
            return 1;
        }
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
