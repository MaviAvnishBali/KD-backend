package com.kiladarbar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** A marketing/campaign push composed in the admin panel. */
@Data
public class CampaignNotificationRequest {

    @NotBlank @Size(max = 80)
    private String title;

    @NotBlank @Size(max = 240)
    private String body;

    /** promo | engagement — maps to the mobile handler & channel. */
    private String type = "promo";

    /** Optional channel id override (e.g. promo_flash). */
    private String channel;

    /** kiladarbar:// deep link opened on tap. */
    private String deepLink;

    /** Optional big-picture image URL. */
    private String image;

    /** Call-to-action button label. */
    private String cta;

    /** Route a promo through the high-priority flash channel. */
    private boolean flash = false;
}
