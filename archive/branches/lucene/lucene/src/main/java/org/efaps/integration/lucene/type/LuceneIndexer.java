/*
 * Copyright 2007 The eFaps Team
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
 * Author:          jmo
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.integration.lucene.type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Insert;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * Class for Lucene_Indexer
 * 
 * @author jmo
 * 
 */
public class LuceneIndexer {
  /**
   * Logger for this class
   */
  private static final Log LOG = LogFactory.getLog(LuceneIndexer.class);

  private String           ID;

  private String           OID;

  private Class<?>         INDEXER;

  private String           FILETYP;

  public LuceneIndexer(String _ID) {
    setID(_ID);
    Long i = Type.get("Lucene_Indexer").getId();
    setOID(i.toString() + "." + getID());
    initialise();

  }

  private void initialise() {

    SearchQuery query = new SearchQuery();

    try {
      query.setObject(getOID());
      query.addSelect("FileTyp");
      query.addSelect("Indexer");
      query.execute();
      if (query.next()) {
        INDEXER = Class.forName(query.get("Indexer").toString());
        FILETYP = query.get("FileTyp").toString();
      }

    } catch (EFapsException e) {
      
      LOG.error("initialise()", e);
    } catch (ClassNotFoundException e) {
      
      LOG.error("initialise()", e);
    }

  }

  public String getOID() {
    return OID;
  }

  public void setOID(String _OID) {
    OID = _OID;
  }

  public void setID(String _ID) {
    ID = _ID;
  }

  public String getID() {
    return ID;
  }

  public String getFileTyp() {
    return FILETYP;
  }

  public Class<?> getIndexer() {
    return INDEXER;
  }

  /**
   * Creates a new Lucene_Indexer and returns the ID of the new Indexer. If a
   * Indexer with exact the same values exists the ID of the existing one is
   * returned
   * 
   * @param _FileTyp
   *          FileTyp we want to index (e.g. "txt",xls")
   * @param _Indexer
   *          ClassName of the indexer
   * @return ID
   * @see createNew(String _FileTyp, String _Indexer, boolean _forceNew)
   */
  public static String createNew(String _FileTyp, String _Indexer) {
    return createNew(_FileTyp, _Indexer, false);

  }

  /**
   * Creates a new Lucene_Indexer and returns the ID of the new Indexer. If a
   * Indexer with exact the same values exists the ID of the existing one is
   * returned if not explicitly forced to create a new one
   * 
   * @param _FileTyp
   *          FileTyp we want to index (e.g. "txt",xls")
   * @param _Indexer
   *          ClassName of the indexerLuceneIndexer
   * @param _forceNew
   *          forces to create a new, even if one with the same content is
   *          already existing
   * @return ID
   * @see createNew(String _FileTyp, String _Indexer)
   */
  public static String createNew(String _FileTyp, String _Indexer,
      boolean _forceNew) {
    String IndexID = null;
    if (!_forceNew) {
      IndexID = getIDofExistingIndexer(_FileTyp, _Indexer);
    }
    if (IndexID != null) {
      return IndexID;
    } else {

      return create(_FileTyp, _Indexer);
    }
  }

  /**
   * Creates a new Lucene_Indexer and returns the ID of the new Indexer.
   * 
   * @param _FileTyp
   *          FileTyp we want to index (e.g. "txt",xls")
   * @param _Indexer
   *          ClassName of the indexerLuceneIndexer
   * 
   * @return ID
   */
  private static String create(String _FileTyp, String _Indexer) {
    String IndexID;
    Insert insert;
    try {
      insert = new Insert("Lucene_Indexer");
      insert.add("FileTyp", _FileTyp);
      insert.add("Indexer", _Indexer);

      insert.execute();
      IndexID = insert.getId();
      insert.close();
      return IndexID;

    } catch (EFapsException e) {

      LOG.error("create(String, String)", e);
    } catch (Exception e) {

      LOG.error("create(String, String)", e);
    }
    return null;

  }

  /**
   * Searches for an existing Indexer with the parameters as a filter and
   * returns the ID of the Indexer
   * 
   * @param _FileTyp
   *          FileTyp to search for
   * @param _Indexer
   *          ClassName of the LuceneIndexer tp search for
   * @return ID if exits, else null
   */
  private static String getIDofExistingIndexer(String _FileTyp, String _Indexer) {
    SearchQuery query = new SearchQuery();
    try {
      query.setQueryTypes("Lucene_Indexer");
      query.addSelect("ID");
      query.addWhereExprEqValue("FileTyp", _FileTyp);
      query.addWhereExprEqValue("Indexer", _Indexer);
      query.execute();
      if (query.next()) {
        return query.get("ID").toString();
      }
    } catch (EFapsException e) {

      LOG.error("getIDofExistingIndexer(String, String)", e);
    }

    return null;
  }

}
