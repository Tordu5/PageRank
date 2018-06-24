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



		if (false){
			startTime = System.currentTimeMillis();

			Master master = new Master();
			master.setStartUrl("https://duckduckgo.com/");
			master.setAmountOfThreads(4);
			master.setMaxNodes(600);
			master.start();

			stopTime = System.currentTimeMillis();
			elapsedTime = stopTime - startTime;
			System.out.println("Duration :"+elapsedTime);
		} else if (false){
			startTime = System.currentTimeMillis();

			pagerank.setCalculationDamped(false);
			pagerank.setDampingFactor(0.85);
			pagerank.setCompareDelta(0.005);
			pagerank.calculate();

			stopTime = System.currentTimeMillis();
			elapsedTime = stopTime - startTime;
			System.out.println("Duration :"+elapsedTime);

		}

		System.out.println("starting");
		startTime = System.currentTimeMillis();

		new Master().createVizualizationJson();

		stopTime = System.currentTimeMillis();
		elapsedTime = stopTime - startTime;
		System.out.println(elapsedTime);
		System.out.println("json created");

	}

}
