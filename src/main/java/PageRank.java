import java.sql.*;

public class PageRank {

    private double compareDelta;
    private double dampingFactor;
    private boolean isCalculationDamped;
    private DataAccess dbAccess;
    private Double  equalDistributionValue;

    private Connection dbConnection ;


    public PageRank() throws SQLException, ClassNotFoundException {
        compareDelta = 0.000001;
        dampingFactor = 0.9;
        isCalculationDamped = true;
        dbAccess = DataAccess.getAccess();
        equalDistributionValue = 1/(double)getAmountOfNodes();
        initMandatoryCalculationValues(equalDistributionValue);
    }

    public void setCalculationDamped(boolean isDamped){
        this.isCalculationDamped = isDamped;
    }

    public void setDampingFactor(double dampingPercentage){
        this.dampingFactor = dampingPercentage;
    }

    public void setCompareDelta(double delta){
        this.compareDelta = delta;
    }

    public int getAmountOfNodes() throws SQLException {
        return dbAccess.getNodesCount();
    }

    private void initMandatoryCalculationValues(Double equalDistributionValue) throws SQLException {
        dbAccess.initMandatoryCalculationValues(equalDistributionValue);
    }

    public void calculate() throws SQLException {
        while (!isCalculationFinished()){
            long startTime = System.currentTimeMillis();

            preparePageRankCalculation();
            calculatePageRank();
            if (isCalculationDamped){
                dampPageRank();
            }

            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            System.out.println();
            System.out.println("calculation round in : "+ elapsedTime);
        }
    }

    private void dampPageRank() throws SQLException {
        dbAccess.dampPageRank(dampingFactor,equalDistributionValue);
    }

    public void preparePageRankCalculation() throws SQLException {
        dbAccess.prepareCalculationRound();
    }

    public void calculatePageRank() throws SQLException {
        dbAccess.calculatePageRank();
    }

    private boolean isCalculationFinished() throws SQLException {
        return dbAccess.getUnfinishedNodesWithDifBiggerThen(compareDelta).isAfterLast();
    }

    private boolean isCalculationDamped() {
        return isCalculationDamped;
    }

    /*
    set every vektor at the beginning of the calculation to initial value which is 1/amountOfNodes
     */
    /* private void prepareNextRound() throws SQLException {
         dbAccess.prepareCalculation();
     }*/

    /*
        private void initializeOutgoing() throws SQLException {
            ResultSet node = dbAccess.getWebcrawlerTable();

            while (node.next()){
                int id = node.getInt("id");
                int amountOutgoingLinks = getOutgoingLinksCount(id);
                dbAccess.setOutgoingLinksValue(amountOutgoingLinks,id);
            }
        }
    */
/*
    private void initializeVector() throws SQLException {
        dbAccess.initVektor(getEqualDistributionVektorValue());
    }
*/

    /*
    looping calculation while pagerank != vektor, every loop is headed by setting vektor = pagerank and pagerank = 0
     */

    /*
    iterate over all nodes, calculate increasing value by dividing vektor by the amount of outgoing links and
    increase the pagerank value of the nodes of the outgoing links
     */
/*
    public void calculate() throws SQLException {
        ResultSet nodes = dbAccess.getWebcrawlerTable();
        System.out.println("calculate");
        long startTime = System.currentTimeMillis();
        System.out.println("starting calculation");
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
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("Calulation finished in :"+elapsedTime);
        return;
    }
*/

    /*
    get amount of outgoing links for the specified sourceId
     */
/*
    private int getOutgoingLinksCount(int sourceId) throws SQLException {
        return dbAccess.countOutgoingLinks(sourceId);
    }
*/

    /*
    get all outgoing links and increase the value of the Pagerank of the connected nodes
     */
/*
    private void increaseOutgoingLinksByValue(int sourceId,double value) throws SQLException {
        ResultSet outgoingLink = dbAccess.getOutgoingLinks(sourceId);

        while (outgoingLink.next()){
            int targetId = outgoingLink.getInt("target");
            increasePagerankByValue(targetId,value);
        }
        //dbAccess.executePagerankBatch();
    }
*/

    /*
    increases the pageranke of a specified node by the given value
     */
/*
    private void increasePagerankByValue(int targetId,double increasingValue) throws SQLException {
        dbAccess.increasePageRank(increasingValue,targetId);
        //dbAccess.increasePageRankBatch(increasingValue,targetId);
    }
*/

    /*
    prepare the next calculation round by setting vektor to pagerank and pagerank to 0
     */

    /*
    compare vektor and pagerank of given node by given COMPARE_DELTA
     */
/*
    private boolean isPageRankSimilarToVektor(ResultSet node) throws SQLException {
        double vektor = node.getDouble("Vektor");
        double pageRank = node.getDouble("pagerank");
        if (Math.abs(vektor-pageRank) >= COMPARE_DELTA){
            return false;
        } else {
            return true;
        }
    }
*/

   /* private double getEqualDistributionVektorValue() throws SQLException {
        if (equalDistributionValue==null){
            equalDistributionValue = (1.0/dbAccess.getNodesCount());
        }
        return equalDistributionValue;
    }*/

   /*    public void preparePageRankCalculation(){
        dbConnection = dbAccess.getConnection();

        try {
            dbConnection.createStatement().execute("Update Webcrawler set\n" +
                    "vektor = pagerank,\n" +
                    "value = pagerank/outgoing,\n" +
                    "pagerank = 0");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void pageRankCalculation(){
        dbConnection = dbAccess.getConnection();

        try {
            dbConnection.createStatement().execute("update Webcrawler \n" +
                    "set pagerank = (\n" +
                    "\tSelect sum(value) \n" +
                    "\tfrom Webcrawler w1 \n" +
                    "\tjoin link l1 \n" +
                    "\ton w1.id = l1.source \n" +
                    "\twhere l1.target = Webcrawler.id\n" +
                    ")\n");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }*/
}
