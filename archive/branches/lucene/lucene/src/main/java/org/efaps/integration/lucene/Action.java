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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Class witch implements actions with a <code>lucene.document.Document</code>
 * in a given LuceneIndex like delete and unlocks a locked LuceneIndex
 * 
 * @author jmo
 * 
 */
public class Action {
  /**
   * Logger for this class
   */
  private static final Log LOG = LogFactory.getLog(Action.class);

  /**
   * Deletes a LuceneDocument from an Index
   * 
   * @param _IndexDir
   *          Directory wich contains the Index
   * @param _FieldName
   *          Fieldname is used together with _Fieldvalue to identifier for the
   *          documents witch will be deleted
   * @param _FieldValue
   *          Value for the Field
   * @return the number of documents deleted
   */

  public static int delete(File _IndexDir, String _FieldName, String _FieldValue) {

    try {
      Directory directory = FSDirectory.getDirectory(_IndexDir, false);
      IndexReader reader = IndexReader.open(directory);

      int deleted = reader.deleteDocuments(new Term(_FieldName, _FieldValue));

      reader.close();
      directory.close();
      return deleted;
    } catch (Exception e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("delete(File, String, String) -  caught a " + e.getClass()
            + "n with message: " + e.getMessage());
      }
    }
    return 0;
  }

  /**
   * Checks if a Lucene Index is locked and unlockes it in case of
   * 
   * @param indexDir
   *          Directory wich contains the Index
   */
  public static void unLock(File _indexDir) {

    try {

      Directory directory = FSDirectory.getDirectory(_indexDir, false);

      IndexReader.open(directory);

      if (IndexReader.isLocked(directory)) {

        IndexReader.unlock(directory);

      }

    } catch (IOException e) {

      e.getMessage();

    }

  }

  /**
   * Optimizes a LucenIndex
   * 
   * @param _indexDir
   *          Directory wich contains the Index
   */
  public static void optimize(File _indexDir) {
    try {
      IndexWriter writer = new IndexWriter(_indexDir, new StandardAnalyzer(),
          false);
      writer.optimize();
      writer.close();
    } catch (IOException e) {
      
      LOG.error("optimize(File)", e);
    }
  }

  
  
  /**
   * The number of <code>lucene.documents.Document</code> wich are containded
   * in an index
   * 
   * @return number of documents
   */
  public static long getDocCount(File _indexDir){
    Directory directory;
    try {
       directory = FSDirectory.getDirectory(_indexDir, false);

       IndexReader reader = IndexReader.open(directory);
       return reader.numDocs();
    } catch (IOException e) {
      
      LOG.error("doccount(File)", e);
    }

    return 0;
  }
  
}
