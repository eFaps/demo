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

package org.efaps.integration.lucene.indexer;

import java.io.InputStreamReader;

import org.apache.lucene.document.Field;

/**
 * Class for getting the content out of an "Txt"-File
 * 
 * @author jmo
 * 
 */
public class TxtIndexer extends AbstractIndexer {

  @Override
  public String getContent() {
    return new InputStreamReader(getStream()).toString();
  }

  @Override
  public Field getContentField() {
    return new Field("contents", new InputStreamReader(getStream()));
  }

}
