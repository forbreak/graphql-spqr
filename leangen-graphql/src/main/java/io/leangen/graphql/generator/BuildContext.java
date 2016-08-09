package io.leangen.graphql.generator;

import java.util.HashSet;
import java.util.Set;

import graphql.relay.Relay;
import io.leangen.graphql.generator.proxy.ProxyFactory;
import io.leangen.graphql.generator.strategy.AbstractTypeGenerationStrategy;
import io.leangen.graphql.generator.strategy.CappedTypeGenerationStrategy;
import io.leangen.graphql.generator.strategy.CircularTypeGenerationStrategy;
import io.leangen.graphql.generator.strategy.FlatTypeGenerationStrategy;
import io.leangen.graphql.metadata.strategy.input.GsonInputDeserializer;
import io.leangen.graphql.query.DefaultIdTypeMapper;
import io.leangen.graphql.query.ExecutionContext;
import io.leangen.graphql.query.IdTypeMapper;

/**
 * Created by bojan.tomic on 3/30/16.
 */
public class BuildContext {

	public final AbstractTypeGenerationStrategy typeStrategy;
	public final ExecutionContext executionContext;
	public final QueryRepository queryRepository;
	public final TypeRepository typeRepository;
	public final ProxyFactory proxyFactory;
	public final IdTypeMapper idTypeMapper;
	public final Relay relay;

	public final Set<String> inputsInProgress = new HashSet<>();

	public enum TypeGenerationMode {
		FLAT, CAPPED, CIRCULAR
	}

	public BuildContext(TypeGenerationMode mode, QueryRepository queryRepository) {
		switch (mode) {
			case CAPPED:    this.typeStrategy = new CappedTypeGenerationStrategy(queryRepository); break;
			case CIRCULAR:  this.typeStrategy = new CircularTypeGenerationStrategy(queryRepository); break;
			default:        this.typeStrategy = new FlatTypeGenerationStrategy(queryRepository);
		}
		this.queryRepository = queryRepository;
		this.typeRepository = new TypeRepository();
		this.idTypeMapper = new DefaultIdTypeMapper();
		this.proxyFactory = new ProxyFactory();
		this.relay = new Relay();
		this.executionContext = new ExecutionContext(relay, typeRepository, proxyFactory, idTypeMapper, new GsonInputDeserializer());
	}
}
