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

import java.util.HashSet;
import java.util.Set;

import org.efaps.db.Insert;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * Class for access to the StopWordList for an Analyzer, this Class is not for
 * all Analyzers needed but the use of Stopworlists is recomende if applicable
 * 
 * @author jmo
 * 
 */
public class LuceneStopWords {
  /**
   * Logger for this class
   */
  private static final Log LOG = LogFactory.getLog(LuceneStopWords.class);

  /**
   * Reads all StopWords which are linked to an <b>Lucene_Analyzer</b> from the
   * Database and returns a Set of Stopwords
   * 
   * @param _AnalyzerID
   *          the ID of an Lucene_Analyzer
   * @return Set of Stopwords
   */
  public static Set getStopWords(String _AnalyzerID) {
    Set<String> StopWords = new HashSet<String>();
    SearchQuery query = new SearchQuery();
    try {
      query.setQueryTypes("Lucene_StopWords");
      query.addSelect("Word");
      query.addWhereExprEqValue("Analyzer", _AnalyzerID);
      query.execute();
      while (query.next()) {
        StopWords.add(query.get("Word").toString());
      }
      return StopWords;
    } catch (EFapsException e) {
      LOG.error("getStopWords(String)", e);
    }

    return null;

  }

  /**
   * Inserts a new StopWord into the Database, linked to an existing
   * <b>Lucene_Analyzer</b>
   * 
   * @param _AnalyzerID
   *          ID of an Lucene_Analyzer
   * @param _Word
   *          Word to insert
   * @return ID of the Lucen_StopWord if successfull otherwise null
   */
  private static String create(String _AnalyzerID, String _Word) {
    try {
      Insert insert = new Insert("Lucene_StopWords");
      insert.add("Word", _Word);
      insert.add("Analyzer", _AnalyzerID);
      insert.execute();
      String LucenStopWordID = insert.getId();
      insert.close();
      return LucenStopWordID;
    } catch (EFapsException e) {
      LOG.error("insert(String, String)", e);
    } catch (Exception e) {
      LOG.error("insert(String, String)", e);
    }
    return null;

  }

  /**
   * Creates a new Lucene_StopWord and returns the ID of the new
   * Lucene_StopWord. If a Lucene_StopWord with exact the same values exists the
   * ID of the existing one is returned if not explicitly forced to create a new
   * one
   * 
   * @param _AnalyzerID
   *          ID of the LuceneAnalyzer
   * @param _Word
   *          Word to insert
   * 
   * @return ID
   * @see createNew(String _FileTyp, String _Indexer,boolean _forceNew)
   */
  public static String createNew(String _AnalyzerID, String _Word) {
    return createNew(_AnalyzerID, _Word, false);

  }

  /**
   * Creates a new Lucene_StopWord and returns the ID of the new
   * Lucene_StopWord. If a Lucene_StopWord with exact the same values exists the
   * ID of the existing one is returned if not explicitly forced to create a new
   * one
   * 
   * @param _AnalyzerID
   *          ID of the LuceneAnalyzer
   * @param _Word
   *          Word to insert
   * @param _forceNew
   *          forces to create a new, even if one with the same content is
   *          already existing
   * @return ID
   * @see createNew(String _FileTyp, String _Indexer)
   */
  public static String createNew(String _AnalyzerID, String _Word,
      Boolean _forceNew) {

    String IndexID = null;
    if (!_forceNew) {
      IndexID = getIDofExistingStopWord(_AnalyzerID, _Word);
    }
    if (IndexID != null) {
      return IndexID;
    } else {

      return create(_AnalyzerID, _Word);
    }

  }

  /**
   * Searches for an existing Stopword with the parameters as a filter and
   * returns the ID of the StopWord
   * 
   * @param _AnalyzerID
   *          ID of a Lucene_Analyzer
   * @param _Word
   *          StopWord
   * @return ID if exits, else null
   */
  private static String getIDofExistingStopWord(String _AnalyzerID, String _Word) {
    SearchQuery query = new SearchQuery();
    try {
      query.setQueryTypes("Lucene_StopWords");
      query.addSelect("ID");
      query.addWhereExprEqValue("Word", _Word);
      query.addWhereExprEqValue("Analyzer", _AnalyzerID);
      query.execute();
      if (query.next()) {
        return query.get("ID").toString();
      }
    } catch (EFapsException e) {

      LOG.error("getIDofExistingStopWord(String, String)", e);
    }

    return null;
  }

}
