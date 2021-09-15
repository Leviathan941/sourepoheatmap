Sourepo Heatmap [![Build Status](https://travis-ci.org/leviathan941/sourepoheatmap.svg?branch=master)](https://travis-ci.org/leviathan941/sourepoheatmap)
==============
Application for building changes heat map of a source repository.

Only **Git**, **[Repo](https://git.wiki.kernel.org/index.php/Interfaces,_frontends,_and_tools#repo)** repositories supported at the moment!

How to build Sourepo Heatmap
----------------------------
### Requirements ###

You need to have the following to compile and run the project:
* [OpenJDK](https://openjdk.java.net/) v. 8 or newer.
* [OpenJFX](https://openjdk.java.net/projects/openjfx/) v. 8 or newer.
* [SBT](http://www.scala-sbt.org/) (v. 1.0 or newer).

Other dependencies are downloaded by SBT automatically.

### Compile and Run using SBT ###

* Open a terminal and go to the directory with the project source code.
* Specify `JAVA_HOME` environment variable.
* Type `sbt "project guiApp" run` to compile and launch the application.
* Type `sbt "project guiApp" assembly` to create _fat_ JAR.

License
-------
Copyright (C) 2015-2021 Alexey Kuzin. All rights reserved.

This project is governed by the BSD New license. For details see the file
titled LICENSE in the project root folder.
