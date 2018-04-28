import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class app {

	public static void main(String[] args) {


		try {
			long startTime = System.currentTimeMillis();
			Crawler crawler = new Crawler(40);
			crawler.startAtSite("https://duckduckgo.com/");
			//new PageRank().calculatePageRank();
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			System.out.println(elapsedTime);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}



		//new Master("https://duckduckgo.com/",4);

	}

}
