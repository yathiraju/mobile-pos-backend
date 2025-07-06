package com.pos.controller;

import com.pos.model.Order;
import com.pos.repository.OrderRepository;
import com.razorpay.Order as RazorpayOrder;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    private RazorpayClient razorpayClient;

    public OrderController() throws Exception {
        this.razorpayClient = new RazorpayClient("YOUR_RAZORPAY_KEY_ID", "YOUR_RAZORPAY_SECRET");
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody Order order) {
        try {
            JSONObject options = new JSONObject();
            options.put("amount", (int)(order.getTotalAmount() * 100)); // in paise
            options.put("currency", "INR");
            options.put("receipt", "txn_" + System.currentTimeMillis());

            RazorpayOrder razorpayOrder = razorpayClient.orders.create(options);

            order.setOrderTime(LocalDateTime.now());
            order.setPaymentId(razorpayOrder.get("id"));
            order.setPaymentStatus("created");
            orderRepository.save(order);

            return ResponseEntity.ok(razorpayOrder.toString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Payment creation failed: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return ResponseEntity.of(orderRepository.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOrder(@PathVariable Long id) {
        orderRepository.deleteById(id);
        return ResponseEntity.ok("Order deleted");
    }
}
