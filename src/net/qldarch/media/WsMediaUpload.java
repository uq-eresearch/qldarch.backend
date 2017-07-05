package net.qldarch.media;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
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
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;

import net.qldarch.archobj.ArchObj;
import net.qldarch.configuration.Cfg;
import net.qldarch.hibernate.HS;
import net.qldarch.interview.Interview;
import net.qldarch.interview.Utterance;
import net.qldarch.jaxrs.ContentType;
import net.qldarch.person.Person;
import net.qldarch.search.Index;
import net.qldarch.search.update.UpdateMediaJob;
import net.qldarch.security.SignedIn;
import net.qldarch.security.User;
import net.qldarch.transcript.Exchange;
import net.qldarch.transcript.ParseResult;
import net.qldarch.transcript.Transcripts;
import net.qldarch.util.ContentDispositionSupport;
import net.qldarch.util.M;
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

  @Inject
  private Transcripts transcripts;

  @Inject
  private Index index;

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
      return Response.status(403).entity(M.of("msg", "Unauthorised user")).build();
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
        String mimetype = mimetypes.mimetype(temp);
        String suffix = mimetypes.suffix(mimetype);
        if(StringUtils.contains(mimetype, "image/tiff") || suffix.toLowerCase().equals("tiff")
            || suffix.toLowerCase().equals("tif")) {
          temp = new TiffConverterToPng(temp).getOutput();
          mimetype = mimetypes.mimetype(temp);
          suffix = mimetypes.suffix(mimetype);
        }
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
                return Response.status(500).entity(M.of("msg", "Media archive error")).build();
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
          if(isIntvwTscp(fArchive)) {
            Interview interview = hs.get(Interview.class, depicts);
            Set<Person> speakers = new HashSet<>();
            for(Person e : interview.getInterviewee()) {
              speakers.add(e);
            }
            for(Person r : interview.getInterviewer()) {
              speakers.add(r);
            }
            ParseResult pr = transcripts.parse(fArchive);
            List<Exchange> exchanges = pr.getTranscript().getExchanges();
            for(Exchange exchange : exchanges) {
              Utterance utterance = new Utterance();
              utterance.setInterview(interview);
              utterance.setSpeaker(selectPerByInit(speakers, exchange.getSpeaker()));
              utterance.setTime(Math.toIntExact(exchange.getSeconds()));
              utterance.setTranscript(exchange.getTranscript());
              hs.save(utterance);
            }
          }
          Analyzer analyzer = new StandardAnalyzer();
          IndexWriterConfig config = new IndexWriterConfig(analyzer);
          try(Directory directory = index.directory()) {
            try(IndexWriter writer = new IndexWriter(directory, config)) {
              new UpdateMediaJob(media, archive).run(writer);
              writer.commit();
            } catch(Exception e) {
              throw new RuntimeException("update search index failed", e);
            }
          } catch(IOException e) {
            throw new RuntimeException("failed to open search directory", e);
          }
          return Response.ok().entity(media).build();
        } else {
          return Response.status(400).entity(M.of("msg", String.format(
              "hash '%s', suffix '%s', label '%s', type '%s'",
              hash, suffix, label, type))).build();
        }
      } catch(Exception e) {
        throw new RuntimeException("file upload failed", e);
      } finally {
        FileUtils.deleteQuietly(temp);
      }
    }
    return Response.status(400).entity(M.of("msg", "Missing or unknown file, or bad request")).build();
  }

  private boolean isIntvwTscp(File f) {
    boolean isIntvwTscp = false;
    try {
      String line = new LineNumberReader(transcripts.reader(f)).readLine();
      if(StringUtils.startsWith(line.toLowerCase(), "interview")) {
        isIntvwTscp = true;
      }
    } catch(Exception e) {
      throw new RuntimeException("is interview transcripts reader failed: ", e);
    }
    return isIntvwTscp;
  }

  private double maxPerLblByStrDist(Set<Person> persons, String str) {
    List<Double> distances = new ArrayList<Double>();
    for(Person p : persons) {
      distances.add(StringUtils.getJaroWinklerDistance(p.getLabel(), str));
    }
    return Collections.max(distances);
  }

  private Person selectPerByInit(Set<Person> persons, String initial) {
    double maxDistance = maxPerLblByStrDist(persons, initial);
    for(Person p : persons) {
      if(maxDistance == StringUtils.getJaroWinklerDistance(p.getLabel(), initial)) {
        return p;
      }
    }
    return new Person();
  }
}
