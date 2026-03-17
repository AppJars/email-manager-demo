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

import com.appjars.emailmanager.demo.views.tour.DemoTours;
import com.appjars.emailmanager.demo.views.tour.DemoTours.DemoTour;
import com.appjars.emailmanager.demo.views.tour.TourMenu;
import com.appjars.emailmanager.flow.view.EmailCrudView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.Flex;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;

// Anonymous so the public landing page (HomeView) can render inside this layout (the demo itself
// has no authentication)
/** The main view is a top-level placeholder for other views. */
@SuppressWarnings("serial")
@AnonymousAllowed
public class MainLayout extends AppLayout implements AfterNavigationObserver {

  private H2 viewTitle;

  public MainLayout() {
    setPrimarySection(Section.DRAWER);
    addDrawerContent();
    addHeaderContent();
  }

  private void addHeaderContent() {
    DrawerToggle toggle = new DrawerToggle();
    toggle.getElement().setAttribute("aria-label", "Menu toggle");

    viewTitle = new H2();
    viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

    // Pushed to the far end of the navbar, so a guided tour can be started (or restarted) from any
    // view without going back to the landing page — where the same menu is also offered.
    TourMenu tourMenu = new TourMenu();
    tourMenu.getStyle().set("margin-inline-start", "auto").set("margin-inline-end",
        "var(--lumo-space-m)");

    addToNavbar(true, toggle, viewTitle, tourMenu);
  }

  private void addDrawerContent() {
    VerticalLayout drawerLayout = new VerticalLayout();
    drawerLayout.addClassNames(Margin.NONE, Padding.NONE, AlignItems.STRETCH, Gap.XSMALL);
    drawerLayout.setSizeFull();

    Image logo = new Image("/icons/icon.png", null);
    logo.setHeight("5vh");
    logo.setWidth("5vh");

    H3 title = new H3(getTranslation("appjars.emailmanager.demo.layout.drawertitle"));

    Header header = new Header(logo, title);
    header.addClassNames(Display.FLEX, Gap.XSMALL, AlignItems.CENTER, Margin.MEDIUM);
    title.addClassName(Flex.GROW);

    Scroller scroller = new Scroller(createNavigation());

    drawerLayout.add(header, scroller);
    drawerLayout.expand(scroller);

    addToDrawer(drawerLayout);
  }

  private SideNav createNavigation() {
    SideNav nav = new SideNav();

    SideNavItem homeItem =
        new SideNavItem(getTranslation("appjars.emailmanager.demo.menuitem.home"), HomeView.class);
    homeItem.setPrefixComponent(VaadinIcon.HOME.create());

    SideNavItem emailManagerItem =
        new SideNavItem(getTranslation("appjars.emailmanager.demo.menuitem.emailManagerItem"));
    emailManagerItem.setPrefixComponent(VaadinIcon.LIST.create());
    emailManagerItem.setExpanded(true);
    emailManagerItem.addItem(
        new SideNavItem(
            getTranslation("appjars.emailmanager.demo.menuitem.emailList"),
            EmailCrudView.class));

    nav.addItem(homeItem, emailManagerItem);

    return nav;
  }

  private Footer createFooter() {
    return new Footer();
  }

  @Override
  public void afterNavigation(AfterNavigationEvent event) {
    updateTitle();
    startPendingTour();
  }

  /** Reflects the active view's dynamic title in the navbar, falling back to the app name. */
  private void updateTitle() {
    if (getContent() instanceof HasDynamicTitle hasTitle) {
      viewTitle.setText(hasTitle.getPageTitle());
    } else {
      viewTitle.setText(getTranslation("appjars.emailmanager.demo.layout.drawertitle"));
    }
  }

  private void startPendingTour() {
    VaadinSession session = VaadinSession.getCurrent();
    if (session.getAttribute(DemoTours.PENDING_TOUR_ATTRIBUTE) instanceof DemoTour pending
        && getContent() != null) {
      Class<?> target = getContent().getClass();
      boolean matches = pending == DemoTour.EMAILS && EmailCrudView.class.equals(target);
      if (matches) {
        session.setAttribute(DemoTours.PENDING_TOUR_ATTRIBUTE, null);
        DemoTours.start(pending, this, this::getTranslation);
      }
    }
  }

}
