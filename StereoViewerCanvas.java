import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.awt.datatransfer.*;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.filechooser.*;

public class StereoViewerCanvas extends Canvas implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, ActionListener {
	StereoViewer parent;
	Image iFileImage = null;

	JPopupMenu pmPopupMenu;
	JMenuItem miCopyFullPath, miCopyImage;

	public StereoViewerCanvas(StereoViewer StereoViewer) {
		parent = StereoViewer;

		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);

		pmPopupMenu = new JPopupMenu();
		miCopyFullPath = new JMenuItem("Copy Full Path");
		miCopyFullPath.addActionListener(this);
		pmPopupMenu.add(miCopyFullPath);
		miCopyImage = new JMenuItem("Copy Image");
		miCopyImage.addActionListener(this);
		pmPopupMenu.add(miCopyImage);
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
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3 || (e.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK) {
			togglePopup(e);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3 || (e.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK) {
			togglePopup(e);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {

	}

	@Override
	public void mouseMoved(MouseEvent e) {

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e){
	}

	@Override
	public void keyReleased(KeyEvent e){

	}

	@Override
	public void keyTyped(KeyEvent e){
		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object source = arg0.getSource();
		if (source == miCopyFullPath) {
			String fileName = parent.getCurrentFilePath();
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			StringSelection selection = new StringSelection(fileName);
			clipboard.setContents(selection, null);
		} else if (source == miCopyImage) {
	        ImageSelection imageSelection = new ImageSelection(this.iFileImage);
	        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	        clipboard.setContents(imageSelection, null);
		}
	}

	public void togglePopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			pmPopupMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	public class ImageSelection implements Transferable, ClipboardOwner {
		protected Image data;

		public ImageSelection(Image image) {
			this.data = image;
		}

		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { DataFlavor.imageFlavor };
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return DataFlavor.imageFlavor.equals(flavor);
		}

		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (DataFlavor.imageFlavor.equals(flavor)) {
				return data;
			}
			throw new UnsupportedFlavorException(flavor);
		}

		public void lostOwnership(Clipboard clipboard, Transferable contents) {
			this.data = null;
		}
	}
}
