import io.whitefox.core.*;
import io.whitefox.persistence.StorageManager;
import io.whitefox.persistence.exceptions.InvalidPageTokenException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import mapper.ShareMapper;
import repository.ShareRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
@ApplicationScoped
public class JdbcStorageManager implements StorageManager {

    @Inject
    ShareRepository shareRepository;

    @Inject
    public JdbcStorageManager(){
        this.shareRepository = new ShareRepository();
    }

    @Override
    public Optional<Share> getShare(String share) {
        return Optional.empty();
    }

    @Override
    public ResultAndTotalSize<List<Share>> getShares(int offset, int maxResultSize) {

        List<Share> shares = shareRepository.listAll().stream()
                .map(ShareMapper::shareDaoToShare)
                .collect(Collectors.toList());
        var totalSize = shares.size();
        if (offset > totalSize) {
            throw new InvalidPageTokenException(
                    String.format("Invalid Next Page Token: token %s is larger than totalSize", offset));
        } else {
            return new ResultAndTotalSize<>(
                    shares.stream().skip(offset).limit(maxResultSize).collect(Collectors.toList()),
                    totalSize);
        }
    }

    @Override
    public Optional<SharedTable> getSharedTable(String share, String schema, String table) {
        return Optional.empty();
    }

    @Override
    public Optional<ResultAndTotalSize<List<Schema>>> listSchemas(String share, int offset, int maxResultSize) {
        return Optional.empty();
    }

    @Override
    public Optional<ResultAndTotalSize<List<SharedTable>>> listTables(String share, String schema, int offset, int maxResultSize) {
        return Optional.empty();
    }

    @Override
    public Optional<ResultAndTotalSize<List<SharedTable>>> listTablesOfShare(String share, int offset, int finalMaxResults) {
        return Optional.empty();
    }

    @Override
    public Metastore createMetastore(Metastore metastore) {
        return null;
    }

    @Override
    public Optional<Metastore> getMetastore(String name) {
        return Optional.empty();
    }

    @Override
    public Storage createStorage(Storage storage) {
        return null;
    }

    @Override
    public Optional<Storage> getStorage(String name) {
        return Optional.empty();
    }

    @Override
    public Provider createProvider(Provider provider) {
        return null;
    }

    @Override
    public Optional<Provider> getProvider(String name) {
        return Optional.empty();
    }

    @Override
    public InternalTable createInternalTable(InternalTable internalTable) {
        return null;
    }

    @Override
    public Share createShare(Share share) {
        shareRepository.persist(ShareMapper.shareToShareDao(share));
        return share;
    }

    @Override
    public Share updateShare(Share newShare) {
        return null;
    }

    @Override
    public Share addTableToSchema(Share shareObj, Schema schemaObj, Provider providerObj, InternalTable table, SharedTableName sharedTableName, Principal currentUser, long millis) {
        return null;
    }
}
