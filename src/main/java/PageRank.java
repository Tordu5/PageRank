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
        initializeVector();
        initializeOutgoing();
    }

    /*
    set every vektor at the beginning of the calculation to initial value which is 1/amountOfNodes
     */
    private void initializeVector() throws SQLException {
        dbAccess.initVektor(getEqualDistributionVektorValue());
    }

    private void initializeOutgoing() throws SQLException {
        ResultSet node = dbAccess.getWebcrawlerTable();

        while (node.next()){
            int id = node.getInt("id");
            int amountOutgoingLinks = getOutgoingLinksCount(id);
            dbAccess.setOutgoingLinksValue(amountOutgoingLinks,id);
        }
    }

    /*
    looping calculation while pagerank != vektor, every loop is headed by setting vektor = pagerank and pagerank = 0
     */
    public void calculatePageRank() throws SQLException {
        calculate();
        while (!isCalculationFinished()){
            System.out.println(dbAccess.getVektorSum());
            prepareNextRound();
            calculate();
        }
    }

    /*
    count amount of nodes
     */
    public int getAmountOfNodes() throws SQLException {
        return dbAccess.getNodesCount();
    }

    /*
    iterate over all nodes, calculate increasing value by dividing vektor by the amount of outgoing links and
    increase the pagerank value of the nodes of the outgoing links
     */
    public void calculate() throws SQLException {
        ResultSet nodes = dbAccess.getWebcrawlerTable();
        while (nodes.next()){
            int id = nodes.getInt("id");
            double vektor = nodes.getDouble("Vektor");
            //int outgoingLinksCount = getOutgoingLinksCount(id);
            int outgoingLinksCount = nodes.getInt("Outgoing");
            double increasingValue = vektor/outgoingLinksCount;
            increaseOutgoingLinksByValue(id,increasingValue);
        }
        if (isCalculationDamped()){
            dampPageRank();
        }
        return;
    }

    private void dampPageRank() throws SQLException {
        dbAccess.dampPageRank(DAMPINGFACTOR,equalDistributionValue);
    }

    /*
    get amount of outgoing links for the specified sourceId
     */
    private int getOutgoingLinksCount(int sourceId) throws SQLException {
        return dbAccess.countOutgoingLinks(sourceId);
    }

    /*
    get all outgoing links and increase the value of the Pagerank of the connected nodes
     */
    private void increaseOutgoingLinksByValue(int sourceId,double value) throws SQLException {
        ResultSet outgoingLink = dbAccess.getOutgoingLinks(sourceId);

        while (outgoingLink.next()){
            int targetId = outgoingLink.getInt("target");
            increasePagerankByValue(targetId,value);
        }
        //dbAccess.executePagerankBatch();
    }

    /*
    increases the pageranke of a specified node by the given value
     */
    private void increasePagerankByValue(int targetId,double increasingValue) throws SQLException {
        dbAccess.increasePageRank(increasingValue,targetId);
        //dbAccess.increasePageRankBatch(increasingValue,targetId);
    }

    /*
    prepare the next calculation round by setting vektor to pagerank and pagerank to 0
     */
    private void prepareNextRound() throws SQLException {
        dbAccess.prepareCalculation();
    }

    private boolean isCalculationDamped() {
        return isCalculationDamped;
    }

    /*
    check every node if the vektor and pagerank are similar, if not it returns false by the first node which dont fit
     */
    private boolean isCalculationFinished() throws SQLException {
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

    private double getEqualDistributionVektorValue() throws SQLException {
        if (equalDistributionValue==null){
            equalDistributionValue = (1.0/dbAccess.getNodesCount());
        }
        return equalDistributionValue;
    }
}
