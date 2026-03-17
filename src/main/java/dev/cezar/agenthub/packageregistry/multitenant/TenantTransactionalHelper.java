package dev.cezar.agenthub.packageregistry.multitenant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantTransactionalHelper {

    private final DatabaseClient databaseClient;
    private final TransactionalOperator transactionalOperator;

    public <T> Mono<T> inTenantTransaction(Mono<T> source) {
        return TenantContextHolder.getContextFromReactor()
                .flatMap(ctx -> databaseClient
                        .sql("SET search_path TO " + ctx.getSchemaName())
                        .fetch()
                        .rowsUpdated()
                        .then(source))
                .as(transactionalOperator::transactional);
    }
}
