import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

public class StereoViewer extends JFrame implements ActionListener {
	JPanel pNavigatePanel ,pHeaderPanel, pCanvasPanel, pFileSelectorPanel;
	JMenuBar mbMenuBar;
	JMenu mFile, mStereoscopy;
	JMenuItem miOpen,miPrevImage,miNextImage,miCleanFileHistory,miShowGridList,miQuit;
	JMenuItem miMonoscopic,miStereoscopic,miSwitchImages;
	GridLayout glGridLayout;
	StereoViewerImport imageViewerImport;
	StereoViewerCanvas leftStereoViewerCanvas, rightStereoViewerCanvas;
	JComboBox cbHistoryPulldown;
	BorderLayout blFileSelectorLayout;
	JButton bOpen, bGridList, bPrevImage, bNextImage;
	StereoViewerGridList svglGridList;
	
	StereoViewer() {
		super();

		setSize(600,812);
		setLayout(new BorderLayout());
		setTitle("StereoViewer");

		mbMenuBar = new JMenuBar();

		mFile = new JMenu("File");
		miOpen = new JMenuItem("Open...");
		miOpen.addActionListener(this);
		miOpen.setAccelerator(KeyStroke.getKeyStroke('O',KeyEvent.CTRL_MASK));
		mFile.add(miOpen);
		mFile.addSeparator();
		miPrevImage = new JMenuItem("Previous Image");
		miPrevImage.addActionListener(this);
		miPrevImage.setAccelerator(KeyStroke.getKeyStroke('[',KeyEvent.CTRL_MASK));
		mFile.add(miPrevImage);
		miNextImage = new JMenuItem("Next Image");
		miNextImage.addActionListener(this);
		miNextImage.setAccelerator(KeyStroke.getKeyStroke(']',KeyEvent.CTRL_MASK));
		mFile.add(miNextImage);
		miCleanFileHistory = new JMenuItem("Clean File History");
		miCleanFileHistory.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE,KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK));
		miCleanFileHistory.addActionListener(this);
		mFile.add(miCleanFileHistory);
		mFile.addSeparator();
		miShowGridList = new JMenuItem("Show Grid List...");
		miShowGridList.addActionListener(this);
		mFile.add(miShowGridList);
		mFile.addSeparator();
		miQuit = new JMenuItem("Quit");
		miQuit.addActionListener(this);
		miQuit.setAccelerator(KeyStroke.getKeyStroke('Q',KeyEvent.CTRL_MASK));
		mFile.add(miQuit);
		mbMenuBar.add(mFile);

		mStereoscopy = new JMenu("Stereoscopy");
		miMonoscopic = new JMenuItem("Monoscopic");
		miMonoscopic.addActionListener(this);
		mStereoscopy.add(miMonoscopic);
		miStereoscopic = new JMenuItem("Stereoscopic");
		miStereoscopic.addActionListener(this);
		mStereoscopy.add(miStereoscopic);
		mStereoscopy.addSeparator();
		mbMenuBar.add(mStereoscopy);
		miSwitchImages = new JMenuItem("Switch Images");
		miSwitchImages.addActionListener(this);
		mStereoscopy.add(miSwitchImages);

		setJMenuBar(mbMenuBar);

		pCanvasPanel = new JPanel();
		glGridLayout = new GridLayout(1, 1);
		pCanvasPanel.setLayout(glGridLayout);

		leftStereoViewerCanvas = new StereoViewerCanvas(this);
		pCanvasPanel.add(leftStereoViewerCanvas);

		rightStereoViewerCanvas = new StereoViewerCanvas(this);

		imageViewerImport = new StereoViewerImport(this);

		add("Center", pCanvasPanel);

		pNavigatePanel = new JPanel();
		pNavigatePanel.setLayout(new GridLayout(1, 2));
		pNavigatePanel.setBorder(BorderFactory.createEmptyBorder());
		bPrevImage = new JButton("<");
		bPrevImage.addActionListener(this);
		pNavigatePanel.add(bPrevImage);
		bNextImage = new JButton(">");
		bNextImage.addActionListener(this);
		pNavigatePanel.add(bNextImage);

		pFileSelectorPanel = new JPanel();
		blFileSelectorLayout = new BorderLayout();
		pFileSelectorPanel.setLayout(blFileSelectorLayout);
		cbHistoryPulldown = new JComboBox(new String[0]);
		cbHistoryPulldown.addActionListener(this);
		pFileSelectorPanel.add("Center", cbHistoryPulldown);
		bOpen = new JButton("Open");
		bOpen.addActionListener(this);
		// pFileSelectorPanel.add("West", bOpen);
		bGridList = new JButton("Grid List");
		bGridList.addActionListener(this);
		pFileSelectorPanel.add("East", bGridList);
		pFileSelectorPanel.add("West", pNavigatePanel);
		add("North", pFileSelectorPanel);

		pHeaderPanel = new JPanel();
		pHeaderPanel.setLayout(new BorderLayout());

		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				closeWindow();
			}
		});

		new DropTarget(this,new Dropper(this));
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object source = arg0.getSource();
		if (source == miQuit) {
			closeWindow();
		} else if (source == miOpen) {
			imageViewerImport.showImportFileDialog();
		} else if (source == bOpen) {
			imageViewerImport.showImportFileDialog();
		} else if (source == miPrevImage || source == bPrevImage) {
			if (cbHistoryPulldown.getSelectedIndex() > 0) {
				cbHistoryPulldown.setSelectedIndex(cbHistoryPulldown.getSelectedIndex() - 1);
			}
		} else if (source == miNextImage || source == bNextImage) {
			if (cbHistoryPulldown.getSelectedIndex() < cbHistoryPulldown.getItemCount() - 1) {
				cbHistoryPulldown.setSelectedIndex(cbHistoryPulldown.getSelectedIndex() + 1);
			}
		} else if (source == miShowGridList) {
			showGridList();
		} else if (source == bGridList) {
			showGridList();
		} else if (source == miCleanFileHistory) {
			imageViewerImport.cleanFileHistory();
		} else if (source == miMonoscopic) {
			showAsMonoscopic();
		} else if (source == miStereoscopic) {
			showAsStereoscopic();
		} else if (source == miSwitchImages) {
			Image newLeftImage = rightStereoViewerCanvas.iFileImage;
			Image newRightImage = leftStereoViewerCanvas.iFileImage;
			leftStereoViewerCanvas.paintImage(newLeftImage);
			rightStereoViewerCanvas.paintImage(newRightImage);
		} else if (source == cbHistoryPulldown) {
			if (cbHistoryPulldown.getSelectedItem() != null) {
				String path = (String)(cbHistoryPulldown.getSelectedItem());
				imageViewerImport.importImageFromFile(new File(path));
			}
		}
	}

	public void showGridList() {
		if (svglGridList == null) {
			svglGridList = new StereoViewerGridList(imageViewerImport);
		}
		svglGridList.show();
		svglGridList.updateGridList();
	}

	public void showAsMonoscopic() {
		pCanvasPanel.remove(leftStereoViewerCanvas);
		pCanvasPanel.remove(rightStereoViewerCanvas);

		glGridLayout = new GridLayout(1, 1);
		pCanvasPanel.add(leftStereoViewerCanvas);
		pCanvasPanel.setLayout(glGridLayout);
		Rectangle originalBounds = this.getBounds();
		this.pack();
		this.setBounds(originalBounds);
		this.invalidate();
	}

	public void showAsStereoscopic() {
		pCanvasPanel.remove(leftStereoViewerCanvas);
		pCanvasPanel.remove(rightStereoViewerCanvas);

		glGridLayout = new GridLayout(1, 2);
		pCanvasPanel.add(leftStereoViewerCanvas);
		pCanvasPanel.add(rightStereoViewerCanvas);
		pCanvasPanel.setLayout(glGridLayout);
		Rectangle originalBounds = this.getBounds();
		this.pack();
		this.setBounds(originalBounds);
		this.invalidate();
	}

	public static void main(String[] argv){
		StereoViewer StereoViewer = new StereoViewer();
		StereoViewer.show();
	}

	public void closeWindow(){
		System.exit(0);
	}

	public void importHistoryUpdated(String history[], String currentFile) {
		int selectedIndex = history.length - 1;
		cbHistoryPulldown.removeAllItems();
		for (int i = 0; i < history.length; i++) {
			cbHistoryPulldown.addItem(history[i]);
			if (currentFile == history[i]) {
				selectedIndex = i;
			}
		}
		cbHistoryPulldown.setSelectedIndex(selectedIndex);
		svglGridList.updateGridList();
	}

	public String getCurrentFilePath() {
		return imageViewerImport.getCurrentFilePath();
	}

	class Dropper extends DropTargetAdapter{
		StereoViewer parent;

		Dropper(StereoViewer imageViewer){
			super();
			parent = imageViewer;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void drop(DropTargetDropEvent arg0) {
			try {
				Transferable t = arg0.getTransferable();
				if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					arg0.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

					java.util.List<File> files = ((java.util.List<File>)t.getTransferData(DataFlavor.javaFileListFlavor));
					for (int i = 0; i < files.size(); i++) {
						if (files.get(i).isDirectory()) {
							FilenameFilter filter = new FilenameFilter() {
								public boolean accept(File dir, String name) {
									if (name.endsWith(".jpg") || name.endsWith(".JPG") || name.endsWith(".mpo") || name.endsWith(".MPO") || name.endsWith(".jpeg") || name.endsWith(".JPEG")) {
										return true;
									} else {
										return false;
									}
								}
							};
							File[] filesInFolder = files.get(i).listFiles(filter);
							if (filesInFolder.length <= 0) {
								continue;
							}
							for (int j = 0; j < filesInFolder.length; j++) {
								imageViewerImport.addFileToHistoryWithoutUpdating(filesInFolder[j].getAbsolutePath());
							}
							imageViewerImport.requestUpdateImportHistory();
							File file = filesInFolder[filesInFolder.length - 1];
							imageViewerImport.importImageFromFile(file);
						} else {
							File file = ((java.util.List<File>)t.getTransferData(DataFlavor.javaFileListFlavor)).get(i);
							imageViewerImport.importImageFromFile(file);
						}
					}
				}
			}
			catch (Exception ex){
				ex.printStackTrace(System.err);
			}
		}
	}
}
