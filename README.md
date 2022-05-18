# Overview

The FortiPenTest Jenkins Plugin integrates with the Jenkins' build
process, triggers automated FortiPenTest scans as part of the build
process inside of the Jenkins CI platform. It can be configured to
trigger either Quick Scan or Full Scan on the provided scan URL.

Download Jenkins from <https://jenkins.io/download/>. It works on
platforms such as Windows, Linux distributions, and in Docker
containers. You can integrate FortiPenTest with Jenkins so that your
builds can trigger a scan.

# 1.Compiling, Installing and Configuring FortiPenTest Jenkins Plugin

## 1.1. Compiling FortiPenTest Jenkins Plugin

-   Jenkins is based on Java, so to build Jenkins plugins you need to
    install a Java Development Kit (JDK). Recent Jenkins releases
    require JDK 8 to run.

-   Jenkins's plugins mostly use Maven to build. So, download and
    install Maven from <https://maven.apache.org/>.

-   Navigate to the source code directory where pom.xml file resides.

-   Run *mvn install command.*

-   It will generate FortiPenTest.hpi file under target folder.

## 1.2. Installing FortiPenTest Jenkins Plugin

-   In the Jenkins UI, click on the *Manage Jenkins* menu option.

-   On the Manage Jenkins page, click on the *Manage Plugins* option.
    This will open the *Jenkins Plugin Manager* page.

-   On the *Jenkins Plugin Manager* page, click on the *Available* tab.

-   In the *Filter* field, type *FortiPenTest*.

-   Click on the checkbox next to the *FortiPenTest* plugin.

-   Click on the *Install without restart* button.

-   When the installation is complete, click on the checkbox next to
    *Restart Jenkins when installation is complete and no jobs are
    running*.

## 1.3. Configuring FortiPenTest Jenkins Plugin

-   In the Jenkins UI, click on the *Manage Jenkins* menu option.

-   On the *Manage Jenkins* page, click on the *Configure System*
    option. Scroll down to the bottom, to the *FortiPenTest* section.

-   By default, the *FortiPenTest API URL* field is set
    to <https://fortipentest.com/api/v1.0>.

-   Provide Username in the FortiPenTest UserName field.

-   Click on the *Apply* button to save the FortiPenTest configuration.

-   Click on Validate to validate the fields.

## 1.4. Adding FortiPenTest Scan as a Build Step in a Jenkins Job

-   In the Jenkins UI, click on the New Item.

-   Provide some name in the "Enter an item name", select Freestyle
    Project and click on OK.

-   Scroll down to the bottom to the Build section.

-   Click on Add build step in the Build section and Select
    FortiPenTest.

-   You can see below UI.

-   Scan Type: Choose Scan Type either Quick Scan or Full Scan.

-   Scan Target: Select Scan URL on which you want to run the scan.

-   API Key: Click on the *Add* button next to the *API Key* field and
    select the *Jenkins* option.

-   In the *Jenkins Credentials Provider* dialog:

-   In the *Kind* field, select *Secret text*.

-   In the *Scope* field, select *Global (Jenkins, nodes, items, all
    child items, etc)*.

-   Open the FortiPenTest user interface to retrieve the FortiPenTest
    API key

    -   Click on User icon and click on settings

    -   Under API Key Generation, click on Generate Button to generate
        API Key.

    -   Copy the API Key

-   Click on the *Add* button to close the *Jenkins Credentials
    Provider* dialog.

-   Click on the *Apply* button to save the FortiPenTest Build
    configuration.

## 1.5. Scan Report

 Once your initiated scan is completed, you can see the scan report on
the build result window.
