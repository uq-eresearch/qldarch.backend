package net.qldarch.openstack;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class FileInfo {

  public final Long id;

  public final String name;

  public final Long bytes;

  public final String mimetype;

  public final String hash;

}
