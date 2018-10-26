package com.cognifide.bridge.whiteBoardTest;

import java.io.IOException;
import java.net.JarURLConnection;
import java.security.CodeSource;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public interface Checksum extends Comparable<Checksum>{

  String BUNDLE_NAME = "Bundle-Name";
  String META_INF_MANIFEST_MF = "META-INF/MANIFEST.MF";

  void init();

  String getChecksum();

  String getModuleName();

  default String prepareModuleName() {
    Bundle bundle = FrameworkUtil.getBundle(getClass());
    return bundle.getHeaders().get(BUNDLE_NAME).toString() + StringUtils.SPACE + bundle.getVersion();
  }

  default String calculateChecksum() throws IOException {
    CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();

    List<Long> checkSumsCrc32 = ((JarURLConnection) codeSource.getLocation().openConnection()).getJarFile()
        .stream()
        .filter(file -> !file.getName().equals(META_INF_MANIFEST_MF))
        .sorted(Comparator.comparing(ZipEntry::getName))
        .map(ZipEntry::getCrc)
        .collect(Collectors.toList());

    return DigestUtils.md5Hex(checkSumsCrc32.toString());
  }

  default int compareTo(Checksum o) {
    return this.getModuleName().compareTo(o.getModuleName());
  }
}
