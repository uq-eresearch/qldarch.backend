-- select first row in each group
-- nice "distinct on" explanation here: http://stackoverflow.com/a/7630564
with images as (
  select distinct on (depicts) id, depicts from media
  where depicts is not null and deleted is null and
  type in ('Photograph', 'Portrait', 'Image')
  order by depicts, preferred desc nulls last, media.id
)
select
  distinct on(interviewee.interviewee)
  interviewee.interviewee,
  interviewee.interview,
  archobj.label,
  images.id as media
from
  interviewee
  join archobj on interviewee.interviewee = archobj.id
  join images on images.depicts = interviewee.interviewee
where
  archobj.deleted is null
order by
  interviewee.interviewee,
  interviewee.interview
;
