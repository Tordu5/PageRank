import java.sql.*;

public class PageRank {

    private static double COMPARE_DELTA = 0.0001;
    private static double DAMPINGFACTOR = 0.9;
    private static boolean isCalculationDamped = true;
    private DataAccess dbAccess;
    private Double  equalDistributionValue;
    public Connection dbConnection;

    /*
    connect to database and initialize vektor of every node with 1/amountOfNodes
     */
    public PageRank() throws SQLException, ClassNotFoundException {
        dbAccess = DataAccess.getAccess();
        //getConnection();
        initializeVector();
    }

    /*
    looping calculation while pagerank != vektor, every loop is headed by setting vektor = pagerank and pagerank = 0
     */
    public void calculatePageRank() throws SQLException {
        calculate();
        while (!isCalculationFinished()){
            //Summe des Vektors Berechnen
            /*
            ResultSet result = dbConnection.createStatement().executeQuery("select sum(vektor) From Webcrawler");
            System.out.println(result.getDouble(1));
            */
            System.out.println(dbAccess.getVektorSum());
            prepareNextRound();
            calculate();
        }
    }

    /*
    set every vektor at the beginning of the calculation to initial value which is 1/amountOfNodes
     */
    private void initializeVector() throws SQLException {
        /*
        equalDistributionValue = getEqualDistributionValue();
        PreparedStatement updateQuery = dbConnection.prepareStatement("Update Webcrawler SET vektor=?,pagerank=0");
        updateQuery.setDouble(1,equalDistributionValue);
        updateQuery.execute();
        */
        dbAccess.initVektor(getEqualDistributionVektorValue());
    }

    /*
    count amount of nodes
     */
    public int getAmountOfNodes() throws SQLException {
        /*
        ResultSet countNodesStatement = dbConnection.createStatement().executeQuery("SELECT Count(*) AS Count FROM Webcrawler");
        return countNodesStatement.getInt("Count");
        */
        return dbAccess.getNodesCount();
    }

    /*
    iterate over all nodes, calculate increasing value by dividing vektor by the amount of outgoing links and
    increase the pagerank value of the nodes of the outgoing links
     */
    public void calculate() throws SQLException {
        /*
        PreparedStatement nodesQuery = dbConnection.prepareStatement("SELECT * FROM Webcrawler");
        ResultSet node = nodesQuery.executeQuery();
        */
        ResultSet nodes = dbAccess.getWebcrawlerTable();
        while (nodes.next()){
            int id = nodes.getInt("id");
            double vektor = nodes.getDouble("Vektor");
            int outgoingLinksCount = getOutgoingLinksCount(id);
            double increasingValue = vektor/outgoingLinksCount;
            increaseOutgoingLinksByValue(id,increasingValue);
        }
        if (isCalculationDamped()){
            dampPageRank();
        }
        return;
    }

    private void dampPageRank() throws SQLException {
        /*
        PreparedStatement prepareDampStatement = dbConnection.prepareStatement("Update Webcrawler SET pagerank=pagerank*?+?*?");
        prepareDampStatement.setDouble(1,DAMPINGFACTOR);
        prepareDampStatement.setDouble(2,getEqualDistributionValue());
        prepareDampStatement.setDouble(3,1-DAMPINGFACTOR);
        prepareDampStatement.execute();
        */
        dbAccess.dampPageRank(DAMPINGFACTOR,equalDistributionValue);
    }

    /*
    get amount of outgoing links for the specified sourceId
     */
    private int getOutgoingLinksCount(int sourceId) throws SQLException {
        /*
        PreparedStatement countOutgoingLinksStatement = dbConnection.prepareStatement("SELECT Count(*) AS COUNT FROM Link WHERE source = ?");
        countOutgoingLinksStatement.setInt(1,sourceId);
        ResultSet result = countOutgoingLinksStatement.executeQuery();
        return result.getInt("Count");
        */
        return dbAccess.countOutgoingLinks(sourceId);
    }

    /*
    get all outgoing links and increase the value of the Pagerank of the connected nodes
     */
    private void increaseOutgoingLinksByValue(int sourceId,double value) throws SQLException {
        /*
        PreparedStatement outgoingLinksQuery = dbConnection.prepareStatement("SELECT * FROM Link WHERE source=?");
        outgoingLinksQuery.setInt(1,sourceId);
        ResultSet outgoingLink = outgoingLinksQuery.executeQuery();
        */
        ResultSet outgoingLink = dbAccess.getOutgoingLinks(sourceId);

        while (outgoingLink.next()){
            int targetId = outgoingLink.getInt("target");
            increasePagerankByValue(targetId,value);
        }
    }

    /*
    increases the pageranke of a specified node by the given value
     */
    private void increasePagerankByValue(int targetId,double increasingValue) throws SQLException {
        /*
        PreparedStatement increasePagerankStatement = dbConnection.prepareStatement("Update Webcrawler SET pagerank=pagerank+? WHERE id=?");
        increasePagerankStatement.setDouble(1,value);
        increasePagerankStatement.setInt(2,targetId);
        increasePagerankStatement.execute();
        */
        dbAccess.increasePageRank(increasingValue,targetId);
    }

    /*
    prepare the next calculation round by setting vektor to pagerank and pagerank to 0
     */
    private void prepareNextRound() throws SQLException {

        /*
        PreparedStatement prepareCalculationStatement = dbConnection.prepareStatement("Update Webcrawler SET vektor=pagerank, pagerank=0");
        prepareCalculationStatement.execute();
        */
        dbAccess.prepareCalculation();
    }

    private boolean isCalculationDamped() {
        return isCalculationDamped;
    }

    /*
    check every node if the vektor and pagerank are similar, if not it returns false by the first node which dont fit
     */
    private boolean isCalculationFinished() throws SQLException {
        /*
        PreparedStatement nodesQuery = dbConnection.prepareStatement("SELECT * FROM Webcrawler");
        ResultSet node = nodesQuery.executeQuery();
        */
        ResultSet node = dbAccess.getWebcrawlerTable();
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

    private void getConnection() throws ClassNotFoundException, SQLException {
        //Erstelle Verbindung zu DB
        Class.forName("org.sqlite.JDBC");
        dbConnection = DriverManager.getConnection("jdbc:sqlite:WebcrawlerData.db");
    }
    */
    private double getEqualDistributionVektorValue() throws SQLException {
        if (equalDistributionValue==null){
            equalDistributionValue = (1.0/dbAccess.getNodesCount());
        }
        return equalDistributionValue;
    }
}
