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

package nl.esciencecenter.ptk.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Properties;

import nl.esciencecenter.ptk.io.FSUtil;
import nl.esciencecenter.ptk.io.IOUtil;
import nl.esciencecenter.ptk.io.RandomReadable;
import nl.esciencecenter.ptk.io.RandomWritable;
import nl.esciencecenter.ptk.io.ResourceProvider;
import nl.esciencecenter.ptk.net.URIFactory;
import nl.esciencecenter.ptk.util.logging.ClassLogger;

/**
 * Generic ResourceLoader class which supports (relative) URIs and URLs. It is recommended to use relative URLs and URIs
 * to local files and other resources which might be on the classpath and not rely on absolute file locations.<br>
 * This allows for a more flexible run time environment in applet and servlet environments.
 * <p>
 * 
 * @author Piter.T. de Boer
 */
public class ResourceLoader
{
    private static ClassLogger logger = ClassLogger.getLogger(ResourceLoader.class);

    /** Default UTF-8 */
    public static final String CHARSET_UTF8 = "UTF-8";

    /** UTF-16 Big Endian or "wide char" */
    public static final String CHARSET_UTF16BE = "UTF-16BE";

    /** UTF-16 Little Endian or "wide char" */
    public static final String CHARSET_UTF16LE = "UTF-16LE";

    /** 7-bits (US) ASCII */
    public static final String CHARSET_US_ASCII = "US-ASCII";

    /** 8-bits US and Euro 'standard' encoding */
    public static final String CHARSET_ISO_8859_1 = "ISO-8859-1";

    /** Latin is an alias for ISO-8859-1 (All Roman/Latin languages) */
    public static final String CHARSET_LATIN = "ISO-8869-1";

    /** Old extended (US) ASCII Code Page 437 */
    public static final String CHARSET_CP437 = "CP437";

    /** Default is UTF-8 */
    public static final String DEFAULT_CHARSET = CHARSET_UTF8;

    /**
     * Supported character sets.
     */
    public static final String charEncodings[] =
    {
            CHARSET_UTF8,
            CHARSET_UTF16BE,
            CHARSET_UTF16LE,
            CHARSET_US_ASCII,
            CHARSET_ISO_8859_1,
            CHARSET_LATIN,
            CHARSET_CP437
    };

    private static ResourceLoader instance;

    static
    {
       //logger.setLevelToDebug();
    }
    
    // =================================================================
    //
    // =================================================================

    /**
     * Util class for all the URL resolve methods. Relative URLs are different in that they can be resolved against to
     * the Java Classpath.
     */
    public static class URLResolver
    {
        protected URLClassLoader classLoader = null;

        public URLResolver(URLClassLoader parentClassLoader, URL[] urls) throws MalformedURLException
        {
            init(parentClassLoader, toDirUrls(urls));
        }

        public URLResolver(URL[] urls)
        {
            init(null, urls);
        }

        protected void init(ClassLoader parentLoader, URL urls[])
        {
            if (parentLoader == null)
            {
                // context class loader including extra search path:
                parentLoader = Thread.currentThread().getContextClassLoader();
            }

            if (urls != null)
            {
                classLoader = new URLClassLoader(urls, parentLoader);
            }
        }

        /**
         * Resolve relative path and return URL to existing resource.
         */
        public URL resolveUrlPath(String relativePath)
        {
            return resolveUrlPath(null, relativePath);
        }

        /**
         * Resolve relative path and return URL to existing resource.
         */
        public java.net.URL resolveUrlPath(ClassLoader optClassLoader, String relativeUrl)
        {
            logger.debugPrintf("resolveUrl():%s\n", relativeUrl);

            URL resolvedUrl = null;
            // Normalize to URI style path:
            String urlStr = URIFactory.uripath(relativeUrl, false, File.separatorChar);

            if (urlStr == null)
            {
                throw new NullPointerException("URL String can not be null");
            }

            // (I) First optional Class Loader !
            if (optClassLoader != null)
            {
                resolvedUrl = optClassLoader.getResource(urlStr);

                if (resolvedUrl != null)
                {
                    logger.debugPrintf("resolveUrl() I: Resolved URL by using extra class loader:%s\n", resolvedUrl);
                }
            }

            // (II) Use Resource Classloader
            if ((resolvedUrl == null) && (this.classLoader != null))
            {
                resolvedUrl = this.classLoader.getResource(urlStr);

                if (resolvedUrl != null)
                {
                    logger.debugPrintf("resolveURL() II:Resolved URL by using resource classloader:%s\n", resolvedUrl);
                }
            }

            // (III) Check default (global) classloader for resources which are on the global classpath.
            if (resolvedUrl == null)
            {
                resolvedUrl = this.getClass().getClassLoader().getResource(urlStr);

                if (resolvedUrl != null)
                {
                    logger.debugPrintf("resolveURL() III:Resolved URL by using global classloader:%s\n", resolvedUrl);
                }
            }

            // keep as is:
            if (resolvedUrl == null)
            {
                try
                {
                    URL url2 = new URL(urlStr);
                    resolvedUrl = url2;
                }
                catch (MalformedURLException e)
                {
                    logger.warnPrintf("resolveURL() IV: Not an absolute url:%s\n", urlStr);
                }
            }

            logger.debugPrintf("resolveURL(): '%s' -> '%s' \n", urlStr, resolvedUrl);

            return resolvedUrl;
        }

        public URL[] getURLs()
        {
            URL urls[] = null;

            if (this.classLoader != null)
            {
                urls = this.classLoader.getURLs();
            }

            return urls;
        }
    }

    // =================================================================
    // Static methods
    // =================================================================

    public static String[] getDefaultCharEncodings()
    {
        return charEncodings;
    }

    public static ResourceLoader getDefault()
    {
        if (instance == null)
            instance = new ResourceLoader(null);

        return instance;
    }

    /**
     * URL ClassLoaders needs DIR style URLs where directory paths must end with a slash '/'. Also URL paths must
     * contain forward slashes.
     * 
     * @throws MalformedURLException
     */
    public static URL[] toDirUrls(URL urls[]) throws MalformedURLException
    {
        URL dirUrls[] = new URL[urls.length];
        for (int i = 0; i < urls.length; i++)
        {
            URL url = urls[i];
            if (url.getPath().endsWith("/") == false)
            {
                dirUrls[i] = new URL(url.toString() + "/");
            }
            else
            {
                dirUrls[i] = url; // keep as-is.
            }
        }

        return dirUrls;
    }

    // =================================================================
    // Instance
    // =================================================================

    protected String charEncoding = DEFAULT_CHARSET;

    protected URLResolver urlResolver = null;

    protected ResourceProvider resourceProvider;

    public ResourceLoader()
    {
        init(FSUtil.getDefault(), null);
      
    }

    /**
     * Initialize ResourceLoader with extra URL search path. When resolving relative URLs these path URLs will be
     * searched as well similar as using a PATH environment variable.
     * 
     * @param urls
     *            - URL search paths
     */
    public ResourceLoader(URL urls[])
    {
        init(FSUtil.getDefault(), urls);
    }

    /**
     * Initialize ResourceLoader with extra URL search path. When resolving relative URLs these path URLs will be
     * searched as well similar as using a PATH environment variable.
     * 
     * @param FSUtil
     *            - custom FileSystem utility.
     * @param urls
     *            - URL search paths
     */
    public ResourceLoader(ResourceProvider resourceProvider, URL urls[])
    {
        init(resourceProvider, urls);
    }

    protected void init(ResourceProvider resourceProvider, URL urls[])
    {
        urlResolver = new URLResolver(urls);

        this.resourceProvider = resourceProvider;

        if (this.resourceProvider == null)
        {
            this.resourceProvider = FSUtil.getDefault();
        }
    }

    /**
     * Returns default characted encoding which is used when reading text.
     */
    public String getCharEncoding()
    {
        return charEncoding;
    }

    /**
     * Specify default character encoding which is used when reading text.
     */
    public void setCharEncoding(String encoding)
    {
        charEncoding = encoding;
    }

    // =================================================================
    // URI/URL resolving
    // =================================================================

    public URLResolver getURLResolver()
    {
        return this.urlResolver;
    }

    /**
     * Resolve URL string to absolute URL
     * 
     * @see ResourceLoader#resolveUrl(ClassLoader, String)
     */
    public URL resolveUrl(String urlString)
    {
        return resolveUrl(null, urlString);
    }

    /**
     * Resolve relative resource String and return absolute URL. The URL String can be matched against the optional
     * ClassLoader in the case the URL points to a resource loaded by a custom ClasLoader that is not accessible by the
     * classloader which loaded this (ResourceLoader) class.
     * 
     * If the ResourceLoader has been initialized with extra (ClassPath) URLs, these will be searched also.
     * 
     * @param optClassLoader
     *            - Optional ClassLoader from plugin class Loader
     * @param url
     *            - relative URL String, might be absolute but then there is nothing to 'resolve'.
     * @return resolved Absolute URL
     */
    public URL resolveUrl(ClassLoader optClassLoader, String url)
    {
        // delegate:
        return urlResolver.resolveUrlPath(optClassLoader, url);
    }

    /**
     * Returns current URL search path for relative resources.
     */
    public URL[] getSearchPath()
    {
        return urlResolver.getURLs();
    }

    // =================================================================
    // Input- and OutputStreams
    // =================================================================

    /**
     * Resolves relative URL string and returns InputStream to resource. If the urlstr is an absolute URL this method is
     * similar to <code>URL.openConnection().getInputStream()</code>.
     * 
     * @see #resolveUrl(ClassLoader, String)
     * 
     * @param urlstr
     *            - can be both relative (classpath) url or absolute URI
     * @return InputStream - InputStream to resource.
     * @throws IOException
     */
    public InputStream createInputStream(String urlstr) throws IOException
    {
        URL url = resolveUrl(null, urlstr);

        if (url == null)
        {
            throw new FileNotFoundException("Couldn't resolve:" + urlstr);
        }

        return createInputStream(url);
    }

    /**
     * Creates a new InputStream from the specified URL.
     */
    public InputStream createInputStream(URL url) throws IOException
    {
        if (url == null)
        {
            throw new NullPointerException("createInputStream():URL is NULL!");
        }

        try
        {
            return resourceProvider.createInputStream(new URIFactory(url).toURI());
        }
        catch (URISyntaxException e)
        {
            // wrap:
            throw new IOException("Invalid URL: Cannot get inputstream from" + url + "\n" + e.getMessage(), e);
        }
        catch (IOException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new IOException("Cannot get inputstream from" + url + "\n" + e.getMessage(), e);
        }
    }

    public InputStream createInputStream(URI uri) throws IOException
    {
        // use URI Provider:
        try
        {
            return resourceProvider.createInputStream(uri);
        }
        catch (IOException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * Creates a new InputStream from the specified URL.
     */
    public OutputStream createOutputStream(URL url) throws IOException
    {
        if (url == null)
        {
            throw new NullPointerException("createOutputStream():URL is NULL!");
        }

        try
        {
            return resourceProvider.createOutputStream(new URIFactory(url).toURI());
        }
        catch (URISyntaxException e)
        {
            // wrap:
            throw new IOException("Invalid URL: Cannot create OutputStream from" + url + "\n" + e.getMessage(), e);
        }
        catch (IOException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new IOException("Cannot get inputstream from" + url + "\n" + e.getMessage(), e);
        }
    }

    
    public OutputStream createOutputStream(URI uri) throws IOException
    {
        try
        {
            return resourceProvider.createOutputStream(uri);
        }
        catch (IOException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new IOException(e.getMessage(), e);
        }
    }

    // =================================================================
    // Random IO Interface
    // =================================================================

    /**
     * Returns RandomReader if supported by the URI scheme.
     * 
     * @throws IOException
     */
    public RandomReadable createRandomReader(URI loc) throws IOException
    {
        try
        {
            return resourceProvider.createRandomReader(loc);
        }
        catch (IOException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new IOException(e.getMessage(), e);
        }

    }

    /**
     * Returns RandomWriter if supported by the URI scheme.
     * 
     * @throws IOException
     */
    public RandomWritable createRandomWriter(URI loc) throws IOException
    {
        try
        {
            return resourceProvider.createRandomWriter(loc);
        }
        catch (IOException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new IOException(e.getMessage(), e);
        }
    }

    // =================================================================
    // Read and Write methods
    // =================================================================

    public String readText(URL location) throws IOException
    {
        return readText(location, this.charEncoding);
    }

    public String readText(URI location) throws IOException
    {
        return readText(location, this.charEncoding);
    }

    /**
     * Returns resource as String.
     */
    public String readText(URL location, String charset) throws IOException
    {
        InputStream inps = createInputStream(location);

        try
        {
            String text = readText(inps, charset);
            return text;
        }
        finally
        {
            IOUtil.autoClose(inps);
        }
    }

    public String readText(URI uri, String charset) throws IOException
    {
        InputStream inps = createInputStream(uri);

        try
        {
            String text = readText(inps, charset);
            return text;
        }
        finally
        {
            IOUtil.autoClose(inps);
        }
    }

    public byte[] readBytes(String pathOrUrl) throws IOException
    {
        InputStream inps = createInputStream(pathOrUrl);
        byte bytes[] = readBytes(inps);
        IOUtil.autoClose(inps);
        return bytes;
    }

    /**
     * Read text from InputSream using charset as String encoding. Default is UTF-8.
     * 
     * @param inps
     *            - The InputStream. all byte will be read, but the InputStream won't be closed
     * @param charset
     *            - Optional Character Encoding. Can be null.
     */
    public String readText(InputStream inps, String charset) throws IOException
    {
        if (charset == null)
        {
            charset = charEncoding;
        }

        // just read all:
        try
        {
            byte bytes[] = readBytes(inps);
            return new String(bytes, charset);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new IOException("UnsupportedEncoding:" + charset, e);
        }
    }

    /**
     * Read all bytes from InputStream until an EOF or other IOException occored. InputStream won't be closed.
     */
    public byte[] readBytes(InputStream inps) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] buf = new byte[32 * 1024]; // typical TCP/IP packet size:
        int len = 0;

        try
        {
            while ((len = inps.read(buf)) > 0)
            {
                bos.write(buf, 0, len);
            }
        }
        catch (IOException e)
        {
            throw new IOException("Couldn't read from input stream", e);
        }

        byte[] data = bos.toByteArray();
        return data;
    }

    public Properties loadProperties(URL url) throws IOException
    {
        // delegate to universal URI method:
        try
        {
            return loadProperties(url.toURI());
        }
        catch (URISyntaxException e)
        {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * Load properties file from specified location.<br>
     */
    public Properties loadProperties(URI uri) throws IOException
    {
        Properties props = new Properties();

        try
        {
            InputStream inps = this.resourceProvider.createInputStream(uri);
            props.load(inps);
            logger.debugPrintf("Read properties from:%s\n", uri);
            IOUtil.autoClose(inps);
        }
        catch (IOException e)
        {
            throw new IOException("Couldn't load properties from:" + uri + "\n" + e.getMessage(), e);
        }
        // in the case of applet startup: Not all files are
        // accessable, wrap exception for gracefull exception handling.
        catch (java.security.AccessControlException ex)
        {
            // Applet/Servlet environment !
            throw new IOException("Security Exception: Permission denied for:" + uri, ex);
        }
        catch (Exception e)
        {
            throw new IOException("Couldn't load properties from:" + uri + "\n" + e.getMessage(), e);
        }

        for (Enumeration<Object> keys = props.keys(); keys.hasMoreElements();)
        {
            String key = (String) keys.nextElement();
            String value = props.getProperty(key);
            logger.debugPrintf("Read property='%s'='%s'\n", key, value);
        }

        return props;
    }

    /**
     * Save properties file to specified location.
     */
    public void saveProperties(URI loc, Properties props) throws IOException
    {
        saveProperties(loc, props, "Properties file");
    }

    /**
     * Save properties file to specified location.
     */
    public void saveProperties(URI loc, Properties props, String comments) throws IOException
    {
        OutputStream outps = createOutputStream(loc);
        try
        {
            props.store(outps, comments);
        }
        finally
        {
            IOUtil.autoClose(outps);
        }
    }

    /**
     * Save properties file to specified location.
     */
    public void writeTextTo(URI loc, String text) throws IOException
    {
        writeBytesTo(loc, text.getBytes(this.charEncoding));
    }

    /**
     * Save properties file to specified location.
     */
    public void writeTextTo(URI loc, String text, String charset) throws IOException
    {
        writeBytesTo(loc, text.getBytes(charset));
    }

    
    /**
     * Save properties file to specified location.
     */
    public void writeTextTo(URL loc, String text, String charset) throws IOException
    {
        writeBytesTo(loc, text.getBytes(charset));
    }
    
    /**
     * Write bytes to URI location.
     * 
     * @param uri
     *            - URI of location. URI scheme must support OutputStreams.
     * @param bytes
     *            - bytes to write to the location
     * @throws IOException
     */
    public void writeBytesTo(URI uri, byte[] bytes) throws IOException
    {
        OutputStream outps = createOutputStream(uri);
        outps.write(bytes);
        IOUtil.autoClose(outps);
    }

    /**
     * Write bytes to URI location.
     * 
     * @param uri
     *            - URI of location. URI scheme must support OutputStreams.
     * @param bytes
     *            - bytes to write to the location
     * @throws IOException
     */
    public void writeBytesTo(URL url, byte[] bytes) throws IOException
    {
        OutputStream outps = createOutputStream(url);
        outps.write(bytes);
        IOUtil.autoClose(outps);
    }
    
    public void writeBytes(OutputStream outps, byte[] bytes) throws IOException
    {
        outps.write(bytes);
    }

}