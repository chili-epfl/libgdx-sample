libgdx-sample
=============

This is a sample libgdx project to get familiar with libgdx integration with
Chilitags, camera etc. It should display a growing and swaying funky palm tree
on each detected tag. 

The build system uses Gradle extensively, which can be thought of a CMake 
counterpart for Java. 

Below are the official libgdx documentation for Gradle:

- [How to create a new libgdx project with Gradle](
https://github.com/libgdx/libgdx/wiki/Project-Setup-Gradle)
- [Gradle-enabled libgdx project structure details](
https://github.com/libgdx/libgdx/wiki/Dependency-management-with-Gradle)
- [Gradle guide for command line](
https://github.com/libgdx/libgdx/wiki/Gradle-on-the-Commandline)
- [IDE setup for Gradle](
https://github.com/libgdx/libgdx/wiki/Setting-up-your-Development-Environment-%28Eclipse%2C-Intellij-IDEA%2C-NetBeans%29)

Project structure
-----------------

libgdx projects come in the following directory structure:

```
├── android/
│   ├── AndroidManifest.xml
│   ├── assets
│   ├── build.gradle
│   ├── proguard-project.txt
│   ├── project.properties
│   └── src/
├── build.gradle
├── core/
│   ├── build.gradle
│   └── src/
├── desktop/
│   ├── build.gradle
│   └── src/
├── gradle/
├── gradlew
├── gradlew.bat
├── html/
│   ├── build.gradle
│   ├── src/
│   └── webapp/
├── ios/
│   ├── build.gradle
│   ├── Info.plist.xml
│   ├── robovm.properties
│   ├── robovm.xml
│   └── src/
└── settings.gradle
```

- The `core` directory contains the actual application that is platform 
independent.
- The `desktop`,`android`, `html` and `ios` directories contains the small
platform dependent portions of the project. With any luck, you won't even have
to touch them.
- The `build.gradle` in the root directory has all of the library dependencies
of your project, including the platform specific dependencies. Most of these
dependencies are automatically installed by Maven when you build your project.
This file also contains general information about your project.
- The `build.gradle` files in the platform specific directories have platform
specific configurations such as small run and debug scripts. 
- The `android/assets/` directory contains all the assets for the project like
3D models or sprites. It is under the `android` directory because of technical 
reasons and is currently being worked on.
- libgdx projects come with `gradlew` and `gradlew.bat` scripts that can run in
Linux/MacOS and Windows respectively. You can use these to build/run your
projects in the command line. Alternatively, you can use one of Eclipse, 
Netbeans or Intellij IDEA.

How to Build & Run
------------------

Currently, only the desktop and Android versions of this sample are in working
condition. Web version needs proper javascript bindings for Chilitags and iOS
version might be available at a (much) later date.

1. Clone this sample: `git clone git@github.com:chili-epfl/libgdx-sample.git`

2. Follow the instructions at 
https://github.com/chili-epfl/chilitags/blob/master/README-ANDROID.md to build
and install Chilitags/OpenCV inside the Android specific portion of the 
project. That guide will work you through installing the Android SDK and NDK. 
In this case, the `ANDROID_PROJECT_ROOT` variable inside the guide would be 
`<libgdx-sample-path>/android`. 

3. It is necessary at this point to install Chilitags inside the Android project
even if you are only going to run the desktop version, since the JNI wrappers
inside the Android project are also referenced by the desktop project. In the
near future, this is going to be replaced by proper libraries that will be
automatically downloaded from Maven repositories. 

4. Set the `ANDROID_HOME` environment variable to the Android SDK root
if you don't have it already. 

Once you install Chilitags/OpenCV inside the sample project, you should be able
to run everything. Follow the instructions below for different build/run
options.

### Using the command line

Go to the project's root directory and run the following:

- For desktop, run `./gradlew desktop:run`. Since we are using JNI to reference
native Chilitags and OpenCV libraries, make sure that you have them installed
on your system as dynamic libraries and the `LD_LIBRARY_PATH` environment 
variable contains their paths, e.g 
`/usr/lib/:/usr/lib32/:/usr/lib64/:/usr/local/lib/:/usr/local/lib32/:/usr/local/lib64/`
- For Android, run `./gradlew android:installDebug android:run`. This will
package the project in an APK, upload it to the first device/emulator and run
it. 
- For web, run `./gradlew html:superDev`. This will compile the Java code to
Javascript using Google Web Toolkit and launch it in superDev mode, which will
enable you to debug it. Go to 
[http://localhost:8080/gwt](http://localhost:8080/gwt) to view the project. 
This will of course work after we have proper javascript bindings for
Chilitags.

### Using Eclipse

To import the project:

- Install the ADT plugin from this update site: 
[https://dl-ssl.google.com/android/eclipse/](
https://dl-ssl.google.com/android/eclipse/). 
- Install Gradle integration plugin for Eclipse from this update site 
[http://dist.springsource.com/release/TOOLS/gradle](
http://dist.springsource.com/release/TOOLS/gradle). 
- Import the root directory of your project as a `Gradle Project`. 
- Click `Build Model`.
- Select all projects (including the root one) and click `Finish`. 

To run the project:

- For desktop, run as `Java Application` and select `Main.java` in the
`ch.epfl.chili.libgdx_sample.desktop` package.
- For Android, run as `Android Application`.
- For web, configure an external `Program` via `External Tools Configuration`.
Set `Location` to the `gradlew` in your project root. Set the `Working
Directory` to your project root. Set the Argument to `html:superDev`. Now you
can use this external tool to launch the project in superDev mode just like
the command line. 

For other IDEs, please see the official libgdx documentation. 
