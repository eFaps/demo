/*
 * Copyright 003 -2007 The eFaps Team
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

import java.lang.reflect.Method;

import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Insert;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * Class for the Lucene_Field Type. It definies which Classes are to be used for
 * getting the Attributes from a Type, how to store them into an index and how
 * to index them
 * 
 * @author jmo
 * 
 */
public class LuceneField {
  private static String                          ID  = null;

  private org.apache.lucene.document.Field.Store STORE;

  private org.apache.lucene.document.Field.Index INDEX;

  private String                                 OID = null;

  private Method                                 ATTRIBUTEMETHOD;

  public LuceneField(String _ID) {
    ID = _ID;

    Long i = Type.get("Lucene_Field").getId();
    setOID(i.toString() + "." + getID());

    initialise();
  }

  public Index getIndex() {
    return INDEX;
  }

  private void initialise() {
    SearchQuery query = new SearchQuery();
    try {
      query.setObject(getOID());
      query.addSelect("Class");
      query.addSelect("Method");
      query.addSelect("Store");
      query.addSelect("Index");

      query.execute();

      if (query.next()) {
        setStore(query.get("Store").toString());
        setIndex(query.get("Index").toString());
        setAttributeMethod(query.get("Class").toString(), query.get("Method")
            .toString());
      }

      query.close();
    } catch (EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (SecurityException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  private void setAttributeMethod(String _Class, String _AttMethod) {
    if (_Class.length() != 0) {
      try {
        Class<?> c = Class.forName(_Class);
        ATTRIBUTEMETHOD = c.getMethod(_AttMethod, new Class[] {});
      } catch (SecurityException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (NoSuchMethodException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  public Method getAttributeMethode() {
    return ATTRIBUTEMETHOD;

  }

  private void setIndex(String _Index) {

    if (_Index.equalsIgnoreCase("UN_TOKENIZED")) {
      INDEX = Index.UN_TOKENIZED;
    }

    if (_Index.equalsIgnoreCase("TOKENIZED")) {
      INDEX = Index.TOKENIZED;
    }
    if (_Index.equalsIgnoreCase("NO")) {
      INDEX = Index.NO;
    }
    if (_Index.equalsIgnoreCase("NO_NORMS")) {
      INDEX = Index.NO_NORMS;
    }

  }

  private void setStore(String _Store) {
    if (_Store.equalsIgnoreCase("YES")) {
      STORE = Store.YES;
    }
    if (_Store.equalsIgnoreCase("NO")) {
      STORE = Store.NO;
    }

    if (_Store.equalsIgnoreCase("COMPRESS")) {
      STORE = Store.COMPRESS;
    }
  }

  class StoreYES {

  }

  public String getID() {
    return ID;
  }

  public Store getStore() {
    return STORE;
  }

  /**
   * Creates a new Lucene_Field Type in the Datebase
   * 
   * @param _Class
   *          Name of the Class to be used
   * @param _Method
   *          Name of the Method to be invoked
   * @param _Store
   *          Entry of a how to store into the LuceneIndex:"YES","NO","COMPRESS"
   * 
   * @param _index
   *          Entry of how to index into the Lucene Index:"UN_TOKENIZED",
   *          "TOKENIZED","NO","NO_NORMS"
   * @return the ID of the new Lucene_Field
   */
  public static String createNew(String _Class, String _Method, String _Store,
      String _index) {
    Insert insert;

    try {
      insert = new Insert("Lucene_Field");
      insert.add("Class", _Class);
      insert.add("Method", _Method);
      insert.add("Store", _Store);
      insert.add("Index", _index);
      insert.execute();
      String FieldID = insert.getId();
      insert.close();
      return FieldID;

    } catch (EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;

  }

  public void setOID(String _OID) {
    OID = _OID;
  }

  public String getOID() {

    return OID;
  }
}
