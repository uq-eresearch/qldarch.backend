package net.qldarch.media;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import lombok.Data;

@Entity
@Table(name="thumbnail")
@Data
public class Thumbnail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name="media")
  private Media media;

  private int width;

  private int height;

  private String path;

  private String hash;

  @Temporal(TemporalType.TIMESTAMP)
  private Date created;

  private String mimetype;

  private long filesize;

  @Transient
  private ContentProvider contentprovider;

}
