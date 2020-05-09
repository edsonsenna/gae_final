package com.edson.gae_final.repository;

import com.edson.gae_final.exception.UserNotFoundException;
import com.edson.gae_final.model.ProductInterest;
import com.edson.gae_final.model.User;
import com.google.appengine.api.datastore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;

@Repository
public class ProductInterestRepository {

    @Autowired
    private UserRepository userRepository;

    private static final Logger log = Logger.getLogger("ProductInterestRepository");

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

    public ProductInterest saveOrUpdateProductInterest(ProductInterest productInterest) throws UserNotFoundException{
        DatastoreService datastore = DatastoreServiceFactory
                .getDatastoreService();
        try {
            Optional<User> user = this.userRepository.getByCpf(productInterest.getCpf());
            Entity productInterestEntity = this.getProductInterestIfAlreadyExists(productInterest);
            if(productInterestEntity == null) {
                Key productInterestKey = KeyFactory.createKey(PRODUCT_INTEREST_KIND, PRODUCT_INTEREST_KEY);
                productInterestEntity = new Entity(PRODUCT_INTEREST_KIND, productInterestKey);
                productInterestToEntity(productInterest, productInterestEntity);
                datastore.put(productInterestEntity);
                return entityToProductInterest(productInterestEntity);
            } else {
                // TODO: Update
            }
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException("Usuário com CPF: "+productInterest.getCpf()+" não encontrado");
        }

        return null;
    }
}
