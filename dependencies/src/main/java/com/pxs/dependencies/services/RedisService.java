package com.pxs.dependencies.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.pxs.dependencies.constants.Constants;
import com.pxs.dependencies.model.Node;
import com.pxs.utilities.converters.json.JsonToObjectConverter;
import com.pxs.utilities.converters.json.ObjectToJsonConverter;

@Service
public class RedisService {

	public static final String REDIS_KEY_PREFIX = "virtual:";
	public static final String _VIRTUAL_FLAG = "virtual";

	private RedisTemplate<String, String> redisTemplate;

	private RedisConnectionFactory redisConnectionFactory;

	@Autowired
	public RedisService(final RedisTemplate<String, String> redisTemplate,
			final RedisConnectionFactory redisConnectionFactory) {
		this.redisTemplate = redisTemplate;
		((JedisConnectionFactory) redisConnectionFactory).setTimeout(10000);
		this.redisConnectionFactory = redisConnectionFactory;
	}

	public List<Node> getAllNodes() {
		List<Node> results = new ArrayList<>();
		Set<String> keys = redisTemplate.keys(REDIS_KEY_PREFIX + "*");
		for (String key : keys) {
			String nodeString = redisTemplate.opsForValue().get(key);
			Node node = getNode(nodeString);
			results.add(node);
		}
		return results;
	}

	public void saveNode(final String nodeData) {
		String nodeId = getNodeId(nodeData);
		Node node = getNode(nodeData);
		node.getDetails().put(_VIRTUAL_FLAG, true);
		ObjectToJsonConverter<Node> converter = new ObjectToJsonConverter<>();
		redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + nodeId, converter.convert(node));
	}

	public void deleteNode(final String nodeId) {
		redisTemplate.delete(nodeId);
	}

	public void deleteAllNodes() {
		redisTemplate.delete(redisTemplate.keys("*"));
	}

	private String getNodeId(String nodeData) {
		Node node = getNode(nodeData);
		return node.getId();
	}

	private Node getNode(String nodeData) {
		JsonToObjectConverter<Node> converter = new JsonToObjectConverter<>(Node.class);
		return converter.convert(nodeData);
	}

	public void flushDB() {
		redisConnectionFactory.getConnection().flushDb();
	}

	@CacheEvict(value = Constants.GRAPH_CACHE_NAME, allEntries = true)
	public void evictCache() {
	}
}