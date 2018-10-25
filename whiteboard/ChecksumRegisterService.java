package com.cognifide.bridge.whiteBoardTest;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
    service = ChecksumRegister.class,
    immediate = true,
    property = {
        Constants.SERVICE_DESCRIPTION + "=Service register checksum service.",
        Constants.SERVICE_VENDOR + "=Cognifide"
    }
)
public class ChecksumRegisterService implements ChecksumRegister {

  @Reference(
      cardinality = ReferenceCardinality.MULTIPLE,
      policy = ReferencePolicy.DYNAMIC,
      service = Checksum.class
  )
  private final List<Checksum> checksumServices = new LinkedList<>();

  private void bindChecksumServices(Checksum checksumService) {
    checksumServices.add(checksumService);
  }

  private void unbindChecksumServices(Checksum checksumService) {
    checksumServices.remove(checksumService);
  }

  @Override
  public List<Checksum> getChecksum() {
    return Collections.unmodifiableList(Lists.newArrayList(checksumServices));
  }
}
