import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.Color;
import javax.swing.JProgressBar;
import javax.swing.Timer;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.SwingConstants;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Window_Splash {

	private JFrame frame;
	private Timer t = null;
	private int count = 0;
	

	

	/**
	 * Create the application.
	 */
	public Window_Splash() {
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
		
		JProgressBar progressBar = new JProgressBar();
		progressBar.setForeground(new Color(255, 204, 0));
		progressBar.setBackground(new Color(255, 255, 255));
		
		
		
		progressBar.setBounds(206, 238, 447, 28);
		frame.getContentPane().add(progressBar);
		
		JButton btnNewButton = new JButton("Weiter");
		btnNewButton.setBounds(321, 287, 208, 33);
		frame.getContentPane().add(btnNewButton);
		
		JLabel lblNewLabel = new JLabel("PageRank");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 38));
		lblNewLabel.setBounds(206, 22, 447, 80);
		frame.getContentPane().add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Herzlich Willkommen in der PageRank Applikation. \r\n");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblNewLabel_1.setBounds(177, 123, 474, 57);
		frame.getContentPane().add(lblNewLabel_1);
		
		JLabel lblMitDieserApp = new JLabel("Mit dieser App k\u00F6nnen Sie Websiten durchsuchen und analysieren lassen.");
		lblMitDieserApp.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblMitDieserApp.setBounds(177, 154, 495, 43);
		frame.getContentPane().add(lblMitDieserApp);
		btnNewButton.setVisible(false);
		frame.setBounds(100, 100, 887, 416);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		t = new Timer(100, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				count++;
				count++;
				progressBar.setValue(count);
				if(progressBar.getValue()<100){
					progressBar.setValue(progressBar.getValue()+1);
					
				}
				if(progressBar.getValue()==100){
					frame.setVisible(false);
					try {
						Window_Main main = new Window_Main();
						
						
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					t.stop();
				}
				
			}
		});
		t.start();
	}
}
