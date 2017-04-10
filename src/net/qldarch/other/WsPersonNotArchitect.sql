with images as (
  select distinct on (depicts) id, depicts from media
  where depicts is not null and deleted is null and
  type in ('Photograph', 'Portrait', 'Image')
  order by depicts, preferred desc nulls last, media.id
)
select
  person.id,
  archobj.label,
  person.preflabel,
  person.firstname,
  person.lastname,
  images.id as media
from
  person join archobj on person.id = archobj.id
  left outer join images on person.id = images.depicts
where
 person.architect = false and
 archobj.deleted is null
order by
  person.id
;