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

import com.appjars.emailmanager.model.AttachmentDto;
import com.appjars.emailmanager.model.EmailDto;
import com.appjars.emailmanager.model.EmailStatus;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Builds the sample e-mails that populate the demo on first run.
 *
 * <p>Instead of a single repeated state, the sample set deliberately covers every feature of the
 * appjar so an evaluator can try them all without setting anything up: the four delivery statuses
 * (created, pending, sent, failed), plain-text and HTML bodies, CC/BCC recipients, attachments and
 * captured failure stack traces.
 *
 * <p>Sent e-mails are dated in the past so they do not consume the free-tier "sent today" quota,
 * leaving the daily send limit free for the evaluator to exercise.
 */
public class EmailGenerator {

  private EmailGenerator() {}

  private static final String FROM = "notifications@appjars.com";

  /** Reference instant used to spread the sample data over the last few days. */
  private static final Instant NOW = Instant.now();

  /**
   * Returns a curated list of sample e-mails covering every delivery status and content type
   * supported by the appjar.
   */
  public static List<EmailDto> sampleEmails() {
    return List.of(
        // --- Drafts (CREATED): can be sent, queued, edited or deleted ---
        created(
            "Welcome to the Email Manager demo",
            "Hi there,\n\nThis draft is ready to be sent. Use the row actions to send it now or "
                + "add it to the delivery queue.\n\nEnjoy the demo!",
            recipients("evaluator@example.com"),
            Duration.ofMinutes(5)),
        ccBcc(
            created(
                "Q3 planning sync — agenda",
                "Team,\n\nPlease review the agenda before our sync. Managers are in CC, the "
                    + "leadership alias is in BCC.",
                recipients("alice@example.com", "bob@example.com", "carol@example.com"),
                Duration.ofMinutes(30)),
            recipients("manager@example.com"),
            recipients("leadership@example.com")),
        html(
            created(
                "🎉 New feature announcement",
                "<h2>Say hello to guided tours</h2>"
                    + "<p>This is an <b>HTML</b> e-mail. Use the <i>Preview HTML</i> row action to "
                    + "see how it renders.</p>"
                    + "<ul><li>Rich content</li><li>Inline formatting</li><li>Links &amp; lists</li></ul>",
                recipients("subscribers@example.com"),
                Duration.ofHours(1))),
        withAttachment(
            created(
                "March invoice attached",
                "Dear customer,\n\nPlease find your invoice attached.\n\nRegards,\nBilling",
                recipients("customer@example.com"),
                Duration.ofHours(2)),
            attachment("invoice-2024-0781.pdf", "application/pdf",
                "%PDF-1.4 sample invoice content")),
        created(
            "Team offsite logistics",
            "Hi everyone, here are the details for next month's offsite. Reply with any dietary "
                + "requirements.",
            recipients(
                "alice@example.com", "bob@example.com", "carol@example.com", "dave@example.com"),
            Duration.ofHours(3)),

        // --- Queued (SEND_PENDING): handled by the background sender ---
        pending(
            html(
                created(
                    "Weekly newsletter",
                    "<h1>This week at AppJars</h1><p>Queued for delivery by the scheduled sender.</p>",
                    recipients("newsletter@example.com"),
                    Duration.ofMinutes(10)))),
        pending(
            ccBcc(
                created(
                    "Subscription renewal reminder",
                    "Your subscription renews next week. No action is needed to continue.",
                    recipients("member@example.com"),
                    Duration.ofMinutes(15)),
                recipients("billing@example.com"),
                recipients())),

        // --- Delivered (SEND_SUCCESSFUL): sent in the past, view only ---
        sent(
            created(
                "Your order #10432 has shipped",
                "Good news! Your order is on its way and should arrive within 3 business days.",
                recipients("buyer@example.com"),
                Duration.ofDays(3)),
            Duration.ofDays(3)),
        sent(
            html(
                created(
                    "Password changed successfully",
                    "<p>Your password was changed. If this wasn't you, contact support "
                        + "immediately.</p>",
                    recipients("user@example.com"),
                    Duration.ofDays(5))),
            Duration.ofDays(5)),
        sent(
            withAttachment(
                created(
                    "Monthly report — February",
                    "Attached is the February activity report.",
                    recipients("stakeholders@example.com"),
                    Duration.ofDays(2)),
                attachment("report-february.csv", "text/csv",
                    "month,sent,failed\nFebruary,128,3")),
            Duration.ofDays(2)),

        // --- Failed (SEND_FAILED): carry a stack trace, can be inspected and retried ---
        failed(
            created(
                "Delivery to an invalid recipient",
                "This message could not be delivered because the recipient address was rejected.",
                recipients("does-not-exist@invalid.example"),
                Duration.ofDays(1)),
            mailStackTrace("Invalid Addresses; nested exception is 550 5.1.1 "
                + "<does-not-exist@invalid.example>: Recipient address rejected")),
        failed(
            html(
                created(
                    "Promo blast (bounced)",
                    "<h2>Big sale!</h2><p>Delivery failed — inspect the error details to see why.</p>",
                    recipients("bounced@invalid.example"),
                    Duration.ofDays(1).plusHours(2))),
            mailStackTrace("Mail server connection failed; nested exception is "
                + "java.net.ConnectException: Connection refused")));
  }

  private static EmailDto created(
      String subject, String body, Set<String> recipients, Duration ago) {
    return EmailDto.builder()
        .senderAddress(FROM)
        .recipientAddresses(recipients)
        .subject(subject)
        .body(body)
        .status(EmailStatus.CREATED)
        .creationInstant(NOW.minus(ago))
        .build();
  }

  private static EmailDto html(EmailDto email) {
    email.setHtml(true);
    return email;
  }

  private static EmailDto ccBcc(EmailDto email, Set<String> cc, Set<String> bcc) {
    email.setCarbonCopyRecipients(cc);
    email.setBlindCarbonCopyRecipients(bcc);
    return email;
  }

  private static EmailDto withAttachment(EmailDto email, AttachmentDto attachment) {
    List<AttachmentDto> attachments = new ArrayList<>(email.getAttachments());
    attachments.add(attachment);
    email.setAttachments(attachments);
    return email;
  }

  private static EmailDto pending(EmailDto email) {
    email.setStatus(EmailStatus.SEND_PENDING);
    return email;
  }

  private static EmailDto sent(EmailDto email, Duration sentAgo) {
    email.setStatus(EmailStatus.SEND_SUCCESSFUL);
    email.setSentInstant(NOW.minus(sentAgo));
    return email;
  }

  private static EmailDto failed(EmailDto email, String failureReason) {
    email.setStatus(EmailStatus.SEND_FAILED);
    email.setFailureReason(failureReason);
    return email;
  }

  private static AttachmentDto attachment(String fileName, String contentType, String content) {
    return AttachmentDto.builder()
        .fileName(fileName)
        .contentType(contentType)
        .data(content.getBytes(StandardCharsets.UTF_8))
        .addedInstant(NOW)
        .build();
  }

  private static Set<String> recipients(String... addresses) {
    return new LinkedHashSet<>(List.of(addresses));
  }

  private static String mailStackTrace(String message) {
    return "org.springframework.mail.MailSendException: " + message + "\n"
        + "\tat org.springframework.mail.javamail.JavaMailSenderImpl.doSend"
        + "(JavaMailSenderImpl.java:389)\n"
        + "\tat org.springframework.mail.javamail.JavaMailSenderImpl.send"
        + "(JavaMailSenderImpl.java:329)\n";
  }
}
