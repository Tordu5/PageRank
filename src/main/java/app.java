import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class app {

	public static void main(String[] args) {
		try {
			Crawler crawler = new Crawler(30);
			crawler.startAtSite("https://duckduckgo.com/");
			new PageRank().calculatePageRank();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		
	}

}
