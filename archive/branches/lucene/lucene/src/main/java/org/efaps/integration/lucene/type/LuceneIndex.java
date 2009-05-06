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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.efaps.db.Insert;
import org.efaps.db.SearchQuery;
import org.efaps.integration.lucene.Action;
import org.efaps.integration.lucene.TypeFileFactory;
import org.efaps.integration.lucene.indexer.AbstractIndexer;
import org.efaps.util.EFapsException;

/**
 * This represent one Lucene Index Database
 * 
 * @author jmo
 * 
 */
public class LuceneIndex {
  /**
   * Logger for this class
   */
  private static final Log           LOG        = LogFactory
                                                    .getLog(LuceneIndex.class);

  private static String              ID         = null;

  private static String              OID        = null;

  protected static File              INDEX_DIR;

  private static String              NAME;

  private static Map<String, Object> INDEXTYPES = new HashMap<String, Object>();

  private int                        DELETED;

  private boolean                    NEW_INDEX_FILES;

  public LuceneIndex(String _OID) {
    this.setOID(_OID);
    setIndex(_OID);
  }

  public LuceneIndex() {

  }

  /**
   * Defines a new Index into the given Folder
   * 
   * @param _Path
   *          Path to the Folder
   * @param _Name
   * @return ID of the Index
   */
  public static String createNew(String _Path, String _Name) {
    Insert insert;

    try {
      insert = new Insert("Lucene_Index");
      insert.add("Path", _Path);
      insert.add("Name", _Name);
      insert.execute();
      String ID = insert.getId();
      insert.close();
      return ID;

    } catch (EFapsException e) {

      LOG.error("createNew(String, String)", e);
    } catch (Exception e) {

      LOG.error("createNew(String, String)", e);
    }
    return null;

  }

  private void setIndex2Type() {
    SearchQuery query = new SearchQuery();
    try {
      query.setExpand(getOID(), "Lucene_Index2Type\\Index");
      query.addSelect("OID");
      query.addSelect("Type");
      query.execute();

      while (query.next()) {
        INDEXTYPES.put(query.get("Type").toString(), new LuceneIndex2Type(query
            .get("OID").toString()));

      }

    } catch (EFapsException e) {

      LOG.error("setIndex2Type()", e);
    }

  }

  public void initialise() {

    setIndex2Type();
    LOG.info("Index initialises");
  }

  public File getIndexDir() {
    return INDEX_DIR;
  }

  public String getName() {
    return NAME;

  }

  public String getIndexPath() {

    return getIndexDir().getAbsolutePath();
  }

  public void setName(String _Name) {
    NAME = _Name;
  }

  public void setIndexDir(File IndexDir) {
    INDEX_DIR = IndexDir;
  }

  /**
   * Sets the index wich is going to be used
   * 
   * @param _OID
   *          bbject id
   */
  public void setIndex(String _OID) {
    SearchQuery query = new SearchQuery();
    try {
      query.setObject(_OID);
      query.addSelect("Path");
      query.addSelect("Name");
      query.addSelect("OID");
      query.addSelect("ID");
      query.execute();
      query.next();
      setIndexDir(new File((String) query.get("Path")));
      setName((String) query.get("Name"));

      this.setOID(query.get("OID").toString());
      this.setID(query.get("ID").toString());

      query.close();
    } catch (EFapsException e) {

      LOG.error("setIndexDir(String)", e);
    }

  }

  /**
   * Sets the index wich is going to be used
   * 
   * @param _Name
   *          Name of the Index
   */
  public void setIndexbyName(String _Name) {
    SearchQuery query = new SearchQuery();
    try {
      query.setQueryTypes("Lucene_Index");
      query.addSelect("OID");
      query.addSelect("ID");
      query.addSelect("Path");
      query.addSelect("Name");
      query.addWhereExprEqValue("Name", _Name);

      query.execute();
      if (query.next()) {

        setIndexDir(new File((String) query.get("Path")));

        this.setOID(query.get("OID").toString());
        this.setID(query.get("ID").toString());

      } else {
        LOG.error("invalid IndexName");
      }
      query.close();
    } catch (EFapsException e) {

      LOG.error("setIndexDir_Name(String)", e);
    }
  }

  public static String getOID() {
    return OID;
  }

  public void setOID(String _OID) {
    OID = _OID;
  }

  public String getID() {
    return ID;
  }

  public void setID(String _ID) {
    ID = _ID;

  }

  public Map getTypeList() {
    return INDEXTYPES;
  }

  public LuceneIndex2Type getIndex2Type(String _TypID) {
    return (LuceneIndex2Type) INDEXTYPES.get(_TypID);
  }

  public LuceneIndex2Type getIndex2Type(long _TypID) {
    return (LuceneIndex2Type) INDEXTYPES.get(_TypID);
  }

  /**
   * adds an Object identified by the OID to the index
   * 
   * @param _OID
   */
  public void addObject(String _OID) {
    TypeFileFactory typedocfactory = new TypeFileFactory(_OID);

    LuceneIndex2Type index2type = getIndex2Type(typedocfactory.getTypeID());

    Class<?> cl = index2type.getDocIndexer(typedocfactory.getFileTyp());

    try {
      AbstractIndexer indexer = (AbstractIndexer) cl.newInstance();
      indexer.setIndex(this);
      indexer.setTypeDocFactory(typedocfactory);
      DELETED += indexer.indexUnique();

    } catch (InstantiationException e) {

      LOG.error("addObject(String)", e);
    } catch (IllegalAccessException e) {

      LOG.error("addObject(String)", e);
    }
    LOG.info("added Object to Index ");
  }

  public int getDeleted() {
    return DELETED;

  }

  /**
   * Optimizes the index
   */
  public void optimize() {
    Action.optimize(getIndexDir());

  }

  /**
   * This method should be called for terminating the index, in case of an error
   * it also ensures that the index is unlocked
   * 
   */
  public void close() {
    Action.unLock(getIndexDir());
    LOG.info("Index closed");

  }

  /**
   * The number of <code>lucene.documents.Document</code> wich are containded
   * in an index
   * 
   * @return number of documents
   */
  public long getDocumentCount() {
    return Action.getDocCount(getIndexDir());

  }

  public boolean getNewIndexFiles() {
    return NEW_INDEX_FILES;
  }

  public void setNewIndexFiles(Boolean _new) {
    NEW_INDEX_FILES = _new;
  }

}
