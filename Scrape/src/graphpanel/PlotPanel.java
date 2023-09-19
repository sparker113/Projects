package graphpanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JPanel;

public class PlotPanel<T> extends JPanel{
	private int width;
	private int height;
	private int xScale=10;
	private int yScale[]= {10};
	private ArrayList<ArrayList<Float>> convertedArray = new ArrayList<>();
	private HashMap<Integer,HashMap<String,Integer>> properties = new HashMap<>();
	private Boolean autoAdjust = true;
	public PlotPanel(Dimension dimension){
		this.width=dimension.width;
		this.height=dimension.height;
	}
	
	
	public PlotPanel(int width,int height){
		this.width=width;
		this.height=height;
	}
	public PlotPanel(){
		
	}
	public void setAutoAdjustScales(Boolean autoAdjust) {
		this.autoAdjust = autoAdjust;
	}

	public void addDataArray(ArrayList<T> data) {
		ArrayList<Float> floatArray = convertArray(data);
		properties.put(convertedArray.size(), getDefaultProperties());
		convertedArray.add(floatArray);
	}
	public void addDataArray(ArrayList<T> data,Integer color,Integer stroke) {
		ArrayList<Float> floatArray = convertArray(data);
		properties.put(convertedArray.size(), getPropertiesMap(color,stroke));
		convertedArray.add(floatArray);
	}
	public int getMaxY(ArrayList<Float> dataArray) {
		Float max = dataArray.size()>0?dataArray.get(0):0;
		for(Float f:dataArray) {
			max=f>max?f:max;
		}
		return max.intValue();
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public void setXScale(int xScale) {
		this.xScale = xScale;
	}
	public void setYScale(int[] yScale) {
		this.yScale = yScale;
	}
	public int getXScale() {
		return this.xScale;
	}
	public int[] getYScale() {
		return this.yScale;
	}
	public Float getUnitPixels(int dim,int scale) {
		return Float.valueOf(dim)/Float.valueOf(scale);
	}
	private ArrayList<Float> convertArray(ArrayList<T> array) {
			ArrayList<Float> floatArray = new ArrayList<>();
			for(T t:array) {
				floatArray.add(String.valueOf(t).matches("([A-Za-z\\s]+)")?0f:Float.valueOf(String.valueOf(t)));
			}
			return floatArray;
			
	}

	public void plotDataSets(Graphics2D g2d,int zeroX,int zeroY) {
		if(autoAdjust) {
			adjustMaxX();
			adjustMaxY();
		}
		int count = 0;
		for(ArrayList<Float> array:convertedArray) {
			g2d.setColor(getColor(properties.get(count).get(COLOR_NAME)));
			g2d.setStroke(new BasicStroke(properties.get(count).get(STROKE_NAME)));
			plotSet(g2d,array,count,zeroX,zeroY);
			count++;
		}
	}
	private void adjustMaxX() {
		for(ArrayList<Float> array:convertedArray) {
			if(xScale<array.size()-1) {
				setXScale(array.size()-1);
			}
		}
	}
	private int getIntDigits(int integer) {
		float start = 10;
		int count = 1;
		while((float)integer/start>1) {
			start*=10;
			count++;
		}
		return count;
	}
	private int addAnotherMaxY(int maxY) {
		int scaleDigits = getIntDigits(maxY);
		int count = 0;
		for(int i:yScale) {
			int currentDigits = getIntDigits(i);
			if(currentDigits==scaleDigits) {
				return count;
			}
			count++;
		}
		return -1;
	}
	private void addToYScale(int newYScale) {
		int[] newYScaleArray = new int[yScale.length+1];
		int count = 0;
		for(int i:yScale) {
			newYScaleArray[count] = i;
			count++;
		}
		newYScaleArray[count] = newYScale;
		yScale = newYScaleArray;
	}
	private void addToAxisMap(int arrayIndex,int axisIndex) {
		axisMap.put(arrayIndex, axisIndex);
	}
	private HashMap<Integer,Integer> axisMap = new HashMap<>();
	///LATER IMPROVEMENT-->ROUND ADJUST Y SCALE BASED ON THE WHOLE PLACE THE FLOAT GOES OUT TO
	private void adjustMaxY() {
		int count = 0;
		for(ArrayList<Float>array:convertedArray) {
			int maxY = getMaxY(array);
			int axisIndex = addAnotherMaxY(maxY);
			if(axisIndex<0) {
				addToYScale(maxY);
				addToAxisMap(count,yScale.length-1);
				count++;
				continue;
			}
			addToAxisMap(count,axisIndex);
			count++;
		}
	}
	
	private void plotSet(Graphics2D g2d,ArrayList<Float> array,int zeroX,int zeroY) {
		Float xUnit = getUnitPixels(width,xScale);
		Float yUnit = getUnitPixels(height,yScale[0]);
		for(int i=1;i<array.size();i++) {
			g2d.drawLine(getScaledValue(xUnit,(float)i-1)+zeroX, height - zeroY - getScaledValue(yUnit
					,array.get(i-1)),getScaledValue(xUnit,(float)i)+zeroX
					, height-zeroY-getScaledValue(yUnit,array.get(i)));
		}
	}
	private void plotSet(Graphics2D g2d,ArrayList<Float> array,int arrayIndex,int zeroX,int zeroY) {
		Float xUnit = getUnitPixels(width,xScale);
		Float yUnit = getUnitPixels(height,yScale[axisMap.get(arrayIndex)]);
		for(int i=1;i<array.size();i++) {
			g2d.drawLine(getScaledValue(xUnit,(float)i-1)+zeroX, height - zeroY - getScaledValue(yUnit
					,array.get(i-1)),getScaledValue(xUnit,(float)i)+zeroX
					, height-zeroY-getScaledValue(yUnit,array.get(i)));
		}
	}
	private int getScaledValue(Float ratio,Float value) {
		return Math.round(ratio*value);
	}
	
	
	private final static String COLOR_NAME = "color";
	private final static String STROKE_NAME = "width";
	public HashMap<String,Integer> getDefaultProperties(){
		HashMap<String,Integer> map = new HashMap<>();
		map.put(COLOR_NAME, 1);
		map.put(STROKE_NAME, 2);
		return map;
	}
	public HashMap<String,Integer> getPropertiesMap(Integer color,Integer stroke){
		HashMap<String,Integer> propMap = new HashMap<>();
		propMap.put(COLOR_NAME, color);
		propMap.put(STROKE_NAME, stroke);
		return propMap;
	}
	public static Color getColor(Integer colorIndex) {
		switch(colorIndex){
		case(1):
			return Color.black;
		case(2):
			return Color.blue;
		case(3):
			return Color.red;
		case(4):
			return Color.green;
		case(5):
			return Color.yellow;
		case(6):
			return Color.orange;
		case(7):
			return Color.magenta;
		default:
			return Color.black;
		}
	}
	public final static int BLACK = 1;
	public final static int BLUE = 2;
	public final static int RED = 3;
	public final static int GREEN = 4;
	public final static int YELLOW = 5;
	public final static int ORANGE = 6;
	public final static int PURPLE = 7;
	@Override
	public void paintComponent(Graphics g) {

	}
}
