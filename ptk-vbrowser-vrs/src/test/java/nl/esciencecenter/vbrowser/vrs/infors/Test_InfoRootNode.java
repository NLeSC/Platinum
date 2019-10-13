package nl.esciencecenter.vbrowser.vrs.infors;

import nl.esciencecenter.vbrowser.vrs.VRSClient;
import org.junit.Assert;
import org.junit.Test;

public class Test_InfoRootNode {

    @Test
    public void testGetRootNode() throws Exception {
        VRSClient vrsClient = Test_InfoRS.initTestClient();
        InfoRootNode rootNode = vrsClient.getInfoRootNode();
        Assert.assertNotNull(rootNode);

    }

}
