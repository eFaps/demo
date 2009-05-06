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

package org.efaps.admin.property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;

/**
 * This class reads the Properties for eFaps from the connected Database and
 * holds them in a cache. It is possible to use a localized Version for the
 * Properties, by setting the Language of the Properties. If no Language is
 * explicitly set the default from the System is used.<br>
 * The value returned is the value from the localised version, if one is
 * existing, otherwise it returns the default value.
 * 
 * @author jmo
 * 
 */
public class DBProperties {
  /**
   * Logger for this class
   */
  private static final Log           LOG       = LogFactory
                                                   .getLog(DBProperties.class);

  /**
   * Cache for the Properties
   */
  private static Map<String, String> PROPCACHE = new HashMap<String, String>();

  /**
   * used Language
   */
  private static String              LANGUAGE  = null;

  /**
   * are the Properties initialised?
   */
  private static boolean             INITIALISED;

  /**
   * Constructor using the System default language
   */
  public DBProperties() {
    new DBProperties(getLanguage());
  }

  /**
   * Constructor for using an explicit language
   * 
   * @param _Language
   *          Language to use for the Properties
   */
  public DBProperties(String _Language) {
    setLanguage(_Language);
    initialise();
  }

  /**
   * Method for setting an explicit language
   * 
   * @param _Language
   */
  public static void setLanguage(String _Language) {
    LANGUAGE = _Language;

  }

  /**
   * Method that returns the value, depending on the language, for the given key
   * 
   * @param _key
   *          Key to Search for
   * @return if key exists, the value for the key, otherwise null
   */
  public static String getProperty(String _key) {
    if (!isInitialised()) {
      initialise();
    }
    String value = PROPCACHE.get(_key);
    return (value == null) ? _key : value;
    
  }

  /**
   * For getting all Properties in a Map
   * 
   * @return Map with all Properties
   */
  public static Map getProperties() {
    return PROPCACHE;
  }

  /**
   * Method that returns the actual language used for the current properties
   * 
   * @return language
   */
  public static String getLanguage() {
    if (LANGUAGE == null) {
      setLanguage(Locale.getDefault().getLanguage());
    }
    return LANGUAGE;
  }

  /**
   * Method to initialise the Propeties using an explicit language
   * 
   * @param _Language
   *          language to use
   */
  public static void initialise(String _Language) {
    setLanguage(_Language);
    initialise();
  }

  /**
   * Method to initialise the Propeties using the System default language
   */
  public static void initialise() {
    String SQLStmt = "select distinct KEY, DEFAULTV, VALUE ,SEQUENCE from T_ADPROPBUN "
        + " left join T_ADPROP on (T_ADPROPBUN.ID = T_ADPROP.BUNDLEID) "
        + " left join (select PROPID, VALUE from T_ADPROPLOC"
        + " left join T_ADLANG on (T_ADPROPLOC.LANGID = T_ADLANG.ID)"
        + " where LANG ='"
        + getLanguage()
        + "') tmp "
        + " on ( T_ADPROP.ID = tmp.propid ) " + " order by SEQUENCE ";

    initialiseCache(SQLStmt);

  }

  /**
   * Returns, if the properties are initialised
   * 
   * @return true if initilised, otherwise false
   */
  public static boolean isInitialised() {
    return INITIALISED;
  }

  /**
   * Method to initialise only a Bundle of the Properties
   * 
   * @see initialiseBundle(String _BundleUUID)
   * @param _BundleUUID
   *          UUID of the Bundle to use
   * @param _Language
   *          Language to use
   */
  public void initialiseBundle(String _BundleUUID, String _Language) {
    setLanguage(_Language);
    initialiseBundle(_BundleUUID);
  }

  /**
   * Method to initialise only a Bundle of the Properties
   * 
   * @param _BundleUUID
   *          UUID of the Bundle to use
   */
  public void initialiseBundle(String _BundleUUID) {
    String SQLStmt = "select distinct KEY, DEFAULTV, VALUE from T_ADPROPBUN "
        + " left join T_ADPROP on (T_ADPROPBUN.ID = T_ADPROP.BUNDLEID) "
        + " left join (select PROPID, VALUE from T_ADPROPLOC"
        + " left join T_ADLANG on (T_ADPROPLOC.LANGID = T_ADLANG.ID)"
        + " where LANG ='" + getLanguage() + "') tmp "
        + " on ( T_ADPROP.ID = tmp.propid ) " + " where UUID = '" + _BundleUUID
        + "'";
    initialiseCache(SQLStmt);
  }

  /**
   * This method is initialising the cache
   * 
   * @param _SQLStmt
   *          SQl-Statment to access the database
   */
  private static void initialiseCache(String _SQLStmt) {
    ConnectionResource con;
    String value;
    try {
      con = Context.getThreadContext().getConnectionResource();
      Statement stmt = con.getConnection().createStatement();

      ResultSet rs = stmt.executeQuery(_SQLStmt);
      while (rs.next()) {
        value = rs.getString("VALUE");
        if (value == null) {
          value = rs.getString("DEFAULTV");
        }
        PROPCACHE.put(rs.getString("KEY").trim(), value.trim());
      }
      INITIALISED = true;
    } catch (EFapsException e) {

      LOG.error("initialiseCache()", e);
    } catch (SQLException e) {

      LOG.error("initialiseCache()", e);
    }

  }
}
