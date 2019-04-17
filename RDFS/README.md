### Libraries
### Configuration or Installation Issues
In Ubuntu/Linux OS, you might encounter JDK versioning issue. You should install open-jdk-8. Sometimes it does not install javafx with it. You might need to install it separately. Or in the worst case, you might need to follow these steps:

 -  Go to Oracle's JJava SE Development Kit 8 Downloads page (https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
 - Download the latest linux version from there (currently it is jdk-8u91-linux-x64.tar.gz).
 - When you extract the tar file you downloaded, you will find jfxrt.jar in the jre/lib/ext directory.
 - Copy jfxrt.jar file in your open-jdk-8 folder in jre/lib/ext directory. 
 - It will resolve the issue regarding import javafx.util.pair in TempFiles.java 