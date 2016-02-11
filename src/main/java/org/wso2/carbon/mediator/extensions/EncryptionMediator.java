/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.mediator.extensions;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axiom.soap.SOAPBody;
import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.jaxen.JaxenException;
import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EncryptionMediator extends AbstractMediator {
    private String xpath;
    private String disabled;

    @Override
    public boolean mediate(MessageContext messageContext) {

        // If encryption has been disabled
        if (!disabled.isEmpty() && Boolean.parseBoolean(disabled)) {
            return true;
        }

        SOAPBody soapBody = messageContext.getEnvelope().getBody();

        if (xpath.isEmpty()) { // This property is mandatory
            handleException("xpath property not set", messageContext);
        }

        try {
            AXIOMXPath xpathExpression = new AXIOMXPath(xpath);

            List nodeList = xpathExpression.selectNodes(soapBody);

            log.debug("Number of matching elements found : " + nodeList.size());

            for (Object item : nodeList) {
                OMNode node = (OMNode) item;

                if (OMNode.ELEMENT_NODE == node.getType()) {
                    String value = ((OMElement) node).getText();

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    Date currentDateTime = new Date();

                    // Concat value with current datetime for better obfuscation
                    value = value.concat('|' + dateFormat.format(currentDateTime));

                    // Base64 hash value
                    ((OMElement) node).setText(new String(Base64.encodeBase64(value.getBytes(StandardCharsets.UTF_8)),
                                                                        StandardCharsets.UTF_8));
                }
            }

        } catch (JaxenException e) {
            handleException("Error in xpath expression", e, messageContext);
        }

        return true;
    }


    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public String getDisabled() {
        return disabled;
    }

    public void setDisabled(String disabled) {
        this.disabled = disabled;
    }
}
