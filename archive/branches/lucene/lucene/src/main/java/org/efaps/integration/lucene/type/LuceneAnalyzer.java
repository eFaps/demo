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

package org.efaps.integration.lucene.type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * Class for Building a <code>lucene.analysis.Analyzer</code>
 * 
 * @author jmo
 * 
 */
public class LuceneAnalyzer {
  /**
   * Logger for this class
   */
  private static final Log LOG = LogFactory.getLog(LuceneAnalyzer.class);

  private String           OID = null;

  private String           ID  = null;

  private Analyzer         ANALYZER;

  private Set              STOPWORDS;

  public LuceneAnalyzer(String _OID) {
    setOID(_OID);
    this.initialise();
  }

  public void initialise() {
    setStopWords();
    setAnalyzer();
  }

  public LuceneAnalyzer() {

  }

  public void setOID(String _OID) {
    OID = _OID;
  }

  public String getOID() {
    return OID;
  }

  public Analyzer getAnalyzer() {
    return ANALYZER;

  }

  public void setID(String _ID) {
    ID = _ID;
  }

  public String getID() {
    if (ID == null) {
      Long id = new Instance(getOID()).getId();
      setID(id.toString());
    }
    return ID;

  }

  public void setStopWords() {

    STOPWORDS = LuceneStopWords.getStopWords(getID());
  }

  public boolean hasStopWords() {
    return !STOPWORDS.isEmpty();

  }

  /**
   * Creates a new Lucene_Analyzer and returns the ID of the new
   * Lucene_Analyzer. If a Lucene_Analyzer with exact the same values exists the
   * ID of the existing one is returned
   * 
   * @param _className
   *          className of the Analyzer
   * 
   * @return ID
   */
  public static String createNew(String _className) {

    return createNew(_className, false);

  }

  /**
   * Creates a new Lucene_Analyzer and returns the ID of the new
   * Lucene_Analyzer. If a Lucene_Analyzer with exact the same values exists the
   * ID of the existing one is returned if not explicitly forced to create a new
   * one
   * 
   * @param _className
   *          className of the Analyzer
   * @param _forceNew
   *          forces to create a new, even if one with the same content is
   *          already existing
   * @return ID
   */
  public static String createNew(String _className, boolean _forceNew) {

    String IndexID = null;
    if (!_forceNew) {
      IndexID = getIDofAnalyzer(_className);
    }
    if (IndexID != null) {
      return IndexID;
    } else {

      return create(_className);
    }

  }

  /**
   * Creates a new Lucene_Analyzer
   * 
   * @param _className
   *          ClassName of the Analyzer
   * @return ID
   */
  private static String create(String _className) {

    try {
      Insert insert = new Insert("Lucene_Analyzer");
      insert.add("Analyzer", _className);
      insert.execute();
      String LuceneAnalyzerID = insert.getId();
      insert.close();
      return LuceneAnalyzerID;
    } catch (EFapsException e) {

      LOG.error("create(String)", e);
    } catch (Exception e) {

      LOG.error("create(String)", e);
    }
    return null;

  }

  /**
   * Searches for an existing Analyzer with the parameter as a filter and
   * returns the ID of the Analyzer
   * 
   * @param _className
   *          ClassName of the Analyzer to search for
   * @return ID if exits, else null
   */
  private static String getIDofAnalyzer(String _className) {
    SearchQuery query = new SearchQuery();
    try {
      query.setQueryTypes("Lucene_Analyzer");
      query.addSelect("ID");
      query.addWhereExprEqValue("Analyzer", _className);
      query.execute();
      if (query.next()) {
        return query.get("ID").toString();
      }

    } catch (EFapsException e) {

      LOG.error("getIDofAnalyzer(String)", e);
    }
    return null;

  }

  private void setAnalyzer() {
    SearchQuery query = new SearchQuery();

    String className = null;

    Class AnalyzerClass = null;
    try {

      query.setObject(getOID());
      query.addSelect("Analyzer");

      query.execute();
      query.next();

      className = (String) query.get("Analyzer");
      AnalyzerClass = Class.forName(className);

      if (hasStopWords()) {

        Constructor constructor = AnalyzerClass
            .getConstructor(new Class[] { Set.class });

        ANALYZER = (Analyzer) constructor
            .newInstance(new Object[] { STOPWORDS });
      } else {
        ANALYZER = (Analyzer) AnalyzerClass.newInstance();
      }

      query.close();
    } catch (ClassNotFoundException e) {
      LOG.error("setAnalyzer()", e);
    } catch (InstantiationException e) {
      LOG.error("setAnalyzer()", e);
    } catch (IllegalAccessException e) {
      LOG.error("setAnalyzer()", e);
    } catch (EFapsException e) {
      LOG.error("setAnalyzer()", e);
    } catch (SecurityException e) {
      LOG.error("setAnalyzer()", e);
    } catch (NoSuchMethodException e) {
      LOG.error("setAnalyzer()", e);
    } catch (IllegalArgumentException e) {
      LOG.error("setAnalyzer()", e);
    } catch (InvocationTargetException e) {
      LOG.error("setAnalyzer()", e);
    }

  }

}
