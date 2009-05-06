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

package org.efaps.importer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

public class ForeignObject {
  private String              ATTRIBUTE  = null;

  private String              TYPE       = null;

  private Map<String, String> ATTRIBUTES = new HashMap<String, String>();

  public void setAttribute(String _Name, String _Value) {
    this.ATTRIBUTES.put(_Name, _Value);
  }

  public void setLinkAttribute(String _Name, String _Type) {
    this.ATTRIBUTE = _Name;
    this.TYPE = _Type;

  }

  public String getAttribute() {

    return this.ATTRIBUTE;
  }

  public String getID() {
    SearchQuery query = new SearchQuery();
    String ID = null;
    try {

      query.setQueryTypes(this.TYPE);
      query.addSelect("ID");

      query.setExpandChildTypes(true);

      for (Entry element : this.ATTRIBUTES.entrySet()) {
        query.addWhereExprEqValue(element.getKey().toString(), element
            .getValue().toString());
      }
      query.executeWithoutAccessCheck();
      if (query.next()) {

        ID = (String) query.get("ID").toString();

      }
      query.close();

      return ID;

    }
    catch (EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return null;
  }
}
