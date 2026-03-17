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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.function.SerializableFunction;
import java.util.ArrayList;
import java.util.List;
import org.vaadin.addons.antlerflow.tour.EngineType;
import org.vaadin.addons.antlerflow.tour.Tour;
import org.vaadin.addons.antlerflow.tour.TourButton;
import org.vaadin.addons.antlerflow.tour.TourButtonType;
import org.vaadin.addons.antlerflow.tour.TourStep;

/**
 * Factory of the guided tour offered by the demo. Steps are anchored to the {@code data-testid}
 * attributes exposed by the email view (see {@code TestIds}) and to stable Vaadin tag selectors, so
 * no changes to the appjar are needed. Uses the Driver.js engine (MIT-licensed); Shepherd.js is not
 * free for commercial use and must never be used in these public demos.
 */
public final class DemoTours {

  /** Session attribute used to start a tour after navigating to its view. */
  public static final String PENDING_TOUR_ATTRIBUTE = DemoTours.class.getName() + ".pendingTour";

  static final String KEY_PREFIX = "appjars.emailmanager.demo.tour.";

  /**
   * Client-side companion of the tour: watches which Driver popover is visible (by its rendered
   * title, since Driver exposes no step id on the DOM) and opens the first row's actions menu (⋮)
   * programmatically, so the {@code emails.actions} step can explain the per-row options while the
   * user sees them deployed. {@code $0} is the translated title of that step.
   */
  private static final String MENU_HOOK_JS =
      """
      if (window.__demoTourMenus) { window.__demoTourMenus.stop(); }
      const actionsTitle = $0;
      const closeOverlays = () => document
          .querySelectorAll('vaadin-menu-bar-overlay, vaadin-popover-overlay')
          .forEach(o => { o.opened = false; });
      const openRowActions = () => {
        const btn = document.querySelector(
            'vaadin-grid-cell-content vaadin-menu-bar vaadin-menu-bar-button');
        if (btn) { btn.click(); }
      };
      let current = null;
      const sync = () => {
        const el = document.querySelector('.driver-popover-title');
        const title = el ? el.textContent : null;
        if (title === current) { return; }
        current = title;
        closeOverlays();
        if (title === actionsTitle) { setTimeout(openRowActions, 150); }
      };
      const obs = new MutationObserver(sync);
      obs.observe(document.body, {childList: true, subtree: true, characterData: true});
      document.documentElement.classList.add('demo-tour-active');
      window.__demoTourMenus = {
        stop() {
          obs.disconnect();
          closeOverlays();
          document.documentElement.classList.remove('demo-tour-active');
          window.__demoTourMenus = null;
        }
      };
      sync();
      """;

  private static final String MENU_HOOK_STOP_JS =
      "if (window.__demoTourMenus) { window.__demoTourMenus.stop(); }";

  /**
   * Driver tags the step target with {@code .driver-active-element} and forces {@code
   * overflow:hidden} on that element's parent; when the target shares a container with sibling
   * content (e.g. the create-button sits in the same {@code #top-filters} row as the filter
   * fields), that content gets clipped away. Restores {@code overflow:visible} on active-element
   * parents for the duration of the tour. (Driver keeps the popover itself inside the viewport, so
   * unlike Shepherd no edge-clamping hack is needed here.)
   */
  private static final String TOUR_CSS_JS =
      """
      if (!document.getElementById('demo-tour-css')) {
        const style = document.createElement('style');
        style.id = 'demo-tour-css';
        style.textContent =
            'body :not(body):has(> .driver-active-element) { overflow: visible !important; }';
        document.head.appendChild(style);
      }
      """;

  private static final String TOUR_CSS_STOP_JS =
      "document.getElementById('demo-tour-css')?.remove();";

  /**
   * Vaadin 25 renders its overlays (modal dialogs included) in the browser top layer via the native
   * Popover API, where z-index cannot compete: anything outside the top layer paints below them, so
   * an open dialog covers the tour step's own Back/Next/close controls. This promotes Driver's
   * popover into the top layer as well — but only while such an overlay is actually open, since
   * promotion moves the popover's containing block to the viewport and can desynchronise Driver's
   * positioning against the step target, which every step of this demo (all anchored to plain view
   * elements) relies on. Top-layer paint order follows the last {@code showPopover()} call, so the
   * popover is re-asserted whenever another overlay opens after it (listening for the native {@code
   * toggle} event Vaadin's overlays fire). Companion CSS undoing the UA styles that come with the
   * popover attribute lives in {@code styles.css}.
   */
  private static final String OVERLAY_TOP_LAYER_HOOK_JS =
      """
      if (window.__demoTourTopLayer) { window.__demoTourTopLayer.stop(); }
      const popover = () => document.querySelector('.driver-popover');
      const overlayOpen = () => Array.from(document.querySelectorAll('[popover]'))
          .some(el => !el.classList.contains('driver-popover') && el.matches(':popover-open'));
      const promote = () => {
        const el = popover();
        if (!el) { return; }
        if (el.getAttribute('popover') !== 'manual') { el.setAttribute('popover', 'manual'); }
        try { if (!el.matches(':popover-open')) { el.showPopover(); } } catch (e) { /* not ready */ }
      };
      const demote = () => document.querySelectorAll('.driver-popover[popover]').forEach(el => {
        try { el.hidePopover(); } catch (e) { /* already hidden */ }
        el.removeAttribute('popover');
      });
      const sync = () => { if (overlayOpen()) { promote(); } else { demote(); } };
      const onToggle = (e) => {
        const t = e.target;
        if (!t || !t.classList || t.classList.contains('driver-popover')) { return; }
        if (e.newState !== 'open') { sync(); return; }
        promote();
        const el = popover();
        if (el && el.matches(':popover-open')) {
          try { el.hidePopover(); el.showPopover(); } catch (e2) { /* n/a */ }
        }
      };
      document.addEventListener('toggle', onToggle, true);
      const obs = new MutationObserver(sync);
      obs.observe(document.body, {childList: true, subtree: true});
      window.__demoTourTopLayer = {
        stop() {
          obs.disconnect();
          document.removeEventListener('toggle', onToggle, true);
          demote();
          window.__demoTourTopLayer = null;
        }
      };
      sync();
      """;

  private static final String OVERLAY_TOP_LAYER_STOP_JS =
      "if (window.__demoTourTopLayer) { window.__demoTourTopLayer.stop(); }";

  /**
   * Client-side companion of the two compose steps, which are what keeps the compose dialog usable
   * during the tour: Driver dims the page with {@code pointer-events: none} and re-enables only the
   * element it highlights, so a dialog opened while some other step is showing renders normally but
   * cannot be clicked at all — and the compose dialog, being the only one that does not close on an
   * outside click, used to trap the visitor completely. Highlighting the dialog itself is what
   * re-enables it, so this hook chains the two steps around that:
   *
   * <ul>
   * <li>on the {@code emails.create} step (which deliberately has no Next button), the visitor's own
   * click on the highlighted create button advances the tour, once the dialog is actually on
   * screen;</li>
   * <li>on the {@code emails.compose} step, Next first closes the dialog — answering the
   * discard-changes prompt if the visitor typed something — because every remaining step lives
   * behind that modal dialog; closing it by hand instead simply moves the tour on as well.</li>
   * </ul>
   *
   * {@code $0} and {@code $1} are the translated titles of those two steps: Driver puts no step id on
   * the DOM, so the rendered popover title is what identifies the current step (same technique as
   * {@link #MENU_HOOK_JS}).
   */
  private static final String COMPOSE_HOOK_JS =
      """
      if (window.__demoTourCompose) { window.__demoTourCompose.stop(); }
      const CREATE_TITLE = $0;
      const COMPOSE_TITLE = $1;
      const CREATE_BUTTON = "[data-testid='create-button']";
      const DIALOG = "[data-testid='email-dialog']";
      const CANCEL = "[data-testid='cancel-dialog']";
      const DISCARD = 'vaadin-confirm-dialog [slot="confirm-button"]';
      // What a dialog slots outside its content layout: its own footer buttons, and the buttons of
      // the discard-changes prompt. They sit outside whatever the step highlights, so Driver leaves
      // them dead; an inline style is what reliably wins over driver.css, whatever the document and
      // shadow cascades do between them.
      const CHROME = '[slot="footer"], [slot="confirm-button"], [slot="cancel-button"]';
      const isVisible = (el) => {
        const r = el.getBoundingClientRect();
        return r.width > 0 && r.height > 0;
      };
      const visible = (selector) =>
          Array.from(document.querySelectorAll(selector)).find(isVisible);
      const title = () => {
        const el = document.querySelector('.driver-popover-title');
        return el ? el.textContent : null;
      };
      const enableChrome = () => Array.from(document.querySelectorAll(CHROME))
          .filter(el => el.style.pointerEvents !== 'auto' && isVisible(el)
              && el.closest('vaadin-dialog, vaadin-dialog-overlay, vaadin-confirm-dialog'))
          .forEach(el => { el.style.pointerEvents = 'auto'; });
      // Driver exposes no imperative API to the page (antler keeps its instance private), but it does
      // listen for its own keyboard controls on window — on KEYUP for the arrows and Escape, keydown
      // being only its Tab focus trap. A synthetic event dispatched on window has no propagation path
      // through the document, so no Vaadin component can see it. Retried a few times because Driver
      // ignores the key while a step transition is still animating, and guarded by the step it was
      // asked from so the mutation observer below cannot double-advance.
      let advanceFrom = null;
      const advance = () => {
        const from = title();
        if (advanceFrom === from) { return; }
        advanceFrom = from;
        const attempt = (tries) => {
          if (title() !== from) { return; }
          window.dispatchEvent(new KeyboardEvent('keyup', {key: 'ArrowRight'}));
          if (tries > 0) { setTimeout(() => attempt(tries - 1), 250); }
        };
        attempt(3);
      };
      let awaitingDialog = false;
      let closing = false;
      const sync = () => {
        const current = title();
        if (advanceFrom !== null && advanceFrom !== current) { advanceFrom = null; }
        enableChrome();
        if (awaitingDialog) {
          if (visible(DIALOG)) { awaitingDialog = false; advance(); }
          return;
        }
        if (current !== COMPOSE_TITLE) { return; }
        if (closing) {
          const discard = visible(DISCARD);
          if (discard) { discard.click(); return; }
        }
        if (!visible(DIALOG)) { closing = false; advance(); }
      };
      const onClick = (e) => {
        const target = e.target;
        if (!target || !target.closest) { return; }
        if (title() === CREATE_TITLE) {
          if (target.closest(CREATE_BUTTON)) { awaitingDialog = true; }
          return;
        }
        if (title() !== COMPOSE_TITLE) { return; }
        if (!target.closest('.driver-popover .driver-button:not(.secondary)')) { return; }
        e.preventDefault();
        e.stopPropagation();
        e.stopImmediatePropagation();
        closing = true;
        const cancel = visible(CANCEL);
        if (cancel) { cancel.click(); }
      };
      document.addEventListener('click', onClick, true);
      const obs = new MutationObserver(sync);
      obs.observe(document.body, {childList: true, subtree: true, attributes: true,
          attributeFilter: ['opened', 'hidden', 'class']});
      window.__demoTourCompose = {
        stop() {
          obs.disconnect();
          document.removeEventListener('click', onClick, true);
          window.__demoTourCompose = null;
        }
      };
      sync();
      """;

  private static final String COMPOSE_HOOK_STOP_JS =
      "if (window.__demoTourCompose) { window.__demoTourCompose.stop(); }";

  public enum DemoTour {
    EMAILS
  }

  private DemoTours() {}

  public static Tour create(DemoTour tour, SerializableFunction<String, String> translator) {
    List<TourStep> steps = switch (tour) {
      case EMAILS -> emailsSteps(translator);
    };
    return Tour.builder().engineType(EngineType.DRIVER).steps(steps).showCancelButton(true)
        .allowClose(true).build();
  }

  /**
   * Creates the tour, attaches it to {@code host} and starts it, detaching it again once it is
   * completed or canceled.
   */
  public static void start(DemoTour tour, Component host,
      SerializableFunction<String, String> translator) {
    Tour t = create(tour, translator);
    host.getElement().appendChild(t.getElement());
    t.addTourCompletedListener(e -> stop(t, host));
    t.addTourCanceledListener(e -> stop(t, host));
    t.start();
    host.getElement().executeJs(MENU_HOOK_JS, translator.apply(KEY_PREFIX + "emails.actions.title"));
    host.getElement().executeJs(TOUR_CSS_JS);
    host.getElement().executeJs(OVERLAY_TOP_LAYER_HOOK_JS);
    host.getElement().executeJs(COMPOSE_HOOK_JS,
        translator.apply(KEY_PREFIX + "emails.create.title"),
        translator.apply(KEY_PREFIX + "emails.compose.title"));
  }

  // Not reached while a modal dialog is open: Flow marks components outside the dialog inert and
  // drops their RPC, so ending the tour from its own X/Done button there only tears it down on the
  // client. Each hook's start script stops any previous instance of itself, so a later tour still
  // re-arms cleanly.
  private static void stop(Tour tour, Component host) {
    host.getElement().executeJs(MENU_HOOK_STOP_JS);
    host.getElement().executeJs(TOUR_CSS_STOP_JS);
    host.getElement().executeJs(OVERLAY_TOP_LAYER_STOP_JS);
    host.getElement().executeJs(COMPOSE_HOOK_STOP_JS);
    tour.getElement().removeFromParent();
  }

  private static List<TourStep> emailsSteps(SerializableFunction<String, String> t) {
    return List.of(
        step(t, "emails.grid", "vaadin-grid", "top", true, false),
        step(t, "emails.status", "vaadin-grid", "top", false, false),
        step(t, "emails.filters", "#top-filters", "bottom", false, false),
        // No Next button: the visitor's own click on the create button is what advances the tour,
        // and opening the dialog is also what makes it usable — Driver leaves only the element it
        // highlights interactive, so the compose step below highlights the dialog itself.
        stepAdvanceOnly(t, "emails.create", "[data-testid='create-button']", "bottom"),
        // No Back button: it would point at the create button sitting behind this modal dialog. Its
        // Next closes the dialog before moving on (see COMPOSE_HOOK_JS).
        stepNoBack(t, "emails.compose", "[data-testid='email-dialog']", "left"),
        // No Back button either: the compose step it would return to has had its dialog closed.
        stepNoBack(t, "emails.actions", null, null),
        step(t, "emails.license", null, null, false, true));
  }

  private static TourStep step(SerializableFunction<String, String> t, String key, String attachTo,
      String position, boolean first, boolean last) {
    List<TourButton> buttons = new ArrayList<>();
    if (!first) {
      buttons.add(backButton(t));
    }
    buttons.add(nextButton(t, last));
    return step(t, key, attachTo, position, buttons);
  }

  /** A step that only offers Back: it advances on a real action of the visitor's instead of on Next. */
  private static TourStep stepAdvanceOnly(SerializableFunction<String, String> t, String key,
      String attachTo, String position) {
    return step(t, key, attachTo, position, List.of(backButton(t)));
  }

  /** A step that only offers Next, for steps whose predecessor can no longer be returned to. */
  private static TourStep stepNoBack(SerializableFunction<String, String> t, String key,
      String attachTo, String position) {
    return step(t, key, attachTo, position, List.of(nextButton(t, false)));
  }

  private static TourStep step(SerializableFunction<String, String> t, String key, String attachTo,
      String position, List<TourButton> buttons) {
    return TourStep.builder().id(key.replace('.', '-')).attachTo(attachTo).position(position)
        .title(t.apply(KEY_PREFIX + key + ".title")).content(t.apply(KEY_PREFIX + key + ".desc"))
        .buttons(buttons).build();
  }

  private static TourButton backButton(SerializableFunction<String, String> t) {
    return TourButton.builder().label(t.apply(KEY_PREFIX + "btn.back")).secondary(true)
        .type(TourButtonType.PREVIOUS).build();
  }

  private static TourButton nextButton(SerializableFunction<String, String> t, boolean last) {
    return TourButton.builder().label(t.apply(KEY_PREFIX + (last ? "btn.done" : "btn.next")))
        .type(TourButtonType.NEXT).build();
  }
}
