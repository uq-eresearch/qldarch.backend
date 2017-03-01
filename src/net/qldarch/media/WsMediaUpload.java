package net.qldarch.media;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.mail.internet.ContentDisposition;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;

import net.qldarch.archobj.ArchObj;
import net.qldarch.configuration.Cfg;
import net.qldarch.hibernate.HS;
import net.qldarch.jaxrs.ContentType;
import net.qldarch.security.SignedIn;
import net.qldarch.security.User;
import net.qldarch.util.ContentDispositionSupport;
import net.qldarch.util.ObjUtils;

@Path("media/upload")
public class WsMediaUpload {

  @Inject
  private HS hs;

  @Inject @Nullable
  private User user;

  @Inject @Cfg("media.upload")
  private String uploadFolder;

  @Inject
  private MediaArchive archive;

  @Inject
  private Mimetypes mimetypes;

  private AtomicInteger tmpfilename = new AtomicInteger();

  private File saveTemp(InputStream in) {
    try {
      final File f = new File(new File(uploadFolder), Integer.toString(tmpfilename.getAndIncrement()));
      FileUtils.deleteQuietly(f);
      FileUtils.copyInputStreamToFile(in, f);
      return f;
    } catch(Exception e) {
      throw new RuntimeException("failed to save uploaded file to temp location", e);
    }
  }

  private String getParam(InputPart part) {
    try {
      return part.getBodyAsString();
    } catch(Exception e) {
      throw new RuntimeException("failed to read body as String", e);
    }
  }

  private String md5Hash(File f) throws IOException {
    try(FileInputStream in = new FileInputStream(f)) {
      return DigestUtils.md5Hex(in);
    }
  }

  private String randomString() {
    return String.format("%08x", new Random().nextLong());
  }

  @POST
  @Consumes("multipart/form-data")
  @Produces(ContentType.JSON)
  @SignedIn
  public Response upload(MultipartInput input) {
    if(user.isReader()) {
      return Response.status(403).build();
    }
    Long depicts = null;
    String filename = null;
    File temp = null;
    String label = null;
    MediaType type = null;
    for(InputPart part : input.getParts()) {
      ContentDisposition cd = ContentDispositionSupport.contentDisposition(part.getHeaders());
      if(cd != null) {
        final String name = cd.getParameter("name");
        if(StringUtils.equals("file", name)) {
          filename = cd.getParameter("filename");
          try {
            temp = saveTemp(part.getBody(InputStream.class, null));
          } catch(Exception e) {
            throw new RuntimeException("failed to save file", e);
          }
        } else if(StringUtils.equals("depicts", name)) {
          depicts = ObjUtils.asLong(getParam(part));
        } else if(StringUtils.equals("label", name)) {
          label = getParam(part);
        } else if(StringUtils.equals("type", name)) {
          try {
            type = MediaType.valueOf(getParam(part));
          } catch(Exception e) {}
        }
      }
    }
    if(temp != null) {
      try {
        final String mimetype = mimetypes.mimetype(temp);
        final String suffix = mimetypes.suffix(mimetype);
        final String hash = md5Hash(temp);
        final long filesize = temp.length();
        if(StringUtils.isNotBlank(hash) && StringUtils.isNotBlank(suffix) &&
            StringUtils.isNotBlank(label) && (type != null)) {
          String path = String.format("%s.%s", hash, suffix);
          File fArchive = archive.getLocalFile(path);
          if(fArchive.exists()) {
            if(!FileUtils.contentEquals(temp, fArchive)) {
           // handle md5 hash collision
              path = String.format("%s.%s", randomString(), suffix);
              fArchive = archive.getLocalFile(path);
              if(fArchive.exists()) {
                return Response.status(500).build();
              }
              FileUtils.moveFile(temp, fArchive);
            }
          } else {
            FileUtils.moveFile(temp, fArchive);
          }
          Media media = new Media();
          media.setLabel(label);
          media.setFilename(filename);
          media.setPath(path);
          media.setStoragetype(StorageType.Local);
          media.setMimetype(mimetype);
          media.setFilesize(filesize);
          media.setHash(hash);
          media.setType(type);
          if(depicts != null) {
            media.setDepicts(hs.get(ArchObj.class, depicts));
          }
          media.setOwner(user.getId());
          hs.save(media);
          return Response.ok().entity(media).build();
        } else {
          return Response.status(400).entity(String.format(
              "hash '%s', suffix '%s', label '%s', type '%s'",
              hash, suffix, label, type)).build();
        }
      } catch(Exception e) {
        throw new RuntimeException("file upload failed", e);
      } finally {
        FileUtils.deleteQuietly(temp);
      }
    }
    return Response.status(400).build();
  }
}
