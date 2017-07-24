package syncto;

import java.awt.Graphics;
import java.awt.Image;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class ThemeImage extends JPanel{

	private Image image=null;
	private String path;
	private int Height;
	private int Width;
	private int x_axis;
	private int y_axis;
	
	public ThemeImage(String path,int x,int y,int w,int h){
		this.path = path;
		this.x_axis = x;
		this.y_axis = y;
		this.Width = w;
		this.Height = h;
		
	}
		
	public void paint(Graphics g){
	     try {
	         image=ImageIO.read(new File(path));
	         g.drawImage(image,x_axis,y_axis,Width,Height,null);
	     } catch (Exception e) {
	         e.printStackTrace();
	     }
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}