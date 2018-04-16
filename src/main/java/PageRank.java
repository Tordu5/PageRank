import java.sql.*;

public class PageRank {

    private static double COMPARE_DELTA = 0.0001;
    public Connection dbConnection;

    /*
    connect to database and initialize vektor of every node with 1/amountOfNodes
     */
    public PageRank() throws SQLException, ClassNotFoundException {
        getConnection();
        initializeVector();
    }

    /*
    looping calculation while pagerank != vektor, every loop is headed by setting vektor = pagerank and pagerank = 0
     */
    public void calculatePageRank() throws SQLException {
        calculate();
        while (!isCalculationFinished()){
            //Summe des Vektors Berechnen
            ResultSet result = dbConnection.createStatement().executeQuery("select sum(vektor) From Webcrawler");
            System.out.println(result.getDouble(1));
            prepareNextRound();
            calculate();
        }
    }

    /*
    set every vektor at the beginning of the calculation to initial value which is 1/amountOfNodes
     */
    private void initializeVector() throws SQLException {
        int nodesCount = getAmountOfNodes();
        double initialValue = 1.0/nodesCount;

        PreparedStatement updateQuery = dbConnection.prepareStatement("Update Webcrawler SET vektor=?,pagerank=0");
        updateQuery.setDouble(1,initialValue);
        updateQuery.execute();
    }

    /*
    count amount of nodes
     */
    public int getAmountOfNodes() throws SQLException {
        ResultSet countNodesStatement = dbConnection.createStatement().executeQuery("SELECT Count(*) AS Count FROM Webcrawler");
        return countNodesStatement.getInt("Count");
    }

    /*
    iterate over all nodes, calculate increasing value by dividing vektor by the amount of outgoing links and
    increase the pagerank value of the nodes of the outgoing links
     */
    public void calculate() throws SQLException {
        PreparedStatement nodesQuery = dbConnection.prepareStatement("SELECT * FROM Webcrawler");
        ResultSet node = nodesQuery.executeQuery();

        while (node.next()){
            int id = node.getInt("id");
            double vektor = node.getDouble("Vektor");
            int outgoingLinksCount = getOutgoingLinksCount(id);
            double increasingValue = vektor/outgoingLinksCount;
            increaseOutgoingLinksByValue(id,increasingValue);
        }
        return;
    }

    /*
    get amount of outgoing links for the specified sourceId
     */
    private int getOutgoingLinksCount(int sourceId) throws SQLException {
        PreparedStatement countOutgoingLinksStatement = dbConnection.prepareStatement("SELECT Count(*) AS COUNT FROM Link WHERE source = ?");
        countOutgoingLinksStatement.setInt(1,sourceId);
        ResultSet result = countOutgoingLinksStatement.executeQuery();
        return result.getInt("Count");
    }

    /*
    get all outgoing links and increase the value of the Pagerank of the connected nodes
     */
    private void increaseOutgoingLinksByValue(int sourceId,double value) throws SQLException {
        PreparedStatement outgoingLinksQuery = dbConnection.prepareStatement("SELECT * FROM Link WHERE source=?");
        outgoingLinksQuery.setInt(1,sourceId);
        ResultSet outgoingLink = outgoingLinksQuery.executeQuery();

        while (outgoingLink.next()){
            int targetId = outgoingLink.getInt("target");
            increasePagerankByValue(targetId,value);
        }
    }

    /*
    increases the pageranke of a specified node by the given value
     */
    private void increasePagerankByValue(int targetId,double value) throws SQLException {
        PreparedStatement increasePagerankStatement = dbConnection.prepareStatement("Update Webcrawler SET pagerank=pagerank+? WHERE id=?");
        increasePagerankStatement.setDouble(1,value);
        increasePagerankStatement.setInt(2,targetId);
        increasePagerankStatement.execute();
    }

    /* Erwies sich vorerst als nicht notwendig
    private double getVektorValue(int id) throws SQLException {
        PreparedStatement updateQuery = dbConnection.prepareStatement("SELECT Vektor FROM Webcrawler WHERE id = ?");
        updateQuery.setInt(1,id);
        ResultSet result = updateQuery.executeQuery();
        return result.getDouble("Vektor");
    }
    */

    /*
    prepare the next calculation round by setting vektor to pagerank and pagerank to 0
     */
    private void prepareNextRound() throws SQLException {
        PreparedStatement prepareCalculationStatement = dbConnection.prepareStatement("Update Webcrawler SET vektor=pagerank, pagerank=0");
        prepareCalculationStatement.execute();
    }

    /*
    check every node if the vektor and pagerank are similar, if not it returns false by the first node which dont fit
     */
    private boolean isCalculationFinished() throws SQLException {
        PreparedStatement nodesQuery = dbConnection.prepareStatement("SELECT * FROM Webcrawler");
        ResultSet node = nodesQuery.executeQuery();

        while (node.next()){
            if (!isPageRankSimilarToVektor(node)){
                return false;
            }
        }
        return true;
    }

    /*
    compare vektor and pagerank of given node by given COMPARE_DELTA
     */
    private boolean isPageRankSimilarToVektor(ResultSet node) throws SQLException {
        double vektor = node.getDouble("Vektor");
        double pageRank = node.getDouble("pagerank");
        if (Math.abs(vektor-pageRank) >= COMPARE_DELTA){
            return false;
        } else {
            return true;
        }
    }

    /*
    connection to database
     */
    private void getConnection() throws ClassNotFoundException, SQLException {
        //Erstelle Verbindung zu DB
        Class.forName("org.sqlite.JDBC");
        dbConnection = DriverManager.getConnection("jdbc:sqlite:WebcrawlerData.db");
    }
}
