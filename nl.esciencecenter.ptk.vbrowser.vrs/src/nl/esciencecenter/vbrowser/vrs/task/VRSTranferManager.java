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

package nl.esciencecenter.vbrowser.vrs.task;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.ptk.data.ExtendedList;
import nl.esciencecenter.ptk.data.Holder;
import nl.esciencecenter.ptk.data.VARHolder;
import nl.esciencecenter.ptk.io.IOUtil;
import nl.esciencecenter.ptk.task.ITaskMonitor;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.vbrowser.vrs.VFSPath;
import nl.esciencecenter.vbrowser.vrs.VFileSystem;
import nl.esciencecenter.vbrowser.vrs.VPath;
import nl.esciencecenter.vbrowser.vrs.VRSClient;
import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.infors.VInfoResource;
import nl.esciencecenter.vbrowser.vrs.infors.VInfoResourcePath;
import nl.esciencecenter.vbrowser.vrs.io.VDeletable;
import nl.esciencecenter.vbrowser.vrs.io.VStreamReadable;
import nl.esciencecenter.vbrowser.vrs.io.VStreamWritable;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Default Copy and move manager to perform both VFS and non VFS Copy/Move
 * actions. Not all resources can be read from or written to.
 */
public class VRSTranferManager
{
    private static final ClassLogger logger = ClassLogger.getLogger(VRSTranferManager.class);

    // ========
    // Instance
    // ========

    protected VRSClient vrsClient;

    protected VRSTaskWatcher taskWatcher;

    public VRSTranferManager(VRSClient vrsClient)
    {
        this.vrsClient = vrsClient;
        // Use static instance for now:
        this.taskWatcher = VRSTaskWatcher.getTaskWatcher();
    }

    // ===
    // Implementation
    // ===

    public boolean doLinkDrop(List<VRL> vrls, VRL destVrl, VARHolder<VPath> destPathH, VARHolder<List<VPath>> resultPathsH,
            ITaskMonitor monitor) throws VrsException
    {
        String taskName = "LinkDrop to:" + destVrl;
        logger.debugPrintf("doLinkDrop():%s, vrls=%s\n", destVrl, new ExtendedList<VRL>(vrls));
        monitor.startTask(taskName, vrls.size());
        monitor.logPrintf(">>> doLinkDrop on:%s,  vrls=%s\n", destVrl, new ExtendedList<VRL>(vrls));

        try
        {
            VPath destPath = this.vrsClient.openPath(destVrl);
            if (destPathH != null)
            {
                destPathH.set(destPath);
            }

            int index = 0;

            ArrayList<VPath> nodes = new ArrayList<VPath>();

            for (VRL vrl : vrls)
            {
                monitor.logPrintf(" - linkDrop %s\n", vrl);

                if (destPath instanceof VInfoResource)
                {
                    VInfoResourcePath newNode = ((VInfoResource) destPath).createResourceLink(vrl, "Link to:" + vrl.getBasename());
                    monitor.logPrintf("Created new node:%s\n", newNode);
                    nodes.add(newNode);
                }
                else if (destPath instanceof VFSPath)
                {
                    logger.errorPrintf(" - FIXME: linkDrop on:%s<<%s\n", destVrl, vrl);
                    monitor.logPrintf(" - FIXME: linkDrop on:%s<<%s\n", destVrl, vrl);

                    try
                    {
                        Thread.sleep(250);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }

                    return false;
                }
                else
                {
                    throw new VrsException("LinkDrop not support on resource:" + destVrl);
                }

                monitor.updateTaskDone(++index);
            }
            resultPathsH.set(nodes);
            monitor.endTask(taskName);
            return true;
        }
        catch (Throwable t)
        {
            monitor.setException(t);
            monitor.logPrintf("Exception:%s\n", t);
            throw new VrsException(t.getMessage(), t);
        }

        // return false;
    }

    public boolean doCopyMove(List<VRL> vrls, VRL destVrl, boolean isMove, VARHolder<VPath> destPathH, VARHolder<List<VPath>> resultPathsH,
            Holder<List<VPath>> deletedNodesH, ITaskMonitor monitor) throws VrsException
    {
        logger.errorPrintf("***FIXME:doCopyMove():%s, vrls=%s\n", destVrl, new ExtendedList<VRL>(vrls));

        // Pre Checks:
        if ((vrls == null) || (vrls.size() < 1))
        {
            throw new VrsException("No resource to copy/move");
        }

        String taskName = isMove ? "Move" : "Copy";
        monitor.startTask(taskName, vrls.size());
        monitor.logPrintf(">>> doCopyMove(isMove=%s) on:%s, vrls=%s\n", isMove, destVrl, new ExtendedList<VRL>(vrls));
        int index = 0;

        VPath destPath = vrsClient.openPath(destVrl);
        VPath firstPath = vrsClient.openPath(vrls.get(0));

        if (destPath instanceof VFSPath)
        {
            VFSPath vfsDestPath = (VFSPath) destPath;
            destPathH.set(destPath);
            boolean status;

            if (vfsDestPath.isDir())
            {
                status = doCopyMoveToDir(vrls, vfsDestPath, isMove, resultPathsH,deletedNodesH, monitor);
            }
            else if (vfsDestPath.isFile())
            {
                if (vrls.size() > 1)
                {
                    // could be arguments for a script/binary here
                    throw new VrsException("Cannot drop multiple resources onto single file");
                }

                status = doCopyMoveResourceToFile(firstPath, vfsDestPath, isMove, monitor);
                if (isMove)
                {
                    ArrayList<VPath> deletedPaths=new ArrayList<VPath>(); 
                    deletedPaths.add(firstPath);
                    deletedNodesH.set(deletedPaths);
                }
            }
            else
            {
                throw new VrsException("Don't know how to copy to VFS Path:" + destPath);
            }

            monitor.endTask(taskName);
            return status;
        }

        monitor.endTask(taskName);

        throw new VrsException("Don't know how to copy to:" + destPath);
    }

    private boolean doCopyMoveResourceToFile(VPath sourcePath, VFSPath targetPath, boolean isMove, ITaskMonitor monitor)
            throws VrsException
    {
        logger.errorPrintf("doCopyMoveResourceToFile: source:%s\n", sourcePath);
        logger.errorPrintf("doCopyMoveResourceToFile: dest  :%s\n", targetPath);

        VResourceSystem sourceRs = sourcePath.getResourceSystem();
        VFileSystem targetVFS = targetPath.getFileSystem();

        if ((isMove) && (targetVFS.equals(sourceRs)))
        {
            // rename/move on same filesystem
            String renameTask = "Renaming:" + sourcePath.getVRL().getPath() + " to:" + targetPath.getVRL().getPath();

            monitor.startSubTask(renameTask, 1);
            monitor.logPrintf("%s\n", renameTask);
            VPath newPath = sourcePath.renameTo(sourcePath.getVRL().getPath());
            monitor.updateSubTaskDone(renameTask, 1);
            monitor.endSubTask(renameTask);
            return true;
        }

        VDeletable deletable = null;

        // ---------------------------
        // Check move.
        // A Move-Drop is a Copy+Delete. (like windows).
        // ---------------------------

        if (isMove)
        {
            if (sourcePath instanceof VDeletable)
            {
                deletable = (VDeletable) sourcePath;
            }
            else
            {
                throw new VrsException("Can not move resource if original source path can't be deleted:" + sourcePath);
            }
        }

        // ---------------------------
        // Default is stream copy to target file.
        // ---------------------------
        streamCopyFile(sourcePath, targetPath, monitor);

        if (isMove)
        {
            deletable.delete();
        }

        return true;
    }

    private boolean doCopyMoveToDir(List<VRL> vrls, VFSPath targetDirPath, boolean isMove, VARHolder<List<VPath>> resultPathsH,
            Holder<List<VPath>> deletedNodesH, ITaskMonitor monitor) throws VrsException
    {

        monitor.logPrintf("CopyMove to Directory:%s\n", targetDirPath);

        List<VPath> resultPaths = new ArrayList<VPath>();
        resultPathsH.set(resultPaths);
        
        List<VPath> deletedPaths = new ArrayList<VPath>();
        deletedNodesH.set(resultPaths);

        for (VRL vrl : vrls)
        {
            monitor.logPrintf(" - CopyMove source:%s\n", vrl);
            VPath sourcePath = vrsClient.openPath(vrl);

            if (sourcePath instanceof VFSPath)
            {
                VFSPath destSubPath = targetDirPath.resolvePath(sourcePath.getVRL().getBasename());
                logger.errorPrintf("Resolve: '%s' + '%s' => '%s'\n", targetDirPath.getVRL(), sourcePath.getVRL().getBasename(),
                        destSubPath.getVRL());

                VFSPath vfsPath = (VFSPath) sourcePath;

                if (vfsPath.isFile())
                {
                    this.doCopyMoveResourceToFile(vfsPath, destSubPath, isMove, monitor);
                    resultPaths.add(destSubPath);
                    if (isMove)
                    {
                        deletedPaths.add(vfsPath);
                    }
                }
                else
                {
                    monitor.logPrintf(" - Skipping Directory:%s\n", vfsPath);
                }
            }
            else
            {
                monitor.logPrintf(" - Skipping non VFS Path:%s\n", sourcePath);
            }
        }

        // DirHeapCopy dirHeapCopy=new DirHeapCopy(vrls,dirPath);
        return (resultPaths.size() > 0);
    }

    public void streamCopyFile(VPath sourcePath, VFSPath targetFile, ITaskMonitor monitor) throws VrsException
    {

        if ((sourcePath instanceof VStreamReadable) == false)
        {
            throw new VrsException("Can not read from input source:" + sourcePath);
        }

        if ((targetFile instanceof VStreamWritable) == false)
        {
            throw new VrsException("Can not write to target file (not stream writable):" + targetFile);
        }

        // actual copy:

        try
        {
            boolean targetExists = targetFile.exists(LinkOption.NOFOLLOW_LINKS);

            long len = -1;
            if (sourcePath instanceof VFSPath)
            {
                VFSPath vfsPath = (VFSPath) sourcePath;
                len = vfsPath.getLength();
            }

            InputStream inps = ((VStreamReadable) sourcePath).createInputStream();
            OutputStream outps = ((VStreamWritable) targetFile).createOutputStream(false);

            IOUtil.circularStreamCopy(inps, outps, len, 1024 * 1024, false, monitor);

            IOUtil.autoClose(inps);
            IOUtil.autoClose(outps);

        }
        catch (Exception e)
        {
            throw new VrsException("Copy Failed:" + e.getMessage(), e);
        }

    }
}
