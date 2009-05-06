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

package org.efaps.integration.lucene.indexer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;

import org.efaps.integration.lucene.TypeFileFactory;
import org.efaps.integration.lucene.Action;
import org.efaps.integration.lucene.type.LuceneIndex;
import org.efaps.util.EFapsException;

/**
 * Class for Indexer. Responsible for indexing a Document (e.g. ".xls",".doc").
 * Here the basic functions are defined. To get the actual context from the
 * document, there has to be always a own class.
 * 
 * @author jmo
 * 
 */
public abstract class AbstractIndexer {
  /**
   * Logger for this class
   */
  private static final Log LOG    = LogFactory.getLog(AbstractIndexer.class);

  private TypeFileFactory  TYPEDOCFACTORY;

  private InputStream      STREAM = null;

  private String           FILE_NAME;

  private String           OID;

  private IndexWriter      WRITER;

  protected LuceneIndex    INDEX;

  /**
   * gives access to actual Content of the file we wanted to index in form of a
   * String
   * 
   * @return String with the content
   */
  public abstract String getContent();

  /**
   * the access to the <code>lucene.document.Field</code> which contains the
   * actual content of the file we wanted to index
   * 
   * @return a lucene.document.Field
   */
  public abstract Field getContentField();

  /**
   * @return LuceneDocument for adding it to an index
   * @throws EFapsException
   */
  public Document getLuceneDocument() throws EFapsException {

    Document doc;
    doc = getTypeDocFactory().getLuceneDocument(this.getIndex());
    doc.add(this.getContentField());

    return doc;

  }

  public IndexWriter getWriter() {
    return WRITER;
  }

  public void setWriter(Analyzer ana) {
    try {
      WRITER = new IndexWriter(getIndexDir(), ana, createIndex());
    } catch (IOException e) {
      LOG.error("setWriter(Analyzer)-", e);
    }
  }

  public String getOID() {
    return OID;
  }

  public void setOID(String oid) {
    this.OID = oid;
  }

  public String getFileName() {
    return FILE_NAME;
  }

  public void setFileName(String fileName) {
    this.FILE_NAME = fileName;
  }

  public InputStream getStream() {
    if (!(STREAM == null)) {
      return STREAM;
    } else {
      this.setStream(this.getTypeDocFactory().getStream());
    }
    return STREAM;

  }

  public void setStream(InputStream is) {
    this.STREAM = is;
  }

  public File getIndexDir() {
    return getIndex().getIndexDir();
  }

  public boolean indexExists() {
    return IndexReader.indexExists(getIndex().getIndexDir());
  }

  public boolean createIndex() {
    if (indexExists()) {
      return getIndex().getNewIndexFiles();
    }
    return true;
  }

  /**
   * Adds a lucene.document to the index
   * 
   * @param _Analyzer
   */
  public synchronized void addDoctoIndex(Analyzer _Analyzer) {

    if (getWriter() == null) {
      setWriter(_Analyzer);
    }

    try {
      getWriter().addDocument(getLuceneDocument());
      getWriter().close();
    } catch (IOException e) {

      LOG.error("addDoctoIndex(Analyzer)", e);
    } catch (EFapsException e) {

      LOG.error("addDoctoIndex(Analyzer)", e);
    }

  }

  public synchronized int indexUnique(Analyzer _Analyzer) {
    int del = 0;
    if (this.indexExists()) {
      del = Action.delete(INDEX.getIndexDir(), "OID", getTypeDocFactory()
          .getOID());
    }
    addDoctoIndex(_Analyzer);
    return del;

  }

  public int indexUnique() {
    int del = 0;
    if (this.indexExists()) {
      del = Action.delete(INDEX.getIndexDir(), "OID", getTypeDocFactory()
          .getOID());
    }

    addDoctoIndex(getIndex().getIndex2Type(
        getTypeDocFactory().getTypeID().toString()).getLuceneAnalyzer()
        .getAnalyzer());
    return del;

  }

  /**
   * optimizes the index
   */
  public void optimize() {
    try {
      getWriter().optimize();
    } catch (IOException e) {

      LOG.error("optimize()", e);
    }
  }

  public void setTypeDocFactory(TypeFileFactory _typedocfactory) {
    TYPEDOCFACTORY = _typedocfactory;

  }

  public TypeFileFactory getTypeDocFactory() {

    return TYPEDOCFACTORY;
  }

  public LuceneIndex getIndex() {
    return INDEX;
  }

  public void setIndex(LuceneIndex _LuceneIndex) {
    INDEX = _LuceneIndex;
  }

  public static Log getLog() {
    return LOG;
  }
}
