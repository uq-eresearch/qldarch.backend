with images as (
  select distinct on (depicts) id, depicts from media
  where depicts is not null and deleted is null and
  type in ('Photograph', 'Portrait', 'Image')
  order by depicts, preferred desc nulls last, media.id
),
typologies as (
  select distinct on (structure) structure, typology from buildingtypology
  order by structure
)
select
  structure.id,
  structure.australian,
  structure.lat,
  structure.lng,
  archobj.label,
  images.id as media,
  typologies.typology
from
  structure join archobj on structure.id = archobj.id
  left outer join images on structure.id = images.depicts
  left outer join typologies on structure.id = typologies.structure
where
  archobj.deleted is null
order by
  structure.id
;