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

import org.efaps.db.Insert;
import org.efaps.util.EFapsException;

/**
 * Class representing the Relatoin between an Lucene_Index2Type and an
 * Lucene_Indexer
 * 
 * @author jmo
 * 
 */
public class LuceneType2Indexer {

  /**
   * Inserts a new Lucene_Type2Indexer into the Database
   * 
   * @param _Index2Typ
   *          ID of a Lucene_Index2Type
   * @param _IndexerID
   *          ID of a Lucene_Indexer
   * @return ID of the new Type2Indexer
   */
  public static String createNew(String _Index2Typ, String _IndexerID) {
    Insert insert;
    try {
      insert = new Insert("Lucene_Type2Indexer");
      insert.add("Indexer", _IndexerID);
      insert.add("Index2Type", _Index2Typ);
      insert.execute();
      String Type2IndexerID = insert.getId();
      insert.close();
      return Type2IndexerID;
    } catch (EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return null;

  }
}
