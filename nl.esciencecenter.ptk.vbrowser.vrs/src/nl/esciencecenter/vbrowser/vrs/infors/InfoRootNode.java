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

package nl.esciencecenter.vbrowser.vrs.infors;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.vbrowser.vrs.VFSPath;
import nl.esciencecenter.vbrowser.vrs.VPath;
import nl.esciencecenter.vbrowser.vrs.VRSClient;
import nl.esciencecenter.vbrowser.vrs.data.xml.XMLData;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class InfoRootNode extends InfoRSNode implements VInfoResourceFolder
{
    private static final ClassLogger logger=ClassLogger.getLogger(InfoRootNode.class);
    
    // ========
    // Instance
    // ========
    
    protected InfoRS infors;

    protected LocalSystem localSystem;

    protected InfoConfigNode configNode;

    protected boolean autoSaveConfig=true; 
    
    public InfoRootNode(InfoRS infoRS) throws VrsException
    {
        super(infoRS, InfoRSConstants.INFOSYSTEMROOTNODE, new VRL("info", null, 0, "/"));
        infors = infoRS;
        init();
    }

    protected void init() throws VrsException
    {
        initChilds();
    }

    protected void initChilds() throws VrsException
    {
        this.nodes.clear();
        this.addNode(getConfigNode());
        this.addNode(getLocalSystem());
    }

    public InfoRSNode getNode(VRL vrl) throws VrsException
    {
        String paths[] = vrl.getPathElements();

        if (paths == null)
            return this;

        int n = paths.length;
        if (n == 0)
        {
            return this;
        }

        if (n > 0)
        {
            InfoRSNode node = this.findSubNode(vrl, true);

            if (node != null)
            {
                return node;
            }
        }

        throw new VrsException("Node not found:" + vrl);
    }

    protected LocalSystem getLocalSystem() throws VrsException
    {
        if (localSystem == null)
        {
            initLocalSystem();
        }

        return localSystem;
    }

    protected InfoConfigNode getConfigNode()
    {
        if (configNode == null)
        {
            initConfigNode();
        }

        return configNode;
    }

    protected void initLocalSystem() throws VrsException
    {
        localSystem = new LocalSystem(this);
    }

    protected void initConfigNode()
    {
        configNode = new InfoConfigNode(this);
    }

    public VInfoResource addResourceLink(String folderName, String logicalName, VRL targetLink, String optIconURL) throws VrsException
    {
        InfoRSNode parentNode;

        if (folderName != null)
        {
            parentNode = this.getSubNode(folderName);
            if (parentNode == null)
            {
                parentNode = this.createResourceFolder(folderName, null);
            }
        }
        else
        {
            parentNode = this;
        }

        InfoResourceNode node = InfoResourceNode.createLinkNode(parentNode, logicalName, targetLink, optIconURL, true);
        parentNode.addNode(node);
        save(); 
        return node; 
    }

    public InfoResourceNode createResourceFolder(String folderName, String optIconURL) throws VrsException
    {
        InfoRSNode node = this.getSubNode(folderName);
        
        InfoResourceNode folder; 
        
        if (node instanceof InfoResourceNode)
        {
            return (InfoResourceNode) node;
        }
        else if (node != null)
        {
            throw new VrsException("Type Mismatch: InfoRSNode name'" + folderName + "' already exists, but is not a InfoResourceNode:"
                    + node);
        }
        else
        {
            folder = InfoResourceNode.createFolderNode(this, folderName, optIconURL);
            this.addNode(folder);
            
        }
        save();
        return folder;  
    }

    public List<String> getChildResourceTypes()
    {
        // Root Node support default InfoRS types:
        return defaultFolderChildTypes;
    }

    @Override
    public void addSubNode(InfoRSNode subNode) throws VrsException
    {
        this.addNode(subNode); 
    }

    @Override
    public InfoResourceNode createFolder(String name) throws VrsException
    {
        return this.createResourceFolder(name, null); 
    }

    @Override
    public boolean isResourceLink()
    {
        return false;
    }

    @Override
    public VRL getTargetVRL()
    {
        return null;
    }

    @Override
    public boolean isResourceFolder()
    {
        return true; 
    }

    @Override
    public VInfoResource createResourceLink(VRL targetVRL,String logicalName) throws VrsException
    {
        return addResourceLink(null,logicalName,targetVRL,null); 
    }

    public String toXML() throws VrsException
    {
        XMLData xmlData=new XMLData(this.getVRSContext()); 
        String xml=xmlData.toXML(this); 
        return xml; 
    }

    public VRL getPersistantConfigVRL()
    {
        VRL configVrl=this.getVRSContext().getPersistantConfigLocation();
        if (configVrl==null)
        {
            logger.errorPrintf("Persistant configuration enabled, but no persistant save location defined\n");
            return null;
        }
        
        VRL rootConfigVrl=configVrl.appendPath("infors.rsfx");
        return rootConfigVrl;
    }
    
    protected void save()
    {
        // check autosave 
        if ((this.getVRSContext().hasPersistantConfig()==false) || (autoSaveConfig==false)) 
        {
            return; 
        }
        
        VRL saveVrl=getPersistantConfigVRL(); 
        
        try
        {
            saveTo(saveVrl);
        }
        catch(VrsException e)
        {
            logger.logException(ClassLogger.ERROR, e,"Failed to save RootNode:%s to:%s\n",this,saveVrl);
        }
    }
    
    protected void saveTo(VRL configVrl) throws VrsException 
    {
        VRSClient vrsClient=this.infors.getVRSClient(); 
        String xml=toXML();
        xml=XMLData.prettyFormat(xml, 3); 
        try
        {
            VFSPath path = vrsClient.openVFSPath(configVrl); 
            VFSPath dir=path.getParent();
            
            if (dir.exists()==false)
            {
                logger.infoPrintf("Creating new config dir:%s\n",dir);
                dir.mkdirs(true);
            }
            
            vrsClient.createResourceLoader().writeTextTo(configVrl.toURI(), xml);
        }
        catch (IOException | URISyntaxException e)
        {
            throw new VrsException(e.getMessage(),e); 
        }
    }
    
    protected void load()
    {
        if (this.getVRSContext().hasPersistantConfig()==false)
        {
            return; 
        }
        
        VRL loadVrl=getPersistantConfigVRL(); 
        
        try
        {
            loadFrom(loadVrl);
        }
        catch(VrsException e)
        {
            logger.logException(ClassLogger.ERROR, e,"Failed to save RootNode:%s to:%s\n",this,loadVrl);
        }
    }

    protected void loadFrom(VRL loadVrl) throws VrsException
    {
        VRSClient vrsClient=this.infors.getVRSClient();
            
        try
        {
            VFSPath path = vrsClient.openVFSPath(loadVrl); 
            if (path.exists()==false)
            {
                logger.infoPrintf("Root xonfig XML file not found:%s", loadVrl);
                return;
            }
            
            String xml=vrsClient.createResourceLoader().readText(loadVrl.toURI());
            XMLData data=new XMLData(this.getVRSContext()); 
            data.addXMLResourceNodesTo(this, xml);
            
        }
        catch (IOException | URISyntaxException e)
        {
            throw new VrsException("Failed to load config from:"+loadVrl+".\n"+e.getMessage(),e); 
        }
    }
    
    public void loadPersistantConfig()
    {
        this.load();
    }
    
}
