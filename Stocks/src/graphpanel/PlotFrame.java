package graphpanel;

import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class PlotFrame extends JFrame{
    AxisPanel axisPanel;
    final static int DEFAULT_PLOT_WIDTH = 800;
    final static int DEFAULT_PLOT_HEIGHT = 600;
    final static int ADD_FOR_INSETS = 50;
    public PlotFrame(AxisPanel axisPanel){
        this.axisPanel = axisPanel;
        nittyGritty();
    }
    public <T> PlotFrame(Set<ArrayList<T>> arrays){
        this.axisPanel = getAxisPanel(arrays);
        nittyGritty();
    }
    private ArrayList<LocalDate> dates;
    public <T> PlotFrame(Set<ArrayList<T>> arrays,ArrayList<LocalDate> dates){
        this.dates = dates;
        this.axisPanel = getAxisPanel(arrays);
        nittyGritty();
    }
    <T> AxisPanel getAxisPanel(Set<ArrayList<T>> arrays){
        AxisPanel axisPanel = new AxisPanel(DEFAULT_PLOT_WIDTH,DEFAULT_PLOT_HEIGHT);
        if(dates!=null){
            axisPanel.setDateXAxis(dates);
        }
        PlotPanel<T> plotPanel = new PlotPanel<>(DEFAULT_PLOT_WIDTH,DEFAULT_PLOT_HEIGHT);
        for(ArrayList<T> array:arrays){
            plotPanel.addDataArray(array);
        }
        axisPanel.addPlotPanel(plotPanel);
        return axisPanel;
    }
    void nittyGritty(){
        setBounds(getCenterX(axisPanel.WIDTH+axisPanel.PADDING),getCenterY(axisPanel.HEIGHT+axisPanel.PADDING),axisPanel.WIDTH,axisPanel.HEIGHT+ADD_FOR_INSETS);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        add(axisPanel);
        setVisible(true);
    }
    public static void saveImage(File file, JPanel panel, int width, int height) throws IOException {
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bufferedImage.createGraphics();
		panel.paintAll(g);
		g.dispose();
		ImageIO.write(bufferedImage, "png", file);
	}
    int getCenterY(int height){
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        return (screenHeight/2)-(height/2);
    }
    int getCenterX(int width){
        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        return (screenWidth/2)-width/2;
    }
}
