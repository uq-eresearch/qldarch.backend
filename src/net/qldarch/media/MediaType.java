package net.qldarch.media;

public enum MediaType {
  Article,
  Image,
  LineDrawing,
  Photograph,
  Portrait,
  Spreadsheet,
  Transcript,
  Text,
  Youtube,
  Audio,
  ;

  public static boolean equalsAny(MediaType type, MediaType... search) {
    for(MediaType s : search) {
      if(type.equals(s)) {
        return true;
      }
    }
    return false;
  }
}
