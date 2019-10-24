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
            logger.debug("Add Entity Manager to thread: {}",
                    Thread.currentThread().getName());
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
        logger.debug("Unbinded EntityManager from thread: {}",
                Thread.currentThread().getName());
    }

    public void reloadEntityFromDb(Object entity)
            throws Exception, IllegalArgumentException {
        EntityManager entityManager;
        entityManager = ((EntityManagerHolder) TransactionSynchronizationManager
                .getResource(entityManagerFactory)).getEntityManager();
        try {
            if (entityManager.isOpen()) {
                entityManager.refresh(entity);
            } else {
                unbindSession();
                bindSession();
            }
        } catch (IllegalArgumentException iae) {
            logger.error(iae.getMessage());
            throw iae;
        } catch (Exception e) {
            logger.error("EntityManager can't be null");
            throw e;
        }
    }

}