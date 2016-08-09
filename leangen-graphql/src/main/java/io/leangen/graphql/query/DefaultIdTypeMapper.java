package io.leangen.graphql.query;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by bojan.tomic on 4/13/16.
 */
public class DefaultIdTypeMapper implements IdTypeMapper {

	protected Serializer<Object> TO_BASE64_STRING;
	protected Serializer<Object> TO_STRING;
	protected Deserializer<Object> FROM_BASE64_STRING;
	private Map<Type, Serializer<?>> serializers = new HashMap<>();
	private Map<Type, Deserializer<?>> deserializers = new HashMap<>();

	public DefaultIdTypeMapper() {
		TO_STRING = Object::toString;
		TO_BASE64_STRING = value -> {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
				out.writeObject(value);
				return Base64.getEncoder().encodeToString(bytes.toByteArray());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		};
		FROM_BASE64_STRING = serializedValue -> {
			ByteArrayInputStream bytes = new ByteArrayInputStream(Base64.getDecoder().decode(serializedValue));
			try (ObjectInputStream out = new ObjectInputStream(bytes)) {
				return out.readObject();
			} catch (IOException | ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		};
		withSerializer(Date.class, date -> Long.toString(date.getTime()));
		withSerializer(String.class, val -> val);
		withSerializer(Long.class, TO_STRING);
		withSerializer(Integer.class, TO_STRING);
		withSerializer(UUID.class, TO_STRING);
		withSerializer(URI.class, TO_STRING);
		withDeserializer(Date.class, val -> new Date(Long.parseLong(val)));
		withDeserializer(String.class, val -> val);
		withDeserializer(Long.class, Long::parseLong);
		withDeserializer(Integer.class, Integer::parseInt);
		withDeserializer(UUID.class, UUID::fromString);
		withDeserializer(URI.class, URI::create);
	}

	protected <T> DefaultIdTypeMapper withSerializer(Class<T> clazz, Serializer<? super T> serializer) {
		serializers.put(clazz, serializer);
		return this;
	}

	protected <T> DefaultIdTypeMapper withDeserializer(Class<T> clazz, Deserializer<? super T> deserializer) {
		deserializers.put(clazz, deserializer);
		return this;
	}

	@Override
	public boolean supports(Type type) {
		return serializers.containsKey(type);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> String serialize(T id) {
		return ((Serializer<T>) serializers.getOrDefault(id.getClass(), TO_BASE64_STRING)).serialize(id);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T deserialize(String id, Type type) {
		return ((Deserializer<T>) deserializers.getOrDefault(type, FROM_BASE64_STRING)).deserialize(id);
	}

	@FunctionalInterface
	protected interface Serializer<T> {
		String serialize(T value);
	}

	@FunctionalInterface
	protected interface Deserializer<T> {
		T deserialize(String value);
	}
}
