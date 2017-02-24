select
  r.id as relationshipId,
  r.type as relationship,
  r.fromyear,
  r.untilyear,
  r.subject,
  s.label as subjectlabel,
  r.object,
  o.label as objectlabel,
  r.note,
  ir.utterance,
  r.created
from 
  relationship r
  join archobj s on r.subject = s.id
  join archobj o on r.object = o.id
  join interviewrelationship ir on r.id = ir.id
where 
  ir.interview = :interview
;
