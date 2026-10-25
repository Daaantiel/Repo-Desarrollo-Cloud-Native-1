package cl.duoc.ms_orders.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "payment_provider")
    private String paymentProvider;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItemEntity> items = new ArrayList<>();

    protected OrderEntity() {}

    public OrderEntity(String userId) {
        this.userId = userId;
        this.status = "PENDIENTE_PAGO";
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void addItem(OrderItemEntity item) {
        items.add(item);
        item.setOrder(this);
    }

    public void resolverPagoSimulado(boolean aprobado) {
        this.paymentProvider = "SIMULADO";
        this.paymentReference = "DEMO-" + UUID.randomUUID();
        if (aprobado) {
            this.status = "PAGADO";
            this.paidAt = OffsetDateTime.now(ZoneOffset.UTC);
        } else {
            this.status = "CANCELADO";
            this.paidAt = null;
        }
    }

    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public String getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getPaidAt() { return paidAt; }
    public String getPaymentReference() { return paymentReference; }
    public List<OrderItemEntity> getItems() { return items; }
}
