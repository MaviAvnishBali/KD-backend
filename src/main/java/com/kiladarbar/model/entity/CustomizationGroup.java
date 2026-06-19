package com.kiladarbar.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "customization_groups")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomizationGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private MenuItem menuItem;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 10)
    private String type = "SINGLE";

    @Column(name = "is_required", nullable = false)
    private boolean required = false;

    @Column(name = "min_select")
    private short minSelect = 0;

    @Column(name = "max_select")
    private short maxSelect = 1;

    @Column(name = "display_order")
    private short displayOrder = 0;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<CustomizationOption> options;
}
