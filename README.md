# hop-mcm

> **_NOTE:_**
> Latest Update: 2023-12-20 
> Hop Version 2.8.0

Custom plugins for Apache Hop for the use of 2024-mcm-everitt-ryan project.

Current plugins are:

* ***Bias sentences:*** breaks down a corpus into sentences and then scans those sentences for the presence of terms from the bias taxonomy.

## Building

Run

`mvn clean install`

You will find the plugin zip files in the various target folders of

`assemblies/plugins/*`

for example:

`assemblies/plugins/exceloutput/target/hop-assemblies-plugins-transforms-bias-sentences-2.8.0.zip`

## Installing

To install a plugin, unzip the plugin assembly zip file in the plugins/ folder of your Apache Hop client installation.

Attribution:

* **sentence-bias-detector.svg:** Free Scale Icon in Glyph Style By Vaadin Icons https://iconscout.com/contributors/vaadin-icons
* **detect-language.svg:** Free Scale Icon in Glyph Style By Vaadin Icons https://iconscout.com/contributors/vaadin-icons
