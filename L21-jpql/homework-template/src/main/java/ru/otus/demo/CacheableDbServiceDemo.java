package ru.otus.demo;

import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.RandomStringUtils;
import ru.otus.core.repository.DataTemplateHibernate;
import ru.otus.core.repository.HibernateUtils;
import ru.otus.core.sessionmanager.TransactionManagerHibernate;
import ru.otus.crm.dbmigrations.MigrationsExecutorFlyway;
import ru.otus.crm.model.Address;
import ru.otus.crm.model.Client;
import ru.otus.crm.model.Phone;
import ru.otus.crm.service.CacheableDbServiceClient;
import ru.otus.crm.service.DBServiceClient;
import ru.otus.crm.service.DbServiceClientImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CacheableDbServiceDemo {

    private static final Logger log = LoggerFactory.getLogger(DbServiceDemo.class);
    public static final String HIBERNATE_CFG_FILE = "hibernate.cfg.xml";

    public static void main(String[] args) {
        TransactionManagerHibernate transactionManager = initTransactionManager(HIBERNATE_CFG_FILE);
        var dbServiceClient = new DbServiceClientImpl(
                transactionManager, new DataTemplateHibernate<>(Client.class)
        );
        DBServiceClient cacheableDbServiceClient = CacheableDbServiceClient.decorate(dbServiceClient);

        var testClientsNum = 100;
        var timeWithoutCache = measureSaveAndGetClientsTime(testClientsNum, dbServiceClient);
        log.info(String.format("Test running time without cache: %s s", (double) timeWithoutCache / Math.pow(10, 9)));
        var timeWithCache = measureSaveAndGetClientsTime(testClientsNum, cacheableDbServiceClient);
        log.info(String.format("Test running time with cache: %s s", (double) timeWithCache / Math.pow(10, 9)));
    }

    private static long measureSaveAndGetClientsTime(int n, DBServiceClient dbServiceClient) {
        var start = System.nanoTime();
        var savedClients = saveRandomClients(n, dbServiceClient);
        getClients(savedClients, dbServiceClient);
        return System.nanoTime() - start;
    }

    private static List<Long> saveRandomClients(int number, DBServiceClient dbServiceClient) {
        List<Long> insertedIds = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            var client = dbServiceClient.saveClient(new Client(RandomStringUtils.randomAlphabetic(10)));
            insertedIds.add(client.getId());
        }
        return insertedIds;
    }

    private static List<Optional<Client>> getClients(List<Long> clientIds, DBServiceClient dbServiceClient) {
        List<Optional<Client>> res = clientIds.stream().map(id -> {
            Optional<Client> client = dbServiceClient.getClient(id);
            if (client.isEmpty()) {
                log.warn(String.format("Client with id %s not found", id));
            }
            return client;
        }).collect(Collectors.toList());
        return res;
    }

    private static TransactionManagerHibernate initTransactionManager(String hiberCfgFile) {
        var configuration = new Configuration().configure(hiberCfgFile);
        var dbUrl = configuration.getProperty("hibernate.connection.url");
        var dbUserName = configuration.getProperty("hibernate.connection.username");
        var dbPassword = configuration.getProperty("hibernate.connection.password");
        new MigrationsExecutorFlyway(dbUrl, dbUserName, dbPassword).executeMigrations();
        var sessionFactory = HibernateUtils.buildSessionFactory(
                configuration, Client.class, Address.class, Phone.class
        );
        return new TransactionManagerHibernate(sessionFactory);
    }
}
