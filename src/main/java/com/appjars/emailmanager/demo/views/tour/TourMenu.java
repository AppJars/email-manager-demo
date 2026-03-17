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
package com.appjars.emailmanager.demo.views.tour;

import com.appjars.emailmanager.demo.views.MainLayout;
import com.appjars.emailmanager.demo.views.tour.DemoTours.DemoTour;
import com.appjars.emailmanager.flow.view.EmailCrudView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.server.VaadinSession;

/**
 * The "Guided tour" menu, offered both by the landing page's try-it section and by the navbar, so a
 * visitor can start a tour from anywhere — including from the very view the tour runs on.
 */
@SuppressWarnings("serial")
public class TourMenu extends MenuBar {

  private static final String KEY_PREFIX = "appjars.emailmanager.demo.home.";

  public TourMenu() {
    addThemeVariants(MenuBarVariant.LUMO_TERTIARY);
    SubMenu tours = addItem(new Div(VaadinIcon.MAP_MARKER.create(),
        new Span(getTranslation(KEY_PREFIX + "tour.button")))).getSubMenu();
    // Guided tours cover the appjar's views and their configurations, not this landing page.
    tours.addItem(getTranslation(KEY_PREFIX + "tour.emails"),
        e -> startViewTour(DemoTour.EMAILS, EmailCrudView.class));
  }

  // Navigating to the view a tour runs on is what starts it (see MainLayout.startPendingTour). When
  // that view is already the one on screen — this menu also sits in the navbar — navigating to the
  // current route fires no AfterNavigationEvent, so the tour has to be started right away instead.
  private void startViewTour(DemoTour tour, Class<? extends Component> view) {
    MainLayout layout = findAncestor(MainLayout.class);
    if (layout != null && view.isInstance(layout.getContent())) {
      DemoTours.start(tour, layout, this::getTranslation);
      return;
    }
    VaadinSession.getCurrent().setAttribute(DemoTours.PENDING_TOUR_ATTRIBUTE, tour);
    getUI().ifPresent(ui -> ui.navigate(view));
  }
}
