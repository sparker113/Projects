package frame;

import java.awt.Rectangle;

public class PanelPos {
	private float top;
	private float bottom;
	private float left;
	private float right;

	public PanelPos(float top, float bottom, float left, float right) {
		this.top = top;
		this.bottom = bottom;
		this.left = left;
		this.right = right;
	}

	public float getTop() {
		return this.top;
	}

	public float getBottom() {
		return this.bottom;
	}

	public float getLeft() {
		return this.left;
	}

	public float getRight() {
		return this.right;
	}

	public int getPanelLeft(int parentWidth) {
		
		int l = ((int) (left * parentWidth));
		System.out.println("Panel Left: "+l);
		if(l == 640) {
			System.out.println("sam");
		}
		return l;
	}

	public int getPanelTop(int parentHeight) {
		int t = ((int) (top * parentHeight));
		System.out.println("Panel Top: "+t);
		return t;
	}

	public int getPanelWidth(int parentWidth) {
		int w = ((int) ((1f -(left + right)) * parentWidth));
		System.out.println("Panel Width: "+w);
		return w;
	}

	public int getPanelHeight(int parentHeight) {
		int h = ((int) ((1f - (top + bottom)) * parentHeight));
		System.out.println("Panel Height: "+h);
		return h;
	}

	public Rectangle getPanelRect(Rectangle parentRect) {
		return new Rectangle(getPanelLeft(parentRect.width), getPanelTop(parentRect.height),
				getPanelWidth(parentRect.width), getPanelHeight(parentRect.height));
	}
}
