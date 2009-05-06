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

package org.efaps.integration.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.digester.Digester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.efaps.admin.datamodel.Type;
import org.efaps.integration.lucene.type.LuceneAnalyzer;
import org.efaps.integration.lucene.type.LuceneAttribute2Field;
import org.efaps.integration.lucene.type.LuceneField;
import org.efaps.integration.lucene.type.LuceneIndex;
import org.efaps.integration.lucene.type.LuceneIndex2Type;
import org.efaps.integration.lucene.type.LuceneIndexer;
import org.efaps.integration.lucene.type.LuceneStopWords;
import org.efaps.integration.lucene.type.LuceneType2Analyzer;
import org.efaps.integration.lucene.type.LuceneType2Indexer;
import org.xml.sax.SAXException;

/**
 * Class creates a new Lucene_Index with all necesarry informations delifered
 * from a XML-File
 * 
 * @author jmo
 * 
 */
public class CreateLuceneIndex {
  /**
   * Logger for this class
   */
  private static final Log    LOG              = LogFactory
                                                   .getLog(CreateLuceneIndex.class);

  private static String       ID_LUCENEANALYZER;

  private static List<String> ID_LUCENEINDEXER = new ArrayList<String>();

  private static String       ID_LUCENEINDEX;

  private static Type         TYPE;

  private static String       ID_INDEX2TYP;

  /**
   * Starts the creation of a new Lucene_Index
   * 
   * @param _XMLName
   *          Path to the XML-File
   * @param prozess
   */
  public static void create(String _XMLName) {
    readXML(_XMLName);

    createType2Indexer();

    createType2Analyzer();

  }

  private static void createType2Analyzer() {
    LuceneType2Analyzer.createNew(ID_LUCENEANALYZER, getIndex2TypeID());
  }

  /**
   * Reads the XML-File with Digester
   * 
   * @param _XMLName
   *          Path to the XML-File
   */
  private static void readXML(String _XMLName) {
    File indexxml = new File(_XMLName);

    Digester digester = new Digester();
    // TODO dtd schreiben
    digester.setValidating(false);

    digester.addObjectCreate("lucene-index", CreateLuceneIndex.class);

    digester.addCallMethod("lucene-index/index", "createLuceneIndex", 2);
    digester.addCallParam("lucene-index/index", 0, "path");
    digester.addCallParam("lucene-index/index", 1, "name");

    digester.addCallMethod("lucene-index/lucene-analyzer/analyzer",
        "createLuceneAnalyzer", 2, new Class[] { String.class,Boolean.class });
    digester.addCallParam("lucene-index/lucene-analyzer/analyzer", 0,"class");
    digester.addCallParam("lucene-index/lucene-analyzer/analyzer", 1,"forcenew");
    
    digester.addCallMethod("lucene-index/lucene-analyzer/stopword",
        "createStopword", 0);

    digester.addCallMethod("lucene-index/lucene-indexer/indexer",
        "createLuceneIndexer", 2);
    digester.addCallParam("lucene-index/lucene-indexer/indexer", 0, "filetyp");
    digester.addCallParam("lucene-index/lucene-indexer/indexer", 1, "indexer");

    digester.addCallMethod("lucene-index/type/name", "setType", 0);

    digester.addCallMethod("lucene-index/type/attribute",
        "createAttribute2Field", 5);
    digester.addCallParam("lucene-index/type/attribute", 0, "name");
    digester.addCallParam("lucene-index/type/attribute/lucene-field", 1,
        "class");
    digester.addCallParam("lucene-index/type/attribute/lucene-field", 2,
        "method");
    digester.addCallParam("lucene-index/type/attribute/lucene-field", 3,
        "store");
    digester.addCallParam("lucene-index/type/attribute/lucene-field", 4,
        "index");

    try {
      digester.parse(indexxml);

    } catch (IOException e) {
      LOG.error("readXML(String) - can't read XML-File", e);
    } catch (SAXException e) {
      LOG.error("readXML(String) - invalide XML-File", e);
    }

  }

  /**
   * creates Lucene_Type2Indexer
   */
  private static void createType2Indexer() {
    for (String object : ID_LUCENEINDEXER) {
      LuceneType2Indexer.createNew(getIndex2TypeID(), object);

    }

  }

  /**
   * creates Lucene Attribute2Field
   * 
   * @param _name
   *          of the Attribute
   * @param _class
   *          Name of the Class
   * @param _method
   *          Name of the Method
   * @param _store
   *          How to store this Field
   * @param _index
   *          how to index this Field
   * @see #{@link getLuceneFieldID()}
   */
  public void createAttribute2Field(String _name, String _class,
      String _method, String _store, String _index) {

    LuceneAttribute2Field.createNew(getIndex2TypeID(), getAttributeID(_name),
        getLuceneFieldID(_class, _method, _store, _index));

  }

  /**
   * gets the ID of an Lucene_Field
   * 
   * @param _class
   *          Name of the Class
   * @param _method
   *          Name of the Method
   * @param _store
   *          How to store this Field
   * @param _index
   *          how to index this Field
   * @return ID
   */
  private String getLuceneFieldID(String _class, String _method, String _store,
      String _index) {

    return LuceneField.createNew(_class, _method, _store, _index);
  }

  /**
   * get the ID of an Attribute
   * 
   * @param _name
   *          Name of the Attribute
   * @return ID
   */
  private String getAttributeID(String _name) {
    Long Id = TYPE.getAttribute(_name).getId();
    return Id.toString();
  }

  /**
   * get the ID of a Lucene_Index2Type
   * 
   * @return ID
   */
  private static String getIndex2TypeID() {
    if (ID_INDEX2TYP == null) {
      createIndex2Type();
    }
    return ID_INDEX2TYP;
  }

  /**
   * create a new Lucene_Index2Typ
   */
  private static void createIndex2Type() {
    ID_INDEX2TYP = LuceneIndex2Type.createNew(ID_LUCENEINDEX, getTypeID());
  }

  /**
   * get the Id of the Type
   * 
   * @return ID
   */
  private static String getTypeID() {
    Long Id = TYPE.getId();
    return Id.toString();
  }

  /**
   * set the Type for the Index
   * 
   * @param _name
   *          Name of the Type
   */
  public void setType(String _name) {
    TYPE = Type.get(_name);
  }

  /**
   * create a new Lucene_Index
   * 
   * @param _path
   *          Path to the Folder wich is going to contain the index
   * @param _name
   *          Name of the Index
   */
  public void createLuceneIndex(String _path, String _name) {
    ID_LUCENEINDEX = LuceneIndex.createNew(_path, _name);
  }

  /**
   * creates a new Lucene_Indexer
   * 
   * @param _FileTyp
   *          FileTyp to index
   * @param _Indexer
   *          className of the Indexer
   */
  public void createLuceneIndexer(String _FileTyp, String _Indexer) {
    ID_LUCENEINDEXER.add(LuceneIndexer.createNew(_FileTyp, _Indexer));

  }

  public void createLuceneIndexer(String _FileTyp, String _Indexer,
      boolean _forceNew) {
    ID_LUCENEINDEXER
        .add(LuceneIndexer.createNew(_FileTyp, _Indexer, _forceNew));

  }

  /**
   * create a new Lucene_Analyzer
   * 
   * @param _Analyzer
   *          className of the Analyzer
   */
  public void createLuceneAnalyzer(String _Analyzer,
      boolean _forceNew) {
    ID_LUCENEANALYZER = LuceneAnalyzer.createNew(_Analyzer,_forceNew);
  }

  /**
   * create a new StopWord
   * 
   * @param _StopWord
   */
  public void createStopword(String _StopWord) {
    LuceneStopWords.createNew(ID_LUCENEANALYZER, _StopWord);
  }
}
