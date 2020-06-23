Evaluation of LTL formulas using bitmap manipulations
=====================================================

This project is an extension to the [BeepBeep
3](https://liflab.github.io/beepbeep-3), event stream processing engine,
called a *palette*, that provides functionalities to evaluate formulas of
Linear Temporal Logic using bitmap manipulatons.

What is this?
-------------

Please refer to the following research paper for detailed information and
examples of what this extension can do.

> K. Xie, S. Hallé. (2020). *Offline Monitoring of LTL with Bit Vectors*.
> Submitted to Runtime Verification 2020 (tool paper).

Building this palette
---------------------

To compile the palette, make sure you have the following:

- The Java Development Kit (JDK) to compile. The palette complies
  with Java version 6; it is probably safe to use any later version.
- [Ant](http://ant.apache.org) to automate the compilation and build process

The palette also requires the following Java libraries:

- The latest version of [BeepBeep 3](https://liflab.github.io/beepbeep-3)
- The modified bitmap manipulation libraries [Concise](https://github.com/phoenixxie/extendedset),
  [JavaEWAH](https://github.com/phoenixxie/javaewah) and
  [Roaring](https://github.com/phoenixxie/RoaringBitmap). The Ant build script
  can download pre-compiled binaries of these libraries.

These dependencies can be automatically downloaded and placed in the
`dep` folder of the project by typing:

    ant download-deps

From the project's root folder, the sources can then be compiled by simply
typing:

    ant

This will produce a file called `ltl-bitmaps.jar` in the folder. This file
is *not* runnable and stand-alone. It is meant to be used in a Java project
alongside `beepbeep-3.jar` and the JAR dependencies for the bitmap
manipulation libraries.

<!-- :maxLineLen=78: -->
