package com.cognifide.bridge.whiteBoardTest;

import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
    service = Checksum.class,
    immediate = true,
    property = {
        Constants.SERVICE_DESCRIPTION + "=Servlet provides checksum.",
        Constants.SERVICE_VENDOR + "=Cognifide"
    }
)
/*
* this service is responsible for initializing the calculation of checksum from a given project module.
* It should be copied to each calculation module. Is a part of White Board Pattern
* The value of checksum and module name is set once while the module is being started or upload
* */
public class ChecksumService implements Checksum {

  private static final Logger LOG = LoggerFactory.getLogger(ChecksumService.class);

  private String checksum;

  private String modulesName;

  @Activate
  public void init() {
    modulesName = prepareModuleName();
    checksum = geSafeChecksum();
  }

  @Override
  public String getChecksum() {
    return checksum;
  }

  @Override
  public String getModuleName() {
    return modulesName;
  }


  private String geSafeChecksum() {
    String tmpChecksum = StringUtils.EMPTY;
    try {
      long startTime = System.currentTimeMillis();//todo for test only, Will delete
      tmpChecksum = calculateChecksum();
      long stopTime = System.currentTimeMillis();
      long elapsedTime = stopTime - startTime;
      LOG.info("Execution time for calculateChecksum: {} millis for {} module", elapsedTime, "bridge");
    } catch (IOException e) {
      LOG.error("Can not calculate checksum from jar file", e);
    }
    return tmpChecksum;
  }



}
