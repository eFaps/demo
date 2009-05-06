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

package org.efaps.integration.lucene.search;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.IndexSearcher;
import org.efaps.db.SearchQuery;
import org.efaps.integration.lucene.AbstractTransaction;
import org.efaps.integration.lucene.type.LuceneIndex;
import org.efaps.integration.lucene.type.LuceneIndex2Type;
import org.efaps.util.EFapsException;

/**
 * This class defines the abstract search for all searches
 * 
 * @author jmo
 * 
 */
public abstract class AbstractSearch extends AbstractTransaction {
  /**
   * Logger for this class
   */
  private static final Log     LOG        = LogFactory
                                              .getLog(AbstractSearch.class);

  private static Analyzer      ANALYZER   = null;

  private static IndexSearcher SEARCHER   = null;

  private static LuceneIndex   INDEX;

  private static int           MAXHITPAGE = 50;

  protected int                HITSCOUNT;

  protected List               HITS;

  public List getHits() {
    return HITS;
  }

  protected abstract void setHitsCount(int _HitsCount);

  protected abstract void setHits(List _Hits);

  public AbstractSearch(String _IndexID) {
    if ((getIndex() == null)) {
      initialise(_IndexID);
    } else if (!_IndexID.equals(getIndex().getID())) {
      initialise(_IndexID);
    }
  }

  public int getHitsCount() {
    return HITSCOUNT;
  }

  public int getMaxHit() {
    return MAXHITPAGE;
  }

  public void setMaxHit(int _max) {
    MAXHITPAGE = _max;
  }

  public Log getLog() {
    return LOG;
  }

  public LuceneIndex getIndex() {
    return INDEX;
  }

  public void setIndex(String _OID) {
    INDEX = new LuceneIndex(_OID);
    INDEX.initialise();
  }

  public IndexSearcher getSearcher() {
    if (SEARCHER == null) {
      setSearcher(getIndex().getIndexPath());
    }
    return SEARCHER;
  }

  public void setSearcher(String _Path) {
    try {
      SEARCHER = new IndexSearcher(_Path);
    } catch (IOException e) {
      LOG.error("setSearcher", e);

    }
  }

  public void initialise(String _IndexID) {

    loadRunLevel();

    try {
      login("Administrator", "");
      reloadCache();
      startTransaction();
      SearchQuery query = new SearchQuery();
      query.setQueryTypes("Lucene_Index");
      query.addSelect("OID");
      query.execute();
      if (query.next()) {
        setIndex(query.get("OID").toString());

      } else {
        LOG.error("can't find the index");
      }
      abortTransaction();
    } catch (IOException e) {
      LOG.error("initialize()", e);
    } catch (EFapsException e) {
      LOG.error("initialize()", e);
    } catch (Exception e) {
      LOG.error("initialize()", e);
    }

  }

  public Analyzer getAnalyzer() {
    if (ANALYZER == null) {
      Map types = getIndex().getTypeList();

      for (Object elem : types.values()) {
        LuceneIndex2Type type = (LuceneIndex2Type) elem;
        ANALYZER = type.getLuceneAnalyzer().getAnalyzer();
      }
    }
    return ANALYZER;

  }

}
