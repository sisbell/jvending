/*
 *   JVending
 *   Copyright (C) 2004  Shane Isbell
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.jvending.provisioning.stocking.handler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.hibernate.Hibernate;
import org.jvending.provisioning.config.MimeTypeRepository;
import org.jvending.provisioning.model.clientbundle.ContentFile;
import org.jvending.provisioning.stocking.StockingException;
import org.jvending.registry.RepositoryRegistry;


/**
 * This class provides a means for pulling a JAR file from a <code>Map</code>.
 *
 * @author Shane Isbell
 * @since 1.3a
 */

public class JadHandler implements DescriptorHandler {

    private static Logger logger = Logger.getLogger("JadHandler");

    /**
     * RepositoryRegisty instance
     */
    private RepositoryRegistry repositoryRegistry;

    public void setRepositoryRegistry(RepositoryRegistry repositoryRegistry) {
        this.repositoryRegistry = repositoryRegistry;
    }

    /**
     * @param descriptorFileType for a JAD
     * @param contentMap
     * @return List containing one <code>ContentFile</code> for a JAR.
     * @throws StockingException
     */
    public List<ContentFile> getContentFiles(org.jvending.provisioning.stocking.par.DescriptorFile descriptorFileType, 
    		Map<String, byte[]> contentMap) throws StockingException {
        List<ContentFile> contents = new ArrayList<ContentFile>();

        if (descriptorFileType == null || contentMap == null) {
            logger.info("JV-1800-009: One of the required parameters is missing");
            throw new StockingException("JV-1800-009: One of the required parameters is missing");
        }

        if (repositoryRegistry == null) {
            logger.severe("JV-1800-010: RepositoryRegistry not set.");
            throw new StockingException("JV-1800-010: RepositoryRegistry not set.");
        }

        logger.finest("JV-1800-001: Starting JAD HANDLER");

        String uri = descriptorFileType.getValue();

        if (uri == null || isRemoteUri(uri.trim())) {
            logger.finest("JV-1800-002: Remote or null URI. Not processing content: URI = " + uri);
            return contents;
        }

        uri = uri.trim();

        String mimeType = descriptorFileType.getMimeType();
        MimeTypeRepository mimeRepo = (MimeTypeRepository) repositoryRegistry.find("mimetype");
        if (mimeType == null) mimeType = mimeRepo.getMimeTypeFromUri(descriptorFileType.getValue());

        if (mimeType == null) {
            logger.info("JV-1800-003: Cannot determine the mime-type of this descriptor: URI = " + uri);
            return contents;
        }

        if (!mimeType.equals("text/vnd.sun.j2me.app-descriptor")) {
            logger.info("JV-1800-004: Not JAD mime-type: URI = " + uri + ", mimeType = " + mimeType);
            return contents;
        }

        byte[] descriptorBytes = (byte[]) contentMap.get(uri);
        if (descriptorBytes == null) {
            logger.info("JV-1800-005: Cannot find local copy of this descriptor: URI = " + uri);
            return contents;
        }

        Map<String, String> jadMap = jadToMap(new String(descriptorBytes));
        if (jadMap == null) {
            logger.info("JV-1800-006: Invalid JAD: URI = " + uri);
            throw new StockingException("JV-1800-006: Invalid JAD: URI = " + uri);
        }

        String jarFileUri = (String) jadMap.get("MIDlet-Jar-URL");
        byte[] jarFile = (byte[]) contentMap.get(jarFileUri);
        if (jarFile == null) {
            logger.info("JV-1800-007: Could not find local copy of JAR: URI = " + uri);
            return contents;
        }

        Blob contentBlob;
        ByteArrayInputStream is = new ByteArrayInputStream(jarFile);
        contentBlob = Hibernate.createBlob(is, jarFile.length);
        ContentFile contentFile = new ContentFile();
      //  contentFile.setContent(contentBlob);
        contentFile.setFileUri(jarFileUri);
        contentFile.setMimeType("application/java-archive");
      //  contentFile.setBytes(jarFile);
        contents.add(contentFile);
        logger.finest("JV-1800-008: Returning from JAD HANDLER: Mime-Type = application/java-archive, URI = "
                + jarFileUri);
        return contents;
    }

    private boolean isRemoteUri(String uri) {
        return (uri != null && (uri.startsWith("http://") || uri.startsWith("ftp://") || uri.startsWith("https://")));
    }

    /**
     * Converts a JAD to a <code>Map</code> of properties.
     *
     * @param jad string
     * @return JAD property Map
     */
    private Map<String, String> jadToMap(String jad) {
        Map<String, String> map = new HashMap<String, String>();
        Properties prop = new Properties();
        try {
            prop.load(new ByteArrayInputStream(jad.getBytes()));
        } catch (IOException e) {
            return null;
        }

        for (Enumeration<?> e = prop.propertyNames(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            String value = prop.getProperty(key);
            if (key != null && value != null) map.put(key, value);
        }
        return map;
    }

}