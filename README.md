# Overview

The FortiDAST Jenkins Plugin integrates with the Jenkins' build
process, triggers automated FortiDAST scans as part of the build
process inside of the Jenkins CI platform. It can be configured to
trigger either Quick Scan or Full Scan on the provided scan URL.

Download Jenkins from <https://jenkins.io/download/>. It works on
platforms such as Windows, Linux distributions, and in Docker
containers. You can integrate FortiDAST with Jenkins so that your
builds can trigger a scan.

# 2.Compiling, Installing and Configuring FortiDAST Jenkins Plugin

## 2.1. Compiling FortiDAST Jenkins Plugin

-   Jenkins is based on Java, so to build Jenkins plugins you need to install a Java Development Kit (JDK). Recent Jenkins releases require JDK 8 to run. https://www.oracle.com/in/java/technologies/javase/javase8-archive-downloads.html

-   Jenkins's plugins mostly use Maven to build. So, download and install Maven from https://dlcdn.apache.org/maven/maven-3/3.8.8/binaries/apache-maven-3.8.8-bin.zip.
-   Set MAVEN_HOME & JAVA_HOME is environment variable.
-   Navigate to the source code directory where pom.xml file resides.

-   Run *mvn install command.*

-   It will generate FortiDAST.hpi file under target folder.

## 2.2. Installing FortiDAST Jenkins Plugin

-   In the Jenkins UI, click on the *Manage Jenkins* menu option.

-   On the Manage Jenkins page, click on the *Manage Plugins* option.
    This will open the *Jenkins Plugin Manager* page.

-   On the *Jenkins Plugin Manager* page, click on the *Available* tab.

-   In the *Filter* field, type *FortiDAST*.

-   Click on the checkbox next to the *FortiDAST* plugin.

-   Click on the *Install without restart* button.

-   When the installation is complete, click on the checkbox next to
    *Restart Jenkins when installation is complete and no jobs are
    running*.

## 2.3. Configuring FortiDAST Jenkins Plugin

-   In the Jenkins UI, click on the *Manage Jenkins* menu option.

-   On the *Manage Jenkins* page, click on the *Configure System*
    option. Scroll down to the bottom, to the *FortiDAST* section.

-   By default, the *FortiDAST API URL* field is set
    to <https://fortidast.forticloud.com/api/v1.0>.

-   Provide Username in the FortiDAST UserName field.

-   Click on the *Apply* button to save the FortiDAST configuration.

-   Click on Validate to validate the fields.

## 2.4. Adding FortiDAST Scan as a Build Step in a Jenkins Job

-   In the Jenkins UI, click on the New Item.

-   Provide some name in the "Enter an item name", select Freestyle
    Project and click on OK.

-   Scroll down to the bottom to the Build section.

-   Click on Add build step in the Build section and Select
    FortiDAST.

-   You can see below UI.

-   Scan Type: Choose Scan Type either Quick Scan or Full Scan.

-   Scan Target: Select Scan URL on which you want to run the scan.

-   API Key: Click on the *Add* button next to the *API Key* field and
    select the *Jenkins* option.

-   In the *Jenkins Credentials Provider* dialog:

-   In the *Kind* field, select *Secret text*.

-   In the *Scope* field, select *Global (Jenkins, nodes, items, all
    child items, etc)*.

-   Open the FortiDAST user interface to retrieve the FortiDAST
    API key

    -   Click on User icon and click on settings

    -   Under API Key Generation, click on Generate Button to generate
        API Key.

    -   Copy the API Key

-   Click on the *Add* button to close the *Jenkins Credentials
    Provider* dialog.

-   Click on the *Apply* button to save the FortiDAST Build
    configuration.

## 2.5. Scan Report

 Once your initiated scan is completed, you can see the scan report on
the build result window.
