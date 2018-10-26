package com.cognifide.bridge.whiteBoardTest;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.Servlet;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component(
    service = Servlet.class,
    immediate = true,
    property = {
        "sling.servlet.methods=GET",
        "sling.servlet.paths=" + ChecksumRegisteredServlet.CHECKSUM_PATH})
public class ChecksumRegisteredServlet extends SlingAllMethodsServlet {

  @SuppressWarnings("WeakerAccess")
  public static final String CHECKSUM_PATH = "/bin/bridge/checksum/combined";

  public static final String PARAMETER_MODULE = "module";

  private static final String PROJECT_CHECK_SUM = "projectCheckSum";

  private static final String APPLICATION_JSON_CHARSET_UTF_8 = "application/json;charset=UTF-8";

  private static final Logger LOG = LoggerFactory.getLogger(ChecksumService.class);

  public static final String CHECKSUM_0 = "0";

  private final transient Gson gson = new Gson();

  @Reference
  private ChecksumRegister checksumRegister;


  @Override
  protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
      throws IOException {
    response.setContentType(APPLICATION_JSON_CHARSET_UTF_8);
    List<Checksum> checksumServices = checksumRegister.getChecksum();

    Optional<String[]> selectedModule = Optional.ofNullable(request.getParameterValues(PARAMETER_MODULE));

    String checksumResult;
    if (selectedModule.isPresent()) {
      checksumResult = calculateChecksumForSelectedModules(checksumServices, selectedModule.get());
    } else {
      checksumResult = calculateChecksumForAllModules(checksumServices);
    }

    String jsonResult = prepareJson(checksumResult);
    response.getWriter().write(jsonResult);
  }

  private String calculateChecksumForAllModules(List<Checksum> checksumServices) {
    List<String> checksumsResult;
    checksumsResult = checksumServices.stream()
        .sorted(Comparator.comparing(Checksum::getModuleName))
        .map(Checksum::getChecksum)
        .collect(Collectors.toList());
    return DigestUtils.md5Hex(checksumsResult.toString());
  }

  private String calculateChecksumForSelectedModules(List<Checksum> checksumServices, String[] selectedModule) {
    Set<String> requiredModules = new HashSet<>(Arrays.asList(selectedModule));
    List<String> checksumsResult = checksumServices.stream()
        .filter(checksum -> requiredModules.contains(checksum.getModuleName()))
        .sorted(Comparator.comparing(Checksum::getModuleName))
        .map(Checksum::getChecksum)
        .collect(Collectors.toList());
    String checksumResult;
    checksumResult = prepareChecksum(selectedModule, requiredModules, checksumsResult);
    return checksumResult;
  }

  private String prepareChecksum(String[] selectedModule, Set<String> requiredModules, List<String> checksum) {
    String checksumResult;
    if (noChecksumsToCalculate(requiredModules, checksum)) {
      checksumResult = CHECKSUM_0;
      LOG.info("No checksums to calculate. Result: 0");
    } else if (notAllUrlParametersWasRecognizedCorrectly(requiredModules, checksum)) {
      checksumResult = CHECKSUM_0;
      LOG.warn("Not all parameter from url was recognized. At least one parameter in url is incorrect: " + selectedModule.toString());
      //todo throw exception ?
    } else if (checksum.size() == 1) {
      // if we have only one checksum then we could not calculate again
      checksumResult = checksum.get(0);
    } else {
      checksumResult = DigestUtils.md5Hex(checksum.toString());
    }
    return checksumResult;
  }

  private boolean noChecksumsToCalculate(Set<String> requiredModules, List<String> checksum) {
    return checksum.isEmpty() && requiredModules.isEmpty();
  }

  private boolean notAllUrlParametersWasRecognizedCorrectly(Set<String> requiredModules, List<String> checksum) {
    return requiredModules.size() != checksum.size();
  }

  private String prepareJson(String combinedChecksums) {
    HashMap<String, String> map = new HashMap<>();
    map.put(PROJECT_CHECK_SUM, combinedChecksums);
    return gson.toJson(map);
  }

}