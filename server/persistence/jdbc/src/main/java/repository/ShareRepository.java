package repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import model.ShareDao;

@ApplicationScoped
public class ShareRepository implements PanacheRepositoryBase<ShareDao, String> {

    @Override
    public void persist(ShareDao shareDao) {
        PanacheRepositoryBase.super.persist(shareDao);
    }
}
