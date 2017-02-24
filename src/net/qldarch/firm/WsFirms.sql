with images as (
  select distinct on (depicts) id, depicts from media
  where depicts is not null and deleted is null and
  type in ('Photograph', 'Portrait', 'Image')
  order by depicts, preferred desc nulls last, media.id
)
select
  firm.id,
  firm.australian,
  firm.startdate as start,
  firm.enddate as end,
  archobj.label,
  images.id as media
from
  firm join archobj on firm.id = archobj.id
  left outer join images on firm.id = images.depicts
where
  archobj.deleted is null
order by
  firm.id
;