import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.io.*;

import javax.swing.*;

public class StereoViewer extends JFrame implements ActionListener {
	JPanel pHeaderPanel, pCanvasPanel;
	JMenuBar mbMenuBar;
	JMenu mFile, mStereoscopy;
	JMenuItem miOpen,miQuit;
	JMenuItem miMonoscopic,miStereoscopic,miSwitchImages;
	GridLayout glGridLayout;
	StereoViewerImport imageViewerImport;
	StereoViewerCanvas leftStereoViewerCanvas, rightStereoViewerCanvas;
	
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
		glGridLayout = new GridLayout(1,1);
		pCanvasPanel.setLayout(glGridLayout);

		leftStereoViewerCanvas = new StereoViewerCanvas(this);
		pCanvasPanel.add(leftStereoViewerCanvas);

		rightStereoViewerCanvas = new StereoViewerCanvas(this);

		imageViewerImport = new StereoViewerImport(this);

		add("Center", pCanvasPanel);

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
		} else if (source == miMonoscopic) {
			showAsMonoscopic();
		} else if (source == miStereoscopic) {
			showAsStereoscopic();
		} else if (source == miSwitchImages) {
			Image newLeftImage = rightStereoViewerCanvas.iFileImage;
			Image newRightImage = leftStereoViewerCanvas.iFileImage;
			leftStereoViewerCanvas.paintImage(newLeftImage);
			rightStereoViewerCanvas.paintImage(newRightImage);
		}
	}

	public void showAsMonoscopic() {
		pCanvasPanel.remove(leftStereoViewerCanvas);
		pCanvasPanel.remove(rightStereoViewerCanvas);

		glGridLayout = new GridLayout(1,1);
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

		glGridLayout = new GridLayout(1,2);
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

					File file = ((java.util.List<File>)t.getTransferData(DataFlavor.javaFileListFlavor)).get(0);
					Image images[] = imageViewerImport.importImage(file.getAbsolutePath());
					parent.leftStereoViewerCanvas.paintImage(images.length > 0 ? images[0] : null);
					parent.rightStereoViewerCanvas.paintImage(images.length > 1 ? images[1] : null);
					if (images.length == 1) {
						showAsMonoscopic();
					} else {
						showAsStereoscopic();
					}
				}
			}
			catch (Exception ex){
				ex.printStackTrace(System.err);
			}
		}
	}
}
