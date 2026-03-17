# Email Manager Demo

A ready-to-run demonstration of the **AppJars Email Manager** — an AppJar that adds e-mail
management to a Vaadin Flow application: composing messages with recipients, CC and BCC, an HTML or
plain-text body and attachments, holding them in a send queue, delivering them over SMTP, and
keeping a full audit trail of every delivery attempt.

It runs entirely on your machine on a file-based H2 database, with **no login and nothing to
configure**. The e-mail list arrives pre-populated with sample messages in every delivery state, so
every feature is one click away the moment the application starts.

The demo opens on a public landing page that summarizes the features and offers a **guided tour** of
the e-mail view.

Visit the official [AppJars documentation](https://docs.appjars.com) for more information.

---

## What you get out of the box

On first run the application seeds twelve sample e-mails (and leaves them alone on later restarts —
the seeding only happens when the list is empty). The set deliberately covers every delivery status
and every content type the AppJar supports, so nothing has to be set up before you can try an
action:

| Status | Count | What they demonstrate |
|---|---|---|
| **Created** | 5 | Drafts you can send, queue, edit or delete — including one with CC **and** BCC, an HTML announcement, and one carrying a PDF attachment |
| **Send pending** | 2 | Sitting in the delivery queue, picked up by the background sender on its next run |
| **Sent** | 3 | Already delivered, dated in the past — one plain text, one HTML, one with a CSV attachment |
| **Send failed** | 2 | Carry a captured failure reason you can inspect with **Show error details**, then retry |


The sample set lives in
[`EmailGenerator`](src/main/java/com/appjars/emailmanager/demo/util/EmailGenerator.java) if you want
to change what gets seeded.

---

## Prerequisites

- **Java 21+**
- **Maven 3.8+**
- **Docker** and **Docker Compose** — *optional*, only to watch mail actually arrive (see step 2)

---

## 1. Run the application

```bash
mvn
```

(`spring-boot:run` is the default goal.) Then open **http://localhost:8080**. No login is required.

## 2. Watch mail arrive (optional)

Docker is **not** required. Without it every feature works as described, except that a send has
nowhere to go: the e-mail lands in the `Send failed`.

To see messages actually delivered, start the throwaway SMTP server that ships with the demo:

```bash
docker compose up -d
```

That runs [Mailpit](https://mailpit.axllent.org/) — an SMTP server on port `1025` that captures
everything sent to it instead of relaying it anywhere, with a web inbox on
**http://localhost:8025**. The `spring.mail.*` properties already point at it. 

Send an e-mail from the demo, wait for the sender task to fire, and the message shows
up in the inbox, attachments and all.

You can start it before or after the application — no restart needed either way.

---

## Using the demo

### The e-mail list

The **Emails** view is the whole AppJar in one screen. Every message is listed with its sender,
subject, delivery status and creation date, and a colored status badge tells you where it stands:

| Badge | Meaning |
|---|---|
| **Created** | A draft that has not been sent yet |
| **Send pending** | Queued, waiting for the background sender |
| **Sent** | Handed over to the SMTP server |
| **Send failed** | Delivery failed; the reason can be inspected |

Filter the list by address, subject or status to narrow it down. On narrow screens the filters move
behind a button.

### Composing

**Create** opens the compose dialog, where everything an e-mail needs is set in one place: the
**From** address (picked from the configured senders), the **Recipients**, **Carbon copy** and
**Blind carbon copy** lists, the **Subject** and **Body**, a **Send as HTML** toggle that switches
the body from plain text to HTML, and **Attachments** you upload and can remove again.

Saving stores the e-mail as *Created* — sending is a separate, deliberate action on the list.

### Per-row actions

Each row carries an actions menu; which options it offers depends on the e-mail's status:

| Action | What it does |
|---|---|
| **View** | Opens the e-mail read-only |
| **Send now** | Sends it immediately |
| **Add to queue** / **Remove from send queue** | Hands it to the background sender, or takes it back |
| **Edit Email** | Changes any of its fields |
| **Delete** | Removes the e-mail, after a confirmation |
| **Show error details** | Inspects the captured failure reason *(failed e-mails only)* |
| **Preview HTML** | Renders an HTML body as the recipient will see it *(HTML e-mails only)* |

### The delivery queue

Queued e-mails are sent by a background task on a cron schedule. The demo runs it **every 10
seconds** (`email.task.cronexpression`): add an e-mail to the
queue, watch its badge flip to *Sent*, and — with Mailpit running — find it in the inbox.

### Guided tour

The landing page and the navbar both carry a **Guided tour** menu. The tour highlights the real
elements of the e-mail view and explains them one popover at a time, opening the compose dialog and
a row's actions menu along the way so you see them as a user does:

| Step | Covers |
|---|---|
| **The e-mail list** | The grid, its columns and the sample data |
| **Delivery status** | What each of the four status badges means |
| **Search & filter** | Filtering by address, subject or status |
| **Compose a new e-mail** | The button that opens the dialog — the tour continues inside it |
| **The compose dialog** | From, recipients, subject and body, the *Send as HTML* toggle, attachments and Save |
| **Per-row actions** | Every action a row can offer, and when it offers it |
| **Free mode** | The daily send quota and the *Emails sent today* counter |

Leave the tour at any step with `Esc` or by clicking outside the popover.

### Free mode

This demo runs **unlicensed**, which caps it at **5 successfully sent e-mails per day**; the
*Emails sent today* counter above the grid tracks the quota. Beyond the cap an e-mail stays queued instead of being sent. Every
other feature is fully functional and unlimited — a full licence removes the cap and changes nothing
else.

---

## Configuration

All configuration lives in
[`src/main/resources/application.properties`](src/main/resources/application.properties). Every
AppJar property is documented in the
[Email Manager README](https://github.com/AppJars/email-manager#configurable-properties).

### Mail sender

These values match the bundled Mailpit container, which takes no credentials and speaks no TLS.
Pointing the demo at a real relay means filling in the username and password and turning `auth` and
STARTTLS back on.

| Property | What it configures | Value in this demo |
|---|---|---|
| `spring.mail.host` | SMTP host | `localhost` |
| `spring.mail.port` | SMTP port | `1025` |
| `spring.mail.protocol` | Transport protocol | `smtp` |
| `spring.mail.username` | User to authenticate as | *empty* |
| `spring.mail.password` | Password to authenticate with | *empty* |
| `spring.mail.properties.mail.smtp.auth` | Whether the relay requires authentication | `false` |
| `spring.mail.properties.mail.smtp.starttls.enable` | Upgrade the connection with STARTTLS | `false` |

### Email Manager

| Property | What it configures | Value in this demo |
|---|---|---|
| `com.appjars.emailmanager.from` | Sender addresses offered in the compose dialog. One address makes the field read-only; several turn it into a picker | `example@example.com` |
| `email.task.cronexpression` | How often the background sender drains the queue | `0/10 * * * * *` (every 10s) |

### Email Manager defaults left untouched

Not set by the demo, listed because they shape what the compose dialog accepts:

| Property | What it configures | AppJar default |
|---|---|---|
| `com.appjars.emailmanager.url.views.emailcrudview` | Route the e-mail list is registered under | `em/list` |
| `com.appjars.emailmanager.attachments.max-size` | Largest attachment accepted, in bytes | `26214400` (25 MB) |
| `com.appjars.emailmanager.attachments.max-files` | Attachments accepted per e-mail | `5` |
| `com.appjars.emailmanager.attachments.accepted-types` | Allow-list of attachment content types; empty accepts any | common document, image and archive types |

### Database

The demo uses a file-based H2 database under `data/`, so what you compose survives a restart. Delete
that folder to start over from the seeded sample set.

| Property | What it configures | Value in this demo |
|---|---|---|
| `spring.datasource.url` | JDBC URL of the H2 file database | `jdbc:h2:./data/emailmanagerdemo;FILE_LOCK=NO` |
| `spring.datasource.driverClassName` | JDBC driver | `org.h2.Driver` |
| `spring.datasource.username` / `.password` | Datasource credentials | `sa` / *empty* |
| `spring.jpa.database-platform` | Hibernate dialect | `org.hibernate.dialect.H2Dialect` |
| `spring.h2.console.enabled` | Serves the H2 web console at `/h2-console` | `true` |

### JPA / Hibernate

| Property | What it configures | Value in this demo |
|---|---|---|
| `spring.jpa.hibernate.ddl-auto` | Schema generation strategy | `update` |
| `spring.jpa.generate-ddl` | Whether Spring generates the schema | `true` |
| `spring.jpa.show-sql` | Logs the SQL Hibernate issues | `false` |
| `spring.jpa.properties.hibernate.format_sql` | Pretty-prints that SQL when enabled | `true` |

### Uploads and UI

| Property | What it configures | Value in this demo |
|---|---|---|
| `spring.servlet.multipart.max-file-size` | Largest file the servlet container accepts | `100MB` |
| `spring.servlet.multipart.max-request-size` | Largest total upload request | `100MB` |
| `vaadin.i18n.provider` | Vaadin i18n provider supplying the translations | `com.appjars.utils.i18n.AppjarsI18nProvider` |

---

## Stopping the SMTP server

```bash
docker compose down
```

Captured mail is not persisted, so nothing is left behind.
