package com.edson.gae_final.repository;

import com.edson.gae_final.exception.UserNotFoundException;
import com.edson.gae_final.model.ProductInterest;
import com.edson.gae_final.model.User;
import com.google.appengine.api.datastore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Repository
public class ProductInterestRepository {

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = Logger.getLogger("ProductInterestRepository");

    private static final String PRODUCT_INTEREST_KIND = "ProductInterests";
    private static final String PRODUCT_INTEREST_KEY = "productInterestKey";
    private static final String PROPERTY_CPF = "cpf";
    private static final String PROPERTY_PRODUCT_SALES_ID = "productSalesId";
    private static final String PROPERTY_CRM_ID = "crmId";
    private static final String PROPERTY_PRICE = "price";

    private void productInterestToEntity(ProductInterest productInterest, Entity productInterestEntity) {
        productInterestEntity.setProperty(PROPERTY_CPF, productInterest.getCpf());
        productInterestEntity.setProperty(PROPERTY_PRODUCT_SALES_ID, productInterest.getProductSalesId());
        productInterestEntity.setProperty(PROPERTY_CRM_ID, productInterest.getCrmId());
        productInterestEntity.setProperty(PROPERTY_PRICE, productInterest.getPrice());
    }

    private ProductInterest entityToProductInterest(Entity productInterestEntity) {
        ProductInterest productInterest = new ProductInterest();
        productInterest.setCpf((String) productInterestEntity.getProperty(PROPERTY_CPF));
        productInterest.setProductSalesId((String) productInterestEntity.getProperty(PROPERTY_PRODUCT_SALES_ID));
        productInterest.setCrmId((String) productInterestEntity.getProperty(PROPERTY_CRM_ID));
        productInterest.setPrice((Double) productInterestEntity.getProperty(PROPERTY_PRICE));
        return productInterest;
    }

    private Entity getProductInterestIfAlreadyExists(ProductInterest productInterest) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query.Filter filter = new Query.CompositeFilter(Query.CompositeFilterOperator.AND, Arrays.<Query.Filter>asList(
                new Query.FilterPredicate(
                        PROPERTY_CPF,
                        Query.FilterOperator.EQUAL, productInterest.getCpf()
                ),
                new Query.FilterPredicate(
                        PROPERTY_PRODUCT_SALES_ID,
                        Query.FilterOperator.EQUAL, productInterest.getProductSalesId()
                )
        ));
        Query query = new Query(PRODUCT_INTEREST_KIND).setFilter(filter);
        Entity productInterestEntity = datastore.prepare(query).asSingleEntity();
        return productInterestEntity;
    }

    public ProductInterest saveOrUpdateProductInterest(ProductInterest productInterest) throws UserNotFoundException {
        DatastoreService datastore = DatastoreServiceFactory
                .getDatastoreService();
        try {
            Optional<User> optUser = this.userRepository.getByCpf(productInterest.getCpf());
            if(optUser.isPresent()) {
                User user = optUser.get();
                if (user.getCpf().equals(productInterest.getCpf())) {
                    Entity productInterestEntity = this.getProductInterestIfAlreadyExists(productInterest);
                    if (productInterestEntity == null) {
                        Key productInterestKey = KeyFactory.createKey(PRODUCT_INTEREST_KIND, PRODUCT_INTEREST_KEY);
                        productInterestEntity = new Entity(PRODUCT_INTEREST_KIND, productInterestKey);
                        productInterestToEntity(productInterest, productInterestEntity);
                    } else {
                        productInterestEntity.setProperty(PROPERTY_PRICE, productInterest.getPrice());
                    }
                    datastore.put(productInterestEntity);
                    return entityToProductInterest(productInterestEntity);
                }
                return null;
            }
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException("Usuário com CPF: " + productInterest.getCpf() + " não encontrado");
        }
        return null;
    }

    public List<ProductInterest> getUserProductsInterests(String cpf) {
        List<ProductInterest> productInterests = new ArrayList<>();
        DatastoreService datastore = DatastoreServiceFactory
                .getDatastoreService();
        Query query;
        Query.Filter filter = new Query.FilterPredicate(
                PROPERTY_CPF,
                Query.FilterOperator.EQUAL, cpf
        );
        query = new Query(PRODUCT_INTEREST_KIND).setFilter(filter);
        List<Entity> productInterestsEntities = datastore.prepare(query).asList(
                FetchOptions.Builder.withDefaults()
        );
        for (Entity productInterestsEntity : productInterestsEntities) {
            ProductInterest productInterest = entityToProductInterest(productInterestsEntity);
            productInterests.add(productInterest);
        }
        return productInterests;
    }

    public ProductInterest deleteUserProductInterest(String cpf, String productSalesId) {
        DatastoreService datastore = DatastoreServiceFactory
                .getDatastoreService();
        ProductInterest productInterest = new ProductInterest();
        productInterest.setCpf(cpf);
        productInterest.setProductSalesId(productSalesId);
        Entity productInterestEntity = this.getProductInterestIfAlreadyExists(productInterest);
        if (productInterestEntity == null) {
            return null;
        } else {
            datastore.delete(productInterestEntity.getKey());
            return entityToProductInterest(productInterestEntity);
        }
    }

    public List<String> findInterestedUsers(String productSalesId, Double price) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query.Filter filter = new Query.CompositeFilter(Query.CompositeFilterOperator.AND, Arrays.<Query.Filter>asList(
                new Query.FilterPredicate(
                        PROPERTY_PRODUCT_SALES_ID,
                        Query.FilterOperator.EQUAL, productSalesId
                ),
                new Query.FilterPredicate(
                        PROPERTY_PRICE,
                        Query.FilterOperator.GREATER_THAN_OR_EQUAL, price
                )
        ));
        Query query = new Query(PRODUCT_INTEREST_KIND).setFilter(filter);
        List<Entity> productInterestsEntities = datastore.prepare(query).asList(
                FetchOptions.Builder.withDefaults()
        );
        List<String> users = new ArrayList<>();
        for (Entity productInterestsEntity : productInterestsEntities) {
            ProductInterest productInterest = entityToProductInterest(productInterestsEntity);
            users.add(productInterest.getCpf());
        }
        List<String> fcms = this.userRepository.getFcmUsersByCpf(users);
        return fcms;
    }
}
