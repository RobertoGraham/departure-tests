package io.github.robertograham.departuretests.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.options.AriaRole

final class LineCard {

    private final Locator locator

    LineCard(final Locator locator) {
        this.locator = locator
    }

    String getName() {
        locator.getByRole(AriaRole.HEADING, new Locator.GetByRoleOptions(level: 3)).textContent()
    }

    String getOperator() {
        locator.getByRole(AriaRole.HEADING, new Locator.GetByRoleOptions(level: 4)).textContent()
    }

    List<Departure> getDepartures() {
        final def listItems = locator.getByRole(AriaRole.LISTITEM)
        (0..<listItems.count()).collect { new Departure(listItems.nth(it)) }
    }
}
