package net.qldarch.structure;

import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.qldarch.db.Db;
import net.qldarch.db.Rsc;
import net.qldarch.guice.Bind;
import net.qldarch.resteasy.Lifecycle;

@Bind
@Singleton
public class BuildingTypologies implements Lifecycle {

  @Inject
  private Db db;

  private Set<String> types;

  public Set<String> filter(Set<String> typologies) {
    return typologies!=null?typologies.stream().filter(
        typology -> types.contains(typology)).collect(Collectors.toSet()):null;
  }

  @Override
  public void start() {
    try {
      types = db.executeQuery("select type from buildingtypologytype", Rsc::fetchAll).stream().map(
          m -> (String)m.get("type")).collect(Collectors.toSet());
    } catch(Exception e) {
      throw new RuntimeException("failed to initialize building typologies", e);
    }
  }

  @Override
  public void stop() {}

}
