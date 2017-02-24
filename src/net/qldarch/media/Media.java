package net.qldarch.media;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.qldarch.archobj.ArchObj;
import net.qldarch.gson.JsonExclude;
import net.qldarch.guice.Guice;
import net.qldarch.openstack.ObjectStore;

@Entity
@Table(name="media")
@Data
@EqualsAndHashCode(of={"id"})
public class Media {

  @Id
  private Long id;

  @Column(name="title")
  private String label;

  private String description;

  private String filename;

  @JsonExclude
  private String path;

  @JsonExclude
  @Enumerated(EnumType.STRING)
  private StorageType storagetype;

  private String mimetype;

  private Long filesize;

  private String creator;

  private Date created;

  private String identifier;

  private String location;

  private String projectnumber;

  private String rights;

  @Enumerated(EnumType.STRING)
  private MediaType type;

  @JsonExclude
  private Timestamp deleted;

  @JsonExclude
  private String hash;

  private Timestamp preferred;

  @ManyToOne
  @JoinColumn(name = "depicts")
  private ArchObj depicts;

  @Transient
  private String url;

  private Long owner;

  @PostLoad
  public void calculateUrl() {
    if(StorageType.External.equals(storagetype)) {
      this.url = path;
    } else if(StorageType.ObjectStore.equals(storagetype)) {
      this.url = Guice.injector().getInstance(ObjectStore.class).getLocation(path);
    }
  }

  public boolean isDeleted() {
    return deleted != null;
  }

  public Map<String, Object> asMap() {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", id);
    m.put("label", label);
    m.put("description", description);
    m.put("filename", filename);
    m.put("mimetype", mimetype);
    m.put("filesize", filesize);
    m.put("creator", creator);
    m.put("created", created);
    m.put("identifier", identifier);
    m.put("location", location);
    m.put("projectnumber", projectnumber);
    m.put("rights", rights);
    if(type!=null) {
      m.put("type", type.toString());
    }
    return m;
  }

  private static boolean isImage (Media media) {
    return MediaType.equalsAny(media.getType(), MediaType.Image, MediaType.Photograph, MediaType.Portrait,
        MediaType.LineDrawing) && StringUtils.startsWith(media.getMimetype(), "image/");
  }

  private static Comparator<Media> preferred() {
    return (Media m1, Media m2) -> {
      if((m1.getPreferred() == null) && (m2.getPreferred() == null)) {
        return m1.getId().compareTo(m2.getId());
      } else if((m1.getPreferred() != null) && (m2.getPreferred() != null)) {
        return m2.getPreferred().compareTo(m1.getPreferred());
      } else if(m1.getPreferred() == null) {
        return 1;
      } else {
        return -1;
      }
    };
  }
  
  public static Media preferredImage(Collection<Media> media) {
    return media!=null?media.stream().filter(Objects::nonNull).filter(
        Media::isImage).sorted(preferred()).findFirst().orElse(null):null;
  }

}
