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
package com.appjars.emailmanager.demo.service;

import com.appjars.emailmanager.model.PlaceholderDefinition;
import com.appjars.emailmanager.service.PlaceholderCatalog;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Publishes the placeholders this "application" offers to every template.
 *
 * <p>Demonstrates the one integration point the template editor exposes: an application implements
 * {@link PlaceholderCatalog} and its own names show up in every template's editor palette, grouped
 * under {@link #getGroupName()}, without each template author having to declare them by hand.
 *
 * <p>A catalogue is editor metadata, not a source of values — it says which names exist and what a
 * realistic one looks like. The values themselves are supplied per render, in the map handed to
 * {@code TemplateService.render}. Publish as many of these beans as you have groups, or none at all.
 */
@Component
public class DemoPlaceholderCatalog implements PlaceholderCatalog {

  @Override
  public String getGroupName() {
    return "Customer";
  }

  @Override
  public List<PlaceholderDefinition> getPlaceholders() {
    return List.of(
        new PlaceholderDefinition("customerName", "Customer name", "Ada Lovelace"),
        new PlaceholderDefinition("customerEmail", "Customer e-mail", "ada@example.com"),
        new PlaceholderDefinition("companyName", "Company name", "AppJars Store"),
        new PlaceholderDefinition("supportEmail", "Support e-mail", "support@appjars.com"));
  }
}
