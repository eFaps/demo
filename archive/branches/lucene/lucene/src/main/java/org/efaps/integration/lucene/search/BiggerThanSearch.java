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

/**
 * Class for search like field > value
 * 
 * @author jmo
 * 
 */
public class BiggerThanSearch extends RangeSearch {

  public BiggerThanSearch(String _IndexID) {
    super(_IndexID);

  }

  public void find(String _field, String _upperVal, boolean _includeUpper,
      int _startindex) {
    super.find(_field, null, _upperVal, true, _includeUpper, _startindex);

  }

}
