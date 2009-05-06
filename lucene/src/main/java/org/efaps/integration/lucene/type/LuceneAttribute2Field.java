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
 * Class for the Relation between a Lucene_Index2Type, a Attribute from Efaps
 * and a Lucene_Field
 * 
 * @author jmo
 * 
 */
public class LuceneAttribute2Field {

  /**
   * Creates a New Lucene_Attribute2Field
   * 
   * @param _Index2TypeID
   *          ID of a Lucene_Index2Type
   * @param _AttributeID
   *          ID of an Attribute
   * @param _FieldID
   *          ID of a Lucene_Field
   */
  public static String createNew(String _Index2TypeID, String _AttributeID,
      String _FieldID) {
    Insert insert;
    try {
      insert = new Insert("Lucene_Attribute2Field");
      insert.add("IndexTypeLink", _Index2TypeID);
      insert.add("Attribute", _AttributeID);
      insert.add("Field", _FieldID);
      insert.execute();
      String Attribute2FieldID = insert.getId();
      insert.close();
      return Attribute2FieldID;
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
