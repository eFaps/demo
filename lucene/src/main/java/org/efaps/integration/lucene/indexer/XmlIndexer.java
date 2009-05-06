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

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.lucene.document.Field;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class for getting the content out of an "XML"-File
 * 
 * @author jmo
 * 
 */
public class XmlIndexer extends AbstractIndexer {

  static Log LOG = getLog();

  /**
   * Parses the XML-File to a <code>org.w3c.dom.Document</code> and then
   * extracts the content by using <link>parse(org.w3c.dom.Document _document)</link>
   * 
   * @param _InputStream
   *          with the XML-File
   * @return String with the content
   */
  public static String parse(InputStream _InputStream) {
    org.w3c.dom.Document document = null;
    DocumentBuilder builder = null;
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    try {
      builder = factory.newDocumentBuilder();

      document = builder.parse(_InputStream);
    } catch (ParserConfigurationException e) {

      LOG.error("parse(InputStream)", e);
    } catch (SAXException e) {

      LOG.error("parse(InputStream)", e);
    } catch (IOException e) {

      LOG.error("parse(InputStream)", e);
    }

    return parse(document);
  }

  public static String parse(org.w3c.dom.Document _document) {

    StringBuffer XMLStream = new StringBuffer();
    NodeList ls = _document.getChildNodes();
    for (int i = 0; i < ls.getLength(); i++) {

      String text = "";

      short NodType = ls.item(i).getNodeType();

      if (NodType == org.w3c.dom.Node.ELEMENT_NODE
          || NodType == org.w3c.dom.Node.TEXT_NODE) {
        text = ls.item(i).getTextContent().trim();
      } else if (NodType == org.w3c.dom.Node.ATTRIBUTE_NODE) {
        text = ls.item(i).getNodeValue().trim();
      } else if (NodType == org.w3c.dom.Node.CDATA_SECTION_NODE) {
        text = ls.item(i).getTextContent().trim();
      } else if (NodType == org.w3c.dom.Node.COMMENT_NODE) {

      } else if (NodType == org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE) {

      } else if (NodType == org.w3c.dom.Node.ENTITY_NODE) {

      }
      if (text != "") {
        XMLStream.append(text);

      }

    }
    return XMLStream.toString().trim();
  }

  @Override
  public String getContent() {
    return parse(super.getStream());
  }

  @Override
  public Field getContentField() {

    return new Field("contents", getContent(), Field.Store.NO,
        Field.Index.TOKENIZED);
  }

}
