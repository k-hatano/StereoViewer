import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.awt.datatransfer.*;

import javax.swing.*;
import javax.swing.filechooser.*;

public class StereoViewerCanvas extends Canvas implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
	StereoViewer parent;
	Image iFileImage = null;

	public StereoViewerCanvas(StereoViewer StereoViewer) {
		parent = StereoViewer;

		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);
	}

	public void update(Graphics g){
		paint(g);
	}

	public boolean paintImage(Image image) {
		this.iFileImage = image;
		repaint();
		return true;
	}

	public void paint(final Graphics g){
		Image img = createImage(this.getWidth(), this.getHeight());
		Graphics2D grp = (Graphics2D)(img.getGraphics());
		
		grp.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		grp.setColor(new Color(214, 214, 214));
		grp.fillRect(0, 0, this.getWidth(), this.getHeight());

		if (iFileImage != null) {
			int x = 0;
			int y = 0;
			int width = iFileImage.getWidth(null);
			int height = iFileImage.getHeight(null);

			if (width > this.getWidth()) {
				height = height * this.getWidth() / width;
				width = width * this.getWidth() / width;
			}

			if (height > this.getHeight()) {
				width = width * this.getHeight() / height;
				height = height * this.getHeight() / height;
			}

			x = (this.getWidth() - width) / 2;
			y = (this.getHeight() - height) / 2;
			
			grp.drawImage(iFileImage, x, y, width, height, this);
		}

		g.drawImage(img, 0, 0, this);
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	@Override
	public void mousePressed(MouseEvent arg0) {

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {

	}

	@Override
	public void mouseDragged(MouseEvent arg0) {

	}

	@Override
	public void mouseMoved(MouseEvent arg0) {

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {

	}

	@Override
	public void keyPressed(KeyEvent arg0){
	}

	@Override
	public void keyReleased(KeyEvent arg0){

	}

	@Override
	public void keyTyped(KeyEvent arg0){
		
	}

}
