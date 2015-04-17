Sourepo Heatmap
==============
Application for building changes heat map of a source repository.

Only **Git** repository supported at the moment!

How to build Sourepo Heatmap
----------------------------
### Requirements ###

You need to have the following to compile and run the project:
* [Java 1.8 JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html).
* [SBT](http://www.scala-sbt.org/) (v. 0.13 or newer).

Other dependencies are included in the source tree or downloaded by SBT automatically.

### Compile and Run using SBT ###

* Open a terminal and go to the directory with the project source code.
* Specify `JAVA_HOME` environment variable.
* Type `sbt run` to compile and launch the application.

**NOTE:** There are two classes to launch the application at the moment: `GuiApplication`
and `CliApplication`, but the second one is noy implemented yet (just a placeholder), so
run `GuiApplication` when SBT asked.

License
-------
Copyright (C) 2015 Alexey Kuzin. All rights reserved.

This project is governed by the BSD New license. For details see the file
titled LICENSE in the project root folder.
