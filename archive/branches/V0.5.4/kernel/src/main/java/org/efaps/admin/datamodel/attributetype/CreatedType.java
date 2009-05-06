/*
 * Copyright 2006 The eFaps Team
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

package org.efaps.admin.datamodel.attributetype;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import org.efaps.db.Context;
import org.efaps.db.query.CachedResult;

/**
 * The class is the attribute type representation for the created date time of a
 * business object.
 */
public class CreatedType extends DateTimeType {

  // ///////////////////////////////////////////////////////////////////////////
  // interface to the data base

  /**
   * The instance method appends <i>SYSDATE</i> to the sql statement. Because
   * This is not a value, the method returns a <i>true</i>, that the value is
   * hard coded and must not updated via a prepared sql statement.
   * 
   * @param _stmt
   *          string buffer with the statement
   * @return always <i>true</i>
   */
  public boolean prepareUpdate(StringBuilder _stmt) {
    // _stmt.append(Context.getDbType().getCurrentTimeStamp());
    _stmt.append("?");
    return false;

  }

  private Timestamp value = null;

  public void setValue(final Timestamp _value) {
    this.value = _value;
  }

  public void update(Context _context, final PreparedStatement _stmt,
                     final int _index) throws SQLException {
    _stmt.setTimestamp(_index, getValue());
  }

  /**
   * This is the getter method for instance variable {@link #value}.
   * 
   * @return the value of the instance variable {@link #value}.
   * @see #value
   * @see #setValue
   */
  public Timestamp getValue() {
    return this.value;
  }

  public Object readValue(Context _context, CachedResult _rs,
                          ArrayList<Integer> _indexes) {
    setValue(_rs.getTimestamp(_indexes.get(0).intValue()));
    return getValue();
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * 
   * @param _context
   *          context for this request
   * @param _value
   *          new value to set
   */
  public void set(final Context _context, final Object _value) {
    if (_value instanceof Date) {
      setValue(new Timestamp((((Date) _value)).getTime()));
    }
  }

}