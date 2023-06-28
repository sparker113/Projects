package graphpanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.swing.JPanel;

public class AxisPanel extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final int WIDTH;
	final int HEIGHT;
	final int PADDING;
	private int tickLength = 10;
	private int ticks = 5;
	private int[] maxY= {10};
	private int maxX= 10;
	private boolean dateXAxis = false;
	private ArrayList<LocalDate> dates;
	private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd");

	public AxisPanel(int width,int height,int padding){
		WIDTH = width;
		HEIGHT = height;
		PADDING = padding;
	}
	public AxisPanel(int width,int height){
		WIDTH = width;
		HEIGHT = height;
		PADDING = 50;
	}
	public void setDateXAxis(ArrayList<LocalDate> dates){
		this.dateXAxis = true;
		this.dates = dates;
	}
	public void setDateFormat(String pattern){
		dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
	}
	public void setMaxX(int maxX) {
		this.maxX = maxX;
	}
	public void setMaxY(int maxY) {
		this.maxY[0] = maxY;
	}
	public void setTickLength(int tickLength) {
		this.tickLength = tickLength;
	}
	public void setNumberOfTicks(int ticks) {
		this.ticks = ticks;
	}
	public void addAxis(int addMaxY) {
		this.maxY = addToIntArray(this.maxY,addMaxY);
	}
	public int[] addToIntArray(int[] array,int addInt) {
		int[] newArray = new int[array.length+1];
		int count = 0;
		for(int i:array) {
			newArray[count] = i;
			count++;
		}
		newArray[count] = addInt;
		return newArray;
	}
	public <T> void addPlotPanel(PlotPanel<T> plotPanel) {
		plotPanel.setWidth(WIDTH-PADDING-PADDING);
		plotPanel.setHeight(HEIGHT-(PADDING+PADDING));
		plotPanel.setXScale(maxX);
		//plotPanel.setYScale(maxY);
		plotPanel.setVisible(true);
		this.add(plotPanel);
	}
	private void drawLabel(Graphics2D g2d,String label,int x,int y) {
		char[] chars = new char[label.length()];
		chars = stringToChar(label,chars);
		g2d.setFont(g2d.getFont().deriveFont(Font.BOLD));
		g2d.drawChars(chars, 0, label.length(), x, y);
	}
	private char[] stringToChar(String string,char[] chars) {
		for(int i=0;i<string.length();i++) {
			if(string.charAt(i)==0) {
				chars[i] = '0';
				continue;
			}
			chars[i] = string.charAt(i);
		}
		return chars;
	}
	private void drawAxisBox(Graphics2D g2d) {
		g2d.setColor(Color.black);
		g2d.setStroke(new BasicStroke(2));
		g2d.drawLine(PADDING, PADDING, WIDTH-PADDING, PADDING);
		g2d.drawLine(WIDTH-PADDING, PADDING, WIDTH-PADDING, HEIGHT-PADDING);
		g2d.drawLine(PADDING, HEIGHT-PADDING, WIDTH-PADDING, HEIGHT-PADDING);
		g2d.drawLine(PADDING, PADDING, PADDING, HEIGHT-PADDING);
	}
	
	private void drawYTicks(Graphics2D g2d) {
		g2d.setColor(Color.black);
		g2d.setStroke(new BasicStroke(2));
		int yTickInterval = getYTickInterval();
		for(int i=0;i<=ticks;i++) {
			g2d.drawLine(getYTickStart(), HEIGHT-getNthTick(i,yTickInterval,0)
					, getYTickEnd(), HEIGHT-getNthTick(i,yTickInterval,0));
			
		}

		
	}
	private final static int LABEL_PADDING = 3;
	private final static int CHAR_HEIGHT = 8;
	/* private int getTickLabelHeight(int numAxes) {
		return (LABEL_PADDING*(numAxes-1))+CHAR_HEIGHT*numAxes;
	} */
	private void drawLabels(Graphics2D g2d,PlotPanel<String> plotPanel) {
		g2d.setColor(Color.black);
		int yTickInterval = getYTickInterval();
		for(int i=0;i<=ticks;i++) {
			int labelIndex = 0;
			for(int max:plotPanel.getYScale()) {
				String label = String.format(getLabelFormat(max),getLabel(i,getYLabelInterval(max)));
				drawLabel(g2d,label,PADDING/2-label.length()*3,HEIGHT-getNthTick(i,yTickInterval,labelIndex)+5);
				labelIndex++;
			}
		}
	}

	private void drawXTicks(Graphics2D g2d) {
		g2d.setColor(Color.black);
		g2d.setStroke(new BasicStroke(2));
		int xTickInterval = getXTickInterval();
		float xLabelInterval = getXLabelInterval();
		for(int i=0;i<=ticks;i++) {
			g2d.drawLine(getNthTick(i,xTickInterval,0),getXTickStart()
					,getNthTick(i,xTickInterval,0),getXTickEnd());
			String label = dateXAxis?getDateLabel(i,xLabelInterval):String.format(getLabelFormat(maxX),getLabel(i,xLabelInterval));
			drawLabel(g2d,label,getNthTick(i,xTickInterval,0)-3*label.length(),HEIGHT-(PADDING/2));
		}
	}
	private String getLabelFormat(int max) {
		if(max%ticks==0) {
			return "%.0f";
		}
		return "%.1f";
	}
	private float getYLabelInterval(int maxY) {
		return Float.valueOf(maxY)/Float.valueOf(ticks);
	}
	private Float getXLabelInterval() {
		return Float.valueOf(maxX)/Float.valueOf(ticks);
	}
	private Float getLabel(int index,Float interval) {
		return index*interval;
	}
	private String getDateLabel(int index,Float interval){
		Float ind = getLabel(index,interval);
		LocalDate date = dates.get(ind.intValue());
		return date.format(dateTimeFormatter);
	}
	private int getNthTick(int index,int spacing,int sameTickIndex) {
		return spacing*index+PADDING+(LABEL_PADDING+CHAR_HEIGHT)*sameTickIndex;
	}
	private int getXTickEnd() {
		return HEIGHT - PADDING - (tickLength/2);
	}
	private int getXTickStart() {
		return HEIGHT - PADDING + (tickLength/2);
	}
	private int getYTickStart() {
		return PADDING - (tickLength/2);
	}
	private int getYTickEnd() {
		return PADDING + (tickLength/2);
	}
	private int getYTickInterval() {
		return (HEIGHT-PADDING*2)/ticks;
	}
	private int getXTickInterval() {
		return (WIDTH-PADDING*2)/ticks;
	}
	private void checkAdjustXScale(int xScale) {
		if(xScale>maxX){
			setMaxX(xScale);
		}
	}

	/*
	 * private void checkAdjustYScale(int yScale) { if(yScale>maxY){
	 * setMaxY(yScale); } }
	 */
    @SuppressWarnings("unchecked")
	public void paintCopy(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		for(Component c:getComponents()) {
			((PlotPanel<String>)c).plotDataSets(g2d,PADDING,-PADDING);
			checkAdjustXScale(((PlotPanel<String>)c).getXScale());
			//checkAdjustYScale(((PlotPanel<String>)c).getYScale());
		}
		
		drawAxisBox(g2d);
		drawXTicks(g2d);
		drawYTicks(g2d);
	}
	@SuppressWarnings("unchecked")
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		for(Component c:getComponents()) {
			((PlotPanel<String>)c).plotDataSets(g2d,PADDING,-PADDING);
			checkAdjustXScale(((PlotPanel<String>)c).getXScale());
			drawLabels(g2d,(PlotPanel<String>)c);
			//checkAdjustYScale(((PlotPanel<String>)c).getYScale());
		}
		
		drawAxisBox(g2d);
		drawXTicks(g2d);
		drawYTicks(g2d);
	}
	
	
}
