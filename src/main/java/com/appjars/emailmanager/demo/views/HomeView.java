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
package com.appjars.emailmanager.demo.views;

import com.appjars.emailmanager.demo.views.tour.TourMenu;
import com.appjars.emailmanager.flow.view.EmailCrudView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Public landing page of the demo: presents the appjar features, the license model and offers a
 * guided tour of the email view. Section structure (hero / features / try-it / license / links) is
 * meant to be replicated across the other AppJars demos.
 */
@SuppressWarnings("serial")
@AnonymousAllowed
@Route(value = "", layout = MainLayout.class)
public class HomeView extends VerticalLayout implements HasDynamicTitle {

  private static final String KEY_PREFIX = "appjars.emailmanager.demo.home.";

  private static final String APPJARS_SITE_URL = "https://www.appjars.com";
  private static final String GITHUB_ORG_URL = "https://github.com/AppJars";
  private static final String EMAIL_MANAGER_DOCS_URL =
      "https://docs.appjars.com/email-manager/overview/";
  private static final String LOGO_PATH = "icons/icon-appjars-full.png";

  public HomeView() {
    addClassName("home-view");
    add(createHero(), createFeaturesSection(), createTryItSection(), createLicenseSection(),
        createLinksSection());
    setAlignItems(Alignment.STRETCH);
  }

  private Component createHero() {
    Image logo = new Image(LOGO_PATH, t("hero.logoAlt"));
    logo.setWidth("144px");
    logo.setHeight("auto");
    logo.addClassName("home-logo");

    H1 title = new H1(t("hero.title"));
    Paragraph tagline = new Paragraph(t("hero.tagline"));
    tagline.addClassName("home-tagline");

    Div hero = new Div(logo, title, tagline);
    hero.setId("home-hero");
    hero.addClassName("home-hero");
    return hero;
  }

  private Component createFeaturesSection() {
    Div cards = new Div(
        featureCard(VaadinIcon.ENVELOPE_OPEN, "features.compose"),
        featureCard(VaadinIcon.PAPERPLANE, "features.send"),
        featureCard(VaadinIcon.CLOCK, "features.queue"),
        featureCard(VaadinIcon.FLAG, "features.status"),
        featureCard(VaadinIcon.WARNING, "features.errors"),
        featureCard(VaadinIcon.CODE, "features.html"),
        featureCard(VaadinIcon.PAPERCLIP, "features.attachments"),
        featureCard(VaadinIcon.SEARCH, "features.filters"));
    cards.addClassName("home-features");

    return section("home-features", t("features.title"), cards);
  }

  private Card featureCard(VaadinIcon icon, String key) {
    Card card = new Card();
    card.addClassName("home-feature-card");
    Icon prefix = icon.create();
    prefix.addClassName("home-feature-icon");
    card.setHeaderPrefix(prefix);
    card.setTitle(t(key + ".title"));
    card.add(new Paragraph(t(key + ".desc")));
    return card;
  }

  private Component createTryItSection() {
    Paragraph intro = new Paragraph(t("tryit.intro"));

    Button emails = new Button(t("tryit.emails"),
        e -> getUI().ifPresent(ui -> ui.navigate(EmailCrudView.class)));
    emails.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    // Same menu as the navbar's (see MainLayout), so both entry points behave identically.
    Div actions = new Div(emails, new TourMenu());
    actions.addClassName("home-actions");

    return section("home-tryit", t("tryit.title"), intro, actions);
  }

  private Component createLicenseSection() {
    Paragraph desc = new Paragraph(t("license.desc"));
    Anchor link = new Anchor(APPJARS_SITE_URL, t("license.link"));
    link.setTarget("_blank");
    return section("home-license", t("license.title"), desc, new Paragraph(link));
  }

  private Component createLinksSection() {
    Anchor github = new Anchor(GITHUB_ORG_URL, t("links.github"));
    github.setTarget("_blank");
    Anchor readme = new Anchor(EMAIL_MANAGER_DOCS_URL, t("links.readme"));
    readme.setTarget("_blank");
    Div links = new Div(github, readme);
    links.addClassName("home-links");
    return section("home-links", t("links.title"), links);
  }

  private Div section(String id, String title, Component... content) {
    Div section = new Div();
    section.setId(id);
    section.addClassName("home-section");
    section.add(new H3(title));
    section.add(content);
    return section;
  }

  private String t(String key) {
    return getTranslation(KEY_PREFIX + key);
  }

  @Override
  public String getPageTitle() {
    return t("title");
  }
}
