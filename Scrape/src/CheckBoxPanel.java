import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import login.GenerateEncryption;

public class CheckBoxPanel extends JPanel{
	String labelText;
	CheckBox checkBox;
	Color color = Color.getHSBColor(-.85f, .1f, .85f);
	String name;
	int width;
	int height;
	public final static String PARENT_FOLDER_NAME = "Options";
	public final static String PARENT_FOLDER_PATH = findRootPath();
	CheckBoxPanel(String labelText,String name,CheckBox checkBox,int width,int height){
		this.labelText = labelText;
		this.name = name;
		this.checkBox = checkBox;
		this.width = width;
		this.height = height;
		constructPanel();
	}
	void setChecked(boolean checked) {
		if(checked) {
			checkBox.check();
		}
	}
	void constructPanel() {
		nittyGritty();
		add(constructLabel());
		addActionToBox();
		add(checkBox);
		setBackground(color);
		setLayout(new PanelSpringLayout());
		setVisible(true);
	}
	public void addActionToBox() {
		checkBox.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
					Boolean checked = !checkBox.isChecked();
					try {
						writeSelectionToFile(checked);
					}catch(IOException e1) {
						e1.printStackTrace();
						System.out.println(checked);
						return;
					}
					System.out.println(checked);
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

		});
	}
	public static String findRootPath() {
		return getRuntimeDir().replaceAll("[\\s\\n\\r]", "") + "\\"+PARENT_FOLDER_NAME;
	}
	public static String getRuntimeDir() {
		try {
			Process process = Runtime.getRuntime().exec(new String[] { "cmd.exe", "/c", "cd" });
			InputStream inputStream = process.getInputStream();
			byte[] bytes = new byte[1024];
			inputStream.read(bytes);
			process.destroy();
			return GenerateEncryption.getStringFromBytes(bytes).replaceAll("[\\s\\r\\n]", "");
		} catch (IOException e) {
			return "";
		}
	}

	public static HashMap<String,Boolean> readOptionsFromFiles() throws IOException,ClassNotFoundException{
		HashMap<String,Boolean> optionsMap = new HashMap<>();
		File dir = new File(PARENT_FOLDER_PATH);
		if(!dir.exists()||dir.list().length==0) {
			return optionsMap;
		}

		for(File file:dir.listFiles()) {
			ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
			optionsMap.put(file.getName(), (Boolean)objectInputStream.readObject());
			objectInputStream.close();
		}
		return optionsMap;
	}
	public void writeSelectionToFile(Boolean checked) throws IOException{
		File dir = new File(PARENT_FOLDER_PATH);
		if(!dir.exists()) {
			dir.mkdir();
		}
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(new File(PARENT_FOLDER_PATH+"\\"+name)));
		objectOutputStream.writeObject(checked);
		objectOutputStream.close();
	}
	public final static String LABEL_NAME = "label";
	JLabel constructLabel() {
		JLabel label = new JLabel();
		label.setName("label");
		label.setText(labelText);
		label.setBackground(color);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setVerticalAlignment(SwingConstants.CENTER);
		label.setOpaque(true);
		label.setVisible(true);
		return label;
	}

	boolean isChecked() {
		return checkBox.isChecked();
	}
	void nittyGritty() {
		setName(name);
		setSize(width,height);
		setBackground(color);
	}
	void setColor(Color color) {
		this.color = color;
	}
	private class PanelSpringLayout extends SpringLayout{
		public final static float CHECK_BOX_RATIO = .8f;
		PanelSpringLayout(){
			applyLayout();
		}
		void applyLayout() {
			for(Component c:getComponents()) {
				String name = c.getName();
				if(name==null) {
					continue;
				}
				switch(name) {
				case(LABEL_NAME):
					System.out.println("Label Layout");
					labelLayout((JLabel)c);
					break;
				default:
					System.out.println("Check Box Layout");
					checkBoxLayout((CheckBox)c);
					break;
				}
			}
		}
		void labelLayout(JLabel label) {
			putConstraint(NORTH, label, 0, NORTH, CheckBoxPanel.this);
			putConstraint(SOUTH,label,height,NORTH,CheckBoxPanel.this);
			putConstraint(WEST,label,0,WEST,CheckBoxPanel.this);
			putConstraint(EAST,label,(int)(CHECK_BOX_RATIO*width),WEST,CheckBoxPanel.this);
		}
		void checkBoxLayout(CheckBox checkBox) {
			putConstraint(NORTH,checkBox,getBoxNorth(),NORTH,CheckBoxPanel.this);
			putConstraint(SOUTH,checkBox,getBoxNorth()+checkBox.getHeight(),NORTH,CheckBoxPanel.this);
			putConstraint(WEST,checkBox,getBoxWest(),WEST,CheckBoxPanel.this);
			putConstraint(EAST,checkBox,getBoxWest()+checkBox.getWidth(),WEST,CheckBoxPanel.this);
		}
		int getBoxWest() {
			float bufferWest = CHECK_BOX_RATIO*width;
			float netWidth = width - bufferWest;
			float centerWest = netWidth/2 + bufferWest;
			Float boxWest = centerWest-(checkBox.getWidth()/2);
			return boxWest.intValue();
		}
		int getBoxNorth() {
			int vMid = (height/2) - (checkBox.getHeight()/2);
			return vMid;
		}
	}
}
