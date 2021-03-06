/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.control.webharvest.protocol;

import com.esri.gpt.control.webharvest.protocol.factories.AgpProtocolFactory;
import com.esri.gpt.control.webharvest.protocol.factories.ArcGISProtocolFactory;
import com.esri.gpt.control.webharvest.protocol.factories.ArcImsProtocolFactory;
import com.esri.gpt.control.webharvest.protocol.factories.CswProtocolFactory;
import com.esri.gpt.control.webharvest.protocol.factories.OaiProtocolFactory;
import com.esri.gpt.control.webharvest.protocol.factories.ResourceProtocolFactory;
import com.esri.gpt.control.webharvest.protocol.factories.ThreddsProtocolFactory;
import com.esri.gpt.control.webharvest.protocol.factories.WafProtocolFactory;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Collection of protocol factories.
 */
public class ProtocolFactories extends TreeMap<String, ProtocolFactory> {

/**
 * Creates instance of the factories.
 */
public ProtocolFactories() {
  super(String.CASE_INSENSITIVE_ORDER);
}

/**
 * Initializes collection with default protocol factories.
 */
public void initDefault() {
  put("ArcIms" , new ArcImsProtocolFactory());
  put("CSW"    , new CswProtocolFactory());
  put("OAI"    , new OaiProtocolFactory());
  put("WAF"    , new WafProtocolFactory());
  put("RES"    , new ResourceProtocolFactory());
  put("ARCGIS" , new ArcGISProtocolFactory());
  put("AGP"    , new AgpProtocolFactory());
  put("THREDDS", new ThreddsProtocolFactory());
}

/**
 * Parses protocol.
 * @param xmlString protocol as XML string
 * @return protocol
 * @throws ProtocolException if error parsing protocol
 */
public Protocol parseProtocol(String xmlString) {
  try {
    Document doc = DomUtil.makeDomFromString(xmlString, false);

    String protocolName = "";
    long flags = 0;
    List<String> vDest = null;
    StringAttributeMap properties = new StringAttributeMap();
    NodeList protocolNL = doc.getElementsByTagName("protocol");

    if (protocolNL.getLength() >= 1) {
      Node protocolN = protocolNL.item(0);

      NamedNodeMap attributes = protocolN.getAttributes();

      Node protocolTypeN = attributes.getNamedItem("type");
      protocolName = Val.chkStr(protocolTypeN!=null? protocolTypeN.getNodeValue(): "");

      Node flagsN = attributes.getNamedItem("flags");
      flags = flagsN!=null? Val.chkLong(Val.chkStr(flagsN.getNodeValue()), 0): 0;
      
      Node destinationsN = attributes.getNamedItem("destinations");
      String sDest = destinationsN!=null? Val.chkStr(destinationsN.getNodeValue()): null;
      vDest = sDest!=null? Arrays.asList(sDest.split(",")): null;

      NodeList propertiesNL = protocolN.getChildNodes();
      for (int i = 0; i < propertiesNL.getLength(); i++) {
        Node property = propertiesNL.item(i);
        String propertyName = property.getNodeName();
        String propertyValue = property.getTextContent();
        properties.set(propertyName, propertyValue);
      }
    }

    ProtocolFactory protocolFactory = get(protocolName);
    if (protocolFactory == null) {
      throw new IllegalArgumentException("Unsupported protocol: " + protocolName);
    }

    Protocol protocol = protocolFactory.newProtocol();
    protocol.setFlags(flags);
    protocol.applyAttributeMap(properties);
    ProtocolInvoker.setDestinations(protocol, vDest);

    return protocol;
  } catch (ParserConfigurationException ex) {
    throw new IllegalArgumentException("Error parsing protocol.", ex);
  } catch (SAXException ex) {
    throw new IllegalArgumentException("Error parsing protocol.", ex);
  } catch (IOException ex) {
    throw new IllegalArgumentException("Error parsing protocol.", ex);
  }
}

@Override
public String toString() {
  StringBuilder sb = new StringBuilder(getClass().getName()).append(" (\r\n");
  for (Map.Entry<String, ProtocolFactory> e : this.entrySet()) {
    sb.append("protocol: name=\"").append(e.getKey()).append("\", factoryClass=\"").append(e.getValue().getClass().getCanonicalName()).append("\"\r\n");
  }
  sb.append(") ===== end ").append(getClass().getName());
  return sb.toString();
}
}
