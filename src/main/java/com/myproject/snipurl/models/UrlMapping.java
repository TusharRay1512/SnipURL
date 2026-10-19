package com.myproject.snipurl.models;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actual_url", columnDefinition = "TEXT")
    private String actualUrl;

    @Column(name = "snip_url", length = 255)
    private String snipUrl;

    private int clickCount = 0;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private SnipUser user;

    @OneToMany
    @JoinColumn(name = "url_mapping")
    private List<ClickEvent> clickEvents;

    // Soft delete flag
    @Column(nullable = false)
    private boolean deleted = false;

}
