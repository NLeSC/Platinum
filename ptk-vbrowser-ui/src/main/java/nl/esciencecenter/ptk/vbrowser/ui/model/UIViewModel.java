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

import java.awt.Color;
import java.awt.Dimension;

/**
 * Holds UI attributes.
 */
public class UIViewModel {
    // ======
    // Class
    // ======

    public static enum UIDirection {
        HORIZONTAL, VERTICAL
    };

    public static enum UIAlignment {
        LEFT, CENTER, RIGHT, FILL
    };

    public static UIViewModel createTreeViewModel() {
        UIViewModel model = new UIViewModel();
        model.iconSize = 16;
        return model;
    }

    public static UIViewModel createIconsModel(int size) {
        UIViewModel iconsModel = new UIViewModel();

        iconsModel.iconSize = size;
        iconsModel.iconLayoutDirection = UIDirection.HORIZONTAL;
        iconsModel.iconLabelPlacement = UIDirection.VERTICAL;
        iconsModel.maximumIconLabelWidth = size * 5;

        return iconsModel;
    }

    public static UIViewModel createIconsListModel(int size) {
        UIViewModel iconsListModel = new UIViewModel();

        iconsListModel.iconSize = size;
        iconsListModel.iconLayoutDirection = UIDirection.VERTICAL;
        iconsListModel.iconLabelPlacement = UIDirection.HORIZONTAL;
        iconsListModel.maximumIconLabelWidth = size * 20;

        return iconsListModel;
    }

    public static UIViewModel createTableModel() {
        UIViewModel defaultTableModel = new UIViewModel();

        defaultTableModel.iconSize = 16;
        defaultTableModel.maximumIconLabelWidth = 120;

        return defaultTableModel;
    }

    // ========================================================================
    // instance
    // ======================================================================== 

    /**
     * Hierarchical UI properties, if an int value is -1 or an object value is null, inherit the
     * value from the parent.
     */
    final protected UIViewModel parent;

    /** Icons size. -1 = inherit from parent. */
    protected int iconSize = 48;

    /** Direction of icons to layout in list mode. */
    private UIDirection iconLayoutDirection = UIDirection.HORIZONTAL;

    /** Place of label under or next to icon */
    private UIDirection iconLabelPlacement = UIDirection.VERTICAL;

    /** Horizontal gap between icons. -1 = inherit from parent. */
    protected int iconHGap = 8;

    /** Vertical gap between icons. -1 = inherit form parent. */
    protected int iconVGap = 8;

    private int maximumIconLabelWidth = 180;

    private Color fgColor = Color.BLACK;

    private Color bgColor = Color.WHITE;

    private Color fgColorSelected = null; // null=default; Color.RED;

    private Color bgColorSelected = Color.LIGHT_GRAY;

    public UIViewModel() {
        parent = null;
    }

    public UIViewModel(UIViewModel parent) {
        this.parent = parent;
    }

    public UIViewModel getParent() {
        return this.parent;
    }

    public int getIconSize() {
        // iconSize=-1 means inherit from parent; 
        if ((iconSize < 0) && (parent != null))
            return parent.getIconSize();

        return iconSize;
    }

    public Dimension getIconDimensions() {
        return new Dimension(getIconSize(), getIconSize());
    }

    public void setIconSize(int size) {
        iconSize = size;
    }

    public UIDirection getIconLabelPlacement() {
        return iconLabelPlacement;
    }

    public UIDirection getIconLayoutDirection() {
        return iconLayoutDirection;
    }

    // ==============================
    // Layout and GUI stuff  
    // ==============================

    public int getMaxIconLabelWidth() {
        if ((maximumIconLabelWidth < 0) && (parent != null))
            return parent.getMaxIconLabelWidth();

        return this.maximumIconLabelWidth;
    }

    public Color getCanvasBGColor() {
        return Color.white;
    }

    /** Horizontal space between Icons */
    public int getIconHGap() {
        if ((iconHGap < 0) && (parent != null))
            return parent.getIconHGap();

        return iconHGap;
    }

    /** Vertical space between Icons */
    public int getIconVGap() {
        if ((iconVGap < 0) && (parent != null))
            return parent.getIconVGap();

        return iconVGap;
    }

    public Color getFontHighlightColor() {
        return Color.blue;
    }

    public Color getFontColor() {
        return Color.black;
    }

    public Color getForegroundColor() {
        if ((fgColor == null) && (parent != null))
            return parent.fgColor;

        return fgColor;
    }

    public Color getBackgroundColor() {
        if ((bgColor == null) && (parent != null))
            return parent.bgColor;

        return bgColor;
    }

    public Color getSelectedForegroundColor() {
        if ((fgColorSelected == null) && (parent != null))
            return parent.fgColorSelected;

        return fgColorSelected;
    }

    public Color getSelectedBackgroundColor() {
        if ((bgColorSelected == null) && (parent != null))
            return parent.bgColorSelected;

        return bgColorSelected;
    }

}
