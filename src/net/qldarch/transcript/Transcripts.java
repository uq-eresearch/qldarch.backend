package net.qldarch.transcript;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.inject.Inject;

import net.qldarch.media.Mimetypes;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

public class Transcripts {

  @Inject
  private Mimetypes mimetypes;

  public Reader reader(File f) throws Exception {
    String mimetype = mimetypes.mimetype(f);
    String suffix = mimetypes.suffix(mimetype);
    if(StringUtils.contains(mimetype, "application/msword")
        || suffix.toLowerCase().equals("doc")
        || StringUtils
            .contains(mimetype, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        || suffix.toLowerCase().equals("docx")) {
      ParseContext c = new ParseContext();
      Detector detector = new DefaultDetector();
      Parser p = new AutoDetectParser(detector);
      c.set(Parser.class, p);
      StringWriter writer = new StringWriter();
      Metadata metadata = new Metadata();
      try(InputStream in = new FileInputStream(f)) {
        ContentHandler handler = new BodyContentHandler(writer);
        p.parse(in, handler, metadata, c);
        return new StringReader(writer.toString());
      }
    } else {
      return new FileReader(f);
    }
  }

  public ParseResult parse(File f) {
    try(Reader reader = reader(f)) {
      return new TranscriptParser(reader).parse();
    } catch(Exception e) {
      return new ParseResult("failed to parse " + f.getAbsolutePath(), e);
    }
  }
}
