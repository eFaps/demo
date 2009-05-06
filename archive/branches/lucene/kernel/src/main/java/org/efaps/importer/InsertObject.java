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
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Insert;
import org.efaps.util.EFapsException;

public class InsertObject extends AbstractObject {

  private String               TYPE            = null;

  private Map<String, Object>  ATTRIBUTES      = new HashMap<String, Object>();

  private String               PARENTATTRIBUTE = null;

  private List<AbstractObject> CHILDS          = new ArrayList<AbstractObject>();

  private String               ID              = null;

  private Set<ForeignObject>   LINKS           = new HashSet<ForeignObject>();

  public InsertObject() {

  }

  public void setType(String _Type) {
    TYPE = _Type;
  }

  public void setAttribute(String _Name, String _Value) {
    ATTRIBUTES.put(_Name, _Value);
  }

  public void setParentAttribute(String _ParentAttribute) {
    PARENTATTRIBUTE = _ParentAttribute;
  }

  public void addChild(AbstractObject _Object) {
    CHILDS.add(_Object);
  }

  public void addLink(ForeignObject _Object) {
    LINKS.add(_Object);

  }

  public void setID(String _ID) {
    ID = _ID;
  }

  public void insertObject() {
    for (AbstractObject object : this.CHILDS) {

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
        if (object.getParrentAttribute() != null) {
          insert.add(object.getParrentAttribute(), this.ID);
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

  public String getID() {
    return this.ID;
  }

  @Override
  public String getType() {
    return this.TYPE;

  }

  @Override
  public Map<String, Object> getAttributes() {
    for (Entry element : this.ATTRIBUTES.entrySet()) {

      Attribute attribute = Type.get(this.TYPE).getAttribute(
          element.getKey().toString());

      if (attribute.getAttributeType().getClassRepr().getName().equals(
          "org.efaps.admin.datamodel.attributetype.DateTimeType")) {

        Date date = new SimpleDateFormat(RootObject.DATEFORMAT).parse(element
            .getValue().toString(), new ParsePosition(0));

        this.ATTRIBUTES.put((String) element.getKey(), new Timestamp(date
            .getTime()));
      }
    }
    return this.ATTRIBUTES;
  }

  @Override
  public String getParrentAttribute() {

    return this.PARENTATTRIBUTE;
  }

  @Override
  public Set<ForeignObject> getLinks() {

    return this.LINKS;
  }

}
