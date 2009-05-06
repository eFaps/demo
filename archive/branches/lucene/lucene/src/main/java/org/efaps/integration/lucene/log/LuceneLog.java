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

package org.efaps.integration.lucene.log;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;

import org.efaps.db.Insert;

import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;

/**
 * Class for the eFaps-Type Lucene_Log. It usese the Database to write a Log
 * into a SQL-Table. It is useds to determine the last time a Index was
 * launched, how long it took and what was teh return state. Also it is used to
 * find out if a prozess is running in the moment. <br>
 * It must be initialised with Method <code>initialise()</code>
 * 
 * @author jmo
 * 
 */
public class LuceneLog {
  /**
   * Logger for this class
   */
  private static final Log LOG = LogFactory.getLog(LuceneLog.class);

  private Date             LASTSTART;

  private Date             LASTEND;

  private String           OID;

  private String           INDEXID;

  /**
   * If the Class is created with this constructor, the
   * <code>initialise()</code>-Method is called automatically .
   * 
   * @param _IndexID
   */
  public LuceneLog(String _IndexID) {
    setIndexID(_IndexID);
    initialise();
  }

  /**
   * default constructor
   */
  public LuceneLog() {

  }

  /**
   * method to initialise the LuceneLog
   */
  public void initialise() {
    this.getRuntime();
    this.start();
  }

  /**
   * method to initialise the LuceneLog
   */
  public void initialise(String _IndexID) {
    this.setIndexID(_IndexID);
    this.getRuntime();
    this.start();
  }

  /**
   * This method can be called to insert the "running" into the database
   */
  public void start() {

    Insert insert;
    try {

      insert = new Insert("Lucene_Log");
      insert.add("log", "Indiziervorgang gestartet");
      insert.add("Index", this.getIndexID());
      insert.execute();

      OID = insert.getInstance().getOid();
      insert.close();

    } catch (EFapsException e) {

      LOG.error("start()", e);
    } catch (Exception e) {

      LOG.error("start()", e);
    }

  }

  public void setIndexID(String _IndexID) {
    INDEXID = _IndexID;
  }

  public String getIndexID() {
    return INDEXID;
  }

  /**
   * This method has to be called to write the state of success into the
   * database
   * 
   * @param _deleted
   *          Number of deleted <code>lucene.document.Document</code>
   * @param _indexed
   *          Number of indexed <code>lucene.document.Document</code>
   */
  public void end(int _deleted, int _indexed) {

    end("", _deleted, _indexed);
  }

  /**
   * This method has to be called to write the state of success into the
   * database
   * 
   * @param _msg
   *          Message
   * @param _deleted
   *          Number of deleted <code>lucene.document.Document</code>
   * @param _indexed
   *          Number of indexed <code>lucene.document.Document</code>
   */
  public void end(String _msg, int _deleted, int _indexed) {

    try {
      Update update = new Update(OID);
      String log = _msg + " updated: " + _deleted + ", total: " + _indexed;
      update.add("log", log);

      update.execute();
      update.close();
    } catch (EFapsException e) {

      LOG.error("end(int, int)", e);
    } catch (Exception e) {

      LOG.error("end(int, int)", e);
    }
  }

  private void getRuntime() {

    SearchQuery query = new SearchQuery();
    try {

      query.setQueryTypes("Lucene_Log");
      query.addSelect("Created");
      query.addSelect("Modified");

      query.addWhereExprEqValue("ID", new LuceneLogMaxID(this.getIndexID())
          .getLogMaxID());
      query.execute();
      if (query.next()) {
        this.setLastStart((Date) query.get("Created"));
        this.setLastEnd((Date) query.get("Modified"));

      } else {
        this.setLastStart(new Date(0));
        this.setLastStart(new Date(System.currentTimeMillis()));
      }
      query.close();
    } catch (EFapsException e) {

      LOG.error("getRuntime()", e);
    }

  }

  public Date getLastStart() {
    if (LASTSTART == null) {
      this.getRuntime();
    }
    return LASTSTART;

  }

  public Date getLastEnd() {
    if (LASTSTART == null) {
      this.getRuntime();
    }
    return LASTEND;

  }

  /**
   * Method to close the log with an "error"
   */
  public void terminate() {
    try {
      Update update = new Update(OID);

      update.add("log", "failure");

      update.execute();
      update.close();
    } catch (EFapsException e) {

      LOG.error("terminate()", e);
    } catch (Exception e) {

      LOG.error("terminate()", e);
    }
  }

  public void setLastStart(Date _LastStart) {
    LASTSTART = _LastStart;
  }

  public void setLastEnd(Date _LastEnd) {
    LASTEND = _LastEnd;
  }

  public String getOID() {
    return OID;
  }

  public void setOID(String _OID) {
    OID = _OID;
  }

}
