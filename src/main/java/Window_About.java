import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.Color;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.Image;
import java.nio.file.Path;

import javax.swing.SwingConstants;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.JPanel;
import java.awt.BorderLayout;

public class Window_About {

	public JFrame frame;
	public static String path;

	

	/**
	 * Create the application.
	 */
	public Window_About() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.getContentPane().setBackground(new Color(255, 153, 0));
		frame.getContentPane().setLayout(null);
		
		JLabel lblNewLabel = new JLabel("About");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 38));
		lblNewLabel.setBounds(361, 30, 198, 68);
		frame.getContentPane().add(lblNewLabel);
		
		JLabel lblDani = new JLabel("Danielle");
		lblDani.setHorizontalAlignment(SwingConstants.CENTER);
		lblDani.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblDani.setBounds(42, 153, 98, 25);
		frame.getContentPane().add(lblDani);
		
		JLabel lblLeonie = new JLabel("Leonie");
		lblLeonie.setHorizontalAlignment(SwingConstants.CENTER);
		lblLeonie.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblLeonie.setBounds(213, 153, 98, 25);
		frame.getContentPane().add(lblLeonie);
		
		JLabel lblJoannis = new JLabel("Joannis");
		lblJoannis.setHorizontalAlignment(SwingConstants.CENTER);
		lblJoannis.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblJoannis.setBounds(388, 153, 98, 25);
		frame.getContentPane().add(lblJoannis);
		
		JLabel lblThomas = new JLabel("Thomas");
		lblThomas.setHorizontalAlignment(SwingConstants.CENTER);
		lblThomas.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblThomas.setBounds(580, 153, 98, 25);
		frame.getContentPane().add(lblThomas);
		
		JLabel lblLennart = new JLabel("Lennart");
		lblLennart.setHorizontalAlignment(SwingConstants.CENTER);
		lblLennart.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblLennart.setBounds(780, 153, 98, 25);
		frame.getContentPane().add(lblLennart);
		
			
		JLabel lblNewLabel_1 = new JLabel("New label");
		lblNewLabel_1.setBounds(102, 238, 200, 200);
		
		URL url = Window_About.class.getResource("/picture/Inf.jpg");
		
		ImageIcon MyImage = new ImageIcon(url);
		Image img = MyImage.getImage();
		Image newImg = img.getScaledInstance(lblNewLabel_1.getWidth(), lblNewLabel_1.getHeight(), Image.SCALE_SMOOTH);
		ImageIcon image = new ImageIcon(newImg);
		lblNewLabel_1.setIcon(image);
		
		frame.getContentPane().add(lblNewLabel_1);
		
		JLabel lblVersion = new JLabel("Version: ");
		lblVersion.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblVersion.setBounds(480, 249, 66, 14);
		frame.getContentPane().add(lblVersion);
		
		JLabel lblNewLabel_2 = new JLabel("1.0.4");
		lblNewLabel_2.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblNewLabel_2.setBounds(556, 249, 46, 14);
		frame.getContentPane().add(lblNewLabel_2);
		
		JLabel lblWeitereInformationen = new JLabel("Weitere Informationen:");
		lblWeitereInformationen.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblWeitereInformationen.setBounds(480, 312, 157, 14);
		frame.getContentPane().add(lblWeitereInformationen);
		
		JPanel panel = new JPanel();
		panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent arg0) {
				panel.setBackground(new Color(255,204,0));
				
			}
			@Override
			public void mouseExited(MouseEvent e) {
				panel.setBackground(new Color(255,153,0));
			}
		});
		panel.setBackground(new Color(255, 153, 0));
		panel.setBounds(480, 337, 87, 30);
		frame.getContentPane().add(panel);
		panel.setLayout(new BorderLayout(0, 0));
		
		JLabel lblWebsite = new JLabel("Website");
		panel.add(lblWebsite);
		lblWebsite.setHorizontalAlignment(SwingConstants.LEFT);
		lblWebsite.setFont(new Font("Tahoma", Font.PLAIN, 15));
		
		JPanel panel_1 = new JPanel();
		panel_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent arg0) {
				panel_1.setBackground(new Color(255,204,0));
				
			}
			@Override
			public void mouseExited(MouseEvent e) {
				panel_1.setBackground(new Color(255,153,0));
			}
		});
		panel_1.setBackground(new Color(255, 153, 0));
		panel_1.setBounds(480, 367, 87, 30);
		frame.getContentPane().add(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JLabel lblSupport = new JLabel("Support");
		lblSupport.setHorizontalAlignment(SwingConstants.LEFT);
		panel_1.add(lblSupport, BorderLayout.CENTER);
		lblSupport.setFont(new Font("Tahoma", Font.PLAIN, 15));
		
		JPanel panel_2 = new JPanel();
		panel_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent arg0) {
				panel_2.setBackground(new Color(255,204,0));
				
			}
			@Override
			public void mouseExited(MouseEvent e) {
				panel_2.setBackground(new Color(255,153,0));
			}
		});
		panel_2.setBackground(new Color(255, 153, 0));
		panel_2.setBounds(480, 397, 87, 30);
		frame.getContentPane().add(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		JLabel lblDatenschutz = new JLabel("Datenschutz");
		lblDatenschutz.setHorizontalAlignment(SwingConstants.LEFT);
		panel_2.add(lblDatenschutz, BorderLayout.CENTER);
		lblDatenschutz.setFont(new Font("Tahoma", Font.PLAIN, 15));
		frame.setBounds(100, 100, 983, 503);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	}

}
