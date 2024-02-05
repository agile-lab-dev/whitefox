package mapper;

import io.whitefox.core.Principal;
import io.whitefox.core.Schema;
import io.whitefox.core.Share;
import jakarta.inject.Singleton;
import model.ShareDao;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Singleton
public class ShareMapper {

    public static Share shareDaoToShare(ShareDao shareDao){
        String id = shareDao.getId();
        String name = shareDao.getName();
        Optional<String> comment = shareDao.getComment();
        Map<String, Schema> schemas = Map.of();
        Set<Principal> recipients = Set.of();
        Long createdAt = shareDao.getCreatedAt();
        Principal principal = new Principal(shareDao.getCreatedBy());
        Long updatedAt = shareDao.getUpdatedAt();
        return new Share(name, id, schemas, comment, recipients, createdAt, principal, updatedAt, principal, principal   );
    }

    public static ShareDao shareToShareDao(Share share){
        String id = share.id();
        String name = share.name();
        String comment = share.comment().orElse("");
        Long createdAt = share.createdAt();
        String createdBy = share.createdBy().name();
        Long updatedAt = share.updatedAt();
        String updatedBy = share.updatedBy().name();
        String owner = share.owner().name();
        return new ShareDao(id, name, comment, createdAt, createdBy, updatedAt, updatedBy, owner);
    }
}
