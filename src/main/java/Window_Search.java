import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.Color;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import net.proteanit.sql.DbUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.SwingConstants;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Window_Search {

	public JFrame frame;
	private Timer t = null;
	private int count = 0;
	private JTextField textField;
	private JTable table;
	private Connection dbConnection;
	

	

	/**
	 * Create the application.
	 */
	public Window_Search() {
		initialize();
		frame.setVisible(true);
		
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.getContentPane().setBackground(new Color(255, 153, 0));
		frame.getContentPane().setLayout(null);
		
		JLabel lblText = new JLabel("Text:");
		lblText.setFont(new Font("Tahoma", Font.BOLD, 22));
		lblText.setBounds(33, 11, 95, 35);
		frame.getContentPane().add(lblText);
		
		textField = new JTextField();
		
		textField.setBounds(127, 11, 408, 35);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(33, 84, 798, 393);
		frame.getContentPane().add(scrollPane);
		
		table = new JTable();
		scrollPane.setViewportView(table);
		
		
		frame.setBounds(100, 100, 900, 544);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		}
	
	private void getConnection() throws ClassNotFoundException, SQLException, IOException {
		// Erstelle Verbindung zu DB
		Class.forName("org.sqlite.JDBC");
		dbConnection = DriverManager.getConnection("jdbc:sqlite:WebcrawlerData.db");
		
	}
}
