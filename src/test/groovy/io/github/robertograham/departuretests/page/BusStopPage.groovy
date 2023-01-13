package io.github.robertograham.departuretests.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.LoadState

final class BusStopPage {

    private final Page page
    private final Locator lineCards

    BusStopPage(final Page page) {
        this.page = page
        lineCards = page.locator('css=div.mdc-card')
    }

    void navigate(final String busStopId) {
        page.navigate "$busStopId/departures"
        page.waitForLoadState(LoadState.NETWORKIDLE)
    }

    String getName() {
        page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions(level: 1)).textContent()
    }

    String getLocality() {
        page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions(level: 2)).textContent()
    }

    List<LineCard> getLines() {
        (0..<lineCards.count()).collect { new LineCard(lineCards.nth(it)) }
    }
}
