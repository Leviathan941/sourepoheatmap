Sourepo Heatmap
==============
Application for building changes heat map of a source repository.

Only **Git**, **[Repo](https://git.wiki.kernel.org/index.php/Interfaces,_frontends,_and_tools#repo)** repositories supported at the moment!

How to build Sourepo Heatmap
----------------------------
### Requirements ###

You need to have the following to compile and run the project:
* [Java 1.8 JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html).
* JavaFX 8. It is included to Oracle JDK 8, for OpenJDK you have to install it separately, for example from [here](http://openjdk.java.net/projects/openjfx/).
* [SBT](http://www.scala-sbt.org/) (v. 0.13 or newer).

Other dependencies are downloaded by SBT automatically.

### Compile and Run using SBT ###

* Open a terminal and go to the directory with the project source code.
* Specify `JAVA_HOME` environment variable.
* Type `sbt run` to compile and launch the application.
* Type `sbt assembly` to create _fat_ JAR.

License
-------
Copyright (C) 2015 Alexey Kuzin. All rights reserved.

This project is governed by the BSD New license. For details see the file
titled LICENSE in the project root folder.
