CHECKSUM 
========

This module is responsible for calculation of checksum from a given project module.
Checksum is calculated from files contained in the jar file of the given module.
The value of checksum and module name is set once while the module is being started or upload.

IMPLEMENTATION
--------------
Compile this project to jar file: mvn clean install.
Add dependencies to this jar.
Implement ChecksumService.java (below is an example)  to modules to be calculated.


SAMPLE IMPLEMENTATION
---------------------
1. Aem
2. Knot (todo)
3. Spring (todo)



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
      tmpChecksum = calculateChecksum();     
    } catch (IOException e) {
      LOG.error("Can not calculate checksum from jar file", e);
    }
    return tmpChecksum;
  }
}


TEST
----
http://localhost:port/bin/checksum/allmodules - show all checksums
http://localhost:port/bin/checksum/combined - show sum of all checksums
http://localhost:port/bin/checksum/combined?module=moduleName&module=moduleName - show sum of selected checksums
