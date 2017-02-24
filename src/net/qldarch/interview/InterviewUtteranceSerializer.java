package net.qldarch.interview;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.qldarch.gson.serialize.Context;
import net.qldarch.gson.serialize.Serializer;
import net.qldarch.person.Person;

public class InterviewUtteranceSerializer implements Serializer {

  @Override
  public JsonElement serialize(Object o, Context ctx) {
    final Utterance utterance = (Utterance)o;
    JsonObject result = new JsonObject();
    Person speaker = utterance.getSpeaker();
    result.addProperty("id", utterance.getId());
    result.addProperty("speakerid", speaker.getId());
    result.addProperty("speaker", StringUtils.isNotBlank(speaker.getPreflabel())?
        speaker.getPreflabel():speaker.getLabel());
    result.addProperty("time", utterance.getTime());
    result.addProperty("transcript", utterance.getTranscript());
    if(utterance.getRelationships() != null) {
      result.add("relationships",
          ctx.serialize(utterance.getRelationships(), Serializer.bracket("relationships")));
    }
    return result;
  }

}
