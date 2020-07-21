package net.runeduniverse.libs.rogm.parser.json;

import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;

import lombok.RequiredArgsConstructor;
import net.runeduniverse.libs.rogm.Configuration;
import net.runeduniverse.libs.rogm.parser.Parser;

@SuppressWarnings("deprecation")
public class JSONParser implements Parser {
	private static final ObjectMapper MAPPER = new ObjectMapper();

	static {
		MAPPER.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false);
		MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	@Override
	public Instance build(Configuration cnf) {
		JsonAnnotationIntrospector introspector = new JsonAnnotationIntrospector(cnf.getDbType().getModule());

		AnnotationIntrospector serial = new AnnotationIntrospectorPair(introspector,
				MAPPER.getSerializationConfig().getAnnotationIntrospector());
		AnnotationIntrospector deserial = new AnnotationIntrospectorPair(introspector,
				MAPPER.getDeserializationConfig().getAnnotationIntrospector());
		return new JSONParserInstance(MAPPER.copy().setAnnotationIntrospectors(serial, deserial));
	}
	
	@RequiredArgsConstructor
	private class JSONParserInstance implements Instance{
		private final ObjectMapper mapper;
		
		@Override
		public String serialize(Object object) throws JsonProcessingException {
			return mapper.writeValueAsString(object);
		}

		@Override
		public String serialize(Map<String, Object> map) throws Exception {
			return mapper.writeValueAsString(map);
		}

		@Override
		public <T> T deserialize(Class<T> clazz, String value)
				throws JsonMappingException, JsonProcessingException, InstantiationException, IllegalAccessException {
			if (value == null)
				return clazz.newInstance();
			return mapper.readValue(value, clazz);
		}
	}
}