import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Desktop;

import net.proteanit.sql.DbUtils;

import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Window_Main {

	private JFrame frame;
	private final JPanel panel_1 = new JPanel();
	private JPanel panel_2 = new JPanel();
	private JPanel panel_4 = new JPanel();
	private JTextField Text_Page;
	private JTextField Text_Page_Num;
	public JTable table;
	private Connection dbConnection;
	public static final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";
	private JTextField textField;
	

	/**
	 * Create the application.
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public Window_Main() throws ClassNotFoundException, SQLException, IOException {
		initialize();
		getConnection();
		frame.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.getContentPane().setBackground(new Color(255, 255, 255));
		frame.setBounds(100, 100, 1091, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBackground(new Color(255, 153, 0));
		panel.setBounds(0, 0, 265, 605);
		frame.getContentPane().add(panel);
		panel.setLayout(null);
		
		JLabel lblPagerank = new JLabel("PageRank");
		lblPagerank.setHorizontalAlignment(SwingConstants.CENTER);
		lblPagerank.setFont(new Font("Tahoma", Font.BOLD, 30));
		lblPagerank.setBounds(34, 24, 195, 70);
		panel.add(lblPagerank);
		
		JPanel panel_Start = new JPanel();
		
		panel_Start.setBackground(new Color(255, 153, 0));
		panel_Start.setBounds(0, 179, 265, 70);
		panel.add(panel_Start);
		panel_Start.setLayout(null);
		
		JLabel lblStart = new JLabel("Start");
		lblStart.setBounds(76, 11, 106, 39);
		lblStart.setHorizontalAlignment(SwingConstants.CENTER);
		lblStart.setFont(new Font("Tahoma", Font.BOLD, 22));
		panel_Start.add(lblStart);
		
		JPanel panel_Visualisierung = new JPanel();
		panel_Visualisierung.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent arg0) {
				panel_Visualisierung.setBackground(new Color(255,204,0));
				
			}
			@Override
			public void mouseExited(MouseEvent e) {
				panel_Visualisierung.setBackground(new Color(255,153,0));
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				String OS = System.getProperty("os.name").toLowerCase();
								
				try {

					/*
					*---------------------------------------------------------------------------------------------------
					* 									Opening .html
					*---------------------------------------------------------------------------------------------------
					* */
					String location = new File("src/main/resources/index.html").toURI().toString();

					Desktop desktop = Desktop.getDesktop();

					// Adresse mit Standardbrowser anzeigen
					URI uri;
					try
					{
						uri = new URI(location);
						desktop.browse(uri);
					} catch (Exception oError)
					{
						oError.printStackTrace();
						// Hier Fehler abfangen
						System.out.println("Seite kann nicht geöffnet werden." + new File("src/main/resources/index.html").exists());

					}

					
					
				} catch (Exception e2) {
					JOptionPane.showMessageDialog(null, "Datei konnte nicht geladen werden. Bitte Pfad �berpr�fen.");
				}
			}
		});
		panel_Visualisierung.setLayout(null);
		panel_Visualisierung.setBackground(new Color(255, 153, 0));
		panel_Visualisierung.setBounds(0, 248, 265, 70);
		panel.add(panel_Visualisierung);
		
		JLabel lblVisualisieren = new JLabel("Visualisieren");
		lblVisualisieren.setHorizontalAlignment(SwingConstants.CENTER);
		lblVisualisieren.setFont(new Font("Tahoma", Font.BOLD, 22));
		lblVisualisieren.setBounds(53, 11, 156, 39);
		panel_Visualisierung.add(lblVisualisieren);
		
		JPanel panel_About = new JPanel();
		panel_About.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent arg0) {
				panel_About.setBackground(new Color(255,204,0));
				
			}
			@Override
			public void mouseExited(MouseEvent e) {
				panel_About.setBackground(new Color(255,153,0));
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				Window_About window = new Window_About();
				window.frame.setVisible(true);
				
			}
		});
		panel_About.setLayout(null);
		panel_About.setBackground(new Color(255, 153, 0));
		panel_About.setBounds(0, 317, 265, 70);
		panel.add(panel_About);

		JLabel lblAbout = new JLabel("About");
		lblAbout.setHorizontalAlignment(SwingConstants.CENTER);
		lblAbout.setFont(new Font("Tahoma", Font.BOLD, 22));
		lblAbout.setBounds(52, 11, 156, 39);
		panel_About.add(lblAbout);


		
		JPanel panel_6 = new JPanel();
		panel_6.setBackground(new Color(0, 0, 0));
		panel_6.setBounds(34, 95, 195, 3);
		panel.add(panel_6);
		

		panel_1.setBackground(new Color(255, 204, 0));
		panel_1.setBounds(263, 29, 812, 146);
		frame.getContentPane().add(panel_1);
		panel_1.setLayout(null);
		
		JLabel lblWebsite = new JLabel("Website:");
		lblWebsite.setHorizontalAlignment(SwingConstants.CENTER);
		lblWebsite.setFont(new Font("Tahoma", Font.BOLD, 22));
		lblWebsite.setBounds(21, 11, 106, 39);
		panel_1.add(lblWebsite);
		
		JLabel lblAnzahlDerSeiten = new JLabel("Anzahl der Seiten:");
		lblAnzahlDerSeiten.setHorizontalAlignment(SwingConstants.CENTER);
		lblAnzahlDerSeiten.setFont(new Font("Tahoma", Font.BOLD, 22));
		lblAnzahlDerSeiten.setBounds(21, 53, 211, 39);
		panel_1.add(lblAnzahlDerSeiten);
		
		Text_Page = new JTextField();
		Text_Page.setBounds(245, 11, 362, 39);
		panel_1.add(Text_Page);
		Text_Page.setColumns(10);
		
		Text_Page_Num = new JTextField();
		Text_Page_Num.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE))) {
					e.consume();
				}

			}
		});
		Text_Page_Num.setColumns(10);
		Text_Page_Num.setBounds(245, 53, 362, 39);
		panel_1.add(Text_Page_Num);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBackground(new Color(255, 204, 0));
		panel_2.setBounds(3, 92, 799, 43);
		panel_1.add(panel_2);
		panel_2.setVisible(false);
		panel_2.setLayout(null);
		
		
		textField = new JTextField();
		textField.setBounds(242, 3, 362, 39);
		panel_2.add(textField);
		textField.setColumns(10);
		
		JLabel lblSuche_1 = new JLabel("Suche:");
		lblSuche_1.setBounds(21, -2, 92, 39);
		panel_2.add(lblSuche_1);
		lblSuche_1.setHorizontalAlignment(SwingConstants.LEFT);
		lblSuche_1.setFont(new Font("Tahoma", Font.BOLD, 22));

		JPanel panel_4 = new JPanel();
		panel_4.setVisible(false);
		panel_4.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				try {
					PreparedStatement query = dbConnection.prepareStatement("Select * From Webcrawler");
					ResultSet rs = query.executeQuery();

					table.setModel(DbUtils.resultSetToTableModel(rs));

				} catch (SQLException e1) {
					e1.printStackTrace();
				}

			}
		});
		panel_4.setBackground(new Color(255, 204, 0));
		panel_4.setBounds(630, 53, 172, 39);
		panel_1.add(panel_4);

		JLabel lblResetAnsicht = new JLabel("Reset Ansicht");
		lblResetAnsicht.setHorizontalAlignment(SwingConstants.CENTER);
		lblResetAnsicht.setFont(new Font("Tahoma", Font.BOLD, 22));
		panel_4.add(lblResetAnsicht);

		JPanel panel_3 = new JPanel();
		panel_3.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent arg0) {
				panel_3.setBackground(Color.WHITE );
				
			}
			@Override
			public void mouseExited(MouseEvent e) {
				panel_3.setBackground(new Color(255,204,0));
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				if (textField.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null, "Bitte Suchtext eingeben.");
				}else{
					try {
					PreparedStatement query = dbConnection.prepareStatement("SELECT id, url, PageRank FROM Webcrawler WHERE url like '%"+textField.getText()+"%' ORDER BY PageRank DESC LIMIT 10");
					ResultSet rs = query.executeQuery();

					table.setModel(DbUtils.resultSetToTableModel(rs));
					
					panel_4.setVisible(true);

				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				}
				
			}
		});
		panel_3.setBackground(new Color(255, 204, 0));
		panel_3.setBounds(653, 3, 124, 39);
		panel_2.add(panel_3);
		
		JLabel label = new JLabel("Start");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setFont(new Font("Tahoma", Font.BOLD, 22));
		panel_3.add(label);
		

		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(290, 186, 748, 369);
		frame.getContentPane().add(scrollPane);
		
		panel_Start.addMouseListener(new MouseAdapter() {
			
			private int max;
			@Override
			public void mouseEntered(MouseEvent arg0) {
				panel_Start.setBackground(new Color(255,204,0));
				
			}
			@Override
			public void mouseExited(MouseEvent e) {
				panel_Start.setBackground(new Color(255,153,0));
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				
				
				

				Pattern p = Pattern.compile(URL_REGEX);
				Matcher m = p.matcher(Text_Page.getText());//replace with string to compare
				this.max = Integer.parseInt(Text_Page_Num.getText());
				if(!m.find() ) {
					JOptionPane.showMessageDialog(null, "Der Link der Website ist falsch! Bitte geben Sie eine korrekte URL ein.");
				}else if(Text_Page_Num.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null, "Es ist kein Anzahl hinterlegt. Bitte geben Sie diese an.");

				}else{
					JOptionPane.showMessageDialog(null, "Suche gestartet");
					
					panel_2.setVisible(true);
					
					
					

					Master master = new Master();
					master.setStartUrl(Text_Page.getText());
					master.setAmountOfThreads(4);
					master.setMaxNodes(max);



					if(master.start()){
						PageRank pagerank = null;
						try {
							pagerank = new PageRank();
						} catch (SQLException e1) {
							e1.printStackTrace();
						} catch (ClassNotFoundException e1) {
							e1.printStackTrace();
						}


						pagerank.setCalculationDamped(true);
						pagerank.setDampingFactor(0.78);
						pagerank.setCompareDelta(0.000001);

						ResultSet sum = null;

						try {
							pagerank.calculate();
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
						JOptionPane.showMessageDialog(null, "Suche abgeschlossen");
						new Master().createVizualizationJson();
						JOptionPane.showMessageDialog(null, "Json Datei erstellt");
						/*
						try {
							SearchTitle();
						} catch (SQLException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						*/

						try {
							PreparedStatement query = dbConnection.prepareStatement("Select id, url, PageRank from Webcrawler");
							ResultSet rs = query.executeQuery();

							table.setModel(DbUtils.resultSetToTableModel(rs));

						} catch (SQLException e1) {
							e1.printStackTrace();
						}



					}else{
						JOptionPane.showMessageDialog(null, "Fehler bei der Suche");
					}



				}
			}
		});
		
		table = new JTable();
		table.setBackground(new Color(255, 255, 255));
		scrollPane.setViewportView(table);
	}
	
	private void getConnection() throws ClassNotFoundException, SQLException, IOException {
		// Erstelle Verbindung zu DB
		Class.forName("org.sqlite.JDBC");
		dbConnection = DriverManager.getConnection("jdbc:sqlite:WebcrawlerData.db");
		
	}

	/*
	Funktion SearchTitle bitte in den Master einbauen oder so, dass es keine Komplikationen mit den Datenbanken gibt
	 */

	public void SearchTitle() throws SQLException, IOException {
		PreparedStatement query = dbConnection.prepareStatement("Select * from Webcrawler");
		ResultSet rs = query.executeQuery();

		while(rs.next()){
			String url = rs.getString("url");
			String title;

			try {
				Document doc = Jsoup.connect(url).userAgent("Mozilla").get();
				title = doc.title();
			}catch(Exception e){
				continue;
			}
			dbConnection.createStatement()
					.execute("Alter Table Webcrawler ADD text String;");
			dbConnection.createStatement()
					.execute("UPDATE `Webcrawler` SET `text`=" + title + " WHERE id=" + rs.getInt("id"));


			System.out.println("title is: " + title);



		}


	}
}



