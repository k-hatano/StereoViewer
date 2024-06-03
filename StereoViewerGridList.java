import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.datatransfer.*;

public class StereoViewerGridList extends JFrame implements AdjustmentListener {
	final int XMAX = 5;
	final int YMAX = 5;
	final int GRID_SIZE = 160;

	StereoViewerImport parent;
	StereoViewerGridListCanvas gridListCanvas;
	JScrollBar sbScrollBar;

	int nGridSizeX = GRID_SIZE;
	int nGridSizeY = GRID_SIZE;
	int nClickedIndex = 0;

	StereoViewerGridList(StereoViewerImport stereoViewerImport) {
		super();
		parent = stereoViewerImport;

		setSize(nGridSizeX * XMAX + 20, nGridSizeY * YMAX + 20);
		setTitle("Grid List");
		setLayout(new BorderLayout());

		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				updateGridSize();
				gridListCanvas.repaint();
			}
	    });

		gridListCanvas = new StereoViewerGridListCanvas(this);
		add("Center", gridListCanvas);

		sbScrollBar = new JScrollBar(SwingConstants.VERTICAL, 0, 0, 0, 0);
		sbScrollBar.addAdjustmentListener(this);
		add("East", sbScrollBar);

		updateGridSize();
	}

	public void updateGridSize() {
		nGridSizeX = gridListCanvas.getWidth() / XMAX;
		nGridSizeY = gridListCanvas.getHeight() / YMAX;
	}

	public void updateGridList() {
		sbScrollBar.setMaximum((int)Math.floor(parent.lImportHistory.size() / (1.0 * XMAX * YMAX)));
		sbScrollBar.setValue(0);
		gridListCanvas.repaint();
	}

	public void adjustmentValueChanged(AdjustmentEvent e) {
		gridListCanvas.repaint();
	}

	public void selectImageAt(int index) {
		parent.selectImageAt(index);
	}

	private class StereoViewerGridListCanvas extends Canvas implements MouseListener, MouseWheelListener, ActionListener {
		StereoViewerGridList parent;
		JPopupMenu pmPopupMenu;
		JMenuItem miFileName, miShowInMainWindow, miCopyFullPath;
		int wheelDelta = 0;

		public StereoViewerGridListCanvas (StereoViewerGridList gridList) {
			parent = gridList;
			addMouseListener(this);
			addMouseWheelListener(this);
			wheelDelta = 0;

			pmPopupMenu = new JPopupMenu();
			miFileName = new JMenuItem("");
			miFileName.setEnabled(false);
			pmPopupMenu.add(miFileName);
			miShowInMainWindow = new JMenuItem("Show In Main Window");
			miShowInMainWindow.addActionListener(this);
			pmPopupMenu.add(miShowInMainWindow);
			miCopyFullPath = new JMenuItem("Copy Full Path");
			miCopyFullPath.addActionListener(this);
			pmPopupMenu.add(miCopyFullPath);
		}

		public void paint(final Graphics g){
			Image img = createImage(this.getWidth(), this.getHeight());
			Graphics2D grp = (Graphics2D)(img.getGraphics());

			grp.setColor(new Color(214, 214, 214));
			grp.fillRect(0, 0, this.getWidth(), this.getHeight());

			for (int y = 0; y < YMAX; y++) {
				for (int x = 0; x < XMAX; x++) {
					int i = x + y * XMAX + sbScrollBar.getValue() * XMAX * YMAX;
					if (i < parent.parent.lImportHistory.size()) {
						try {
							String path = parent.parent.lImportHistory.get(i);
							Image img1 = ImageIO.read(new File(path));
							if (img1 == null) {
								continue;
							}

							int left = 0;
							int top = 0;
							int width = img1.getWidth(null);
							int height = img1.getHeight(null);

							if (width > nGridSizeX) {
								height = height * nGridSizeX / width;
								width = width * nGridSizeX / width;
							}

							if (height > nGridSizeY) {
								width = width * nGridSizeY / height;
								height = height * nGridSizeY / height;
							}

							left = (nGridSizeX - width) / 2;
							top = (nGridSizeY - height) / 2;

							grp.drawImage(img1, left + x * nGridSizeX, top + y * nGridSizeY, width, height, null);
						} catch (IOException ignore) {

						}
					}
				}
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
			int x = e.getX() / (this.getWidth() / XMAX);
			int y = e.getY() / (this.getHeight() / YMAX);
			nClickedIndex = sbScrollBar.getValue() * XMAX * YMAX + y * XMAX + x;
			if (nClickedIndex >= parent.parent.lImportHistory.size()) {
				return;
			}
			if (e.getButton() == MouseEvent.BUTTON3 || (e.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK) {
				String fileName = (new File(parent.parent.lImportHistory.get(nClickedIndex))).getAbsolutePath();
				miFileName.setText(fileName);
				togglePopup(e);
			} else {
				selectImageAt(nClickedIndex);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			int x = e.getX() / (this.getWidth() / XMAX);
			int y = e.getY() / (this.getHeight() / YMAX);
			nClickedIndex = sbScrollBar.getValue() * XMAX * YMAX + y * XMAX + x;
			if (nClickedIndex >= parent.parent.lImportHistory.size()) {
				return;
			}
			if (e.getButton() == MouseEvent.BUTTON3 || (e.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK) {
				String fileName = (new File(parent.parent.lImportHistory.get(nClickedIndex))).getAbsolutePath();
				miFileName.setText(fileName);
				togglePopup(e);
			}
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent arg0) {
			wheelDelta += arg0.getWheelRotation();
			if (wheelDelta >= 4) {
				sbScrollBar.setValue(sbScrollBar.getValue() + 1);
				wheelDelta -= 4;
			} else if (wheelDelta <= -4) {
				sbScrollBar.setValue(sbScrollBar.getValue() - 1);
				wheelDelta += 4;
			}
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			Object source = arg0.getSource();
			if (source == miShowInMainWindow) {
				selectImageAt(nClickedIndex);
			} else if (source == miCopyFullPath) {
				String fileName = (new File(parent.parent.lImportHistory.get(nClickedIndex))).getAbsolutePath();
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection selection = new StringSelection(fileName);
				clipboard.setContents(selection, null);
			}
		}

		public void togglePopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				pmPopupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}
}
