package com.pxs.dependencies.aggregator;

import static com.pxs.dependencies.constants.Constants.GRAPH_CACHE_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.pxs.dependencies.model.Node;
import com.pxs.utilities.converters.json.ObjectToJsonConverter;

@Component
public class HealthIndicatorsAggregator extends AbstractAggregator<Node> {

	private static final Logger LOG = LoggerFactory.getLogger(HealthIndicatorsAggregator.class);

	private static final long TIMEOUT = 17000L;

	@Cacheable(value = GRAPH_CACHE_NAME, keyGenerator = "simpleKeyGenerator")
	public String fetchCombinedDependencies() {
		List<Node> taskResponses = buildAggregatedDependenciesListFromTaskResponses(getFutureTasks());
		return serializeResponse(taskResponses);
	}

	private List<Node> buildAggregatedDependenciesListFromTaskResponses(final List<FutureTask<Node>> tasks) {
		List<Node> nodes = new ArrayList<>();
		for (FutureTask<Node> task : tasks) {
			String key = null;
			try {
				key = ((IdentifiableFutureTask) task).getId();
				Node value = task.get(TIMEOUT, TimeUnit.MILLISECONDS);
				LOG.debug("task {} is done {}", key, task.isDone());
				value.setId(key);
				nodes.add(value);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				LOG.debug("Problem getting results for task: {} caused by: {}", key, e.toString());
			}
		}
		LOG.debug("Finished fetching combined dependencies");
		return nodes;
	}

	@Override
	protected Callable<Node> instantiateAggregatorTask(final HttpServletRequest originRequest, final String serviceId, final String serviceHost, final int servicePort) {
		return new SingleServiceHealthCollectorTask(serviceId, servicePort, serviceHost, originRequest);
	}

	private String serializeResponse(List<Node> nodes) {
		ObjectToJsonConverter<List<Node>> serializer = new ObjectToJsonConverter<>();
		return serializer.convert(nodes);
	}
}
