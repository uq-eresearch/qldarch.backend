package net.qldarch.media;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TiffConverterToPng {

  @Getter
  File output;

  public TiffConverterToPng(File tiff) {
    this.output = convert(tiff);
  }

  private File convert(File tiff) {
    File png = null;
    try {
      png = new File(tiff.getAbsolutePath() + ".png");
      BufferedImage image = ImageIO.read(tiff);
      ImageIO.write(image, "png", png);
      log.debug("converted tiff image {} to {}", tiff.getAbsolutePath(), png.getAbsolutePath());
    } catch(IOException e) {
      log.debug("failed to convert tiff image, {}", e);
    }
    if(png.exists()) {
      delete(tiff.toPath());
      return png;
    } else {
      log.debug("returned tiff image");
      return tiff;
    }
  }

  private void delete(Path path) {
    try {
      Files.delete(path);
      log.debug("deleted, {}", path.toString());
    } catch(NoSuchFileException x) {
      log.debug("no such file or directory, {}", path.toString());
    } catch(DirectoryNotEmptyException x) {
      log.debug("not empty, {}", path.toString());
    } catch(IOException x) {
      log.debug(x.toString());
    }
  }
}
