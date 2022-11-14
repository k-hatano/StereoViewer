import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

import javax.imageio.*;

import javax.swing.*;
import javax.swing.filechooser.*;

public class StereoViewerImport {
	StereoViewer parent;
	File lastFile = null;

	public StereoViewerImport(StereoViewer StereoViewer) {
		parent = StereoViewer;
	}

	public void showImportFileDialog() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("JPEG file (.jpg)", "jpg", "jpeg"));
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("JPEG Multi-Picture Object (.mpo)", "mpo"));
		if (lastFile != null) {
			chooser.setSelectedFile(lastFile);
		}
		int res = chooser.showOpenDialog(this.parent);
		if (res == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			lastFile = file;
			Image images[] = importImage(file.getAbsolutePath());
			parent.leftStereoViewerCanvas.paintImage(images.length > 0 ? images[0] : null);
			parent.rightStereoViewerCanvas.paintImage(images.length > 1 ? images[1] : null);
			if (images.length == 1) {
				parent.showAsMonoscopic();
			} else {
				parent.showAsStereoscopic();
			}
		}
	}

	public Image[] importImage(String path) {
		java.util.List<Image> resultList = new ArrayList<Image>();

		File file = new File(path);
		this.parent.setTitle(file.getName());

		try {
			InputStream inputStreams[] = getImageInputStream(path);
			for (int i = 0; i < inputStreams.length; i++) {
				Image image = ImageIO.read(inputStreams[i]);
				resultList.add(image);
			}

			return resultList.toArray(new Image[0]);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

		return resultList.toArray(new Image[0]);
	}

	public InputStream[] getImageInputStream(String path) throws FileNotFoundException, IOException {
		java.util.List<InputStream> resultList = new ArrayList<InputStream>();

		byte bytes[][] = readFileIntoBytes(path);
		for (int i = 0; i < bytes.length; i++) {
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes[i]);
			resultList.add(byteArrayInputStream);
		}

		return resultList.toArray(new InputStream[0]);
	}

	final int BUFFER_SIZE = 1024;

	public byte[][] readFileIntoBytes(String path) throws FileNotFoundException, IOException {
		int depth = 0;
		java.util.List<byte[]> resultList = new ArrayList<byte[]>();

		File file = new File(path);
		FileInputStream fileInputStream = new FileInputStream(file);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		byte buffer[] = new byte[BUFFER_SIZE];
		byte lastByte = 0;
		while (true) {
			// TODO: 激遅い
			int length = fileInputStream.read(buffer);
			if (length < 0) {
				break;
			}
			for (int i = 0; i < length; i++) {
				if (lastByte == 0xFF - 0x100 && buffer[i] == 0xD8 - 0x100) {
					byte lastBuffer[] = {lastByte};
					outputStream.write(lastBuffer, 0, 1);
					depth++;
				}
				if (depth > 0) {
					byte currentBuffer[] = {buffer[i]};
					outputStream.write(currentBuffer, 0, 1);
				}
				if (lastByte == 0xFF - 0x100 && buffer[i] == 0xD9 - 0x100) {
					depth--;
					if (depth == 0) {
						resultList.add(outputStream.toByteArray());
						outputStream = new ByteArrayOutputStream();
					}
				}
				lastByte = buffer[i];
			}
		}

		return resultList.toArray(new byte[0][0]);
	}
}
