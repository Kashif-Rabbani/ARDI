# Backend Java Maven Project - RDFS

### Getting Started

#### Libraries 

 - **LogMap Matcher external dependency library**
    - Jar file is generated from [logmap-customized-repo](https://github.com/Kashif-Rabbani/logmap-matcher) (This project is forked from [logmap-original-repo](https://github.com/ernestojimenezruiz/logmap-matcher)). 
    
    - Updates are made in the forked project. For more details see the commits history. 
    
    - Command to generate jar with dependencies: `mvn clean install` and `mvn clean compile assembly:single` 
    - Jar file: OWL2VOWL-0.3.6.jar 
    - Jar file is available in the lib folder of BDI Project. 
    
#### Versioning Conflicts
     
 Use or integration of libraries utilizing different versions of OWL-API can cause conflicts which will ultimately lead to failure of running this project smoothly. 
 
 Make sure you keep the same versions or in case it is not possible, go for the alternative i.e. use the required library as a service as like we did in this project. For more details visit [README-SERVICES](https://github.com/genesis-upc/BDI/tree/master/Services/OWL2VOWL-Rest-Service) 

#### Troubles
In Ubuntu/Linux OS, you might encounter JDK versioning issue. You should install open-jdk-8. Sometimes it does not install javafx with it. You might need to install it separately. Or in the worst case, you might need to follow these steps:

 - Go to Oracle's JJava SE Development Kit 8 Downloads page [LINK](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
 - Download the latest linux version from there (currently it is jdk-8u91-linux-x64.tar.gz).
 - When you extract the tar file you downloaded, you will find jfxrt.jar in the jre/lib/ext directory.
 - Copy jfxrt.jar file in your open-jdk-8 folder in jre/lib/ext directory. 
 - It will resolve the issue regarding import javafx.util.pair in TempFiles.java 