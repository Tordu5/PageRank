import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import no.uib.cipr.matrix.DenseMatrix;

public class PageRankMatrix {
    public static Connection dbConnection;
    public static DenseMatrix tabelle;
    public ResultSet Counter;
    public int RoundCounter;

    public void start() throws ClassNotFoundException, SQLException, IOException {

        getConnection();
        init();
        calc();
        System.out.println("Anzahl der Runden: " + RoundCounter);

    }

    private void init() throws SQLException {
        Counter = dbConnection.createStatement().executeQuery("SELECT Max (id) FROM Webcrawler");

        /*
         * Websiten Liste erstellen
         */
        ResultSet webpages = dbConnection.createStatement().executeQuery("SELECT ID FROM Webcrawler ");
        ArrayList<Double> WebpagesListe = new ArrayList<Double>();
        while (webpages.next()) {
            WebpagesListe.add(webpages.getDouble(1));

        }

        /*
         * Source Liste erstellen
         */
        ResultSet source = dbConnection.createStatement().executeQuery("SELECT Source FROM Link ");
        ArrayList<Integer> Liste = new ArrayList<Integer>();
        while (source.next()) {
            Liste.add(source.getInt(1));
        }

        /*
         * Target Liste erstellen
         */
        ResultSet target = dbConnection.createStatement().executeQuery("SELECT Target FROM Link ");
        ArrayList<Integer> Zielliste = new ArrayList<Integer>();
        while (target.next()) {
            Zielliste.add(target.getInt(1));

        }

        /*
         * Matrix erzeugen aus Websiten und befuellen
         */
        tabelle = new DenseMatrix(Counter.getInt(1), Counter.getInt(1));
        for (int i = 0; i < Liste.size(); i++) {

            int source2 = Liste.get(i) - 1;
            int target2 = Zielliste.get(i) - 1;

            ResultSet targetCounter = dbConnection.createStatement()
                    .executeQuery("SELECT COUNT(target) FROM Link WHERE source = " + Liste.get(i));
            double wert = 1.0 / targetCounter.getDouble(1);

            tabelle.add(target2, source2, wert);
            System.out.println(i+" von " + Liste.size());

        }

        /*
         * Vektor erstellen und befuellen mit Wert 1/Anzahl der Websiten
         */
        DenseMatrix vektor = new DenseMatrix(Counter.getInt(1), 1);
        double value = 1.0 / Counter.getDouble(1);
        for (int i = 0; i < Counter.getInt(1); i++) {
            vektor.add(i, 0, value);
        }

        /*
         * Berechnung Weblisten Matrix x Vektor
         */
        DenseMatrix result = new DenseMatrix(tabelle.numRows(), vektor.numColumns());
        tabelle.mult(vektor, result);

        /*
         * In DB schreiben
         */
        for (int i = 1; i < Counter.getInt(1) + 1; i++) {
            dbConnection.createStatement()
                    .execute("UPDATE `Webcrawler` SET `PageRank`=" + result.get(i - 1, 0) + " WHERE id=" + i);
        }

        /*
         * Erzeugt Summe zum Vergleichen
         */
        ResultSet sum = dbConnection.createStatement().executeQuery("SELECT SUM (PageRank) FROM Webcrawler");
        System.out.println("Summe: " + sum.getDouble(1));

        /*
         * PageRank in Vektor Spalte kopieren
         */
        PreparedStatement prepareCalc = dbConnection
                .prepareStatement("UPDATE Webcrawler SET Vektor=PageRank, pagerank=0");
        prepareCalc.execute();
    }

    /*
     * Pruefen ob PageRank Wert = Vektor ist
     */
    private boolean isPageRankSimilarToVekor() throws SQLException {
        ResultSet ranks = dbConnection.createStatement().executeQuery("SELECT * FROM Webcrawler");
        double vektor = ranks.getDouble("Vektor");
        double pageRank = ranks.getDouble("PageRank");

        if (Math.abs(vektor - pageRank) >= 0.0001) {
            return false;
        } else {
            return true;

        }

    }

    /*
     * Schleife bis PageRank = Vektor
     */
    private void calc() throws SQLException {

        while (!isPageRankSimilarToVekor()) {

            ResultSet VektorSQL = dbConnection.createStatement().executeQuery("SELECT Vektor FROM Webcrawler ");
            ArrayList<Double> VektorSQLListe = new ArrayList<Double>();
            while (VektorSQL.next()) {
                VektorSQLListe.add(VektorSQL.getDouble(1));

            }

            /*
             * Vektor erstellen
             */
            DenseMatrix vektor = new DenseMatrix(Counter.getInt(1), 1);

            for (int i = 0; i < Counter.getInt(1); i++) {
                vektor.add(i, 0, VektorSQLListe.get(i));
            }

            /*
             * Berechnung
             */
            DenseMatrix result = new DenseMatrix(tabelle.numRows(), vektor.numColumns());
            tabelle.mult(vektor, result);

            /*
             * In DB schreiben
             */

            for (int i = 1; i < Counter.getInt(1) + 1; i++) {
                dbConnection.createStatement()
                        .execute("UPDATE `Webcrawler` SET `PageRank`=" + result.get(i - 1, 0) + " WHERE id=" + i);
            }

            ResultSet sum = dbConnection.createStatement().executeQuery("SELECT SUM (PageRank) FROM Webcrawler");
            System.out.println("Summe: " + sum.getDouble(1));

            PreparedStatement prepareCalc = dbConnection
                    .prepareStatement("UPDATE Webcrawler SET Vektor=PageRank, pagerank=0");
            prepareCalc.execute();

            RoundCounter++;
        }
    }

    private void getConnection() throws ClassNotFoundException, SQLException, IOException {
        // Erstelle Verbindung zu DB
        Class.forName("org.sqlite.JDBC");
        dbConnection = DriverManager.getConnection("jdbc:sqlite:WebcrawlerData.db");
    }

}

