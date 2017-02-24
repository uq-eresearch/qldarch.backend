package net.qldarch.archobj;

import java.sql.Timestamp;
import java.time.Instant;

import com.google.gson.Gson;

import net.qldarch.gson.serialize.Json;
import net.qldarch.hibernate.HS;
import net.qldarch.security.User;

public class VersionUtils {

  public static ArchObjVersion createNewVersion(HS hs, User user, ArchObj archobj, String comment) {
    final ArchObjVersion version = new ArchObjVersion();
    version.setComment(comment);
    version.setDocument(new Gson().toJson(new Json().toJsonTree(archobj.asMap())));
    version.setParent(archobj.getVersion());
    version.setOid(archobj.getId());
    version.setCreated(new Timestamp(Instant.now().toEpochMilli()));
    version.setOwner(user.getId());
    hs.save(version);
    archobj.setVersion(version.getId());
    return version;
  }

}
