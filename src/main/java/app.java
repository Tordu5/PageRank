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
			master.setAmountOfThreads(1);
			master.setMaxNodes(100);
			master.start();

			stopTime = System.currentTimeMillis();
			elapsedTime = stopTime - startTime;
			System.out.println("Duration :"+elapsedTime);
		} else if (true){
			startTime = System.currentTimeMillis();

			pagerank.setCalculationDamped(true);
			pagerank.setDampingFactor(0.78);
			pagerank.setCompareDelta(0.000001);
			pagerank.calculate();

			stopTime = System.currentTimeMillis();
			elapsedTime = stopTime - startTime;
			System.out.println("Duration :"+elapsedTime);

		}

	}

}
