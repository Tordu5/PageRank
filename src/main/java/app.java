import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class app {

	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		System.out.println("starting");
		long startTime = System.currentTimeMillis();
		PageRank pagerank = new PageRank();
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println(elapsedTime);
		System.out.println("Pagerank created");


		//try {
		//System.out.println("stoping start time");
		//long startTime = System.currentTimeMillis();
		//	Crawler crawler = new Crawler(40);
		//	crawler.startAtSite("https://duckduckgo.com/");
		//System.out.println("starting calculation");
		//pagerank.calculatePageRank();
		//long stopTime = System.currentTimeMillis();
		//long elapsedTime = stopTime - startTime;
		//System.out.println(elapsedTime);
		//} catch (ClassNotFoundException e) {
		//	e.printStackTrace();
		//} catch (SQLException e) {
		//	e.printStackTrace();
		//}


		new Master("https://duckduckgo.com/",4);
		//System.out.println("Starting Calculation");
		//for (int i = 1;i<=100;i++) {
		//	System.out.println("Calculation Preparation "+i);
		//	startTime = System.currentTimeMillis();
		//	pagerank.preparePageRankCalculation();
		//	stopTime = System.currentTimeMillis();
		//	elapsedTime = stopTime - startTime;
		//	System.out.println(elapsedTime);
		//	System.out.println("Calculation "+i);
		//startTime = System.currentTimeMillis();
		//pagerank.pageRankCalculation();
		//stopTime = System.currentTimeMillis();
		//elapsedTime = stopTime - startTime;
		//System.out.println(elapsedTime);
		//}
	}

}
