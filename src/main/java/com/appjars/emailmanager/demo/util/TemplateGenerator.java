/*-
 * #%L
 * Email Manager AppJars - Demo
 * %%
 * Copyright (C) 2023 - 2026 Flowing Code
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.appjars.emailmanager.demo.util;

import com.appjars.emailmanager.model.TemplateDto;
import com.appjars.emailmanager.model.TemplatePlaceholderDto;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the sample templates that populate the demo on first run.
 *
 * <p>As with {@link EmailGenerator}, the set is curated rather than repetitive: between them these
 * templates cover everything the template editor does — an HTML body and a plain-text one, required
 * and optional placeholders, a placeholder whose value is inserted as markup, a language variant,
 * and an inactive template that stays out of the compose picker.
 */
public class TemplateGenerator {

  private TemplateGenerator() {}

  /** Returns a curated list of sample templates covering every feature of the editor. */
  public static List<TemplateDto> sampleTemplates() {
    return List.of(
        shippingNotice(),
        shippingNoticeSpanish(),
        passwordReset(),
        monthlyReport(),
        retiredAnnouncement());
  }

  /** The showcase template: HTML, a table layout, and every kind of placeholder. */
  private static TemplateDto shippingNotice() {
    TemplateDto template =
        TemplateDto.builder()
            .name("Shipping notice")
            .description("Sent to the customer when an order leaves the warehouse")
            .locale("")
            .subject("Your order {{orderNumber}} is on its way")
            .html(true)
            .active(true)
            .body(
                """
                <p>Dear {{customerName}},</p>
                <p>Good news — your order <b>{{orderNumber}}</b> shipped on {{shippedDate}} and is \
                on its way to you.</p>
                <table width="100%" cellpadding="8" style="border-collapse:collapse;margin:16px 0">
                  <tr style="background:#f4f4f5">
                    <td style="font-weight:600">Tracking number</td>
                    <td>{{trackingNumber}}</td>
                  </tr>
                  <tr>
                    <td style="font-weight:600">Estimated delivery</td>
                    <td>{{estimatedDelivery}}</td>
                  </tr>
                </table>
                <p>Thanks for shopping with us.</p>
                {{signature}}
                """)
            .build();

    template.setPlaceholders(
        placeholders(
            required("customerName", "Customer name", "Ada Lovelace"),
            required("orderNumber", "Order number", "ORD-2026-0781"),
            optional("shippedDate", "Shipped date", "15 September 2026"),
            optional("trackingNumber", "Tracking number", "1Z 999 AA1 01 2345 6784"),
            optional("estimatedDelivery", "Estimated delivery", "18 September 2026"),
            // The one placeholder whose value is inserted as markup rather than escaped. It is what
            // the "Value is HTML" checkbox in the editor is for.
            html("signature", "Signature block",
                "<p style=\"color:#71717a\">— The <b>AppJars</b> store team</p>")));
    return template;
  }

  /** The same template in another language, to show the locale variants the editor offers. */
  private static TemplateDto shippingNoticeSpanish() {
    TemplateDto template =
        TemplateDto.builder()
            .name("Shipping notice")
            .description("Variante en español del aviso de envío")
            .locale("es")
            .subject("Tu pedido {{orderNumber}} está en camino")
            .html(true)
            .active(true)
            .body(
                """
                <p>Hola {{customerName}}:</p>
                <p>Tu pedido <b>{{orderNumber}}</b> salió el {{shippedDate}} y ya está en camino.</p>
                <p>Gracias por tu compra.</p>
                """)
            .build();

    template.setPlaceholders(
        placeholders(
            required("customerName", "Nombre del cliente", "Ada Lovelace"),
            required("orderNumber", "Número de pedido", "ORD-2026-0781"),
            optional("shippedDate", "Fecha de envío", "15 de septiembre de 2026")));
    return template;
  }

  /** A plain-text template: the editor drops its design view for these. */
  private static TemplateDto passwordReset() {
    TemplateDto template =
        TemplateDto.builder()
            .name("Password reset")
            .description("Plain text, so it renders the same in every mail client")
            .locale("")
            .subject("Reset your password")
            .html(false)
            .active(true)
            .body(
                """
                Hello {{userName}},

                Someone asked to reset the password for your account. Open the link below within \
                {{expiryHours}} hours to choose a new one:

                {{resetLink}}

                If this was not you, no action is needed — the link expires on its own.
                """)
            .build();

    template.setPlaceholders(
        placeholders(
            required("userName", "User name", "ada"),
            required("resetLink", "Reset link", "https://example.com/reset?token=6f1c0b2e"),
            optional("expiryHours", "Hours until the link expires", "24")));
    return template;
  }

  /** Shows a placeholder left undeclared on purpose, which the editor warns about. */
  private static TemplateDto monthlyReport() {
    TemplateDto template =
        TemplateDto.builder()
            .name("Monthly report")
            .description("Open this one to see the editor's undeclared-placeholder warning")
            .locale("")
            .subject("{{month}} report for {{customerName}}")
            .html(true)
            .active(true)
            .body(
                """
                <h2>{{month}} at a glance</h2>
                <p>Hello {{customerName}}, here is how last month went.</p>
                <ul>
                  <li>Messages sent: <b>{{messagesSent}}</b></li>
                  <li>Delivery failures: <b>{{deliveryFailures}}</b></li>
                </ul>
                <p>The full breakdown is attached.</p>
                """)
            .build();

    // deliveryFailures is deliberately not declared: the palette flags it as used-but-undeclared,
    // which is exactly the warning an author sees after a typo.
    template.setPlaceholders(
        placeholders(
            required("customerName", "Customer name", "Ada Lovelace"),
            optional("month", "Month", "August 2026"),
            optional("messagesSent", "Messages sent", "12,480")));
    return template;
  }

  /** An inactive template: still listed and editable, but never offered when composing. */
  private static TemplateDto retiredAnnouncement() {
    TemplateDto template =
        TemplateDto.builder()
            .name("Summer sale announcement")
            .description("Retired after the campaign ended — inactive, so it is not offered")
            .locale("")
            .subject("Last days of the summer sale")
            .html(true)
            .active(false)
            .body("<p>Hi {{customerName}}, the summer sale ends on Sunday.</p>")
            .build();

    template.setPlaceholders(
        placeholders(optional("customerName", "Customer name", "Ada Lovelace")));
    return template;
  }

  private static List<TemplatePlaceholderDto> placeholders(TemplatePlaceholderDto... declared) {
    List<TemplatePlaceholderDto> ordered = new ArrayList<>();
    for (int i = 0; i < declared.length; i++) {
      declared[i].setDisplayOrder(i);
      ordered.add(declared[i]);
    }
    return ordered;
  }

  private static TemplatePlaceholderDto required(String name, String label, String sample) {
    return TemplatePlaceholderDto.builder()
        .name(name)
        .label(label)
        .sampleValue(sample)
        .required(true)
        .build();
  }

  private static TemplatePlaceholderDto optional(String name, String label, String sample) {
    return TemplatePlaceholderDto.builder().name(name).label(label).sampleValue(sample).build();
  }

  private static TemplatePlaceholderDto html(String name, String label, String sample) {
    return TemplatePlaceholderDto.builder()
        .name(name)
        .label(label)
        .sampleValue(sample)
        .html(true)
        .build();
  }
}
