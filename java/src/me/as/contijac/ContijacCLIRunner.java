/*
 * Copyright 2019 Antonio Sorrentini
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package me.as.contijac;


import me.as.contijac.compiler.Constants;
import me.as.lib.buildtools.io.JavaBundleSpecs;
import me.as.lib.buildtools.io.JavaBundleSpecsCLIHandler;
import me.as.lib.minicli.CLIOption;
import me.as.lib.minicli.CommandLineHandler;
import me.as.lib.minicli.ConfigFileCLIOption;
import me.as.lib.minicli.HelpCLIOption;
import me.as.lib.minicli.NoOperand;
import me.as.lib.minicli.PathHandler;
import me.as.lib.minicli.VersionCLIOption;
import me.as.lib.core.report.Problems;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.as.contijac.compiler.Compiler;

import static me.as.lib.core.log.DefaultTraceLevels.DEFAULT;
import static me.as.lib.core.system.FileSystemExtras.getCanonicalPath;


public class ContijacCLIRunner
{

 @VersionCLIOption NoOperand<String> version=new NoOperand<>(){{customContent=
  Constants.programName+" \""+Constants.version[0]+"."+Constants.version[1]+"."+Constants.version[2]+"\" "+Constants.date;}};

 @HelpCLIOption NoOperand help;

 @ConfigFileCLIOption String configFile; // "./contijac.config.json";

 @CLIOption
 (
  name= "-watch",
  aliases={".--watch"},
  usage="remain active after compilation so to compile again automatically on every source/resource change. Default is true",
  helpOrder=3
 ) boolean watch=true;


 @CLIOption
 (
  name= "-sourcepath",
  aliases={"-sp", "./sp", ".--sp", ".--sourcepath"},
  operand="<path1[;path]>",
  usage="list of directories where java source files are found.\n" +
   "Elements should be separated with ; or, only on unices systems, :.",
  handlerClass=JavaBundleSpecsCLIHandler.class,
  required=true,
  helpOrder=4
 ) List<JavaBundleSpecs> sourcepath=new ArrayList<>(Collections.singletonList(new JavaBundleSpecs(".")));


 @CLIOption
 (
  name="-classpath",
  aliases={"-cp", "./cp", ".--cp", ".--classpath"},
  operand="<path1[;path]>",
  usage="list of directories and jar and zip files where classes used by the sources are found.\n" +
     "Elements should be separated with ; or, only on unices systems, :.",
  handlerClass=JavaBundleSpecsCLIHandler.class,
  helpOrder=5
 ) List<JavaBundleSpecs> classpath;


 @CLIOption
 (
  name= "-output-root",
  aliases={".--output-root"},
  operand="<path>",
  usage="path to the root directory where to save compiled classes and resource files.",
  handlerClass=PathHandler.class,
  required=true,
  helpOrder=8
 ) String outputRoot=".\\_out";


 @CLIOption
 (
  name= "-verbosity",
  aliases={".--verbosity"},
  operand="<COMMA_SEPARATED_LEVELS>",
  usage="Verbosity levels. Possible values are:\n" +
   "    *           - every message is printed out\n"+
   "    OFF         - no message is printed out\n"+
   "    SEVERE      - SEVERE messages are printed out\n"+
   "    FATAL_ERROR - FATAL_ERROR messages are printed out\n"+
   "    ERROR       - ERROR messages are printed out\n"+
   "    WARNING     - WARNING messages are printed out\n"+
   "    DEBUG       - DEBUG messages are printed out\n"+
   "    INFO        - INFO messages are printed out\n"+
   "    CONFIG      - CONFIG messages are printed out\n"+
   "    FINE        - FINE messages are printed out\n"+
   "    FINER       - FINER messages are printed out\n"+
   "    FINEST      - FINEST messages are printed out\n"+
   "Defaults is FINE,WARNING,ERROR,FATAL_ERROR.",
  helpOrder=11
 ) String verbosity=DEFAULT;


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 CommandLineHandler commandLineHandler;

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 private void start(Problems problems)
 {
  Compiler compiler=new Compiler().
    setConfigFile(configFile).
    setWatch(watch).
    setClasspath(classpath).
    setSourcepath(sourcepath).
    setOutputRoot(outputRoot).
    setVerbosity(verbosity).
    setWorkingDirectory(getCanonicalPath(".")).
    setCommandLineHandler(commandLineHandler);

  compiler.run(problems);
 }


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 public static void main(String args[])
 {
  Problems problems=new Problems();
  ContijacCLIRunner runner=CommandLineHandler.prepare(ContijacCLIRunner.class, args, problems);
  if (runner!=null) runner.start(problems);
  problems.printIfTheCase();
 }


}
