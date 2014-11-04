package pl.lando.asyncloop;

//import javax.security.auth.Subject;
//import java.sql.SQLException;
//import java.util.*;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.stream.Collectors;
//
//import com.google.common.collect.ImmutableSet;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Sets;
//import org.hibernate.Criteria;
//import org.hibernate.criterion.Order;
//import org.hibernate.criterion.Restrictions;
//import org.hibernate.exception.GenericJDBCException;
//import pl.compan.docusafe.AdditionManager;
//import pl.compan.docusafe.boot.Docusafe;
//import pl.compan.docusafe.core.DSApi;
//import pl.compan.docusafe.core.EdmException;
//import pl.compan.docusafe.core.office.workflow.ElasticTaskListModel;
//import pl.compan.docusafe.core.office.workflow.JBPMTaskSnapshot;
//import pl.compan.docusafe.util.Logger;
//import pl.compan.docusafe.util.LoggerFactory;
//
//import javax.security.auth.Subject;
//import java.sql.SQLException;
//import java.util.*;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.stream.Collectors;
//
///**
// * <p>
// *     Tworzy zrzut wpis�w z JBPMTaskSnapshot do ElasticSearch.
// * </p>
// *
// * @author Jan �wi�cki <a href="jan.swiecki@docusafe.pl">jan.swiecki@docusafe.pl</a>
// */
//public class ElasticSearchSynchronizer {
//
//    protected static final int BATCH_SIZE = 500;
//
//    /**
//     * Maksymalna ilo�� obiekt�w do synchronizacji w jednej iteracji.
//     */
////    protected static final int MAX_SYNC_SIZE_DEFAULT = 100;
//
//    /**
//     * <p>
//     *     log4j.properties:
//     *     <code>
//     *         log4j.logger.pl.compan.docusafe.core.elasticsearch.ElasticSearchSynchronizer=TRACE
//     *     </code>
//     * </p>
//     */
//    private static final Logger log = LoggerFactory.getLogger(ElasticSearchSynchronizer.class);
//
//    private static ElasticSearchSynchronizer instance = new ElasticSearchSynchronizer();
//
//    private static ImmutableSet<Integer> jbpmTaskSnapshotsInElastic = ImmutableSet.of();
//
//    private AtomicBoolean isLoopExecuting = new AtomicBoolean(false);
//    private AtomicBoolean isLoopBlocked = new AtomicBoolean(false);
//    private Thread thread;
//
//    public static ElasticSearchSynchronizer getInstance() {
//        return instance;
//    }
//
//    private ElasticSearchSynchronizer() {
//    }
//
//    /**
//     * <p>
//     *     Wype�nia jbpmTaskSnapshotsInElastic list� id z ElasticSearch
//     * </p>
//     */
//    public void initialize() {
//        log.debug("[initialize] begin");
//        ElasticTaskListModel model = new ElasticTaskListModel();
//        List<Integer> ids;
//        if (model.getTaskSnapshotCRUDOperations().isIndexExist()) {
//            ids = model.getAllIds(5000);
//        } else {
//            ids = new ArrayList<>();
//        }
//        jbpmTaskSnapshotsInElastic = ImmutableSet.copyOf(ids);
//        log.debug("[initialize] end, got {} ids", jbpmTaskSnapshotsInElastic.size());
//    }
//
//    public void asyncRun() {
//        if(jbpmTaskSnapshotsInElastic.size() == 0) {
//            initialize();
//        }
//
//        new Thread() {
//            public void run() {
//                log.debug("[asyncRun] start in thread {}", Thread.currentThread());
//
//                try {
//                    execute();
//                } catch (Exception e) {
//                    log.error("[asyncRun] error", e);
//                }
//
//                log.debug("[asyncRun] end in thread {}", Thread.currentThread());
//            }
//        }.start();
//    }
//
//    public void asyncLoopRestart() {
//        jbpmTaskSnapshotsInElastic = ImmutableSet.of();
//        asyncLoop();
//    }
//
//    public void forceExecute(Long... documentIds) throws Exception {
//        if(thread != null) {
//
//            try {
//                // block main loop
//                isLoopBlocked.set(true);
//
//                // wait for main loop to stop
//                thread.join();
//
//                try {
//                    // execute main logic in same thread
//                    execute(documentIds);
//                } finally {
//                    // restore main loop
//                    isLoopBlocked.set(false);
//
//                    // execute main loop
//                    asyncLoop();
//                }
//            } catch (InterruptedException e) {
//                log.error("[forceExecute] interrupted", e);
//            }
//
//        } else {
//            // just execute main loop
//            execute(documentIds);
//        }
//    }
//
//    public void asyncLoop() {
//        if(isLoopExecuting.get()) {
//            log.debug("[asyncLoop] already enabled");
//        } else {
//            isLoopExecuting.set(true);
//
//            if(jbpmTaskSnapshotsInElastic.size() == 0) {
//                initialize();
//            }
//
//            log.debug("[asyncLoop] starting loop");
//            thread = new Thread() {
//                public void run() {
//                    log.debug("[asyncLoop] start in thread {}", Thread.currentThread().getName());
//
//                    Integer interval = Integer.valueOf(Docusafe.getAdditionPropertyOrDefault("Elasticsearch.asyncLoopInterval", "30000"));
//                    log.debug("[asyncLoop] interval = {}", interval);
//
//                    if(interval < 0) {
//                        log.debug("[asyncLoop] stopping and restarting state");
//                        jbpmTaskSnapshotsInElastic = ImmutableSet.of();
//                        isLoopExecuting.set(false);
//                        return;
//                    }
//
//                    try {
//                        Thread.sleep(interval);
//                        log.debug("[asyncLoop] executing ...", interval);
//                        execute();
//                        isLoopExecuting.set(false);
//
//                        if(! isLoopBlocked.get()) {
//                            asyncLoop();
//                        } else {
//                            log.warn("[asyncLoop] loop is blocked, not executing next loop");
//                        }
//                    } catch(InterruptedException ex) {
//                        isLoopExecuting.set(false);
//                        log.error("[asyncLoop] interrupted", ex);
//                    } catch(Exception ex) {
//                        isLoopExecuting.set(false);
//                        log.error("[asyncLoop] error", ex);
//                    }
//
//                    log.debug("[asyncLoop] end in thread {}", Thread.currentThread().getName());
//                }
//            };
//
//            thread.start();
//        }
//    }
//
//    public void synchronizeAll(Subject subject) throws EdmException {
//        log.debug("[execute] begin, batch size = {}", BATCH_SIZE);
//
//        ElasticTaskListModel elasticModel = new ElasticTaskListModel();
//
//        List<JBPMTaskSnapshot> list;
//        int i = 0;
//
//        while( (list = getBatchList(i, i+BATCH_SIZE, 1, subject)).size() > 0 ) {
//            log.debug("[execute]     processing batch {} ({})", i / BATCH_SIZE, i);
//
//            list.forEach((task) -> {
//                //log.debug("[execute]         processing task.id = {}", task.getId());
//                try {
//                    elasticModel.save(task, task.getId().toString());
//                } catch (EdmException e) {
//                    log.error("[execute]         error while processing task.id", task.getId(), e);
//                }
//            });
//
//            i += BATCH_SIZE;
//        }
//        log.debug("[execute] end");
//    }
//
//    /**
//     * Synchronizuje Elasticsearcha z DB
//     * @param additionalDocumentIdsArray dodatkowe priorytetowe id dokumentow do zapisani (pobiera wpisy z jbpmtasksnapshot)
//     * @throws EdmException
//     * @throws java.sql.SQLException
//     */
//    public void execute(Long... additionalDocumentIdsArray) throws Exception {
//        log.debug("[execute] executing in thread {}", Thread.currentThread().getName());
//
//        Integer maxSyncSize = AdditionManager.getIntProperty("Elasticsearch.asyncLoopIterationLimit");
//
//        log.debug("[execute] maxSyncSize = {}", maxSyncSize);
//
//        List<Long> additionalDocumentIds = Arrays.asList(additionalDocumentIdsArray);
//
//        // adding to ES
//        Set<Integer> toAddSet = getAdded();
//
//        List<Integer> toAddList = new ArrayList<Integer>(toAddSet);
//        if(toAddSet.size() > maxSyncSize) {
//            log.warn("[execute] toAddSet is too big, resizing to {} elements", maxSyncSize);
//            // take only first maxSyncSize
//            toAddList.subList(maxSyncSize, toAddList.size()).clear();
//            toAddSet = new HashSet<>(toAddList);
//        }
//
//        if(additionalDocumentIds.size() > 0) {
//            Set<Integer> toAddSetAdditional = getSnapshotIds(additionalDocumentIds);
//            log.debug("[execute] toAddSetAdditional -> {}", toAddSetAdditional);
//            toAddSet = Sets.union(toAddSet, toAddSetAdditional);
//            toAddList = new ArrayList<Integer>(toAddSet);
//        }
//
//        log.debug("[execute] adding to ES -> {}", toAddList);
//        addToElastic(idToJbpmTaskSnapshotBatch(toAddList));
//
//        // removing from ES
//        Set<Integer> toDeleteSet = getDeleted();
//        List<Integer> toDeleteList = new ArrayList<Integer>(toDeleteSet);
//        if(toDeleteSet.size() > maxSyncSize) {
//            log.warn("[execute] toDeleteSet is too big, resizing to {} elements", maxSyncSize);
//            // take only first maxSyncSize
//            toDeleteList.subList(maxSyncSize, toDeleteList.size()).clear();
//        }
//
//        log.debug("[execute] removing from ES -> {}", toDeleteList);
//        deleteFromElastic(toDeleteList);
//
//        // updating current state (stored in jbpmTaskSnapshotsInElastic)
//        Set<Integer> set =  new HashSet<>(jbpmTaskSnapshotsInElastic);
//        set.addAll(toAddList);
//        set.removeAll(toDeleteList);
//        jbpmTaskSnapshotsInElastic = ImmutableSet.copyOf(set);
//
//        // debug
//        log.debug("[execute] jbpmTaskSnapshotsInElastic.size = {}", jbpmTaskSnapshotsInElastic.size());
//    }
//
//    /**
//     * Zwraca list� wszystkich id z DB
//     * @return
//     * @throws EdmException
//     * @throws java.sql.SQLException
//     */
//    public ImmutableSet<Integer> getIdListFromJBPMTaskSnapshot() throws EdmException, SQLException {
//
//
//        try {
//            return DSApi.inAdminContextIfNotOpened(()->{
//                final List<Integer> list = new ArrayList<Integer>();
//                DSApi.inPrepareStatementForEach("select id from dsw_jbpm_tasklist order by id desc", (rs)->{
//                    list.add(rs.getInt("id"));
//                });
//                return ImmutableSet.copyOf(list);
//            });
//        } catch (Exception e) {
//            throw new EdmException("B��d", e);
//        }
//    }
//
//    /**
//     * Zwraca usuni�te id z DB wzgl�dem Elasticsearcha
//     * @return
//     * @throws EdmException
//     * @throws SQLException
//     */
//    public Set<Integer> getDeleted() throws EdmException, SQLException {
//        return Sets.difference(jbpmTaskSnapshotsInElastic, getIdListFromJBPMTaskSnapshot());
//    }
//
//    /**
//     * Zwraca dodane id z DB wzgl�dem Elasticsearcha
//     * @return
//     * @throws EdmException
//     * @throws SQLException
//     */
//    public Set<Integer> getAdded() throws EdmException, SQLException {
//        return Sets.difference(getIdListFromJBPMTaskSnapshot(), jbpmTaskSnapshotsInElastic);
//    }
//
//
////    public Set<Integer> getAdded(Collection<Long> documentIds) throws Exception {
////        Set<Integer> firstSet = getAdded();
////
////    }
//
//    private Set<Integer> getSnapshotIds(Collection<Long> documentIds) throws Exception {
//        if(documentIds.isEmpty()) {
//            return new HashSet<Integer>();
//        }
//
//        return DSApi.inAdminContextIfNotOpened(()->{
//            // ignoring nulls
//            List<Long> notNullDocIds = documentIds.stream().filter((x) -> x != null).collect(Collectors.toList());
//
//            Criteria criteria = DSApi.context().session().createCriteria(JBPMTaskSnapshot.class);
//            criteria.add(Restrictions.in("documentId", notNullDocIds));
//            return ((List<JBPMTaskSnapshot>)criteria.list()).stream().map((s) -> s.getDocumentId().intValue()).collect(Collectors.toSet());
//        });
//    }
//
//    /**
//     * Usuwa z Elasticsearch dokumenty o podanym id
//     * @param list
//     */
//    public void deleteFromElastic(List<Integer> list){
//        list.forEach(i->ElasticTaskListModel.instance().delete(i.longValue()));
//    }
//
//    /**
//     * Dodaje do Elasticsearcha podane JBPMTaskSnapshot
//     * @param list
//     */
//    public void addToElastic(List<JBPMTaskSnapshot> list){
////        list.forEach(elem -> {
////            try {
////                ElasticTaskListModel.instance().save(elem, elem.getId().toString());
////            } catch (EdmException e) {
////                log.error("[deleteFromElastic] error", e);
////            }
////        });
//        try {
//            ElasticTaskListModel.instance().save(list);
//        } catch (Exception e) {
//            log.error("[deleteFromElastic] error", e);
//        }
//    }
//
//    protected List<JBPMTaskSnapshot> idToJbpmTaskSnapshotBatch(final List<Integer> list) throws EdmException {
//        if(list.isEmpty()) {
//            return new ArrayList<JBPMTaskSnapshot>();
//        }
//
//        try {
//            return DSApi.inAdminContextIfNotOpened(()->{
//                List<JBPMTaskSnapshot> acc;
//                List<JBPMTaskSnapshot> ret = new ArrayList<JBPMTaskSnapshot>();
//
//                List<List<Integer>> parts = Lists.partition(list, BATCH_SIZE);
//
//                for(List<Integer> part: parts) {
//                    acc = idToJbpmTaskSnapshot(part);
//                    ret.addAll(acc);
//                }
//                return ret;
//            });
//        } catch (Exception e) {
//            throw new EdmException("B��d", e);
//        }
//    }
//
//    /**
//     * Pobiera JbpmTaskSnapshotu dla podanych id
//     */
//    protected List<JBPMTaskSnapshot> idToJbpmTaskSnapshot(List<Integer> list) {
//        if(list.isEmpty()) {
//            return new ArrayList<JBPMTaskSnapshot>();
//        }
//
//        try {
//            Criteria criteria = DSApi.context().session().createCriteria(JBPMTaskSnapshot.class);
//            criteria.add(org.hibernate.criterion.Restrictions.in("id", list.stream().map(i -> new Long(i)).toArray()));
//            criteria.addOrder(Order.desc("id"));
//            return criteria.list();
//        } catch (EdmException e) {
//            log.error("[idToJbpmTaskSnapshot] error", e);
//        }
//        return new ArrayList<JBPMTaskSnapshot>();
//    };
//
//    /**
//     * <p>
//     *     Pobieramy po <code>BATCH_SIZE</code> wynik�w, �eby nie zablokowa� tabeli.
//     * </p>
//     * @param start
//     * @param size
//     * @param subject
//     * @return
//     * @throws EdmException
//     */
//    protected List<JBPMTaskSnapshot> getBatchList(int start, int size, int triesLeft, Subject subject) throws EdmException {
//        try {
//            Criteria criteria = DSApi.context().session().createCriteria(JBPMTaskSnapshot.class);
//            criteria.addOrder(Order.desc("id"));
//            criteria.setFirstResult(start);
//            criteria.setMaxResults(size);
//            return criteria.list();
//        } catch(GenericJDBCException ex) {
//            if(triesLeft > 0) {
//                log.warn("[getBatchList] error, will try again {} more times", triesLeft, ex);
//                DSApi.open(subject);
//                return getBatchList(start, size, triesLeft-1, subject);
//            } else {
//                throw new EdmException(ex);
//            }
//        }
//    }
//}