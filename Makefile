# Makefile
StereoViewer: StereoViewer.java
	javac -encoding UTF-8 -source 1.8 -target 1.8 StereoViewer.java
	jar cvfm StereoViewer.jar mani.mf *.class
clean:
	rm *.class
	rm *.jar