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
	ArrayList<String> lImportHistory = new ArrayList<String>();

	public StereoViewerImport(StereoViewer StereoViewer) {
		parent = StereoViewer;
	}

	public void selectImageAt(int index) {
		parent.cbHistoryPulldown.setSelectedIndex(index);
	}

	public void removeFromHistoryAt(int index) {
		lImportHistory.remove(index);
		parent.importHistoryUpdated(lImportHistory.toArray(new String[0]), null, true);
	}

	public String getCurrentFilePath() {
		return (String)(parent.cbHistoryPulldown.getSelectedItem());
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
			if (chooser.getSelectedFile().isDirectory()) {
				FilenameFilter filter = new FilenameFilter() {
					public boolean accept(File dir, String name) {
						if (name.endsWith(".jpg") || name.endsWith(".JPG") || name.endsWith(".mpo") || name.endsWith(".MPO") || name.endsWith(".jpeg") || name.endsWith(".JPEG")) {
							return true;
						} else {
							return false;
						}
					}
				};
				File[] filesInFolder = chooser.getSelectedFile().listFiles(filter);
				if (filesInFolder.length <= 0) {
					return;
				}
				ArrayList<File> filesInFolderUnsortedList = new ArrayList<File>();
				for (int j = 0; j < filesInFolder.length; j++) {
					filesInFolderUnsortedList.add(filesInFolder[j]);
				}
				filesInFolderUnsortedList.sort(new Comparator<File>(){
					public int compare(File a, File b) {
						return a.getAbsolutePath().compareTo(b.getAbsolutePath());
					}
				});
				filesInFolder = filesInFolderUnsortedList.toArray(new File[0]);
				for (int j = 0; j < filesInFolder.length; j++) {
					addFileToHistoryWithoutUpdating(filesInFolder[j].getAbsolutePath());
				}
				requestUpdateImportHistory();
				File file = filesInFolder[filesInFolder.length - 1];
				importImageFromFile(file);
			} else {
				File file = chooser.getSelectedFile();
				this.importImageFromFile(file);
			}
		}
	}

	public void showExtractFileDialog() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("JPEG Multi-Picture Object (.mpo)", "mpo"));
		if (lastFile != null) {
			chooser.setSelectedFile(lastFile);
		}
		int res = chooser.showOpenDialog(this.parent);
		if (res == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			try {
				int result = this.extractImage(file);
				JOptionPane.showMessageDialog(parent, new JLabel("Extracted to " + result + " files."));
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(parent, new JLabel(ex.toString()));
	            ex.printStackTrace();
	        }
		}
	}

	public void cleanFileHistory() {
		lImportHistory = new ArrayList<String>();
		parent.importHistoryUpdated(lImportHistory.toArray(new String[0]), null, false);
	}

	public void addFileToHistoryWithoutUpdating(String path) {
		lImportHistory.add(path);
	}

	public void requestUpdateImportHistory() {
		parent.importHistoryUpdated(lImportHistory.toArray(new String[0]), null, false);
	}

	public void importImageFromFile(File file) {
		Image images[] = importImage(file.getAbsolutePath());
		lastFile = file;
		parent.leftStereoViewerCanvas.paintImage(images.length > 0 ? images[0] : null);
		parent.rightStereoViewerCanvas.paintImage(images.length > 1 ? images[1] : null);
		if (images.length == 1) {
			parent.showAsMonoscopic();
		} else {
			parent.showAsStereoscopic();
		}
	}

	private Image[] importImage(String path) {
		java.util.List<Image> resultList = new ArrayList<Image>();

		File file = new File(path);
		this.parent.setTitle(file.getName());

		try {
			InputStream inputStreams[] = getImageInputStream(path);
			for (int i = 0; i < inputStreams.length; i++) {
				try {
					Image image = ImageIO.read(inputStreams[i]);
					resultList.add(image);
				} catch (javax.imageio.IIOException ex) {
		            ex.printStackTrace();
		        }
			}

			if (!lImportHistory.contains(path)) {
				lImportHistory.add(path);
				parent.importHistoryUpdated(lImportHistory.toArray(new String[0]), path, false);
			}

			return resultList.toArray(new Image[0]);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

		return resultList.toArray(new Image[0]);
	}

	private InputStream[] getImageInputStream(String path) throws FileNotFoundException, IOException {
		java.util.List<InputStream> resultList = new ArrayList<InputStream>();

		byte bytes[][] = readFileIntoBytes(path);
		for (int i = 0; i < bytes.length; i++) {
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes[i]);
			resultList.add(byteArrayInputStream);
		}

		return resultList.toArray(new InputStream[0]);
	}

	final int BUFFER_SIZE = 1024;

	private byte[][] readFileIntoBytes(String path) throws FileNotFoundException, IOException {
		int depth = 0;
		java.util.List<byte[]> resultList = new ArrayList<byte[]>();

		File file = new File(path);
		FileInputStream fileInputStream = new FileInputStream(file);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		byte buffer[] = new byte[BUFFER_SIZE];
		byte lastByte = 0;
		// int skippableLength = 0;
		int indexFromHead = 0;
		while (true) {
			// TODO: 激遅い
			int length = fileInputStream.read(buffer);
			if (length < 0) {
				break;
			}
			for (int i = 0; i < length; i++) {
				// if (lastByte == 0xFF - 0x100 && (buffer[i] == 0xD8 - 0x100 || buffer[i] == 0xD9 - 0x100)) {
				// 	System.out.println("" + indexFromHead + " : [" + byteToUnsignedInt(lastByte) + "," + (i < length ? byteToUnsignedInt(buffer[i]) : "-") + "," + (i + 1 < length ? byteToUnsignedInt(buffer[i + 1]) : "-") + "," + (i + 2 < length ? byteToUnsignedInt(buffer[i + 2]) : "-") + "] depth="+depth);
				// }
				if (lastByte == 0xFF - 0x100 && buffer[i] == 0xD8 - 0x100 && (i+1 >= length || buffer[i+1] == 0xFF - 0x100)) {
					// System.out.println("FFD8 : " + indexFromHead);
					byte lastBuffer[] = {lastByte};
					outputStream.write(lastBuffer, 0, 1);
					depth++;
					// skippableLength = byteToUnsignedInt(buffer[i + 3]) * 0x100 + byteToUnsignedInt(buffer[i + 4]);
					// System.out.println("skippableLength = " + skippableLength);
					// if (skippableLength >= length - i - 1) {
					// 	skippableLength = length - i - 1;
					// 	System.out.println("skippableLength trimmed to " + skippableLength);
					// }
				} else if (lastByte == 0xFF - 0x100 && buffer[i] == 0xD9 - 0x100 && (i+1 >= length || buffer[i+1] == 0xFF - 0x100)) {
					// System.out.println("FFD9 : " + indexFromHead);
					// System.out.println("          skippableLength : " + skippableLength);
					depth--;
					if (depth < 0) {
						depth = 0;
					}
					if (depth == 0) {
						resultList.add(outputStream.toByteArray());
						outputStream = new ByteArrayOutputStream();
					}
				}
				if (depth > 0) {
					byte currentBuffer[] = {buffer[i]};
					outputStream.write(currentBuffer, 0, 1);
				}
				
				// if (skippableLength > 0) {
				// 	skippableLength--;
				// }
				lastByte = buffer[i];
				indexFromHead++;
			}
		}

		return resultList.toArray(new byte[0][0]);
	}

	public int byteToUnsignedInt(byte aByte) {
		if (aByte < 0) {
			return (int)(0x100 + aByte);
		}
		return (int)aByte;
	}

	public int extractImage(File file) throws IOException {
		int filesIndex = 0;

		int depth = 0;
		java.util.List<byte[]> resultList = new ArrayList<byte[]>();

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
				if (lastByte == 0xFF - 0x100 && buffer[i] == 0xD8 - 0x100 && (i+1 >= length || buffer[i+1] == 0xFF - 0x100)) {
					byte lastBuffer[] = {lastByte};
					outputStream.write(lastBuffer, 0, 1);
					depth++;
				} else if (lastByte == 0xFF - 0x100 && buffer[i] == 0xD9 - 0x100 && (i+1 >= length || buffer[i+1] == 0xFF - 0x100)) {
					depth--;
					if (depth < 0) {
						depth = 0;
					}
					if (depth == 0) {
						resultList.add(outputStream.toByteArray());
						outputStream = new ByteArrayOutputStream();
					}
				}
				if (depth > 0) {
					byte currentBuffer[] = {buffer[i]};
					outputStream.write(currentBuffer, 0, 1);
				}
				
				lastByte = buffer[i];
			}
		}

		String dirPath = file.getAbsolutePath().replaceAll(".MPO", ".mpo").replaceAll(".mpo", "_extracted");
		File dir = new File(dirPath);
		dir.mkdir();

		String singleFileName = file.getName();
		for (int i = 0; i < resultList.size(); i++) {
			String path = dirPath + File.separator + singleFileName.replaceAll(".MPO", ".mpo").replaceAll(".mpo", "_" + (i < 9 ? "0" : "") + (i + 1) + ".jpg");
			File newFile = new File(path);
			try (FileOutputStream newOutputStream = new FileOutputStream(newFile)) {
			    newOutputStream.write(resultList.get(i));
			    filesIndex++;
			}
		}
		return filesIndex;
	}
}
