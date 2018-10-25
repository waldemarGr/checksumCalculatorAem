package com.cognifide.bridge.whiteBoardTest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.Servlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;


@Component(
    service = Servlet.class,
    immediate = true,
    property = {
        "sling.servlet.methods=GET",
        "sling.servlet.paths=" + ChecksumRegisteredListServlet.CHECKSUM_PATH})
public class ChecksumRegisteredListServlet extends SlingAllMethodsServlet {

  @SuppressWarnings("WeakerAccess")
  public static final String CHECKSUM_PATH = "/bin/bridge/checksum/allmodules";

  private static final String MODULE = "module";

  private static final String CHECKSUM = "checksum";

  private static final String APPLICATION_JSON_CHARSET_UTF_8 = "application/json;charset=UTF-8";

  private final transient Gson gson = new Gson();

  @Reference
  private ChecksumRegister checksumRegister;

  @Override
  protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
      throws IOException {
    response.setContentType(APPLICATION_JSON_CHARSET_UTF_8);
    List<Checksum> sortedChecksumServices = checksumRegister.getChecksum()
        .stream()
        .sorted()
        .collect(Collectors.toList());
    List<JsonObject> checksums = prepareCheckSumsObject(sortedChecksumServices);
    response.getWriter().write(gson.toJson(checksums));
  }

  private List<JsonObject> prepareCheckSumsObject(List<Checksum> checksumServices) {
    List<JsonObject> checksums = new LinkedList<>();
    for (Checksum checksumService : checksumServices) {
      JsonObject jsonObjectChecksum = new JsonObject();
      jsonObjectChecksum.addProperty(MODULE, checksumService.getModuleName());
      jsonObjectChecksum.addProperty(CHECKSUM, checksumService.getChecksum());
      checksums.add(jsonObjectChecksum);
    }
    return checksums;
  }

}