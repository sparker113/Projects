import java.awt.Color;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

public class CheckBox extends JLabel {

	String name;
	JLabel label;
	private Color color;

	CheckBox(String name) {
		this.name = name;
		this.color = Color.getHSBColor(-.85f, .1f, .85f);
		construct();
	}

	CheckBox(String name, Color color) {
		this.name = name;
		this.color = color;
		construct();
	}

	public void construct() {
		setName(name);
		setSize(45, 45);
		setBackground(color);
		this.label = insetBox();
		add(label);
		setLayout(new CheckBoxLayout(this));
		//label.setHorizontalAlignment(SwingConstants.CENTER);
		addMouseListener(new CheckLabel());
		setOpaque(true);
		setVisible(true);
	}

	public JLabel insetBox() {
		JLabel label = new JLabel();
		label.setSize(15, 15);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setVerticalAlignment(SwingConstants.CENTER);
		label.setOpaque(true);
		label.setVisible(true);
		return label;
	}

	public Boolean isChecked() {
		boolean checked = false;
		if (((JLabel) this.getComponent(0)).getIcon() != null) {
			checked = true;
		}
		return checked;
	}

	public void check() {
		if (!isChecked()) {
			ImageIcon image = new ImageIcon("C:\\Scrape\\check.png");
			ImageIcon image1 = new ImageIcon();
			image1.setImage(image.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH));
			((JLabel) this.getComponent(0)).setIcon(image1);
		} else {
			System.out.println("It aint gonna do it");
			((JLabel) this.getComponent(0)).setIcon(null);
		}
	}

	class CheckLabel implements MouseListener {
		Executor executor = Executors.newCachedThreadPool();

		@Override
		public void mouseClicked(MouseEvent e) {
			SwingWorker<Void, Void> worker = new SwingWorker<>() {
				public Void doInBackground() {
					((CheckBox) e.getSource()).check();
					return null;
				}
			};
			worker.execute();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub

		}

	}
	public class CheckBoxLayout extends SpringLayout{
		JLabel backLabel;
		public final static int BOX_NORTH = 15;
		public final static int BOX_SOUTH = 30;
		public final static int BOX_WEST = 15;
		public final static int BOX_EAST = 30;
		CheckBoxLayout(JLabel backLabel){
			this.backLabel = backLabel;
			boxLayout();
		}
		void boxLayout() {
			JLabel label = ((JLabel)backLabel.getComponent(0));
			putConstraint(NORTH,label,BOX_NORTH,NORTH,backLabel);
			putConstraint(SOUTH,label,BOX_SOUTH,NORTH,backLabel);
			putConstraint(WEST,label,BOX_WEST,WEST,backLabel);
			putConstraint(EAST,label,BOX_EAST,WEST,backLabel);


		}
	}
}
