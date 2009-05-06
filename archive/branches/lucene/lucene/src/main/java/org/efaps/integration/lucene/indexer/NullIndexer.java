/*
 * Copyright 2003- 2007 The eFaps Team
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

package org.efaps.integration.lucene.indexer;

import org.apache.lucene.document.Field;

import org.efaps.integration.lucene.indexer.AbstractIndexer;

/**
 * In the case a File should or could not be indexed, this class is used
 * 
 * @author jmo
 * 
 */
public class NullIndexer extends AbstractIndexer {

  @Override
  public String getContent() {

    return "";
  }

  @Override
  public Field getContentField() {

    return new Field("contents", getContent(), Field.Store.NO,
        Field.Index.TOKENIZED);
  }

}
