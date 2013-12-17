package nl.esciencecenter.vbrowser.vrs;

import java.util.Properties;

import nl.esciencecenter.ptk.ssl.CertificateStore;
import nl.esciencecenter.ptk.ssl.CertificateStoreException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.registry.Registry;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceSystemInfoRegistry;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class VRSContext
{
    protected Registry registry;
    
    protected VRSProperties vrsProperties;

    private ResourceSystemInfoRegistry resourceInfoRegistry; 
    
    protected VRSContext(Properties props)
    {
        // default Static Registry ! 
        this.registry=Registry.getInstance(); 
        this.vrsProperties=new VRSProperties(props); 
        resourceInfoRegistry=new ResourceSystemInfoRegistry(this);
    }
    
    protected Registry getRegistry()
    {
        return registry;  
    }
    
    public VRSProperties getProperties()
    {
        return this.vrsProperties; 
    }
    
    public CertificateStore getCertificateStore() throws VrsException
    {
        try
        {
            return CertificateStore.getDefault(true);
        }
        catch (CertificateStoreException e)
        {
           throw new VrsException(e.getMessage(),e); 
        }
    }
    
    public ResourceSystemInfo getResourceSystemInfoFor(VRL vrl, boolean autoCreate) throws VrsException
    {
        VResourceSystemFactory fac = registry.getVResourceSystemFactoryFor(this, vrl.getScheme());
        if (fac==null)
        {   
            throw new VrsException("Scheme not supported. No ResourceSystemFactory for:"+vrl);
        }
        
        String id=fac.createResourceSystemId(vrl);
        
        ResourceSystemInfo info= resourceInfoRegistry.getInfo(id);
        
        if ((info==null) && (autoCreate==true))
        {
            info=new ResourceSystemInfo(vrl);
            
        }
        return info; 
    }

    public void putResourceSystemInfo(String id, ResourceSystemInfo info)
    {
        resourceInfoRegistry.putInfo(id, info); 
    }

}
