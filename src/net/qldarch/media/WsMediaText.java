package net.qldarch.media;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import javax.inject.Inject;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import net.qldarch.configuration.Cfg;
import net.qldarch.hibernate.HS;
import net.qldarch.jaxrs.ContentType;
import net.qldarch.util.M;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.POITextExtractor;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

@Path("media/text")
public class WsMediaText {

  @Inject
  private HS hs;

  @Inject
  private MediaArchive archive;

  @Inject
  @Cfg("media.upload")
  private String uploadFolder;

  @Inject
  private Mimetypes mimetypes;

  private Response notFound() {
    return Response.status(Status.NOT_FOUND).entity(M.of("msg", "Media not found")).build();
  }

  private String extractText(File f) {
    String extractedText = null;
    try {
      final InputStream in = new FileInputStream(f);
      if(in != null) {
        String mimetype = mimetypes.mimetype(f);
        String suffix = mimetypes.suffix(mimetype);
        if((mimetype != null && StringUtils.contains(mimetype,
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
            || (suffix != null && suffix.toLowerCase().equals("docx"))) {
          XWPFDocument doc = new XWPFDocument(in);
          POITextExtractor extractor = new XWPFWordExtractor(doc);
          extractedText = extractor.getText();
          extractor.close();
        } else if((mimetype != null && StringUtils.contains(mimetype, "application/msword"))
            || (suffix != null && suffix.toLowerCase().equals("doc"))) {
          POIFSFileSystem fileSystem = new POIFSFileSystem(in);
          POITextExtractor extractor = ExtractorFactory.createExtractor(fileSystem);
          extractedText = extractor.getText();
          extractor.close();
        } else if((mimetype != null && StringUtils.contains(mimetype, "application/pdf"))
            || (suffix != null && suffix.toLowerCase().equals("pdf"))) {
          PDDocument pdDoc = PDDocument.load(f);
          PDFTextStripper pdfStripper = new PDFTextStripper();
          extractedText = pdfStripper.getText(pdDoc);
          pdDoc.close();
        } else if((mimetype != null && StringUtils.contains(mimetype, "application/rtf"))
            || (suffix != null && suffix.toLowerCase().equals("rtf"))) {
          RTFEditorKit rtfParser = new RTFEditorKit();
          Document document = rtfParser.createDefaultDocument();
          rtfParser.read(new ByteArrayInputStream(FileUtils.readFileToByteArray(f)), document, 0);
          extractedText = document.getText(0, document.getLength());
        } else {
          Scanner txt = new Scanner(f);
          if(txt.hasNextLine()) {
            extractedText = txt.nextLine();
            while(txt.hasNextLine()) {
              extractedText += System.lineSeparator() + txt.nextLine();
            }
          }
          txt.close();
        }
        in.close();
      }
    } catch(Exception e) {
      throw new RuntimeException(String.format("failed to extract text from %s", f.getAbsolutePath()));
    }
    return extractedText;
  }

  @GET
  @Path("/{id}")
  @Produces(ContentType.JSON)
  public Response get(@PathParam("id") Long id) {
    Media media = hs.get(Media.class, id);
    if(media != null) {
      if(media.isDeleted()) {
        return notFound();
      }
      if(!(media.getType() == MediaType.Article || media.getType() == MediaType.Text || media.getType() == MediaType.Transcript)) {
        return Response.status(Status.NOT_FOUND)
            .entity(M.of("msg", "Media type is not an article or text or transcript")).build();
      }
      if(StorageType.External.equals(media.getStoragetype())
          || StorageType.ObjectStore.equals(media.getStoragetype())) {
        try {
          File fexternal = new File(new File(uploadFolder), media.getPath());
          FileUtils.deleteQuietly(fexternal);
          FileUtils.copyInputStreamToFile(archive.stream(media), fexternal);
          return Response.status(Response.Status.OK).entity(M.of("text", extractText(fexternal))).build();
        } catch(IOException e) {
          throw new RuntimeException(
              String.format("failed to extract text from external or objectstore media %s - %s", media.getId(),
                  media.getUrl(), e));
        }
      } else if(StorageType.Local.equals(media.getStoragetype())) {
        File flocal = archive.getLocalFile(media.getPath());
        return Response.status(Response.Status.OK).entity(M.of("text", extractText(flocal))).build();
      } else {
        throw new RuntimeException(String.format("unknown storage type for media %s/%s", media.getId(),
            media.getStoragetype()));
      }
    }
    return notFound();
  }
}
