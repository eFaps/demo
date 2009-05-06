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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.lucene.document.Field;

/**
 * Class for getting the content out of an "Open-Office"-File
 * 
 * @author jmo
 * 
 */
public class OOIndexer extends AbstractIndexer {
  
  static Log LOG = getLog();
  
  /**
   * Parses the content to the XMLIndexer 
   * @param _InputStream the OO-File
   * @return the content of the file
   */
  public static String parse(InputStream _InputStream) {
    List XMLFiles = unzip(_InputStream);
    return  XmlIndexer.parse((InputStream) XMLFiles.get(0));
  
  }

  /**
   * Unzips the OO-File and returns then the XML-File with the Content
   * 
   * @param _InputStream
   * @return
   */
  public static List unzip(InputStream _InputStream) {
    List<InputStream> res = new ArrayList<InputStream>();
    try {
      ZipInputStream in = new ZipInputStream(_InputStream);
      ZipEntry entry = null;
      while ((entry = in.getNextEntry()) != null) {
        if (entry.getName().equals("content.xml")) {
          ByteArrayOutputStream stream = new ByteArrayOutputStream();
          byte[] buf = new byte[1024];
          int len;
          while ((len = in.read(buf)) > 0) {
            stream.write(buf, 0, len);
          }
          InputStream isEntry = new ByteArrayInputStream(stream.toByteArray());
          res.add(isEntry);
        }
      }
      in.close();
    } catch (IOException e) {
      LOG.error("unzip()", e);
    }
    return res;
  }

  @Override
  public String getContent() {
    return parse(getStream());
  }

  @Override
  public Field getContentField() {

    return new Field("contents", getContent(), Field.Store.NO,
        Field.Index.TOKENIZED);
  }

}