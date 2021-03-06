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

package nl.esciencecenter.vbrowser.vrs.registry;

import java.util.LinkedHashMap;
import java.util.Map;

import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VResourceSystem;

public class ResourceSystemInstances extends LinkedHashMap<String, LinkedHashMap<String, VResourceSystem>> {
    // not serializable!
    private static final long serialVersionUID = 1L;

    public Map<String, VResourceSystem> getResourceSystemsFor(VRSContext vrsContext) {
        return this.get("" + vrsContext.getID());
    }

    public VResourceSystem getResourceSystem(VRSContext vrsContext, String resourceId) {
        Map<String, VResourceSystem> list = this.getResourceSystemsFor(vrsContext);
        if (list == null)
            return null;

        return list.get(resourceId);
    }

    public VResourceSystem putResourceSystem(String contextId, String resourceId, VResourceSystem vrs) {
        LinkedHashMap<String, VResourceSystem> list = this.get(contextId);
        if (list == null) {
            list = new LinkedHashMap<String, VResourceSystem>();
            this.put(contextId, list);
        }
        return list.put(resourceId, vrs);
    }

    public void unregisterResourceSystemsFor(VRSContext vrsContext) {
        this.remove("" + vrsContext.getID());
    }

}
