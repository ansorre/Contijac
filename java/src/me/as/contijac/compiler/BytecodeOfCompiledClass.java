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


import javax.tools.SimpleJavaFileObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;

import static me.as.lib.core.system.FileSystemExtras.deleteFile;
import static me.as.lib.core.system.FileSystemExtras.mkdirs;


public class BytecodeOfCompiledClass extends SimpleJavaFileObject
{
 private FileOutputStream fos;


 public BytecodeOfCompiledClass(Compiler owner, String className) throws Exception
 {
  super(new URI(className), Kind.CLASS);

  String classFilePath=getClassFilePath(owner, className);
  String dir=classFilePath.substring(0, classFilePath.lastIndexOf(File.separator));

  File f=new File(dir);
  if (!f.isDirectory()) mkdirs(dir);

  deleteFile(classFilePath);
  fos=new FileOutputStream(new File(classFilePath));
 }


 @Override
 public OutputStream openOutputStream() throws IOException
 {
  return fos;
 }


 public static String getClassFilePath(Compiler owner, String className)
 {
  String pNc[]=className.split("\\.");
  final StringBuilder sb=new StringBuilder(owner.outputRoot);
  Arrays.asList(pNc).forEach(s -> sb.append(File.separator).append(s));
  sb.append(".class");
  return sb.toString();
 }


}
