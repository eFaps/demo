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
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ConstantScoreRangeQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;

/**
 * For Search like "value < Field < value"
 * 
 * @author jmo
 * 
 */
public class RangeSearch extends AbstractSearch {

  public RangeSearch(String _IndexID) {
    super(_IndexID);

  }

  public void find(String _field, String _lowerVal, String _upperVal,
      boolean _includeLower, boolean _includeUpper, int _startindex) {
    List<String> result = new ArrayList<String>();

    Query query = new ConstantScoreRangeQuery(_field, _lowerVal, _upperVal,
        _includeLower, _includeUpper);
    try {

      Hits hits = getSearcher().search(query);

      int thispage = 0;
      setHitsCount(hits.length());
      if ((_startindex + getMaxHit()) > getHitsCount()) {
        thispage = hits.length() - _startindex;
      }

      for (int i = _startindex; i < (thispage + _startindex); i++) {
        Document doc = hits.doc(i);
        result.add(doc.get("OID"));
        getLog().debug(doc.get("OID"));
      }
      setHits(result);
    } catch (IOException e) {

      getLog().error("find(String)", e);
    }

  }

  @Override
  protected void setHits(List _Hits) {
    super.HITS = _Hits;
  }

  @Override
  protected void setHitsCount(int _HitsCount) {
    super.HITSCOUNT = _HitsCount;

  }

}
