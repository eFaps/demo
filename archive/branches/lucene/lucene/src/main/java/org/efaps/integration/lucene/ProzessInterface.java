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

/**
 * Interface for accessing and executing the indexing from e.g. a shell
 * 
 * @author jmo
 * 
 */
public interface ProzessInterface {

  /**
   * execute the indexing of one index
   * 
   * @param _OID
   *          object id (LuceneIndex)
   */
  public void execute(String _OID);

  /**
   * execute the indexing of all indexes in the database
   */
  public void executeAll();

  /**
   * deletes the index and than indexes all Files
   * 
   * @param _OID
   *          object id (LuceneIndex)
   */
  public void reset(String _OID);
  
  
  

  /**
   * deletes all index and than indexes all Files
   */
  public void resetAll();

}
