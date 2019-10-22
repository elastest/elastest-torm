package io.elastest.etm.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.slf4j.Logger;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class DatabaseSessionManager {
    final Logger logger = getLogger(lookup().lookupClass());

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    public void bindSession() {
        if (!TransactionSynchronizationManager
                .hasResource(entityManagerFactory)) {
            entityManager = entityManagerFactory.createEntityManager();
            TransactionSynchronizationManager.bindResource(entityManagerFactory,
                    new EntityManagerHolder(entityManager));
        }
    }

    public void unbindSession() {
        EntityManagerHolder emHolder = (EntityManagerHolder) TransactionSynchronizationManager
                .unbindResource(entityManagerFactory);
        EntityManagerFactoryUtils
                .closeEntityManager(emHolder.getEntityManager());
    }
}