package io.github.robertograham.departuretests.page

import com.microsoft.playwright.Locator


final class Departure {

    private final Locator locator

    Departure(final Locator locator) {
        this.locator = locator
    }

    String getTime() {
        locator.locator('css=.mdc-list-item__primary-text').textContent()
    }

    String getDestination() {
        locator.locator('css=.mdc-list-item__secondary-text').textContent()
    }
}
