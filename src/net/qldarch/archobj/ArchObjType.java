package net.qldarch.archobj;

import org.apache.commons.lang3.StringUtils;

import net.qldarch.firm.Firm;
import net.qldarch.interview.Interview;
import net.qldarch.person.Person;
import net.qldarch.structure.Structure;
import net.qldarch.text.Article;

public enum ArchObjType {

  person(Person.class), structure(Structure.class),
  firm(Firm.class), interview(Interview.class),
  award, event, organisation, education, government,
  place, publication, topic, article(Article.class);

  private final Class<? extends ArchObj> type;

  private ArchObjType(Class<? extends ArchObj> type) {
    this.type = type;
  }

  private ArchObjType() {
    this.type = ArchObj.class;
  }

  public Class<? extends ArchObj> getImplementingClass() {
    return type;
  }

  public static ArchObjType of(String type) {
    return StringUtils.isBlank(type)?null:ArchObjType.valueOf(type);
  }

}
