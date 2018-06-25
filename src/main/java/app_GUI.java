import java.awt.EventQueue;
import java.io.IOException;

import java.sql.ResultSet;

import java.sql.SQLException;

public class app_GUI {

	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Window_Splash splash = new Window_Splash();
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	}
	
	
}
