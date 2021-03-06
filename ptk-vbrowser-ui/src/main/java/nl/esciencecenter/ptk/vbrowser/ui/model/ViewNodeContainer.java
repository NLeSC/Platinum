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

package nl.esciencecenter.ptk.vbrowser.ui.model;

import java.awt.Point;

import javax.swing.JPopupMenu;

import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserInterface;

/**
 * Interface for any (J)Component which can contain ViewNodes. A ViewNodeContainer in itself is also
 * a ViewNodeComponent.
 */
public interface ViewNodeContainer extends ViewNodeComponent {
    public ViewNode getNodeUnderPoint(Point p);

    /**
     * Create Pop-up menu when (right-)clicked the specified actionSourceNode.
     * 
     * @param actionSourceNode
     *            ViewNode the click occured.
     * @param canvasMenu
     *            - whether this is a click the canvas (white space between the icons).
     * @return JPopupMenu
     */
    public JPopupMenu createNodeActionMenuFor(ViewNode actionSourceNode, boolean canvasMenu);

    // === Selection Model === 
    public void clearNodeSelection();

    public ViewNode[] getNodeSelection();

    /** Toggle selection */
    public void setNodeSelection(ViewNode node, boolean isSelected);

    /** Toggle selection of range */
    public void setNodeSelectionRange(ViewNode firstNode, ViewNode lastNode, boolean isSelected);

    /** Request focus for child. Return true if it has focus. */
    public boolean requestNodeFocus(ViewNode node, boolean value);

    public BrowserInterface getBrowserInterface();

}
