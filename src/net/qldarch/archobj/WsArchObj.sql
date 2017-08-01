with images as (
  select distinct on (depicts) id, depicts from media
  where depicts is not null and deleted is null and
  type in ('Photograph', 'Portrait', 'Image', 'LineDrawing')
  order by depicts, preferred desc nulls last, media.id
), typologies as (
  select distinct on (structure) structure, typology from buildingtypology
  order by structure
)
select
  r.id as relationshipId,
  r.type as relationship,
  r.note,
  r.fromyear,
  r.untilyear,
  r.subject,
  s.label as subjectlabel,
  s.type as subjecttype,
  sp.practicedinqueensland as subjectpracticedinqueensland,
  sp.architect as subjectarchitect,
  r.object,
  o.label as objectlabel,
  o.type as objecttype,
  op.practicedinqueensland as objectpracticedinqueensland,
  op.architect as objectarchitect,
  os.completion as objectcompletion,
  r.source,
  ir.interview,
  ir.utterance,
  images.id as media,
  typologies.typology
from
  relationship r
  join archobj s on r.subject = s.id
  left join person sp on s.id = sp.id
  join archobj o on r.object = o.id
  left join person op on o.id = op.id
  left join structure os on o.id = os.id
  left join interviewrelationship ir on r.id = ir.id
  left join images on r.object = images.depicts
  left join typologies on r.object = typologies.structure
where
  r.subject = :id and
  o.deleted is null
union
select
  r.id as relationshipId,
  r.type as relationship,
  r.note,
  r.fromyear,
  r.untilyear,
  r.subject,
  s.label as subjectlabel,
  s.type as subjecttype,
  sp.practicedinqueensland as subjectpracticedinqueensland,
  sp.architect as subjectarchitect,
  r.object,
  o.label as objectlabel,
  o.type as objecttype,
  op.practicedinqueensland as objectpracticedinqueensland,
  op.architect as objectarchitect,
  os.completion as objectcompletion,
  r.source,
  ir.interview,
  ir.utterance,
  images.id as media,
  typologies.typology
from
  relationship r
  join archobj s on r.subject = s.id
  left join person sp on s.id = sp.id
  join archobj o on r.object = o.id
  left join person op on o.id = op.id
  left join structure os on o.id = os.id
  left join interviewrelationship ir on r.id = ir.id
  left join images on r.subject = images.depicts
  left join typologies on r.subject = typologies.structure
where
  r.object = :id and
  s.deleted is null
;
