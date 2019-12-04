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

package me.as.contijac.compiler;


import me.as.lib.buildtools.Watcher;
import me.as.lib.buildtools.engine.CustomJavaFileManager;
import me.as.lib.buildtools.engine.CustomJavaFileObject;
import me.as.lib.buildtools.engine.CustomWriter;
import me.as.lib.buildtools.io.JavaBundleSpecs;
import me.as.lib.buildtools.tool.AbstractBuildTool;
import me.as.lib.core.StillUnimplemented;
import me.as.lib.core.extra.Box;
import me.as.lib.core.extra.BoxFor2;
import me.as.lib.core.extra.BoxFor3;
import me.as.lib.core.extra.TimeCounter;
import me.as.lib.core.lang.ArrayExtras;
import me.as.lib.core.report.Problems;
import me.as.lib.core.system.FileInfo;
import me.as.lib.core.system.FileInfo.Type;
import me.as.lib.core.system.FileSystemExtras;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.FileObject;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import static me.as.contijac.compiler.BytecodeOfCompiledClass.getClassFilePath;
import static me.as.lib.core.lang.StringExtras.replace;
import static me.as.lib.core.log.DefaultTraceLevels.INFO;
import static me.as.lib.core.log.LogEngine.logOut;
import static me.as.lib.core.system.FileSystemExtras.grantDirectory;
import static me.as.lib.core.system.FileSystemExtras.isDirectory;
import static me.as.lib.core.system.FileSystemExtras.isFile;
import static me.as.lib.core.system.FileSystemExtras.lastModified;
import static me.as.lib.core.system.FileSystemExtras.listTheTree;


public class Compiler extends AbstractBuildTool<Compiler> implements DiagnosticListener<JavaFileObject>
{


 private boolean watch;

 private List<JavaBundleSpecs> classpath;

 private List<JavaBundleSpecs> sourcepath;

 String outputRoot;


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 // runtimes

 private String projectBuildDir;
// private SourceManager sourceManager;
 private Watcher watcher;

 JavaCompiler compiler;
 CustomJavaFileManager<FileInfo> fileManager;

 String sourcesRoot;
 final HashMap<String, FileInfo> cachedSources=new HashMap<>();
 List<FileInfo> sources;



 public Compiler()
 {

 }




 public String getDefaultConfigFile()
 {
  return "contijac.config.json";
 }



 protected void adjustParameters()
 {
  super.adjustParameters();


  // outputRoot
  outputRoot=getCanonicalPathForWorkingDirectory(outputRoot, "_out");

  if (!grantDirectory(outputRoot))
  {
   invalidDir("outputRoot", outputRoot);
   return;
  }

  // sourcepath
  adjustJavaBundleSpecs(sourcepath);

  // classpath
  adjustJavaBundleSpecs(classpath);

  // watch
  watcher=watch ? new Watcher() : null;
 }



 private void setupClassPath()
 {
  final List<File> jarsAndDirs=new ArrayList<>();
  jarsAndDirs.add(new File(outputRoot));

  if (ArrayExtras.length(classpath)>0)
   classpath.forEach(b -> jarsAndDirs.add(new File(b.path)));

  fileManager.setLocation(StandardLocation.CLASS_PATH, jarsAndDirs);
 }




 private void setupSources()
 {
  sources=new ArrayList<>();

  final Box<JavaBundleSpecs> current=new Box<>();
  int t, len=ArrayExtras.length(sourcepath);

  for (t=0;t<len;t++)
  {
   if (t>0)
    throw new StillUnimplemented();

   JavaBundleSpecs bundleSpecs=current.element=sourcepath.get(t);

   sourcesRoot=bundleSpecs.path;

   if (!isDirectory(sourcesRoot))
    throw new StillUnimplemented();
   else
   {
    listTheTree(sourcesRoot,
    b3 ->
    {
     String filePath=b3.element1;
     BasicFileAttributes atts=b3.element2;

     if (isFile(filePath))
     {
      FileInfo fi=new FileInfo();
      fi.type=Type.file;
      fi.relativePath=filePath;
      fi.lastModified=atts.lastModifiedTime().toMillis();

      FileInfo cFi=cachedSources.get(filePath);

      if (cFi==null)
      {
       if (filePath.endsWith(".java"))
       {
        String className=replace(filePath.substring(sourcesRoot.length()+1, filePath.length()-5), File.separator, ".");
        String cfp=getClassFilePath(Compiler.this, className);

        if (isFile(cfp))
        {
         cFi=new FileInfo();
         cFi.type=Type.file;
         cFi.relativePath=filePath;
         cFi.lastModified=lastModified(cfp);
         cachedSources.put(filePath, cFi);
        }
       }
      }

      boolean doAdd=(cFi==null || fi.lastModified>cFi.lastModified);

      if (doAdd)
      {
       cachedSources.put(filePath, fi);
       sources.add(fi);
      }
     }
     else
     {
      watcher.addToWatching(filePath, current.element);
     }
    });
   }
  }

 }


 private void go()
 {
  BoxFor3<List<CustomJavaFileObject>, List<FileInfo>, HashMap<String, CustomJavaFileObject>> sourcesAndResourcesAndMore=
   fileManager.getSourcesAndResources(sources);

  List<CustomJavaFileObject> jfoSources=sourcesAndResourcesAndMore.element1;
  List<FileInfo> resources=sourcesAndResourcesAndMore.element2;

  if (ArrayExtras.length(jfoSources)>0)
  {
   CompilationTask task=compiler.getTask(new CustomWriter(), fileManager, this, null, null, jfoSources);
   task.call();
  }

  int len=ArrayExtras.length(resources);
  int srl=sourcesRoot.length();

  if (len>0)
  {
   for (int t=0;t<len;t++)
   {
    FileInfo rf=resources.get(t);

    byte resourceContent[]=FileSystemExtras.loadFromFile(rf.relativePath);
    StringBuilder sb=new StringBuilder(outputRoot);
    sb.append(File.separator).append(rf.relativePath.substring(srl));
    String dest=FileSystemExtras.adjustPath(sb.toString());
    FileSystemExtras.saveInFile(dest, resourceContent);
   }
  }
 }


 public void report(Diagnostic<? extends JavaFileObject> diagnostic)
 {
  logOut.println(diagnostic.toString());
 }


 public void run(Problems problems)
 {
  this.problems=problems;
  adjustParameters();

  if (!problems.areThereShowStoppers())
  {
   boolean anotherRound=true;
   compiler=ToolProvider.getSystemJavaCompiler();
   fileManager=new CustomJavaFileManager<>(compiler.getStandardFileManager(null, null, null))
   {
    protected BoxFor2<File, String> getFileAndWholePath(FileInfo fi)
    {
     return new BoxFor2<>(new File(fi.relativePath), fi.relativePath);
    }

    public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException
    {
     try
     {
      return new BytecodeOfCompiledClass(Compiler.this, className);
     }
     catch (Exception e)
     {
      throw new RuntimeException("Error while creating in-memory output file for "+className, e);
     }
    }
   };

   while (anotherRound)
   {
    TimeCounter tc=TimeCounter.start();

    setupClassPath();
    setupSources();
    go();

    logOut.println(INFO, INFO+": elapsed "+tc.stopAndGetElapsedString());

    if (watcher!=null)
     watcher.waitChanges();
    else
     anotherRound=false;
   }
  }
 }


 public String getConfigFile()
 {
  return configFile;
 }



 public boolean isWatch()
 {
  return watch;
 }

 public Compiler setWatch(boolean watch)
 {
  this.watch=watch;
  return this;
 }

 public List<JavaBundleSpecs> getClasspath()
 {
  return classpath;
 }

 public Compiler setClasspath(List<JavaBundleSpecs> classpath)
 {
  this.classpath=classpath;
  return this;
 }

 public List<JavaBundleSpecs> getSourcepath()
 {
  return sourcepath;
 }

 public Compiler setSourcepath(List<JavaBundleSpecs> sourcepath)
 {
  this.sourcepath=sourcepath;
  return this;
 }


 public String getOutputRoot()
 {
  return outputRoot;
 }

 public Compiler setOutputRoot(String outputRoot)
 {
  this.outputRoot=outputRoot;
  return this;
 }


}
