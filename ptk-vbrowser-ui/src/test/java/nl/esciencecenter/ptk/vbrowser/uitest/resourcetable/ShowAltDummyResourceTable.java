package nl.esciencecenter.ptk.vbrowser.uitest.resourcetable;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import nl.esciencecenter.ptk.browser.uitest.dummy.StartDummyBrowser;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserInterfaceAdaptor;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserPlatform;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.ptk.vbrowser.ui.resourcetable.ResourceTable;
import nl.esciencecenter.ptk.vbrowser.ui.resourcetable.ResourceTableModel;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;

public class ShowAltDummyResourceTable {
    public static void main(String args[]) {
        try {
            showTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showTable() throws VRLSyntaxException, ProxyException {
        BrowserPlatform platform = StartDummyBrowser.getDummyPlatform();

        JFrame frame = new JFrame();
        frame.setSize(800, 600);

        frame.setLayout(new BorderLayout());

        JScrollPane pane = new JScrollPane();
        frame.add(pane, BorderLayout.CENTER);

        BrowserInterfaceAdaptor browserInterface = new BrowserInterfaceAdaptor(platform);

        JPopupMenu popMenu = new JPopupMenu();
        {
            popMenu.add(new JMenuItem("Dummy"));
        }

        browserInterface.setPopupMenu(popMenu);
        ResourceTable table = new ResourceTable(browserInterface, new ResourceTableModel(false));
        pane.setViewportView(table);

        //        VRL vrl = new VRL("dummy:///");
        //        ProxyFactory dummyFac = platform.getProxyFactoryFor(vrl);
        //        ProxyNode root = dummyFac.openLocation("dummy:///");

        table.setDataSource(new AltDummyDataSource(), true);

        // ProxyNodeDataSourceProvider dataSource = new ProxyNodeDataSourceProvider (root);
        // table.setDataSource(dataSource, true);

        frame.setVisible(true);

    }

}
