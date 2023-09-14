package ru.otus.crm.service;

import ru.otus.cache.MyCache;
import ru.otus.crm.model.Client;

import java.util.List;
import java.util.Optional;

public class CacheableDbServiceClient implements DBServiceClient {

    DBServiceClient dbServiceClient;
    MyCache<Long, Client> cache = new MyCache<>();

    public CacheableDbServiceClient(DBServiceClient dbServiceClient) {
        this.dbServiceClient = dbServiceClient;
    }

    public static DBServiceClient decorate(DBServiceClient dbServiceClient) {
        return new CacheableDbServiceClient(dbServiceClient);
    }

    @Override
    public Client saveClient(Client client) {
        var savedClient = dbServiceClient.saveClient(client);
        cache.put(savedClient.getId(), savedClient);
        return savedClient;
    }

    @Override
    public Optional<Client> getClient(long id) {
        var cacheRes = cache.get(id);
        if (cacheRes == null) {
            var client = dbServiceClient.getClient(id);
            client.ifPresent(x -> cache.put(id, x));
            return client;
        } else {
            return Optional.of(cacheRes);
        }
    }

    @Override
    public List<Client> findAll() {
        return dbServiceClient.findAll();
    }
}
