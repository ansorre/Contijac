# Contijac

Contijac is a continuous build tool for Java develoment. It is very small and far from a complete continuous integration and delivery system. Contijac just builds Java sources on every detection of changes in your files.
  

## Key points

 * small 
 * CLI tool
 * focused on one single task: compiles java sources and resources  
 * uses JDK internal Java compiler  
 * optionally runnable with a configuration file in json format
 * clean source code:
     * use it to learn how to make programmatic uses of the internal Java compiler
     * use it as part of a more complex self-made building solution         
 * Apache 2.0 license
 
## Notes & limits
 
 * Contijac doesn't delete files from the compilation destination folder, so for 
 example if you delete a Java source the relative compiled class/es won't be removed, 
 and the same applies to resource files. This also means that renaming Java sources and 
 resources does not remove the old-named corrispective files, just adds the new ones. Pull 
 requests are welcome, even those made to remove this limit.      
 * this is a recent tool, I use it in production but proceed with caution to test if it's 
 really suitable for your use case.
 
## Usage 

A precompiled (and packaged with all the dependencies) contijac.jar binary is present 
in the directory **release**. Just execute:  

    java -jar contijac.jar --help
and instructions on how to use the program will be displayed. 
 

## How to build

To build Contijac download the sources from this repository and also the sources 
of the only three dependecies:
* [As-Libs-Core](https://github.com/ansorre/As-Libs-Core)
* [miniCLI](https://github.com/ansorre/miniCLI) 
* [as-libs-buildtools](https://github.com/ansorre/as-libs-buildtools)
   
 
## Quick links

 * [Github project](https://github.com/ansorre/Contijac)
