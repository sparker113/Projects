package frame;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;


public class ParamPanel extends JPanel{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String[] parameters;
	private Color color = Color.lightGray;
	private PanelPadding panelPadding;
	private ExecutorService executor;
//	private int rows;

	/*
	 * public ParamPanel(Rectangle rectangle,int rows,String...parameters) {
	 * this.parameters = parameters; nittyGritty(rectangle);
	 * construct(rectangle,rows); } public ParamPanel(Rectangle rectangle,Color
	 * color,int rows,String...parameters) { this.parameters = parameters;
	 * this.color = color; nittyGritty(rectangle); construct(rectangle,rows);
	 * for(Component c:getComponents()) {
	 * System.out.println(c.getBounds()+" - "+c.isVisible()); } }
	 */
	public ParamPanel(PanelPadding panelPadding,Rectangle parentRect,int rows,String...parameters) {
		this.parameters = parameters;
		nittyGritty(panelPadding);
		construct(panelPadding,parentRect,rows);
	}
	public ParamPanel(PanelPadding panelPadding,Rectangle parentRect,Color color,int rows,String...parameters) {
		this.parameters = parameters;
		this.color = color;
		nittyGritty(panelPadding);
		construct(panelPadding,parentRect,rows);
		for(Component c:getComponents()) {
			System.out.println(c.getBounds()+" - "+c.isVisible());
		}
	}
	void resetBounds(Rectangle parentRect) {
		setBounds(getPaddedBounds(parentRect));
	}
	private Rectangle getPaddedBounds(Rectangle parentRect) {
		int x = panelPadding.left;
		int y = panelPadding.top;
		int width = parentRect.width-x-panelPadding.right;
		int height = parentRect.height-y-panelPadding.bottom;
		return new Rectangle(x,y,width,height);
	}
	void construct(PanelPadding panelPadding,Rectangle parentRect,int rows) {
		int panelWidth = getInputPanelWidth(panelPadding,parentRect,parameters.length,rows);
		for(String s:parameters) {
			add(new InputPanel(s,panelWidth,getInputPanelHeight(panelPadding,parentRect,rows)));
		}
		setLayout(new GridLayout(0,parameters.length));
		setVisible(true);
	}
	void construct(Rectangle rectangle,int rows) {
		int panelWidth = getInputPanelWidth(rectangle,parameters.length,rows);
		for(String s:parameters) {
			add(new InputPanel(s,panelWidth,getInputPanelHeight(panelPadding,rectangle,rows)));
		}
		setLayout(new GridLayout());
		setVisible(true);
	}
	public PanelPadding getPanelPadding() {
		return this.panelPadding;
	}
	void nittyGritty(PanelPadding panelPadding) {
		this.panelPadding = panelPadding;
		executor = Executors.newSingleThreadExecutor();
		setBackground(color);
		addBoundsChangeListener();
	}
	
	public static int getInputPanelWidth(PanelPadding panelPadding,Rectangle parentRect,int numParams,int rows) {
		int columns = numParams/rows;
		int panelWidth = (parentRect.width-panelPadding.getLeft()-panelPadding.getRight())/columns;
		return panelWidth;
	}
	public static int getInputPanelWidth(Rectangle rectangle,int numParams,int rows) {
		int columns = numParams/rows;
		int panelWidth = rectangle.width/columns;
		return panelWidth;
	}

	public static int getInputPanelHeight(PanelPadding panelPadding,Rectangle parentRect,int rows) {
		return (parentRect.height-panelPadding.getTop()-panelPadding.getBottom())/rows;
	}
	public static int getInputPanelHeight(Rectangle rectangle,int rows) {
		return rectangle.height/rows;
	}
	private void addBoundsChangeListener() {

		HierarchyBoundsListener listener = new HierarchyBoundsListener() {

			@Override
			public void ancestorMoved(HierarchyEvent e) {
				
				
			}

			@Override
			public void ancestorResized(HierarchyEvent e) {
				executor.execute(()->{
					Component component = e.getChanged();
					if(!component.getClass().getSimpleName().equals("JFrame")) {
						return;
					}
					System.out.println("Reset Bounds");
					resetBounds(component.getBounds());
					
				});
				
			}
			
		};
		
		addHierarchyBoundsListener(listener);
	}

	public class InputPanel extends JPanel{
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private int w;
		private int h;

		private final static String LABEL_NAME = "label";
		private final static String TEXTFIELD_NAME = "textbox";
		private final static int INPUT_PADDING = 10;

		InputPanel(String parameter,int width,int height){
			nittyGritty(parameter,width,height);
			constructInput();
			constructLabel(parameter);
		}

		void nittyGritty(String parameter,int width,int height) {
			this.w = width;
			this.h = height;
			System.out.println("Input Panel Width: "+width);
			System.out.println("Input Panel Height: "+height);
			setBackground(color);
			setLayout(null);
			setName(parameter);
		}
		int getTextFieldWidth(int width) {
			return width - (2*INPUT_PADDING);
		}


		private final static float LABEL_RATIO = .2f;
		private final static float TEXTFIELD_RATIO = .2f;
		private final static float TEXTFIELD_PADDING = .1f;
		private final static int LABEL_X = 0;
		private final static int LABEL_Y = 0;

		void constructLabel(String parameter) {
			JLabel label = new JLabel();
			label.setBounds(LABEL_X,LABEL_Y,w,getLabelHeight());
			label.setName(LABEL_NAME);
			label.setText(parameter);
			label.setBackground(color);
			label.setVerticalAlignment(SwingConstants.CENTER);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setOpaque(true);
			add(label);
		}

		void constructInput() {
			JTextField textField = new JTextField();
			textField.setName(TEXTFIELD_NAME);
			textField.setBounds(INPUT_PADDING,getTextFieldTop(),getTextFieldWidth(w),(int)(h*TEXTFIELD_RATIO));
			textField.addKeyListener(new KeyListener() {

				@Override
				public void keyTyped(KeyEvent e) {
					e.consume();

				}

				@Override
				public void keyPressed(KeyEvent e) {
					String s = String.valueOf(e.getKeyChar());
					String currentText = textField.getText();
					if(s.matches("[0-9\\.]")) {
						textField.setText(currentText+s);
						return;
					}
					if(e.getKeyCode()==KeyEvent.VK_ENTER) {
						textField.transferFocus();
					}
				}

				@Override
				public void keyReleased(KeyEvent e) {
					e.consume();
				}

			});
			textField.setEnabled(true);
			add(textField);
		}

		int getLabelHeight() {
			return (int)(LABEL_RATIO*(h));
		}

		int getTextFieldTop() {
			int fieldTop = (int)((LABEL_RATIO+TEXTFIELD_PADDING)*h);
			return fieldTop;
		}
	}
	public static class PanelPadding{
		private int top;
		private int bottom;
		private int left;
		private int right;
		public PanelPadding(int top,int bottom,int left,int right) {
			this.top = top;
			this.left = left;
			this.right = right;
			this.bottom = bottom;
		}
		PanelPadding getPadding() {
			return this;
		}
		int getTop() {
			return this.top;
		}
		int getBottom() {
			return this.bottom;
		}
		int getLeft() {
			return this.left;
		}
		int getRight() {
			return this.right;
		}
	}
}
