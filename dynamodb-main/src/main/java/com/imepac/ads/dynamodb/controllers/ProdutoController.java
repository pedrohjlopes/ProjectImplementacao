package com.imepac.ads.dynamodb.controllers;

import com.imepac.ads.dynamodb.entities.Produto;
import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final DynamoDbTemplate dynamoDbTemplate;

    public ProdutoController(DynamoDbTemplate dynamoDbTemplate) {
        this.dynamoDbTemplate = dynamoDbTemplate;
    }

    @PostMapping
    public ResponseEntity<String> createProduto(@RequestBody Produto produto) {
        produto.setMarcaId(UUID.randomUUID().toString());
        dynamoDbTemplate.save(produto);
        return ResponseEntity.status(201).body("Produto created successfully with ID: " + produto.getMarcaId());
    }

    @GetMapping("/{produtoId}/{marcaId}")
    public ResponseEntity<List<Produto>> getProduto(@PathVariable String produtoId, @PathVariable String marcaId) {

        var key = Key.builder()
                .partitionValue(produtoId)
                .sortValue(marcaId)
                .build();

        var condition = QueryConditional.sortBeginsWith(key);

        var query = QueryEnhancedRequest.builder()
                .queryConditional(condition)
                .build();

        try {
            var produto = dynamoDbTemplate.query(query, Produto.class);

            List<Produto> produtos = produto.items().stream().toList();
            return ResponseEntity.ok(produtos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }
}
