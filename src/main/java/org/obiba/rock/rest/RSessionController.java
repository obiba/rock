/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.rock.rest;


import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import jakarta.servlet.http.HttpServletResponse;
import org.obiba.rock.domain.RServeCommand;
import org.obiba.rock.domain.RServeSession;
import org.obiba.rock.model.RCommand;
import org.obiba.rock.model.RSession;
import org.obiba.rock.r.*;
import org.obiba.rock.security.Roles;
import org.obiba.rock.service.RSessionService;
import org.rosuda.REngine.REXPMismatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
public class RSessionController {

  private static final Logger log = LoggerFactory.getLogger(RSessionController.class);

  @Autowired
  private RSessionService rSessionService;

  /**
   * Get the R session object.
   *
   * @param id R session ID
   * @return
   */
  @GetMapping("/r/session/{id}")
  RSession getSession(@AuthenticationPrincipal User user, @PathVariable String id) {
    return getRServeSessionForManagement(user, id);
  }

  /**
   * Close the R session.
   *
   * @param id R session ID
   * @return
   */
  @DeleteMapping("/r/session/{id}")
  ResponseEntity<?> deleteSession(@AuthenticationPrincipal User user, @PathVariable String id) {
    getRServeSessionForManagement(user, id);
    rSessionService.closeRSession(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Assign an R object to a symbol: content is expected to be the serialized form of the R object, base64 encoded. If
   * asynchronous, the R command object is returned.
   *
   * @param id      R session ID
   * @param symbol  The R symbol to assign in the R session.
   * @param async   If true, the command is put in a queue and executed sequentially when possible.
   * @param content The R object, serialized, base64 encoded.
   * @param ucb
   * @return
   */
  @PostMapping(value = "/r/session/{id}/_assign", consumes = "application/x-rdata")
  ResponseEntity<RCommand> assignData(@AuthenticationPrincipal User user,
                                      @PathVariable String id, @RequestParam(name = "s") String symbol,
                                      @RequestParam(name = "async", defaultValue = "false") boolean async,
                                      @RequestBody String content, UriComponentsBuilder ucb) {
    DataAssignROperation rop = new DataAssignROperation(symbol, content);
    return doAssign(user, id, rop, async, ucb);
  }

  /**
   * Assign an R expression to a symbol. If asynchronous, the R command object
   * is returned.
   *
   * @param id     R session ID
   * @param symbol The R symbol to assign in the R session.
   * @param async  If true, the command is put in a queue and executed sequentially when possible.
   * @param script The R expression to evaluate.
   * @param ucb
   * @return
   */
  @PostMapping(value = "/r/session/{id}/_assign", consumes = "application/x-rscript")
  ResponseEntity<RCommand> assignScript(@AuthenticationPrincipal User user,
                                        @PathVariable String id, @RequestParam(name = "s") String symbol,
                                        @RequestParam(name = "async", defaultValue = "false") boolean async,
                                        @RequestBody String script, UriComponentsBuilder ucb) {
    RScriptROperation rop = new RScriptAssignROperation(String.format("base::assign('%s', %s)", symbol, script));
    return doAssign(user, id, rop, async, ucb);
  }

  /**
   * Evaluates an R expression. If asynchronous, the R command object is returned, else
   * the resulting R object is returned in R serialization format (use base::unserialize() to extract the object).
   *
   * @param id     R session ID
   * @param async  If true, the command is put in a queue and executed sequentially when possible.
   * @param script The R expression to evaluate.
   * @param ucb
   * @return
   */
  @PostMapping(value = "/r/session/{id}/_eval", consumes = "application/x-rscript", produces = "application/octet-stream")
  ResponseEntity<?> evalScript(@AuthenticationPrincipal User user,
                               @PathVariable String id,
                               @RequestParam(name = "async", defaultValue = "false") boolean async,
                               @RequestBody String script, UriComponentsBuilder ucb) {
    RScriptROperation rop = new RScriptROperation(script);
    return doEval(user, id, rop, async, ucb);
  }

  /**
   * Evaluates an R expression. If asynchronous, the R command object is returned, else
   * the resulting R object is returned in JSON format.
   *
   * @param id     R session ID
   * @param async  If true, the command is put in a queue and executed sequentially when possible.
   * @param script The R expression to evaluate.
   * @param ucb
   * @return
   */
  @PostMapping(value = "/r/session/{id}/_eval", consumes = "application/x-rscript", produces = "application/json")
  ResponseEntity<?> evalScriptJSON(@AuthenticationPrincipal User user,
                                   @PathVariable String id,
                                   @RequestParam(name = "async", defaultValue = "false") boolean async,
                                   @RequestBody String script, UriComponentsBuilder ucb) {
    RScriptToJSONROperation rop = new RScriptToJSONROperation(script);
    return doEval(user, id, rop, async, ucb);
  }

  //
  // File transfers
  //

  /**
   * Upload a file at specified location, either relative to the R session root or to the R session temporary directory.
   *
   * @param id        R session ID
   * @param file      File data
   * @param path      Relative path where to upload file (any missing parent directories will be created).
   * @param overwrite Overwrite the file it already exists.
   * @param temp      If true, the root directory is the R session's temporary directory instead of the original working directory.
   * @return
   */
  @PostMapping(value = "/r/session/{id}/_upload", consumes = "multipart/form-data")
  ResponseEntity<?> uploadFile(@AuthenticationPrincipal User user,
                               @PathVariable String id,
                               @RequestParam("file") MultipartFile file,
                               @RequestParam(value = "path", required = false) String path,
                               @RequestParam(value = "overwrite", required = false, defaultValue = "false") boolean overwrite,
                               @RequestParam(value = "temp", required = false, defaultValue = "false") boolean temp) {
    RServeSession rServeSession = getRServeSession(user, id);
    String destinationPath = Strings.isNullOrEmpty(path) ? file.getOriginalFilename() : decodeParameter(path);
    try {
      doWriteFile(file.getInputStream(), temp ? rServeSession.getTempDir() : rServeSession.getWorkDir(), destinationPath, overwrite);
    } catch (IOException e) {
      log.error("File write failed", e);
      throw new RRuntimeException("File write failed");
    }
    return ResponseEntity.ok().build();
  }

  /**
   * Download a file.
   *
   * @param id       R session ID
   * @param path     Relative path of the file.
   * @param temp     If true, the root directory is the R session's temporary directory instead of the original working directory.
   * @param response
   */
  @GetMapping("/r/session/{id}/_download")
  void downloadFile(HttpServletResponse response,
                    @AuthenticationPrincipal User user,
                    @PathVariable String id,
                    @RequestParam(value = "path", required = false) String path,
                    @RequestParam(value = "temp", required = false, defaultValue = "false") boolean temp) {
    RServeSession rServeSession = getRServeSession(user, id);

    File sourceFile = new File(temp ? rServeSession.getTempDir() : rServeSession.getWorkDir(), decodeParameter(path));

    // verify file exist and is regular
    if (!sourceFile.exists()) {
      throw new IllegalArgumentException("File does not exist");
    } else if (sourceFile.isDirectory()) {
      throw new IllegalArgumentException("File is a directory");
    }

    // verify download is from R session's work or temp folder
    try {
      Path sourcePath = sourceFile.toPath().toRealPath();
      Path workPath = new File(rServeSession.getWorkDir()).toPath().toRealPath();
      Path tempPath = new File(rServeSession.getTempDir()).toPath().toRealPath();
      if (!sourcePath.startsWith(workPath) && !sourcePath.startsWith(tempPath)) {
        throw new IllegalArgumentException("Source file path is not valid");
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("Source file path is not valid");
    }

    // guess mime type and prepare response header
    String mimeType = URLConnection.guessContentTypeFromName(sourceFile.getName());
    if (Strings.isNullOrEmpty(mimeType)) {
      try (InputStream in = new FileInputStream(sourceFile)) {
        mimeType = URLConnection.guessContentTypeFromStream(in);
      } catch (IOException e) {
        // ignore
      }
    }
    if (Strings.isNullOrEmpty(mimeType)) {
      // set to binary type if MIME mapping not found
      mimeType = "application/octet-stream";
    }
    response.setContentType(mimeType);
    response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", sourceFile.getName()));

    // write file in response stream
    try {
      // copies all bytes from a file to an output stream
      Files.copy(sourceFile, response.getOutputStream());
      // flushes output stream
      response.getOutputStream().flush();
    } catch (IOException e) {
      throw new IllegalArgumentException("File read failed");
    }
  }

  //
  // Commands management
  //

  /**
   * Get the R session's commands.
   *
   * @param id R session ID
   * @return
   */
  @GetMapping("/r/session/{id}/commands")
  List<RCommand> getCommands(@AuthenticationPrincipal User user, @PathVariable String id) {
    return StreamSupport.stream(getRServeSession(user, id).getRCommands().spliterator(), false)
        .map(c -> (RCommand) c).collect(Collectors.toList());
  }

  /**
   * Get a R session's command.
   *
   * @param id    R session ID
   * @param cmdId R command ID
   * @return
   */
  @GetMapping("/r/session/{id}/command/{cmdId}")
  RCommand getCommand(@AuthenticationPrincipal User user, @PathVariable String id, @PathVariable String cmdId) {
    return getRServeSession(user, id).getRCommand(cmdId);
  }

  /**
   * Delete a R session's command.
   *
   * @param id    R session ID
   * @param cmdId R command ID
   * @return
   */
  @DeleteMapping("/r/session/{id}/command/{cmdId}")
  ResponseEntity<?> deleteCommand(@AuthenticationPrincipal User user, @PathVariable String id, @PathVariable String cmdId) {
    getRServeSession(user, id).removeRCommand(cmdId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Get the result of the R session's command.
   *
   * @param id     R session ID
   * @param cmdId  R command ID
   * @param wait   If true, wait for the command to complete.
   * @param remove Remove command from list after result has been retrieved.
   * @return
   */
  @GetMapping(value = "/r/session/{id}/command/{cmdId}/result", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  ResponseEntity<?> getCommandResult(@AuthenticationPrincipal User user,
                                     @PathVariable String id, @PathVariable String cmdId,
                                     @RequestParam(name = "wait", defaultValue = "false") boolean wait,
                                     @RequestParam(name = "rm", defaultValue = "true") boolean remove) {
    RServeSession rSession = getRServeSession(user, id);
    RServeCommand rCommand = rSession.getRCommand(cmdId);
    ResponseEntity<?> noContent = ResponseEntity.noContent().build();
    if (!rCommand.getFinished()) {
      if (wait) {
        try {
          synchronized (rCommand) {
            rCommand.wait();
          }
        } catch (InterruptedException e) {
          return noContent;
        }
      } else {
        return noContent;
      }
    }
    return getFinishedRCommandResult(rSession, rCommand, remove);
  }

  //
  // Private methods
  //

  private String decodeParameter(String param) {
    if (Strings.isNullOrEmpty(param)) return param;
    try {
      return URLDecoder.decode(param, "utf-8");
    } catch (UnsupportedEncodingException e) {
      return param;
    }
  }

  private ResponseEntity<RCommand> doAssign(User user, String id, ROperation rop, boolean async, UriComponentsBuilder ucb) {
    RServeSession rSession = getRServeSession(user, id);
    if (async) {
      String rCommandId = rSession.executeAsync(rop);
      RCommand rCommand = rSession.getRCommand(rCommandId);
      return ResponseEntity.created(ucb.path("/r/session/{id}/command/{rid}").buildAndExpand(rSession.getId(), rCommandId).toUri())
          .body(rCommand);
    } else {
      rSession.execute(rop);
      return ResponseEntity.ok().build();
    }
  }

  private ResponseEntity<?> doEval(User user, String id, ROperationWithResult rop, boolean async, UriComponentsBuilder ucb) {
    RServeSession rSession = getRServeSession(user, id);
    if (async) {
      String rCommandId = rSession.executeAsync(rop);
      RCommand rCommand = rSession.getRCommand(rCommandId);
      return ResponseEntity.created(ucb.path("/r/session/{id}/command/{rid}").buildAndExpand(rSession.getId(), rCommandId).toUri())
          .contentType(MediaType.APPLICATION_JSON)
          .body(rCommand);
    } else {
      rSession.execute(rop);
      if (rop.hasResult()) {
        if (rop.hasRawResult())
          return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(rop.getRawResult().asBytes());
        else {
          try {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(toJSONResult(rop));
          } catch (REXPMismatchException e) {
            throw new RRuntimeException("No eval result could be extracted as a string");
          }
        }
      }
      throw new RRuntimeException("No eval result could be extracted");
    }
  }

  private String toJSONResult(ROperationWithResult rop) throws REXPMismatchException {
    String json = rop.getResult().asString();
    if ("{}".equals(json))
      json = "null";
    return json;
  }

  private void doWriteFile(InputStream in, String rootFolder, String destinationPath, boolean overwrite) throws IOException {
    File outFile = new File(rootFolder, destinationPath);
    if (outFile.exists()) {
      if (outFile.isDirectory())
        throw new IllegalArgumentException("Destination file cannot be a directory");
      else if (!overwrite)
        throw new IllegalArgumentException("File exists and cannot be overridden");
    }
    // make sure the destination is in the root folder
    Path rootPath = new File(rootFolder).toPath().toRealPath();
    File parentFile = outFile.getParentFile();
    while (!parentFile.exists()) parentFile = parentFile.getParentFile();
    Path parentPath = parentFile.toPath().toRealPath();
    if (!parentPath.startsWith(rootPath)) {
      throw new IllegalArgumentException("Destination folder is not valid");
    }
    if (!outFile.getParentFile().exists() && !outFile.getParentFile().mkdirs())
      throw new IllegalArgumentException("File parent folder cannot be created");

    try (FileOutputStream out = new FileOutputStream(outFile)) {
      ByteStreams.copy(in, out);
    }
  }

  private ResponseEntity<?> getFinishedRCommandResult(RServeSession rSession, RServeCommand rCommand, boolean remove) {
    ResponseEntity<?> resp = ResponseEntity.noContent().build();
    if (rCommand.getWithResult()) {
      ROperationWithResult rop = rCommand.asROperationWithResult();
      if (rop.hasResult()) {
        if (rop.hasRawResult()) {
          resp = ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(rop.getRawResult().asBytes());
        } else {
          try {
            resp = ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(toJSONResult(rop));
          } catch (REXPMismatchException e) {
            throw new RRuntimeException("No eval result could be extracted as a string");
          }
        }
      }
    }
    if (remove) rSession.removeRCommand(rCommand.getId());
    return resp;
  }

  private RServeSession getRServeSession(User user, String id) {
    RServeSession session = rSessionService.getRServeSession(id);
    if (!session.getSubject().equals(rSessionService.getUsername(user)) && !Roles.isAdmin(user))
      throw new AccessDeniedException("R session is not owned by authenticated user");
    return session;
  }

  private RServeSession getRServeSessionForManagement(User user, String id) {
    RServeSession session = rSessionService.getRServeSession(id);
    if (!session.getSubject().equals(rSessionService.getUsername(user)) && (!Roles.isAdmin(user) || !Roles.isManager(user)))
      throw new AccessDeniedException("R session is not owned by authenticated user");
    return session;
  }

}
