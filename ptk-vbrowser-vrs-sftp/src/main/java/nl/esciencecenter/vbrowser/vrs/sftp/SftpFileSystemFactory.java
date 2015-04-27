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

package nl.esciencecenter.vbrowser.vrs.sftp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.VResourceSystemFactory;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceSystemInfo;
import nl.esciencecenter.vbrowser.vrs.sftp.jsch.SftpConfig;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import com.jcraft.jsch.JSch;

public class SftpFileSystemFactory implements VResourceSystemFactory
{
    private final static Logger logger = LoggerFactory.getLogger(SftpFileSystemFactory.class);

    private JSch jsch;

    public SftpFileSystemFactory() throws VrsException
    {
        // an JSch instance is created at the VRSFactory level.
        // an JSch Session is created per SftpFileSystem session.
        this.jsch = new JSch();
    }

    @Override
    public String[] getSchemes()
    {
        return new String[] {
                "sftp", "ssh-ftp"
        };
    }

    @Override
    public String createResourceSystemId(VRL vrl)
    {
        // Add User ? -> check User authenticated contexts
        return "sftp-" + vrl.getHostname() + "-" + vrl.getPort();
    }

    @Override
    public VResourceSystem createResourceSystemFor(VRSContext context, ResourceSystemInfo info, VRL vrl) throws VrsException
    {
        return new SftpFileSystem(jsch, context, info, vrl);
    }

    @Override
    public ResourceSystemInfo updateResourceInfo(VRSContext context, ResourceSystemInfo info, VRL vrl)
    {
        logger.info("updateResourceInfo:{}",info);
        // comma seperated list: 
        info.setIfNotSet(ResourceSystemInfo.ATTR_USER_IDENTITY_FILES, "id_rsa,id_dsa", true);
        info.setIfNotSet(ResourceSystemInfo.SERVER_USERINFO, context.getUserName(), true);
        info.setIfNotSet(SftpFileSystem.SSH_USER_KNOWN_HOSTS_PROPERTY, SftpConfig.SSH_USER_KNOWN_HOSTS, true);
        info.setIfNotSet(SftpFileSystem.SSH_USER_CONFIGSUBDIR_PROPERTY, SftpConfig.SSH_USER_CONFIG_SIBDUR, true);
        info.store(); 
        logger.info("updateResourceInfo:Updated:{}",info);
        return info;
    }
}