package io.github.robertograham.departuretests.page

import com.microsoft.playwright.Locator
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.LoadState
import org.apache.commons.lang3.StringUtils

final class BusStopCard {

    private final Locator busStopPageLink

    BusStopCard(final Locator busStopPageLink) {
        this.busStopPageLink = busStopPageLink
    }

    String getId() {
        StringUtils.substringAfterLast(StringUtils.substringBeforeLast(busStopPageLink.getAttribute('href'), '/'), '/')
    }

    String getName() {
        busStopPageLink.getByRole(AriaRole.HEADING, new Locator.GetByRoleOptions(level: 2)).textContent()
    }

    String getLocality() {
        busStopPageLink.getByRole(AriaRole.HEADING, new Locator.GetByRoleOptions(level: 3)).textContent()
    }

    BusStopPage open() {
        busStopPageLink.click()
        busStopPageLink.page().waitForLoadState(LoadState.NETWORKIDLE)
        new BusStopPage(busStopPageLink.page())
    }
}
