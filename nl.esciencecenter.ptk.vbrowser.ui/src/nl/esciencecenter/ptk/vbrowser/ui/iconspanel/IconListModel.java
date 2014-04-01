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

package nl.esciencecenter.ptk.vbrowser.ui.iconspanel;

import java.util.Vector;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import nl.esciencecenter.ptk.vbrowser.ui.UIGlobal;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class IconListModel // implements ListModel
{
    protected Vector<IconItem> icons = new Vector<IconItem>();

    protected Vector<ListDataListener> listeners = new Vector<ListDataListener>();

    public IconListModel()
    {

    }

    // @Override
    public int getSize()
    {
        return icons.size();
    }

    // @Override
    public IconItem getElementAt(int index)
    {
        return icons.get(index);
    }

    // @Override
    public void addListDataListener(ListDataListener l)
    {
        listeners.add(l);
    }

    // @Override
    public void removeListDataListener(ListDataListener l)
    {
        listeners.remove(l);
    }

    public ListDataListener[] getListeners()
    {
        synchronized (this.listeners)
        {
            ListDataListener _arr[] = new ListDataListener[this.listeners.size()];
            _arr = this.listeners.toArray(_arr);
            return _arr;
        }
    }

    public void setItems(IconItem[] items)
    {
        if (this.icons == null)
        {
            icons = new Vector<IconItem>();
        }
        else
        {
            synchronized (this.icons)
            {
                icons.clear();
            }
        }

        synchronized (this.icons)
        {
            // add item and fire event per item if this is an incremental
            // update;
            if (items != null)
                for (IconItem item : items)
                {
                    addItem(item, false);
                }
        }

        this.uiFireContentsChanged();
    }

    public void addItem(IconItem item, boolean fireEvent)
    {
        int pos;

        synchronized (icons)
        {
            pos = this.icons.size();
            this.icons.add(item);
        }

        if (fireEvent)
        {
            uiFireChildAdded(pos);
        }
    }

    public void deleteItem(VRL vrl, boolean fireEvent)
    {
        IconItem delItem = null;

        synchronized (icons)
        {
            for (IconItem item : icons)
            {
                if (item.getViewNode().matches(vrl))
                {
                    delItem = item;
                    break;
                }
            }
        }

        deleteItem(delItem, fireEvent);
    }

    public void deleteItem(IconItem item, boolean fireEvent)
    {
        synchronized (icons)
        {
            int pos = this.icons.indexOf(item);
            this.icons.remove(item);
            this.uiFireRangeRemoved(pos, pos);
        }
    }

    public void uiFireRangeRemoved(final int pos)
    {
        uiFireRangeRemoved(pos, pos);
    }

    public void uiFireRangeRemoved(final int pos, final int inclusiveEndPos)
    {

        if (UIGlobal.isGuiThread() == false)
        {
            Runnable updater = new Runnable()
            {
                @Override
                public void run()
                {
                    uiFireRangeRemoved(pos, inclusiveEndPos);
                }
            };

            UIGlobal.swingInvokeLater(updater);
            return;
        }

        // range is inclusive: [pos,pos]
        ListDataEvent event = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, pos, inclusiveEndPos);

        for (ListDataListener l : getListeners())
        {
            l.intervalRemoved(event);
        }
    }

    public void uiFireChildAdded(final int pos)
    {
        if (UIGlobal.isGuiThread() == false)
        {
            Runnable updater = new Runnable()
            {
                @Override
                public void run()
                {
                    uiFireChildAdded(pos);
                }
            };

            UIGlobal.swingInvokeLater(updater);
            return;
        }

        // range is inclusive: [pos,pos]
        ListDataEvent event = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, pos, pos);

        for (ListDataListener l : getListeners())
            l.intervalAdded(event);
    }

    public void uiFireContentsChanged()
    {
        if (UIGlobal.isGuiThread() == false)
        {
            Runnable updater = new Runnable()
            {
                @Override
                public void run()
                {
                    uiFireContentsChanged();
                }
            };

            UIGlobal.swingInvokeLater(updater);
            return;
        }

        // range is inclusive: [pos,pos]
        ListDataEvent event = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, icons.size() - 1);

        for (ListDataListener l : getListeners())
            l.contentsChanged(event);
    }

}
