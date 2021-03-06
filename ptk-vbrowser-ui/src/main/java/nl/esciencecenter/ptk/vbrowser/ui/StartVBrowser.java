/*
 * Copyright 2012-2014 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at the following location:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * For the full license, see: LICENSE.txt (located in the root folder of this distribution).
 * ---
 */
// source:

package nl.esciencecenter.ptk.vbrowser.ui;

import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserPlatform;
import nl.esciencecenter.ptk.vbrowser.ui.browser.ProxyBrowserController;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.vrs.VRSProxyFactory;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.infors.InfoRootNode;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Start VBrowser with Virtual Info Resource Root.
 */
public class StartVBrowser {

    public static void main(String args[]) {
        try {
            startVBrowser(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BrowserPlatform getPlatform() {
        // vbrowser
        return BrowserPlatform.getInstance("vbrowser");
    }

    public static ProxyBrowserController startVBrowser(String args[]) throws Exception {

        // BrowserPlatform
        BrowserPlatform platform = getPlatform();

        // VRSContext 
        VRSContext context = platform.getVRSContext();
        VRL config = context.getHomeVRL().resolvePath(".vrsrc");
        platform.setPersistantConfigLocation(config, true);

        // ProxyBrowser
        ProxyBrowserController browser = (ProxyBrowserController) platform.createBrowser();
        VRSProxyFactory fac = VRSProxyFactory.createFor(platform);
        platform.registerProxyFactory(fac);

        // Start with Root InfoNode:
        InfoRootNode rootNode = fac.getVRSClient().getInfoRootNode();
        rootNode.loadPersistantConfig();

        // Add default links, will be ignored if already exists. 
        rootNode.addResourceLink("My Links", "Root:/", new VRL("file:///"), null, false);
        rootNode.addResourceLink("My Links", "Home/", context.getHomeVRL(), null, false);

        // main location to start browsing: 
        ProxyNode root = fac.openLocation("info:/");
        browser.setRoot(root, true, true);

        return browser;
    }

}
