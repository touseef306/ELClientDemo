package org.springbootcourse.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import org.springbootcourse.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Service
public class ProductServiceImpl implements ProductService{

    @Autowired
    ElasticsearchClient client;

    @Override
    public Product createProduct(Product product) {
        try {
            IndexResponse response = client.index(i -> i
                    .index("prducts-002")
                    .id(product.getId())
                    .document(product));
            return product;
        }catch (IOException exception){
            exception.printStackTrace();
            return null;
        }
    }

    @Override
    public Iterable<Product> getAllProducts() throws IOException {

        SearchRequest request = new SearchRequest.Builder()
                .index("prducts-002")
                .build();
        SearchResponse<Product> response = client.search(request, Product.class);

        List<Hit<Product>> hits = response.hits().hits();

        List<Product> products = new ArrayList<>();
        for(Hit<Product> hit:hits){
            Product product = hit.source();
            products.add(product);
        }

        return products;
    }

    @Override
    public Product getProductById(String id) throws IOException {
        GetRequest request = new GetRequest.Builder()
                .index("prducts-002")
                .id(id)
                .build();

        GetResponse<Product> response = client.get(request, Product.class);
        if (response.found()){
           Product product = (Product)response.source();
           return product;
        }else {
            return null;
        }
    }

    @Override
    public Product updateProduct(String id, Product product) throws IOException {
        UpdateRequest updateRequest = new UpdateRequest.Builder()
                .index("prducts-002")
                .id(id)
                .doc(product)
                .build();

        UpdateResponse updateResponse = client.update(updateRequest, Product.class);

        if (updateResponse.result() == Result.Updated || updateResponse.result() == Result.Created){
            return product;
        }else {
            return null;
        }

    }

    @Override
    public boolean deleteProduct(String id) throws IOException {
        DeleteRequest request = new DeleteRequest.Builder()
                .index("prducts-002")
                .id(id)
                .build();
        DeleteResponse response = client.delete(request);

        if (response.result() == Result.Deleted){
            return true;
        }else {
            return false;
        }

    }

    @Override
    public List<Product> getProductByCategory(String category) throws IOException {
        SearchRequest request = new SearchRequest.Builder()
                .index("prducts-002")
                .query(q -> q
                        .match(t -> t
                                .field("category")
                                .query(category)))
                .build();

        SearchResponse<Product> response = client.search(request, Product.class);

        List<Hit<Product>> hits = response.hits().hits();

        List<Product> products = new ArrayList<>();

        for (Hit<Product> hit: hits){
            Product product = hit.source();
            products.add(product);
        }

        return products;
    }

    @Override
    public List<Product> searchByPriceRange(double minPrice, double maxPrice) throws IOException {
        SearchRequest request = new SearchRequest.Builder()
                .index("prducts-002")
                .query(q -> q
                        .range(r -> r
                                .field("price")
                                .gte(JsonData.of(minPrice))
                                .lte(JsonData.of(maxPrice))))
                .build();

        SearchResponse<Product> response = client.search(request, Product.class);

        List<Hit<Product>> hits = response.hits().hits();

        List<Product> products =new ArrayList<>();

        for (Hit<Product> hit: hits){
            Product product = hit.source();
            products.add(product);
        }

        return products;
    }
}
