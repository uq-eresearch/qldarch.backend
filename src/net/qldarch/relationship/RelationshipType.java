package net.qldarch.relationship;

public enum RelationshipType {
  Attended("attended"),
  Authored("authored"),
  Awarded("awarded"),
  Became("became"),
  ClientOf("client of"),
  CollaboratedWith("collaborated with"),
  DesignedBy("designed by"),
  Employment("employed by"),
  Founded("founded"),
  InfluencedBy("influenced by"),
  KnewOf("knew of"),
  KnewProfessionally("knew professionally"),
  KnewSocially("knew socially"),
  MentoredBy("mentored by"),
  MergedWith("merged with"),
  PartnerOf("partner of"),
  Read("reads"),
  Reference("references"),
  RelatedTo("related to"),
  StudiedAt("studied at"),
  StudiedWith("studied with"),
  TaughtAt("taught at"),
  TaughtBy("taught by"),
  TravelledTo("travelled to"),
  WasInfluenceBy("was influenced by"),
  WorkedOn("worked on"),
  WorkedWith("worked with"),
  WroteFor("wrote for");

  private String label;

  private RelationshipType(String label) {
    this.label = label;
  }

  public String label() {
    return label;
  }
}
