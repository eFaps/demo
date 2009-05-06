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

import java.util.HashMap;
import java.util.Map;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.db.SearchQuery;
import org.efaps.integration.lucene.type.LuceneField;
import org.efaps.integration.lucene.type.LuceneIndex2Type;
import org.efaps.util.EFapsException;

/**
 * Class building the Map holding the different <code>LuceneField</code> for
 * putting Information from Efaps into an Index
 * 
 * @author jmo
 * 
 */
public class DocFactory {

  private Map<String, LuceneField> ATTRIBUTE_FIELD = new HashMap<String, LuceneField>();

  /**
   * Map with the LuceneFields
   * 
   * @return ATTRIBUTE_FIELD
   */
  public Map<String, LuceneField> getFields() {
    return ATTRIBUTE_FIELD;

  }

  public DocFactory(String _LuceneIndex2TypeOID) {
    setFields(_LuceneIndex2TypeOID);
  }

  public DocFactory(LuceneIndex2Type _LuceneIndex2Type) {
    setFields(_LuceneIndex2Type.getOID());
  }

  /**
   * Builds the Map
   * 
   * @param _LuceneIndex2TypeOID
   */
  private void setFields(String _LuceneIndex2TypeOID) {
    SearchQuery query = new SearchQuery();

    try {
      query.setExpand(_LuceneIndex2TypeOID,
          "Lucene_Attribute2Field\\IndexTypeLink");
      query.addSelect("Attribute");
      query.addSelect("Field");
      query.execute();
      while (query.next()) {
        Attribute x = Attribute.get((Long) query.get("Attribute"));
        ATTRIBUTE_FIELD.put(x.getName(), new LuceneField(query.get("Field")
            .toString()));
      }
      query.close();
    } catch (EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
}
