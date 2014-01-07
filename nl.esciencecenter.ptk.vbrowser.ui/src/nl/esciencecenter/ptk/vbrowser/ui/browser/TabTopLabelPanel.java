package nl.esciencecenter.ptk.vbrowser.ui.browser;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicButtonUI;

import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.ActionMethod;

public class TabTopLabelPanel extends JPanel
{
    private static final long serialVersionUID = 5566174001482465094L;

    public static enum TabButtonType {Delete,Add}; 
    
    private final static MouseListener buttonMouseListener = new MouseAdapter()
    {
        public void mouseEntered(MouseEvent e)
        {
            Component component = e.getComponent();
            if (component instanceof AbstractButton)
            {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }

        public void mouseExited(MouseEvent e)
        {
            Component component = e.getComponent();
            if (component instanceof AbstractButton)
            {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };
    
    protected class TabButton extends JButton // implements ActionListener
    {
        private static final long serialVersionUID = -3932012699584733182L;
    
        TabButtonType type; 
        
        public TabButton(TabButtonType buttonType)
        {
            int size = 17;
            this.type=buttonType; 
            
            setPreferredSize(new Dimension(size, size));
            switch (type)
            {
                case Delete:
                    setToolTipText("Close this tab");
                    this.setIcon(new ImageIcon(MiniIcons.getTabDeleteImage()));
                    break;
                case Add: 
                    setToolTipText("Copy tab");
                    this.setIcon(new ImageIcon(MiniIcons.getTabAddImage()));
                    break; 
                default: 
                    setToolTipText("?");
                    this.setIcon(new ImageIcon(MiniIcons.getMiniQuestionmark()));
                    break; 
            }
            
            // Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            // Make it transparent
            setContentAreaFilled(false);
            // No need to be focusable
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            // Making nice rollover effect
            // we use the same listener for all buttons
            addMouseListener(buttonMouseListener);
            setRolloverEnabled(true);
            // Close the proper tab by clicking the button
            // addActionListener(this);
        }

        public void updateUI()
        {
        }

        public TabContentPanel getTabPanel()
        {
            return TabTopLabelPanel.this.getTabPanel();
        }
    }
        
    private TabButton addButton;

    private TabButton delButton;
    
    private TabContentPanel tabPane; 
    
    public TabTopLabelPanel(final TabContentPanel pane,final BrowserFrame.TabButtonHandler buttonHandler)
    {
        
        // unset default FlowLayout' gaps
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));

        if (pane == null)
        {
            throw new NullPointerException("Tab pane or Parent TabbedPane is null");
        }
        
        tabPane=pane;

        setOpaque(false);

        // make JLabel read titles from JTabbedPane
        @SuppressWarnings("serial")
        JLabel label = new JLabel()
        {
            public String getText()
            {
                return pane.getName();
            }
        };

        add(label);
        // add more space between the label and the button
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        // tab button

        {
            delButton = new TabButton(TabButtonType.Delete);
            add(delButton);
            delButton.setActionCommand(""+ActionMethod.CLOSE_TAB);  
            delButton.addActionListener(buttonHandler);
        }
        {
            addButton = new TabButton(TabButtonType.Add);
            add(addButton);
            addButton.setActionCommand(""+ActionMethod.NEW_TAB); 
            addButton.addActionListener(buttonHandler);
        }
        
        // add more space to the top of the component
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    }
    
    TabContentPanel getTabPanel()
    {
        return this.tabPane; 
    }
    
    public void setEnableAddButton(boolean value)
    {
        addButton.setVisible(value);
    }
    
   
}