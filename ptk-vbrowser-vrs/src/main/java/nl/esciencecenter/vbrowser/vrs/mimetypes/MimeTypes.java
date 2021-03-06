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

package nl.esciencecenter.vbrowser.vrs.mimetypes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.activation.MimetypesFileTypeMap;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicMatch;
import nl.esciencecenter.ptk.util.logging.PLogger;

/**
 * MimeType util class.
 */
public class MimeTypes {
    // default mime types.
    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String MIME_TEXT_HTML = "text/html";
    public static final String MIME_OCTET_STREAM = "application/octet-stream";
    public static final String MIME_BINARY = MIME_OCTET_STREAM;

    /**
     * Singleton instance.
     */
    private static MimeTypes instance;

    private static PLogger logger;

    static {
        logger = PLogger.getLogger(MimeTypes.class);
    }

    public static MimeTypes getDefault() {
        if (instance == null) {
            instance = new MimeTypes();
        }

        return instance;
    }

    // ========================================================================
    // Instance
    // ========================================================================

    /**
     * Mime type file type map
     */
    private MimetypesFileTypeMap typemap = null;

    public MimeTypes() {
        init();
    }

    private void init() {
        try {
            // Load default mime.types file from classpath.
            String confFile = "mime.types";
            URL result = getClass().getClassLoader().getResource(confFile);

            if (result == null) {
                confFile = "default_mime.types";
                result = getClass().getClassLoader().getResource(confFile);
            }

            if (result != null) {
                InputStream inps = result.openStream();
                typemap = new MimetypesFileTypeMap(inps);
            } else {
                logger.warn("Couldn't locate ANY mime.types file on classpath");
                typemap = new MimetypesFileTypeMap();
            }
        } catch (IOException e) {
            logger.logException(PLogger.WARN, e, "Couldn't initialize default mimetypes\n", e);
            // empty one !
            this.typemap = new MimetypesFileTypeMap();
        }
    }

    /**
     * Add extra mime type definitions.
     */
    public void addMimeTypes(String mimeTypes) {
        String lines[] = mimeTypes.split("\n");
        if (lines != null)
            for (String line : lines) {
                logger.debugPrintf("Adding user mime.type:" + line);
                typemap.addMimeTypes(line);
            }
    }

    /**
     * Returns mimetype string by checking the extension or name of the file
     */
    public String getMimeType(String path) {
        if (path == null)
            return null;

        return typemap.getContentType(path);
    }

    /**
     * Returns the 'magic' MimeType by checking the first bytes of a file against known 'magic'
     * values.
     * 
     * @param firstBytes
     *            The first bytes of a file
     * @return Mime Type.
     * @throws Exception
     */
    public String getMagicMimeType(byte firstBytes[]) throws Exception {
        MagicMatch match;
        match = Magic.getMagicMatch(firstBytes);
        return match.getMimeType();

        //        catch (MagicParseException e)
        //        {
        //            throw new Exception("MagicParseException:\n" + e.getMessage(), e);
        //        }
        //        catch (MagicMatchNotFoundException e)
        //        {
        //            throw new Exception("MagicMatchNotFoundException\n" + e.getMessage(), e);
        //        }
        //        catch (MagicException e)
        //        {
        //            throw new Exception("MagicException\n" + e.getMessage(), e);
        //        }
    }

    /**
     * Check magic type of file. Read first byte of the file and checks against known magic values.
     * 
     * @param file
     *            the file to check. File must exists.
     * @return
     */
    public String getMagicMimeType(File file) {
        MagicMatch match;
        try {
            match = Magic.getMagicMatch(file, false);
            return match.getMimeType();
        } catch (Exception e) {
            logger.logException(PLogger.WARN, e, "Couldn't parse MagicMime type for:%s\n", file);
        }
        return null;
    }
}
