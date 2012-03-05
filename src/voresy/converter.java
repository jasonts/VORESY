package voresy;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

public class converter extends JApplet{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2536670702702881331L;
	private ActionListener buttonaction = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub	
			converter_core core = new converter_core(inputfield.getText(), "/VORESY_TEMP");
			core.start();
		}
		
	};
	
	private MouseListener inputfield_mouse = new MouseListener() {

		@Override
		public void mouseClicked(MouseEvent arg0) {
			// TODO Auto-generated method stub
			inputfield.setText(null);
			jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			jfc.setFileFilter(new FileNameExtensionFilter("MPEG-I Layer 3 (*.mp3)", "mp3"));
			jfc.setFileFilter(new FileNameExtensionFilter("Waveform Audio File Format (*.wav)", "wav"));
			jfc.setFileFilter(new FileNameExtensionFilter("Free Loseless Audio Codec (*.flac)","flac"));
			jfc.setFileFilter(new FileNameExtensionFilter("OGG Vorbis (*.ogg)", "ogg"));
			jfc.setFileFilter(new FileNameExtensionFilter("Audio Interchange File Format (*.aiff, *.aif, *.aifc)", "aiff", "aif", "aifc"));
			jfc.setFileFilter(new FileNameExtensionFilter("�Ҧ��i�Ϊ��榡", "mp3", "wav", "flac", "ogg", "aiff", "aif", "aifc"));
			int jfc_option = jfc.showDialog(null, null);
			
			if (jfc_option == JFileChooser.APPROVE_OPTION) 
				inputfield.setText(jfc.getSelectedFile().toString());
			
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {}
		public void mouseExited(MouseEvent arg0) {}
		public void mousePressed(MouseEvent arg0) {}
		public void mouseReleased(MouseEvent arg0) {}
	};

	private MouseListener outputdir_mouse = new MouseListener() {

		@Override
		public void mouseClicked(MouseEvent arg0) {
			// TODO Auto-generated method stub
			outputdir.setText(null);
			outputjfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int jfc_option = outputjfc.showDialog(null, null);
			if (jfc_option == JFileChooser.APPROVE_OPTION) 
				outputdir.setText(outputjfc.getSelectedFile().toString());
			
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {}
		public void mouseExited(MouseEvent arg0) {}
		public void mousePressed(MouseEvent arg0) {}
		public void mouseReleased(MouseEvent arg0) {}
		
	};
	
	public void init_ui() {
		inputfield = new JTextField(32);
		button = new JButton("�}�l�ഫ");
		outputdir = new JTextField(32);
		
		if (appletmode == false) {
			frame = new JFrame("Audio Converter");
			content = frame.getContentPane();
			frame.addWindowListener( new WindowAdapter() {
				public void windowClosing(WindowEvent e) { System.exit(0);}
				});
			frame.setSize(350, 200);
			frame.setVisible(true);
		} else content = getContentPane();
		
		content.setLayout(new FlowLayout());
		content.add(inputfield);
		content.add(outputdir);
		content.add(button);	
		
		inputfield.setText("���I�惡�B����ɮ�...");
		inputfield.addMouseListener(inputfield_mouse );
		outputdir.setText("���I�惡�B����X���|...");
		outputdir.addMouseListener(outputdir_mouse );
		button.addActionListener(buttonaction);
	}
	

	/**
	 * Constructor
	 */
	public converter() {
		// �˵�O�_��W����A�p�G�O�q�~���I�s�N�������F
		if (standalone == true) init_ui();
	}

	/**
	 * Audio Converter Constructor, it will call the converter core.
	 * @param src Source File
	 * @param destDIR Destination directory
	 */
	public converter(String src, String destDIR) {
		converter_core c = new converter_core(src, destDIR);
		c.start();
	}
	
	/**
	 * Audio Converter Constructor, it will call the converter core.<br>
	 * The output directory is /VORESY/TEMP
	 * @param src Source filename
	 */
	public converter(String src) {
		converter_core c = new converter_core(src, RuntimeCFG.WORKTEMPDIR);
		c.start();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		standalone = true;
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		new converter();
	}
	
	public void init() {

			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedLookAndFeelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	    appletmode = true;
	    init_ui();
	    
	}
	
	private JFrame frame;
	private Container content;
	private JTextField inputfield;
	private JTextField outputdir;
	private JButton button;
	private static boolean standalone = false;
	private static boolean appletmode = false;
	private JFileChooser jfc = new JFileChooser();
	private JFileChooser outputjfc = new JFileChooser();

}
