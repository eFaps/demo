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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumberTools;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Checkout;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.integration.lucene.type.LuceneField;
import org.efaps.integration.lucene.type.LuceneIndex;
import org.efaps.util.EFapsException;

/**
 * Class building the Type which are to be indexed into an Index, throught this
 * Class the actual File behind the Type (which we want to be indexed) can be
 * accesed.
 * 
 * @author janmoxter
 * 
 */
public class TypeFileFactory {
  /**
   * Logger for this class
   */
  private static final Log LOG    = LogFactory.getLog(TypeFileFactory.class);

  private Type             TYPE;

  private static String    FILE_NAME;

  private static String    OID;

  public Boolean           UPDATE = true;

  public TypeFileFactory(String _OID) {
    setType(new Instance(_OID).getType());

    this.setOID(_OID);

    SearchQuery query = new SearchQuery();

    try {
      query.setObject(getOID());
      query.addSelect("Name");
      query.execute();
      query.next();
      this.setFileName(query.get("Name").toString());
      query.close();
    } catch (EFapsException e) {

      LOG.error("TypeFileFactory(String)", e);
    }

  }

  private void setType(Type _type) {
    TYPE = _type;
  }

  public TypeFileFactory(String _OID, String _filename) {
    this.setOID(_OID);
    this.setFileName(_filename);

  }

  public String getFileName() {
    return FILE_NAME;

  }

  public void setFileName(String _filename) {
    FILE_NAME = _filename;
  }

  public String getOID() {
    return OID;
  }

  public void setOID(String _OID) {
    OID = _OID;

  }

  /**
   * Gives accesss to a <code>lucene.document.Document</code>, which can than
   * be added to an Index, the <code>lucene.document.Field</code> in the
   * returned Document are only build from Information from eFaps and not based
   * on information gained be actually indexing a real File
   * 
   * @param _index
   *          LuceneIndex the Document will be added
   * @return Document
   * 
   */
  public Document getLuceneDocument(LuceneIndex _index) {

    Document doc = new Document();

    SearchQuery query = new SearchQuery();
    Map<String, LuceneField> lucenefields = null;

    try {
      query.setObject(getOID());

      doc
          .add(new Field("OID", getOID(),
              org.apache.lucene.document.Field.Store.YES,
              Field.Index.UN_TOKENIZED));

      lucenefields = _index.getIndex2Type(getTypeID()).getLuceneDocFactory()
          .getFields();

      for (String element : lucenefields.keySet()) {
        query.addSelect(element);

      }

      query.execute();

    } catch (EFapsException e) {

      LOG.error("getLuceneDocument(LuceneIndex)", e);
    }

    while (query.next()) {
      for (String element : lucenefields.keySet()) {

        Object orgvalue = null;
        try {
          orgvalue = query.get(element);
        } catch (EFapsException e) {

          LOG.error("getLuceneDocument(LuceneIndex)", e);
        }
        Object objvalue = null;
        LuceneField lucenefield = lucenefields.get(element);

        Method m = lucenefield.getAttributeMethode();
        if (m != null) {
          try {
            objvalue = m.invoke(orgvalue, (Object[]) null);

          } catch (IllegalArgumentException e) {

            LOG.error("getLuceneDocument(LuceneIndex)", e);
          } catch (IllegalAccessException e) {

            LOG.error("getLuceneDocument(LuceneIndex)", e);
          } catch (InvocationTargetException e) {

            LOG.error("getLuceneDocument(LuceneIndex)", e);
          }
        } else {

          objvalue = orgvalue;
        }

        String value = "";
        if (objvalue instanceof Long) {
          value = NumberTools.longToString((Long) objvalue);
        }
        if (objvalue instanceof String) {
          value = objvalue.toString();
        }

        if (objvalue instanceof Date) {
          value = DateTools.dateToString((Date) objvalue,
              DateTools.Resolution.SECOND);
        }

        doc.add(new Field(element, value, lucenefield.getStore(), lucenefield
            .getIndex()));
      }

    }

    try {
      query.close();
    } catch (EFapsException e) {

      LOG.error("getLuceneDocument(LuceneIndex)", e);
    }
    return doc;
  }

  /**
   * Returning the Inputstream from eFaps which contains the actual File, which
   * we want to be indexed
   * 
   * @return InputStream, null on Error
   */
  public InputStream getStream() {

    Checkout checkout = new Checkout(getOID());

    try {
      InputStream inputstream;

      checkout.preprocess();
      inputstream = (InputStream) checkout.execute();

      checkout.close();

      return inputstream;

    } catch (Exception e) {
      
      LOG.error("getStream()", e);
    }
    return null;
  }

  /**
   * Gets the FileTyp (e.g. xls, ppt, pdf) from the FileName of the Object
   * 
   * @return FileTyp
   */
  public String getFileTyp() {

    return getFileName().substring(getFileName().lastIndexOf(".") + 1);

  }

  public String getTypeID() {
    Long id = TYPE.getId();
    return id.toString();
  }
}
