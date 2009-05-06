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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.efaps.db.Insert;
import org.efaps.util.EFapsException;

public class RootObject extends AbstractObject {

  static final List<AbstractObject> CHILDS     = new ArrayList<AbstractObject>();

  static String                     DATEFORMAT = null;

  public void setDateFormat(String _DateFormat) {
    DATEFORMAT = _DateFormat;
  }

  @Override
  public Map<String, Object> getAttributes() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void insertObject() {
    // TODO Auto-generated method stub

  }

  @Override
  public void setID(String _ID) {
    // TODO Auto-generated method stub

  }

  public void addChild(AbstractObject _Object) {
    CHILDS.add(_Object);
  }

  public static void insertDB() {
    for (AbstractObject object : RootObject.CHILDS) {
      try {
        Insert insert = new Insert(object.getType());

        for (Entry element : object.getAttributes().entrySet()) {
          if (element.getValue() instanceof Timestamp) {
            insert.add(element.getKey().toString(), (Timestamp) element
                .getValue());

          } else {
            insert.add(element.getKey().toString(), element.getValue()
                .toString());
          }
        }
        for (ForeignObject link : object.getLinks()) {
          insert.add(link.getAttribute(), link.getID());
        }
        insert.execute();
        String ID = insert.getId();
        insert.close();

        object.setID(ID);
        object.insertObject();

      }
      catch (EFapsException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    }
  }

  @Override
  public String getParrentAttribute() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<ForeignObject> getLinks() {
    // TODO Auto-generated method stub
    return null;
  }
}
