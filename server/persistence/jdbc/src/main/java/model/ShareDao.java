package model;

import jakarta.persistence.*;

import java.util.Optional;
@Entity
@Table(name = "share")
public class ShareDao {

    @Id
    private String id;
    private String name;

    private String comment;
    //todo make these fields
//    private Map<String, String> schema;
//    private Set<PrincipalDAO> recipients;
    private Long createdAt;

    private String createdBy;
    private Long updatedAt;

    private String updatedBy;

    private String owner;

    public ShareDao() {
    }

    public ShareDao(String id, String name, String comment, Long createdAt, String createdBy, Long updatedAt, String updatedBy, String owner) {
        this.id = id;
        this.name = name;
        this.comment = comment;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.owner = owner;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Optional<String> getComment(){
        return Optional.ofNullable(this.comment);
    }
    public void setComment(String comment){
        this.comment = comment;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
