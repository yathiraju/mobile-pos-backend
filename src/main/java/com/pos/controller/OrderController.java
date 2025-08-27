package com.pos.controller;

import com.pos.model.Orders;
import com.pos.model.OrderItem;
import com.pos.model.Product;
import com.pos.repository.OrderRepository;
import com.pos.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private ProductRepository productRepo;

    @PostMapping
    public ResponseEntity<Orders> createOrder(@RequestBody Orders orders) {
        // calculate totals and set associations
        double total = 0;
        for (OrderItem it : orders.getItems()) {
            if (it.getProduct() != null && it.getProduct().getId() != null) {
                Optional<Product> p = productRepo.findById(it.getProduct().getId());
                if (p.isPresent()) {
                    Product prod = p.get();
                    it.setPrice(prod.getPrice());
                    // decrease stock (simple approach)
                    if (prod.getStockQuantity() != null) {
                        prod.setStockQuantity(prod.getStockQuantity() - it.getQuantity());
                        productRepo.save(prod);
                    }
                }
            }
            total += it.getPrice() * it.getQuantity();
            it.setOrder(orders);
        }
        orders.setTotalAmount(total);
        orders.setStatus("PENDING");
        Orders saved = orderRepo.save(orders);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Orders> get(@PathVariable Long id) {
        return orderRepo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<Orders> markPaid(@PathVariable Long id) {
        return orderRepo.findById(id).map(o -> {
            o.setStatus("PAID");
            orderRepo.save(o);
            return ResponseEntity.ok(o);
        }).orElse(ResponseEntity.notFound().build());
    }
}
