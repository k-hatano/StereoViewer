import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.datatransfer.*;

public class StereoViewerGridList extends JFrame implements ActionListener, AdjustmentListener {
	int XMAX = 5;
	int YMAX = 5;
	final int GRID_SIZE = 160;

	StereoViewerImport parent;
	StereoViewerGridListCanvas gridListCanvas;
	JScrollBar sbScrollBar;

	JMenuBar mbMenuBar;
	JMenu mFile, mScroll, mGridSize;
	JMenuItem miRemoveAll, miClose, miScrollToTop, miScrollUp, miScrollDown, miScrollToEnd;
	JRadioButtonMenuItem miGrid1x4, miGrid1x5, miGrid1x6, miGrid2x2 ,miGrid3x3, miGrid4x4, miGrid5x5, miGrid6x6;

	Image iImages[] = new Image[XMAX * YMAX];

	int nGridSizeX = GRID_SIZE;
	int nGridSizeY = GRID_SIZE;
	int nClickedIndex = 0;

	StereoViewerGridList(StereoViewerImport stereoViewerImport) {
		super();
		parent = stereoViewerImport;

		setSize(nGridSizeX * XMAX + 20, nGridSizeY * YMAX + 20);
		setTitle("Grid List");
		setLayout(new BorderLayout());

		mbMenuBar = new JMenuBar();

		mFile = new JMenu("File");
		miRemoveAll = new JMenuItem("Remove All Images From History");
		miRemoveAll.addActionListener(this);
		mFile.add(miRemoveAll);
		mFile.addSeparator();
		miClose = new JMenuItem("Close");
		miClose.addActionListener(this);
		miClose.setAccelerator(KeyStroke.getKeyStroke('W', KeyEvent.CTRL_MASK));
		mFile.add(miClose);
		mbMenuBar.add(mFile);

		mScroll = new JMenu("Scroll");
		miScrollToTop = new JMenuItem("To Top");
		miScrollToTop.addActionListener(this);
		miScrollToTop.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0));
		mScroll.add(miScrollToTop);
		miScrollUp = new JMenuItem("Up");
		miScrollUp.addActionListener(this);
		miScrollUp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));
		mScroll.add(miScrollUp);
		miScrollDown = new JMenuItem("Down");
		miScrollDown.addActionListener(this);
		miScrollDown.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0));
		mScroll.add(miScrollDown);
		miScrollToEnd = new JMenuItem("To End");
		miScrollToEnd.addActionListener(this);
		miScrollToEnd.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0));
		mScroll.add(miScrollToEnd);
		mbMenuBar.add(mScroll);

		ButtonGroup group = new ButtonGroup();
		mGridSize = new JMenu("Grid Size");
		miGrid1x4 = new JRadioButtonMenuItem("1 x 4");
		miGrid1x4.addActionListener(this);
		miGrid1x4.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0, KeyEvent.CTRL_MASK));
		group.add(miGrid1x4);
		mGridSize.add(miGrid1x4);
		miGrid1x5 = new JRadioButtonMenuItem("1 x 5");
		miGrid1x5.addActionListener(this);
		miGrid1x5.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD1, KeyEvent.CTRL_MASK));
		group.add(miGrid1x5);
		mGridSize.add(miGrid1x5);
		miGrid1x6 = new JRadioButtonMenuItem("1 x 6");
		miGrid1x6.addActionListener(this);
		group.add(miGrid1x6);
		mGridSize.add(miGrid1x6);
		mGridSize.addSeparator();
		miGrid2x2 = new JRadioButtonMenuItem("2 x 2");
		miGrid2x2.addActionListener(this);
		miGrid2x2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD2, KeyEvent.CTRL_MASK));
		group.add(miGrid2x2);
		mGridSize.add(miGrid2x2);
		miGrid3x3 = new JRadioButtonMenuItem("3 x 3");
		miGrid3x3.addActionListener(this);
		miGrid3x3.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD3, KeyEvent.CTRL_MASK));
		group.add(miGrid3x3);
		mGridSize.add(miGrid3x3);
		miGrid4x4 = new JRadioButtonMenuItem("4 x 4");
		miGrid4x4.addActionListener(this);
		miGrid4x4.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD4, KeyEvent.CTRL_MASK));
		group.add(miGrid4x4);
		mGridSize.add(miGrid4x4);
		miGrid5x5 = new JRadioButtonMenuItem("5 x 5");
		miGrid5x5.addActionListener(this);
		miGrid5x5.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD5, KeyEvent.CTRL_MASK));
		group.add(miGrid5x5);
		mGridSize.add(miGrid5x5);
		miGrid6x6 = new JRadioButtonMenuItem("6 x 6");
		miGrid6x6.addActionListener(this);
		miGrid6x6.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD6, KeyEvent.CTRL_MASK));
		group.add(miGrid6x6);
		mGridSize.add(miGrid6x6);
		mbMenuBar.add(mGridSize);

		miGrid5x5.setSelected(true);

		setJMenuBar(mbMenuBar);

		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				updateGridSize();
				// gridListCanvas.repaint();
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
		int count = parent.lImportHistory.size();
		sbScrollBar.setMaximum((int)Math.floor(count / (1.0 * XMAX * YMAX)));
		sbScrollBar.setValue(0);
		iImages = new Image[XMAX * YMAX];
		gridListCanvas.repaint();
	}

	public void adjustmentValueChanged(AdjustmentEvent e) {
		iImages = new Image[XMAX * YMAX];
		gridListCanvas.repaint();
	}

	public void selectImageAt(int index) {
		parent.selectImageAt(index);
	}

	public void removeFromHistoryAt(int index) {
		parent.removeFromHistoryAt(index);
		iImages = new Image[XMAX * YMAX];
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object source = arg0.getSource();
		if (source == miRemoveAll) {
			parent.cleanFileHistory();
		} else if (source == miClose) {
			this.setVisible(false);
		} else if (source == miScrollToTop) {
			sbScrollBar.setValue(0);
		} else if (source == miScrollUp) {
			sbScrollBar.setValue(sbScrollBar.getValue() - 1);
		} else if (source == miScrollDown) {
			sbScrollBar.setValue(sbScrollBar.getValue() + 1);
		} else if (source == miScrollToEnd) {
			sbScrollBar.setValue(sbScrollBar.getMaximum());
		} else if (source == miGrid1x4) {
			XMAX = 1;
			YMAX = 4;
			updateGridSize();
			updateGridList();
		} else if (source == miGrid1x5) {
			XMAX = 1;
			YMAX = 5;
			updateGridSize();
			updateGridList();
		} else if (source == miGrid1x6) {
			XMAX = 1;
			YMAX = 6;
			updateGridSize();
			updateGridList();
		} else if (source == miGrid2x2) {
			XMAX = 2;
			YMAX = 2;
			updateGridSize();
			updateGridList();
		} else if (source == miGrid3x3) {
			XMAX = 3;
			YMAX = 3;
			updateGridSize();
			updateGridList();
		} else if (source == miGrid4x4) {
			XMAX = 4;
			YMAX = 4;
			updateGridSize();
			updateGridList();
		} else if (source == miGrid5x5) {
			XMAX = 5;
			YMAX = 5;
			updateGridSize();
			updateGridList();
		} else if (source == miGrid6x6) {
			XMAX = 6;
			YMAX = 6;
			updateGridSize();
			updateGridList();
		}
	}

	private class StereoViewerGridListCanvas extends Canvas implements MouseListener, MouseWheelListener, ActionListener {
		StereoViewerGridList parent;
		JPopupMenu pmPopupMenu;
		JMenuItem miFileName, miShowInMainWindow, miCopyFullPath, miRemoveFromHistory;
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
			pmPopupMenu.addSeparator();
			miRemoveFromHistory = new JMenuItem("Remove From File History");
			miRemoveFromHistory.addActionListener(this);
			pmPopupMenu.add(miRemoveFromHistory);
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
							Image img1;
							if (iImages[x + y * XMAX] != null) {
								img1 = iImages[x + y * XMAX];
							} else {
								setTitle("Loading image (" + (x + y * XMAX + 1) + "/" + (XMAX * YMAX) +")");
								img1 = ImageIO.read(new File(path));
							}
							if (img1 == null) {
								continue;
							}
							if (iImages[x + y * XMAX] == null) {
								iImages[x + y * XMAX] = img1;
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
						} catch (IOException ex) {
							ex.printStackTrace();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			}

			g.drawImage(img, 0, 0, this);

			int count = parent.parent.lImportHistory.size();
			setTitle("Grid List - " + count + " image" + (count == 1 ? "" : "s"));
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
			} else if (source == miRemoveFromHistory) {
				removeFromHistoryAt(nClickedIndex);
			}
		}

		public void togglePopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				pmPopupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}
}
