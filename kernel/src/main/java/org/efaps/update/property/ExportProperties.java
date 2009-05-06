/*
 * Copyright 2003 - 2007 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.update.property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * This class exports the Properties from eFaps into a XML-Properties File, a
 * XML-Properties File sorted by key or a Properties-File
 * 
 * @author jmo
 * 
 */
public class ExportProperties {
  /**
   * Logger for this class
   */
  private static final Log               LOG     = LogFactory
                                                     .getLog(ExportProperties.class);

  /**
   * TreeMap is used to sort the key-value-Relation for the export
   */
  private static TreeMap<String, String> TM      = new TreeMap<String, String>();

  private static List<String>            KEYLIST = new ArrayList<String>();

  /**
   * get the Properties from the Database
   */
  private static void getProperties() {
    SearchQuery query = new SearchQuery();

    String key;
    try {
      query.setQueryTypes("Admin_Properties");
      query.addSelect("Key");
      query.addSelect("Default");

      query.execute();
      while (query.next()) {
        key = query.get("Key").toString();
        TM.put(key, query.get("Default").toString());
        KEYLIST.add(key);
      }

    } catch (EFapsException e) {

      LOG.error("setProperties()", e);
    }
  }

  /**
   * Exports the Properties from the Database into an XML-File sorted by the key
   * 
   * @param _filename
   *          Complete Path/Name of the File to import
   */
  public static void exportToXMLsorted(String _filename) {

    getProperties();
    Collections.sort(KEYLIST);

    BufferedOutputStream x;
    try {
      x = new BufferedOutputStream(new FileOutputStream(_filename));

      BufferedOutputStream bos = new BufferedOutputStream(x);
      OutputStreamWriter osw = new OutputStreamWriter(bos, "UTF-8");
      BufferedWriter bw = new BufferedWriter(osw);

      bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      bw.newLine();
      bw
          .write("<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">");
      bw.newLine();
      bw.write("<properties>");
      bw.newLine();
      bw.write("  <comment>eFaps sorted XML-OUPUT</comment>");
      bw.newLine();

      ListIterator<String> y = KEYLIST.listIterator();
      while (y.hasNext()) {
        String element = (String) y.next();

        bw.write("  <entry key=\"");
        bw.write(element);

        bw.write("\">");
        bw.write(replaceTags(TM.get(element)));
        bw.write("</entry>");
        bw.newLine();
      }

      bw.write("</properties>");

      bw.flush();
      bw.close();
    } catch (FileNotFoundException e) {

      LOG.error("exportToXMLsorted(String)", e);
    } catch (IOException e) {

      LOG.error("exportToXMLsorted(String)", e);
    }

  }

  /**
   * replace Tags
   * 
   * @param _String
   * @return
   */
  private static String replaceTags(String _String) {
    _String = replaceTag(_String, "<", "&lt;");
    _String = replaceTag(_String, ">", "&gt;");
    _String = replaceTag(_String, "&", "&amp;");
    return _String;

  }

  private static String replaceTag(String _String, String _target,
      String _replacement) {
    return _String.replace(_target, _replacement);
  }

  /**
   * Exports the Properties from the Database into an XML-File
   * 
   * @param _filename
   *          Complete Path/Name of the File to import
   */
  public static void exportToXML(String _filename) {

    try {
      copyToProperties().storeToXML(new FileOutputStream(_filename),
          "eFaps unsorted XML-OUPUT");
    } catch (IOException e) {

      LOG.error("exportToXML(String)", e);
    }

  }

  /**
   * Exports the Properties from the Database into an Properties-File
   * 
   * @param _filename
   *          Complete Path/Name of the File to import
   */
  public static void exportToProperties(String _filename) {
    try {
      copyToProperties().store(new FileOutputStream(_filename),
          "eFaps unsorted Properties");
    } catch (IOException e) {

      LOG.error("exportToXML(String)", e);
    }
  }

  /**
   * copy from Map to Properties
   * 
   * @return Properties
   */
  private static Properties copyToProperties() {
    getProperties();

    Properties prop = new Properties();
    Iterator it = TM.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry me = (Map.Entry) it.next();

      prop.setProperty(me.getKey().toString(), me.getValue().toString());

    }
    return prop;
  }
}
